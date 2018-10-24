package com.ibm.tpd.primarydb.ingestmsg.service;

import com.ibm.tpd.primarydb.entity.response.SuccessResponse;
import com.ibm.tpd.primarydb.exception.KafkaProducerException;

/**
 * @author SangitaPal
 *
 */
public interface IngestMessageService {
	public SuccessResponse sendMessageToKafkaTopic(final String msgType, final String msg) throws KafkaProducerException;
	
}
