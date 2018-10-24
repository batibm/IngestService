package com.ibm.tpd.primarydb.ingestmsg.util;

import java.util.Map;

import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

/**
 * @author SangitaPal
 *
 */
@Component
public class IngestMessageHelper {
	public static String separator = ":";
	
	public MessageChannel getMessageChannel(final Map<String, MessageChannel> messageChannelMap, final String messageChannelType, final String messageType) {
		MessageChannel messageChannel = messageChannelMap.get(messageChannelType + separator + messageType);
		
		return messageChannel;
	}
}
