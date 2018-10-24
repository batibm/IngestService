/**
 * 
 */
package com.ibm.tpd.primarydb.ingestmsg.controller;

import com.ibm.tpd.primarydb.entity.response.SuccessResponse;
import com.ibm.tpd.primarydb.exception.KafkaProducerException;

/**
 * @author SangitaPal
 *
 */
public interface IngestMessageController {
	public SuccessResponse ingestMessage(final String msgType, final String msgBody) throws KafkaProducerException;
	
}
