/**
 * 
 */
package com.ibm.tpd.primarydb.ingestmsg.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import com.ibm.tpd.primarydb.entity.response.SuccessResponse;
import com.ibm.tpd.primarydb.exception.InvalidMessageTypeException;
import com.ibm.tpd.primarydb.exception.KafkaErrorTopicFailureException;
import com.ibm.tpd.primarydb.exception.KafkaProducerException;
import com.ibm.tpd.primarydb.ingestmsg.util.IngestMessageHelper;
import com.ibm.tpd.primarydb.logger.PDBLogger;
import com.ibm.tpd.primarydb.util.TPDMessageChannelTypes;
import com.ibm.tpd.primarydb.util.TPDMessageTypes;

/**
 * @author SangitaPal
 * 
 * This is service class for producing message to Kafka topic. Topic name selection is dependent on msgType.
 * Topic names are configured in properties file. Custom KafkaProducerException is thrown in case of any exception
 * during message publication o Kafka. 
 *
 */
//@RefreshScope
@Service
public class IngestMessageServiceImpl implements IngestMessageService{
	
	@Value("${kafka.producer.success.status.code}")
	private String statusCode;

	@Value("${kafka.producer.success.status.desc}")
    private String statusDesc;
	
	@Value("${kafka.producer.timeout.interval}")
    private int timeoutInterval;
	
	@Value("${kafka.producer.retry.topic.max.retry.count}")
    private int maxRetryCount;
	
	private final TPDOutboundMessageStreams messageStreams;
	
	protected final Map<String, MessageChannel> messageChannelMap = new HashMap<String, MessageChannel>();
	
	@Autowired
	PDBLogger pdbLogger;
	
	@Autowired
	IngestMessageHelper ingestMessageHelper;
	
    public IngestMessageServiceImpl(TPDOutboundMessageStreams messageStreams) {
        this.messageStreams = messageStreams;
        this.messageChannelMap.putAll(setMessageChannelMap());
    }
    
    @Override
    public SuccessResponse sendMessageToKafkaTopic(final String msgType, 
    		final String  msg) throws KafkaProducerException {
    	
    	pdbLogger.info("IngestMessageServiceImpl.sendMessageToKafkaTopic::::: begin");
    	try {
    		boolean isMessageSentToMainTopic = sendMessageToOperationalTopic(msgType, msg);
    		
    		//Failed to send message to Operational topic, hence sending it to Retry topic 
    		if(!isMessageSentToMainTopic) {
    			boolean isMessageSentToRetryTopic = sendMessageToRetryTopic(msgType, msg);
    			
    			//Failed to send message to Retry topic as well, hence sending it to Error topic
    			if(!isMessageSentToRetryTopic) {
    				boolean isMessageSentToErrorTopic = sendMessageToDeadLetterQueueTopic(msgType, msg);
    				
    				//Failed to send message to Error topic as well, throwing exception
    				if(!isMessageSentToErrorTopic) {
    					throw new KafkaErrorTopicFailureException("Failed to send message to Error Topic as well:: messageType: " + msgType);
    				}
    			}
    		}
    	} catch(InvalidMessageTypeException ex) {
    		pdbLogger.error("IngestMessageServiceImpl.sendMessageToKafkaTopic::: InvalidMessageTypeException occured::"
    				+ ex.getMessage());
        	throw new InvalidMessageTypeException(ex.getMessage());
        	
        } catch(KafkaErrorTopicFailureException ex) {
    		pdbLogger.error("IngestMessageServiceImpl.sendMessageToKafkaTopic::: KafkaErrorTopicFailureException occured::"
    				+ ex.getMessage());
        	throw new KafkaErrorTopicFailureException(ex.getMessage());
        	
        } catch(Exception ex) {
    		pdbLogger.error("IngestMessageServiceImpl.sendMessageToKafkaTopic::: Exception occured while publishing message to kafka:::"
    				+ ex.getMessage());
    		
        	throw new KafkaProducerException(ex.getMessage());
        }
    	
    	return new SuccessResponse(statusCode, statusDesc);
    }
    
    protected boolean sendMessageToOperationalTopic(final String msgType, final String  msg){
    	boolean isMessageSent = false;
    	
    	MessageChannel messageChannel = Optional.ofNullable(ingestMessageHelper.
    			getMessageChannel(messageChannelMap, TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType(), msgType))
				.orElseThrow(() -> new InvalidMessageTypeException("Invalid Messase type specified in message!"));
		
		isMessageSent = buildMessageAndSendToTopic(msg, messageChannel);
    	
		if(isMessageSent) {
    		pdbLogger.info("Message sent to Operational Topic successfully");
    	}
    	return isMessageSent;
    	
    }
    
