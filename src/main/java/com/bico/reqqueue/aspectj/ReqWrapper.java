package com.bico.reqqueue.aspectj;

import javax.servlet.AsyncContext;
import org.aspectj.lang.ProceedingJoinPoint;
import com.bico.reqqueue.annotation.ReqQueue;

public class ReqWrapper {

	private AsyncContext asyncContext;
	
	private ReqQueue reqQueueAnnotation;
	
	private ProceedingJoinPoint pjp;
	
	private String uuid;
	
	private String queueName;
	
	public ReqWrapper() {
		
	}

	public ReqWrapper(AsyncContext asyncContext,ReqQueue reqQueueAnnotation,ProceedingJoinPoint pjp,String queueName,String uuid) {
		this.asyncContext = asyncContext;
		this.reqQueueAnnotation = reqQueueAnnotation;
		this.pjp = pjp;
		this.queueName = queueName;
		this.uuid = uuid;
	}
	
	public AsyncContext getAsyncContext() {
		return asyncContext;
	}
	public void setAsyncContext(AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}
	
	public ReqQueue getReqQueueAnnotation() {
		return reqQueueAnnotation;
	}
	public void setReqQueueAnnotation(ReqQueue reqQueueAnnotation) {
		this.reqQueueAnnotation = reqQueueAnnotation;
	}
	
	public ProceedingJoinPoint getPjp() {
		return pjp;
	}
	public void setPjp(ProceedingJoinPoint pjp) {
		this.pjp = pjp;
	}
	
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
