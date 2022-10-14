package com.bico.reqqueue.aspectj;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.bico.reqqueue.utils.StringUtil;

public class ResourceMetadataRegistry {

	private static final Map<String, MethodWrapper> HANDLE_HANDLER_MAP = new ConcurrentHashMap<>();

	public static MethodWrapper lookupHandleHandler(String queueName) {
		return HANDLE_HANDLER_MAP.get(queueName);
	}
	
	public static void updateHandleFor(String queueName, Method method) {
		if (StringUtil.isBlank(queueName)) {
			throw new IllegalArgumentException("Bad argument");
		}
		
		HANDLE_HANDLER_MAP.put(queueName, MethodWrapper.wrap(method));
	}
}
