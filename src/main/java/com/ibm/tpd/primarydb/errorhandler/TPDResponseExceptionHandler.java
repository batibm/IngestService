package com.ibm.tpd.primarydb.errorhandler;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.ibm.tpd.primarydb.entity.response.ErrorResponse;
import com.ibm.tpd.primarydb.exception.KafkaProducerException;

/**
 * @author SangitaPal
 * 
 * This is custom exception handler class for TPD primary DB. 
 *
 */

//@RefreshScope
@ControllerAdvice
@RestController
public class TPDResponseExceptionHandler extends ResponseEntityExceptionHandler {

	@Value("${kafka.producer.error.code}")
    private String kafkaProducerErrCode;
	
	@Value("${kafka.producer.error.msg}")
    private String kafkaProducerErrMsg;
	
	@Value("${generic.error.code}")
    private String genericErrCode;
	
	@Value("${generic.error.msg}")
    private String genericErrMsg;

	/**
	 * Method for handling Kafka Producer related  Exceptions.
	 * @param	WebRequest
	 * @param	KafkaProducerException
	 * @return	ResponseEntity<ExceptionResponse>
	 *
	 */
	@ExceptionHandler(KafkaProducerException.class)
	public final ResponseEntity<ErrorResponse> handleKafkaProducerException(KafkaProducerException ex, WebRequest request) {
		return new ResponseEntity<>(getExceptionResponse(request, ex, kafkaProducerErrCode, kafkaProducerErrMsg), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * Method for generic Exceptions.
	 * 
	 * @param	WebRequest
	 * @param	Exception
	 * @return	ResponseEntity<ExceptionResponse>
	 *
	 */
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<ErrorResponse> handleOtherExceptions(Exception ex, WebRequest request) {
		return new ResponseEntity<>(getExceptionResponse(request, ex, genericErrCode, genericErrMsg), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * Method for creating error response
	 *
	 */
	private ErrorResponse getExceptionResponse(WebRequest request, Exception ex, String errCode, String errMsg) {
		ErrorResponse errorResponse = new ErrorResponse(errCode,
				errMsg + ex.getMessage(), request.getDescription(false), new Date());
		
		return errorResponse;
	}
}