package com.ibm.tpd.primarydb.ingestmsg;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Scope;

import com.ibm.tpd.primarydb.ingestmsg.service.TPDOutboundMessageStreams;
import com.ibm.tpd.primarydb.logger.PDBLogger;

/**
 * @author SangitaPal
 *
 */
//@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(TPDOutboundMessageStreams.class)
@ComponentScan({"com.ibm.tpd.primarydb.ingestmsg","com.ibm.tpd.primarydb.errorhandler"})
public class IngestMessageApplication {
	
	public static void main(String[] args) {
        
		SpringApplication.run(IngestMessageApplication.class, args);
	}
	
	@Bean
	@Scope("prototype")
	public PDBLogger pdbLogger() {
		return new PDBLogger(this.getClass());
	}
}
