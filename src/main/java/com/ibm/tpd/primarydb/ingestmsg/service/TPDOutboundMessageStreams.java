package com.ibm.tpd.primarydb.ingestmsg.service;

//import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * @author SangitaPal
 *
 */
//@RefreshScope
//@EnableIntegration
@Component
public interface TPDOutboundMessageStreams {
    //Producer Operational topic channels
	String DeactivateUI_OUT = "deactivateUI_out";
	String ApplyUPUI_OUT = "applyUPUI_out";
	String ApplyAUI_OUT = "applyAUI_out";
	String Dispatch_OUT = "dispatch_out";
    String Arrival_OUT = "arrival_out";
    String TransLoading_OUT = "transLoading_out";
    String DisaggregateAUI_OUT = "disaggregateAUI_out";
    String ReportDeliveryVanToRetail_OUT = "reportDeliveryVanToRetail_out";
    String IssueInvoice_OUT = "issueInvoice_out";
    String IssueOrderNo_OUT = "issueOrderNo_out";
    String PaymentReceipt_OUT = "paymentReceipt_out";
    String Recall_OUT = "recall_out";
    
    //Producer Retry topic channels
    String DeactivateUI_RETRY_OUT = "deactivateUI_retry_out";
    String ApplyUPUI_RETRY_OUT = "applyUPUI_retry_out";
	String ApplyAUI_RETRY_OUT = "applyAUI_retry_out";
	String Dispatch_RETRY_OUT = "dispatch_retry_out";
    String Arrival_RETRY_OUT = "arrival_retry_out";
    String TransLoading_RETRY_OUT = "transLoading_retry_out";
    String DisaggregateAUI_RETRY_OUT = "disaggregateAUI_retry_out";
    String ReportDeliveryVanToRetail_RETRY_OUT = "reportDeliveryVanToRetail_retry_out";
    String IssueInvoice_RETRY_OUT = "issueInvoice_retry_out";
    String IssueOrderNo_RETRY_OUT = "issueOrderNo_retry_out";
    String PaymentReceipt_RETRY_OUT = "paymentReceipt_retry_out";
    String Recall_RETRY_OUT = "recall_retry_out";
    
    //Producer DLQ topic channels
    String DeactivateUI_DLQ_OUT = "deactivateUI_dlq_out";
    String ApplyUPUI_DLQ_OUT = "applyUPUI_dlq_out";
	String ApplyAUI_DLQ_OUT = "applyAUI_dlq_out";
	String Dispatch_DLQ_OUT = "dispatch_dlq_out";
    String Arrival_DLQ_OUT = "arrival_dlq_out";
    String TransLoading_DLQ_OUT = "transLoading_dlq_out";
    String DisaggregateAUI_DLQ_OUT = "disaggregateAUI_dlq_out";
    String ReportDeliveryVanToRetail_DLQ_OUT = "reportDeliveryVanToRetail_dlq_out";
    String IssueInvoice_DLQ_OUT = "issueInvoice_dlq_out";
    String IssueOrderNo_DLQ_OUT = "issueOrderNo_dlq_out";
    String PaymentReceipt_DLQ_OUT = "paymentReceipt_dlq_out";
    String Recall_DLQ_OUT = "recall_dlq_out";
    
    /*
     * Below message channels will be used to push the message to Operational topic.   
     */

    //Operational channel for message type eactivateUI: 2-3 
    //@Bean
    @Output(DeactivateUI_OUT)
    MessageChannel deactivateUIOutboundChannel();
    
    //Operational channel for message type Application of unit level UIs on unit packets: 3-1
    //@Bean
    @Output(ApplyUPUI_OUT)
    MessageChannel applyUPUIOutboundChannel();
    
    //Operational channel for message type Application of aggregate level UIs: 3-2
    //@Bean
    @Output(ApplyAUI_OUT)
    MessageChannel applyAUIOutboundChannel();
    
    //Operational channel for message type Dispatch : 3-3
    //@Bean
    @Output(Dispatch_OUT)
    MessageChannel dispatchOutboundChannel();
    
    //Operational channel for message type Arrival of tobacco products at a facility: 3-4
    //@Bean
    @Output(Arrival_OUT)
    MessageChannel arrivalOutboundChannel();
    
    //Operational channel for message type TransLoading_OUT: 3-5
    //@Bean
    @Output(TransLoading_OUT)
    MessageChannel transLoadingOutboundChannel();
    
    //Operational channel for message type disaggregateAUI: 3-6
    //@Bean
    @Output(DisaggregateAUI_OUT)
    MessageChannel disaggregateAUIOutboundChannel();
    
    //Operational channel for message type reportDeliveryVanToRetail: 3-7
    //@Bean
    @Output(ReportDeliveryVanToRetail_OUT)
    MessageChannel reportDeliveryVanToRetailOutboundChannel();
    
    //Operational channel for message type issueInvoice: 4-1
    //@Bean
    @Output(IssueInvoice_OUT)
    MessageChannel issueInvoiceOutboundChannel();
    
    //Operational channel for message type Issue of Order No: 4-2
    //@Bean
    @Output(IssueOrderNo_OUT)
    MessageChannel issueOrderNoOutboundChannel();
    
