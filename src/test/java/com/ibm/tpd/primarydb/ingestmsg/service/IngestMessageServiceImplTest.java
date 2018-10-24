package com.ibm.tpd.primarydb.ingestmsg.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringRunner;

import com.ibm.tpd.primarydb.entity.response.SuccessResponse;
import com.ibm.tpd.primarydb.exception.InvalidMessageTypeException;
import com.ibm.tpd.primarydb.exception.KafkaErrorTopicFailureException;
import com.ibm.tpd.primarydb.ingestmsg.util.IngestMessageHelper;
import com.ibm.tpd.primarydb.logger.PDBLogger;
import com.ibm.tpd.primarydb.util.TPDMessageChannelTypes;


/*
 * @author SangitaPal
 * 
 */

//@EnableKafka
@RunWith(SpringRunner.class)
@SpringBootTest
public class IngestMessageServiceImplTest {
	
	public static final String APPLYUPUI_INPUT_FILE_NAME="applyupui-3-1.json";
	public static final String APPLYAUI_INPUT_FILE_NAME="applyaui-3-2.json";
	public static final String DISPATCH_INPUT_FILE_NAME="dispatch-3-3.json";
	public static final String ARRIVAL_INPUT_FILE_NAME="arrival-3-4.json";
	public static final String TRANSLOADING_INPUT_FILE_NAME="transloading-3-5.json";
	public static final String DISAGGREGATEAUI_INPUT_FILE_NAME="disaggregateaui-3-6.json";
	public static final String REPORTDELIVERYVANTORETAIL_INPUT_FILE_NAME="reportdeliveryvantoretail-3-7.json";
	public static final String ISSUEINVOICE_INPUT_FILE_NAME="issueinvoice-4-1.json";
	public static final String ISSUEORDER_INPUT_FILE_NAME="issueorder-4-2.json";
	public static final String PAYMENTRECEIPT_INPUT_FILE_NAME="paymentreceipt-4-3.json";
	public static final String DEACTIVATEUI_INPUT_FILE_NAME="deactivateui-2-3.json";
	public static final String RECALL_INPUT_FILE_NAME="recall-5.json";
	
	String applyUPUIJsonMessage="";
	String applyAUIJsonMessage="";
	String dispatchJsonMessage="";
	String arrivalJsonMessage="";
	String transloadingJsonMessage="";
	String disaggregateAUIJsonMessage="";
	String reportDeliveryVanToRetailJsonMessage="";
	String issueInvoiceJsonMessage="";
	String issueOrderJsonMessage="";
	String paymentReceiptJsonMessage="";
	String deactivateUIJsonMessage="";
	String recallJsonMessage="";
	
	static SuccessResponse expectedResponse = null;
	
	@MockBean
	PDBLogger pdbLogger;
	
