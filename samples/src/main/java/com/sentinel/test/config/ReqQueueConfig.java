package com.sentinel.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bico.reqqueue.aspectj.ReqQueueAspect;

@Configuration
public class ReqQueueConfig {

	@Bean
	public ReqQueueAspect reqQueueAspect() {
		return new ReqQueueAspect();
	}
}