    //Operational channel for message type Receipt of payment: 4-3 
    //@Bean
    @Output(PaymentReceipt_OUT)
    MessageChannel paymentReceiptOutboundChannel();
    
    //Operational channel for message type recall: 5 
    //@Bean
    @Output(Recall_OUT)
    MessageChannel recallOutboundChannel();
    
    /*
     * Below message channels will be used to push the message to RETRY topic.   
     */
    
    //Retry channel for message type eactivateUI: 2-3 
    @Output(DeactivateUI_RETRY_OUT)
    MessageChannel deactivateUIRetryOutboundChannel();
    
    //Retry channel for message type Application of unit level UIs on unit packets: 3-1
    @Output(ApplyUPUI_RETRY_OUT)
    MessageChannel applyUPUIRetryOutboundChannel();
    
    //Retry channel for message type Application of aggregate level UIs: 3-2
    @Output(ApplyAUI_RETRY_OUT)
    MessageChannel applyAUIRetryOutboundChannel();
    
    //Retry channel for message type Dispatch : 3-3
    @Output(Dispatch_RETRY_OUT)
    MessageChannel dispatchRetryOutboundChannel();
    
    //Retry channel for message type Arrival of tobacco products at a facility: 3-4
    @Output(Arrival_RETRY_OUT)
    MessageChannel arrivalRetryOutboundChannel();
    
    //Retry channel for message type TransLoading_OUT: 3-5
    @Output(TransLoading_RETRY_OUT)
    MessageChannel transLoadingRetryOutboundChannel();
    
    //Retry channel for message type disaggregateAUI: 3-6
    @Output(DisaggregateAUI_RETRY_OUT)
    MessageChannel disaggregateAUIRetryOutboundChannel();
    
    //Retry channel for message type reportDeliveryVanToRetail: 3-7
    @Output(ReportDeliveryVanToRetail_RETRY_OUT)
    MessageChannel reportDeliveryVanToRetailRetryOutboundChannel();
    
    //Retry channel for message type issueInvoice: 4-1
    @Output(IssueInvoice_RETRY_OUT)
    MessageChannel issueInvoiceRetryOutboundChannel();
    
    //Retry channel for message type Issue of Order No: 4-2
    @Output(IssueOrderNo_RETRY_OUT)
    MessageChannel issueOrderNoRetryOutboundChannel();
    
    //Retry channel for message type Receipt of payment: 4-3 
    @Output(PaymentReceipt_RETRY_OUT)
    MessageChannel paymentReceiptRetryOutboundChannel();
    
    //Retry channel for message type recall: 5 
    @Output(Recall_RETRY_OUT)
    MessageChannel recallRetryOutboundChannel();
    
    /*
     * Below message channels will be used to push the message to dead letter queue i.e. ERROR topic.   
     */
    
    //DLQ channel for message type eactivateUI: 2-3 
    @Output(DeactivateUI_DLQ_OUT)
    MessageChannel deactivatedlqUIOutboundChannel();
    
    //DLQ channel for message type Application of unit level UIs on unit packets: 3-1
    @Output(ApplyUPUI_DLQ_OUT)
    MessageChannel applyUPUIDLQOutboundChannel();
    
    //DLQ channel for message type Application of aggregate level UIs: 3-2
    @Output(ApplyAUI_DLQ_OUT)
    MessageChannel applyAUIDLQOutboundChannel();
    
    //DLQ channel for message type Dispatch : 3-3
    @Output(Dispatch_DLQ_OUT)
    MessageChannel dispatchDLQOutboundChannel();
    
    //DLQ channel for message type Arrival of tobacco products at a facility: 3-4
    @Output(Arrival_DLQ_OUT)
    MessageChannel arrivalDLQOutboundChannel();
    
    //DLQ channel for message type TransLoading_OUT: 3-5
    @Output(TransLoading_DLQ_OUT)
    MessageChannel transLoadingDLQOutboundChannel();
    
    //DLQ channel for message type disaggregateAUI: 3-6
    @Output(DisaggregateAUI_DLQ_OUT)
    MessageChannel disaggregateAUIDLQOutboundChannel();
    
    //DLQ channel for message type reportDeliveryVanToRetail: 3-7
    @Output(ReportDeliveryVanToRetail_DLQ_OUT)
    MessageChannel reportDeliveryVanToRetailDLQOutboundChannel();
    
    //DLQ channel for message type issueInvoice: 4-1
    @Output(IssueInvoice_DLQ_OUT)
    MessageChannel issueInvoiceDLQOutboundChannel();
    
    //DLQ channel for message type Issue of Order No: 4-2
    @Output(IssueOrderNo_DLQ_OUT)
    MessageChannel issueOrderNoDLQOutboundChannel();
    
    //DLQ channel for message type Receipt of payment: 4-3 
    @Output(PaymentReceipt_DLQ_OUT)
    MessageChannel paymentReceiptDLQOutboundChannel();
    
    //DLQ channel for message type recall: 5 
    @Output(Recall_DLQ_OUT)
    MessageChannel recallDLQOutboundChannel();
}
