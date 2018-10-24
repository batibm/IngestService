package com.ibm.tpd.primarydb.ingestmsg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.tpd.primarydb.logger.PDBLogger;

/**
* @author Sangita Pal
*
*/

//@RefreshScope
@RestController
public class GreetController {
   
	@Autowired
	PDBLogger pdbLogger; 
	
    @RequestMapping("/greeting")
    public String greet(){
    	
		pdbLogger.debug("GreetController.greet::hello...we are inside ingest service");	
        return "hello from ingest-service !!!";
    }
}