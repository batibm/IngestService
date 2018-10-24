/**
 * 
 */
package com.ibm.tpd.primarydb.ingestmsg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.tpd.primarydb.entity.response.SuccessResponse;
import com.ibm.tpd.primarydb.exception.KafkaProducerException;
import com.ibm.tpd.primarydb.ingestmsg.service.IngestMessageService;
import com.ibm.tpd.primarydb.logger.PDBLogger;

/**
 * @author SangitaPal
 *
 */
@RestController
@RequestMapping("/tpd/primarydb")
public class IngestMessageControllerImpl implements IngestMessageController{
	
	@Autowired
	private IngestMessageService ingestService;
	
	@Autowired
	PDBLogger pdbLogger;
	
	@Override
	@PostMapping(path="/ingest")
	public SuccessResponse ingestMessage(@RequestParam("msgtype") final String msgType,
			@RequestBody final String msgBody) throws KafkaProducerException {
		
		pdbLogger.info("IngestMessageControllerImpl.ingestMessage::: About to ingest");	
		return ingestService.sendMessageToKafkaTopic(msgType, msgBody);
	}
	
}
