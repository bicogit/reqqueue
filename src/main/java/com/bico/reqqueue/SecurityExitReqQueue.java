package com.bico.reqqueue;

import java.util.concurrent.atomic.AtomicBoolean;

import com.bico.reqqueue.batch.ConsumerThreadPool;

public class SecurityExitReqQueue {

	private static final AtomicBoolean exit = new AtomicBoolean(false);
	
	public static boolean isExit() {
		return exit.get();
	}
	
	public static void securityExit() {
		
		if(exit.compareAndSet(false, true)) {
			ConsumerThreadPool.stopTask();
		}
	}
}