	@SpyBean
	IngestMessageServiceImpl ingestMessageService;
	
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "testTopic");

	@BeforeClass
	public static void setup() {
		System.setProperty("spring.kafka.bootstrap-servers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.brokers", embeddedKafka.getBrokersAsString());
		System.setProperty("spring.cloud.stream.kafka.binder.zkNodes", embeddedKafka.getZookeeperConnectionString());
		
		expectedResponse = new SuccessResponse();
		expectedResponse.setStatusCode("200");
		expectedResponse.setStatusDesc("Message pushed to Kafka topic successfully.");
	}
	
	@Before
	public void loadInputJson() {
		ClassLoader classLoader = new IngestMessageServiceImplTest().getClass().getClassLoader();

		try {
			File file = new File(classLoader.getResource(APPLYUPUI_INPUT_FILE_NAME).getFile());
			if(file.exists())
				applyUPUIJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(APPLYAUI_INPUT_FILE_NAME).getFile());
			if(file.exists())
				applyAUIJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(DISPATCH_INPUT_FILE_NAME).getFile());
			if(file.exists())
				dispatchJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(ARRIVAL_INPUT_FILE_NAME).getFile());
			if(file.exists())
				arrivalJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(TRANSLOADING_INPUT_FILE_NAME).getFile());
			if(file.exists())
				transloadingJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(DISAGGREGATEAUI_INPUT_FILE_NAME).getFile());
			if(file.exists())
				disaggregateAUIJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(REPORTDELIVERYVANTORETAIL_INPUT_FILE_NAME).getFile());
			if(file.exists())
				reportDeliveryVanToRetailJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(ISSUEINVOICE_INPUT_FILE_NAME).getFile());
			if(file.exists())
				issueInvoiceJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(ISSUEORDER_INPUT_FILE_NAME).getFile());
			if(file.exists())
				issueOrderJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(PAYMENTRECEIPT_INPUT_FILE_NAME).getFile());
			if(file.exists())
				paymentReceiptJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(DEACTIVATEUI_INPUT_FILE_NAME).getFile());
			if(file.exists())
				deactivateUIJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
			file = new File(classLoader.getResource(RECALL_INPUT_FILE_NAME).getFile());
			if(file.exists())
				recallJsonMessage = new String(Files.readAllBytes(file.toPath()));
			
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	/*
	 * Unit Test cases for sending message to operational topic.
	 */
	@Test
	public void sendMessageToKafkaTopic_whenApplyUPUIMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-1", applyUPUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenApplyAUIMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-2", applyAUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenDispatchMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-3", dispatchJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenArrivalMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-4", arrivalJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenTransLoadingMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-5", transloadingJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whendDisaggregateAUIMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-6", disaggregateAUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenReportDeliveryVanToRetailMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-7", reportDeliveryVanToRetailJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenIssueInvoiceMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-1", issueInvoiceJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenIssueOrderMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-2", issueOrderJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenPaymentReceiptMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-3", paymentReceiptJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenDeactivateUIMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("2-3", deactivateUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenRecallMessageTypeReceived_thenPushesMessageToOperationalTopicSuccessfully() {
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("5", recallJsonMessage));
	}
	
	@Test(expected=InvalidMessageTypeException.class)
	public void sendMessageToKafkaTopic_whenInvalidMessageTypeReceived_thenThrowsException() {
		ingestMessageService.sendMessageToKafkaTopic("10", applyAUIJsonMessage);
	}
	
	/*
	 * Unit Test cases for Retry Topic. When failed to send message to operational topic, then sending message to retry topic.
	 */
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendApplyUPUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-1", applyUPUIJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-1", applyUPUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendApplyUPUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-1");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-1", applyUPUIJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(applyUPUIJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-1", applyUPUIJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(applyUPUIJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendApplyAUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-2", applyAUIJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-2", applyAUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendApplyAUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-2");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-2", applyAUIJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(applyAUIJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-2", applyAUIJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(applyAUIJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDispatchMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-3", dispatchJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-3", dispatchJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDispatchMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-3");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-3", dispatchJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(dispatchJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-3", dispatchJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(dispatchJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendArrivalMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-4", arrivalJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-4", arrivalJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendArrivalMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-4");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-4", arrivalJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(arrivalJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-4", arrivalJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(arrivalJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendTransLoadingMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-5", transloadingJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-5", transloadingJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendTransLoadingMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-5");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-5", transloadingJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(transloadingJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-5", transloadingJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(transloadingJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDisaggregateAUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-6", disaggregateAUIJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-6", disaggregateAUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDisaggregateAUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-6");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-6", disaggregateAUIJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(disaggregateAUIJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-6", disaggregateAUIJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(disaggregateAUIJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendReportDeliveryVanToRetailMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-7", reportDeliveryVanToRetailJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-7", reportDeliveryVanToRetailJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendReportDeliveryVanToRetailMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "3-7");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-7", reportDeliveryVanToRetailJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(reportDeliveryVanToRetailJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-7", reportDeliveryVanToRetailJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(reportDeliveryVanToRetailJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendIssueInvoiceMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-1", issueInvoiceJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-1", issueInvoiceJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendIssueInvoiceJsonMessageMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "4-1");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-1", issueInvoiceJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(issueInvoiceJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-1", issueInvoiceJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(issueInvoiceJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendIssueOrderMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-2", issueOrderJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-2", issueOrderJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendIssueIOrderJsonMessageMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "4-2");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-2", issueOrderJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(issueOrderJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-2", issueOrderJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(issueOrderJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendPaymentReceiptMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-3", paymentReceiptJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-3", paymentReceiptJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendPaymentReceiptJsonMessageMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "4-3");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-3", paymentReceiptJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(paymentReceiptJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-3", paymentReceiptJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(paymentReceiptJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDeactivateUIMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("2-3", deactivateUIJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("2-3", deactivateUIJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDeactivateUIJsonMessageMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "2-3");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("2-3", deactivateUIJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(deactivateUIJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("2-3", deactivateUIJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(deactivateUIJsonMessage, messageChannel);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendRecallMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInFirstAttempt() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("5", recallJsonMessage);
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("5", recallJsonMessage));
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendRecallJsonMessageMsgToOperationalTopic_thenPushesMessageToRetryTopicSuccessfullyInRetryAttempt() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Retry.msgChannelType() + 
						IngestMessageHelper.separator + "5");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("5", recallJsonMessage);
		
		doReturn(false).doReturn(false).doReturn(false).doReturn(false).doReturn(true).
		when(ingestMessageService).buildMessageAndSendToTopic(recallJsonMessage, messageChannel);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("5", recallJsonMessage));
		verify(ingestMessageService, times(5)).buildMessageAndSendToTopic(recallJsonMessage, messageChannel);
	}
	
	/*
	 * Unit Test cases for Error or Dead Letter Queue Topic. When failed to send message to operational topic and Retry Topic both, then sending message to error topic.
	 */
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendApplyUPUIMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-1", applyUPUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-1", applyUPUIJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-1", applyUPUIJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-1", applyUPUIJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendApplyUPUIMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-1");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-1", applyUPUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-1", applyUPUIJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(applyUPUIJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-1", applyUPUIJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendApplyAUIMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-2", applyAUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-2", applyAUIJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-2", applyAUIJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-2", applyAUIJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendApplyAUIMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-2");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-2", applyAUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-2", applyAUIJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(applyAUIJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-2", applyAUIJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDispatchMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-3", dispatchJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-3", dispatchJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-3", dispatchJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-3", dispatchJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendDispatchMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-3");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-3", dispatchJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-3", dispatchJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(dispatchJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-3", dispatchJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendArrivalMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-4", arrivalJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-4", arrivalJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-4", arrivalJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-4", arrivalJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendArrivalMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-4");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-4", arrivalJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-4", arrivalJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(arrivalJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-4", arrivalJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendTransLoadingMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-5", transloadingJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-5", transloadingJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-5", transloadingJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-5", transloadingJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendTransLoadingMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-5");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-5", transloadingJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-5", transloadingJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(transloadingJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-5", transloadingJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDisaggregateAUIMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-6", disaggregateAUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-6", disaggregateAUIJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-6", disaggregateAUIJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-6", disaggregateAUIJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendDisaggregateAUIMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-6");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-6", disaggregateAUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-6", disaggregateAUIJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(disaggregateAUIJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-6", disaggregateAUIJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendReportDeliveryVanToRetailMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-7", reportDeliveryVanToRetailJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-7", reportDeliveryVanToRetailJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("3-7", reportDeliveryVanToRetailJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("3-7", reportDeliveryVanToRetailJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendReportDeliveryVanToRetailMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "3-7");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("3-7", reportDeliveryVanToRetailJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("3-7", reportDeliveryVanToRetailJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(reportDeliveryVanToRetailJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("3-7", reportDeliveryVanToRetailJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendIssueInvoiceMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-1", issueInvoiceJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("4-1", issueInvoiceJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-1", issueInvoiceJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("4-1", issueInvoiceJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendIssueInvoiceMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "4-1");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-1", issueInvoiceJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("4-1", issueInvoiceJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(issueInvoiceJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("4-1", issueInvoiceJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendIssueOrderMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-2", issueOrderJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("4-2", issueOrderJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-2", issueOrderJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("4-2", issueOrderJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendIssueOrderMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "4-2");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-2", issueOrderJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("4-2", issueOrderJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(issueOrderJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("4-2", issueOrderJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendPaymentReceiptMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-3", paymentReceiptJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("4-3", paymentReceiptJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("4-3", paymentReceiptJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("4-3", paymentReceiptJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendPaymentReceiptMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "4-3");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("4-3", paymentReceiptJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("4-3", paymentReceiptJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(paymentReceiptJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("4-3", paymentReceiptJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendDeactivateUIMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("2-3", deactivateUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("2-3", deactivateUIJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("2-3", deactivateUIJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("2-3", deactivateUIJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendDeactivateUIMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "2-3");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("2-3", deactivateUIJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("2-3", deactivateUIJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(deactivateUIJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("2-3", deactivateUIJsonMessage);
	}
	
	@Test
	public void sendMessageToKafkaTopic_whenFailedToSendRecallMsgToOperationalAndRetryTopic_thenPushesMessageToErrorTopicSuccessfully() {
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("5", recallJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("5", recallJsonMessage);
		
		assertEquals(expectedResponse, ingestMessageService.sendMessageToKafkaTopic("5", recallJsonMessage));
		verify(ingestMessageService, times(1)).sendMessageToDeadLetterQueueTopic("5", recallJsonMessage);
	}
	
	@Test(expected=KafkaErrorTopicFailureException.class)
	public void sendMessageToKafkaTopic_whenFailedToSendRecallMsgToOperationalAndRetryAndErrorTopic_thenThrowsException() {
		MessageChannel messageChannel = ingestMessageService.messageChannelMap.
				get(TPDMessageChannelTypes.Ingest_BAT_Error.msgChannelType() + 
						IngestMessageHelper.separator + "5");
		
		doReturn(false).when(ingestMessageService).sendMessageToOperationalTopic("5", recallJsonMessage);
		doReturn(false).when(ingestMessageService).sendMessageToRetryTopic("5", recallJsonMessage);		
		doReturn(false).when(ingestMessageService).buildMessageAndSendToTopic(recallJsonMessage, messageChannel);
		
		ingestMessageService.sendMessageToKafkaTopic("5", recallJsonMessage);
	}
}