    protected boolean sendMessageToRetryTopic(final String msgType, final String  msg){
    	boolean isMessageSent = false;
    	int attemptCount = 0;
    	
    	MessageChannel messageChannel = Optional.ofNullable(ingestMessageHelper.
    			getMessageChannel(messageChannelMap, TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType(), msgType))
				.orElseThrow(() -> new InvalidMessageTypeException("Invalid Messase type specified in message!"));

    	pdbLogger.info("IngestMessageServiceImpl.sendMessageToRetryTopic::: building message for message type: " + msgType);	
		
		pdbLogger.debug("IngestMessageServiceImpl.sendMessageToRetryTopic: sending message to kafka:: attemptCount:" + attemptCount);
		while(attemptCount < maxRetryCount) {
			attemptCount++;
			isMessageSent = buildMessageAndSendToTopic(msg, messageChannel);
			
			if(isMessageSent) { 
	    		pdbLogger.info("Message sent to Retry Topic successfully");
				break;
			}
		}
    	return isMessageSent;
    }
    
    protected boolean sendMessageToDeadLetterQueueTopic(final String msgType, final String  msg) {
    	boolean isMessageSent = false;
    	
    	MessageChannel messageChannel = Optional.ofNullable(ingestMessageHelper.
    			getMessageChannel(messageChannelMap, TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType(), msgType))
				.orElseThrow(() -> new InvalidMessageTypeException("Invalid Messase type specified in message!"));
			
		isMessageSent = buildMessageAndSendToTopic(msg, messageChannel);
    	
		if(isMessageSent) {
    		pdbLogger.info("Message sent to Error Topic successfully");
    	}
		
    	return isMessageSent;
    }
    
    protected boolean buildMessageAndSendToTopic(final String msg, final MessageChannel messageChannel) {
    	pdbLogger.debug("IngestMessageServiceImpl.buildMessageAndSendToTopic::: building message");
    	Message<String> message = MessageBuilder
    			.withPayload(msg)
    			.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
    			.build();
    	pdbLogger.debug("IngestMessageServiceImpl.buildMessageAndSendToTopic::: About to send message to topic"); 
    	return messageChannel.send(message, timeoutInterval);
    }
    
    private Map<String, MessageChannel> setMessageChannelMap() {
    	String separator = IngestMessageHelper.separator;
    	
		Map<String, MessageChannel> messageChannelMap = new HashMap<String, MessageChannel>();
		
		//Putting Operational channels
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.DeactivateUI.getMsgType(), messageStreams.deactivateUIOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.ApplyUPUI.getMsgType(), messageStreams.applyUPUIOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.ApplyAUI.getMsgType(), messageStreams.applyAUIOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.Dispatch.getMsgType(), messageStreams.dispatchOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.Arrival.getMsgType(), messageStreams.arrivalOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.Transloading.getMsgType(), messageStreams.transLoadingOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.DisaggregateAUI.getMsgType(), messageStreams.disaggregateAUIOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.ReportDelivery.getMsgType(), messageStreams.reportDeliveryVanToRetailOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.IssueInvoice.getMsgType(), messageStreams.issueInvoiceOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.IssueOrderNo.getMsgType(), messageStreams.issueOrderNoOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.PaymentReceipt.getMsgType(), messageStreams.paymentReceiptOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Operational.msgChannelType() + separator 
    			+ TPDMessageTypes.Recall.getMsgType(), messageStreams.recallOutboundChannel());
    	
    	//Putting Retry channels
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.DeactivateUI.getMsgType(), messageStreams.deactivateUIRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.ApplyUPUI.getMsgType(), messageStreams.applyUPUIRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.ApplyAUI.getMsgType(), messageStreams.applyAUIRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.Dispatch.getMsgType(), messageStreams.dispatchRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.Arrival.getMsgType(), messageStreams.arrivalRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.Transloading.getMsgType(), messageStreams.transLoadingRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.DisaggregateAUI.getMsgType(), messageStreams.disaggregateAUIRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.ReportDelivery.getMsgType(), messageStreams.reportDeliveryVanToRetailRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.IssueInvoice.getMsgType(), messageStreams.issueInvoiceRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.IssueOrderNo.getMsgType(), messageStreams.issueOrderNoRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.PaymentReceipt.getMsgType(), messageStreams.paymentReceiptRetryOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + separator 
    			+ TPDMessageTypes.Recall.getMsgType(), messageStreams.recallRetryOutboundChannel());
    	
    	//Putting DLQ channels
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.DeactivateUI.getMsgType(), messageStreams.deactivatedlqUIOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.ApplyUPUI.getMsgType(), messageStreams.applyUPUIDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.ApplyAUI.getMsgType(), messageStreams.applyAUIDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.Dispatch.getMsgType(), messageStreams.dispatchDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.Arrival.getMsgType(), messageStreams.arrivalDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.Transloading.getMsgType(), messageStreams.transLoadingDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.DisaggregateAUI.getMsgType(), messageStreams.disaggregateAUIDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.ReportDelivery.getMsgType(), messageStreams.reportDeliveryVanToRetailDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.IssueInvoice.getMsgType(), messageStreams.issueInvoiceDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.IssueOrderNo.getMsgType(), messageStreams.issueOrderNoDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.PaymentReceipt.getMsgType(), messageStreams.paymentReceiptDLQOutboundChannel());
    	messageChannelMap.put(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + separator 
    			+ TPDMessageTypes.Recall.getMsgType(), messageStreams.recallDLQOutboundChannel());
    	
    	return messageChannelMap;
    }
}
