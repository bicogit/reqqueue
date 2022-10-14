package com.bico.reqqueue.batch;

import java.lang.reflect.Method;
import java.util.Arrays;
import com.bico.reqqueue.aspectj.AbstractReqQueueAspectSupport;
import com.bico.reqqueue.aspectj.ReqWrapper;

public class Consumer implements Runnable {

	private ReqWrapper reqWrapper;

	public Consumer(ReqWrapper reqWrapper) {
		this.reqWrapper = reqWrapper;
	}

	public void run() {

		try {
			String handleMethodName = reqWrapper.getReqQueueAnnotation().handleHandler();
			Class<?>[] handleHandlerClass = reqWrapper.getReqQueueAnnotation().handleHandlerClass();

			Method handleHandlerMethod = AbstractReqQueueAspectSupport.extractHandleHandlerMethod(reqWrapper.getPjp(),reqWrapper.getQueueName(), handleMethodName, handleHandlerClass);
			if (null == handleHandlerMethod) {
				return;
			}

			Object[] originArgs = reqWrapper.getPjp().getArgs();
			// Construct args.
			Object[] args = Arrays.copyOf(originArgs, originArgs.length + 1);
			args[args.length - 1] = reqWrapper;

			AbstractReqQueueAspectSupport.invoke(reqWrapper.getPjp(), handleHandlerMethod, args);

		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (Throwable ex) {
			ex.printStackTrace();
		}
	}
}