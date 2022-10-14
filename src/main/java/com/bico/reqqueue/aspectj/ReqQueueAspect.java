package com.bico.reqqueue.aspectj;

import java.lang.reflect.Method;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.bico.reqqueue.SecurityExitReqQueue;
import com.bico.reqqueue.annotation.ReqQueue;
import com.bico.reqqueue.batch.Consumer;
import com.bico.reqqueue.batch.ConsumerThreadPool;

@Aspect
public class ReqQueueAspect extends AbstractReqQueueAspectSupport implements DisposableBean {

	@Pointcut("@annotation(com.bico.reqqueue.annotation.ReqQueue)")
	public void reqQueueAnnotationPointcut() {
	}

	@Around("reqQueueAnnotationPointcut()")
	public Object invokeWithReqQueue(ProceedingJoinPoint pjp) throws Throwable {
		Method originMethod = resolveMethod(pjp);

		ReqQueue annotation = originMethod.getAnnotation(ReqQueue.class);
		if (annotation == null) {
			throw new IllegalStateException("Wrong state for ReqQueue annotation");
		}

		String queueName = annotation.value();
		int maxQueueLen = annotation.maxQueueLen();
		int threadNum = annotation.threadNum();
		threadNum = threadNum == -1 ? availableProcessors() : threadNum;
		
		ReqWrapper reqWrapper = new ReqWrapper();
		
		try {
			Method handleHandlerMethod = extractHandleHandlerMethod(pjp,queueName,annotation.handleHandler(),annotation.handleHandlerClass());
			if(Objects.isNull(handleHandlerMethod)) {
				return pjp.proceed();
			}
			
			//初始化处理此队列的线程池
			if(!ConsumerThreadPool.hasThreadPool(queueName)) {
				ConsumerThreadPool.initThreadPool(queueName, threadNum);
			}
			
			if(SecurityExitReqQueue.isExit()) {
				return pjp.proceed();
			}
			
			if(maxQueueLen != -1) {
				int queueLen = ConsumerThreadPool.taskNum(queueName);
				if(queueLen > maxQueueLen) {
					return pjp.proceed();
				}
			}
			
			ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			HttpServletRequest req = servletRequestAttributes.getRequest();
			reqWrapper.setAsyncContext(req.startAsync());
			reqWrapper.setReqQueueAnnotation(annotation);
			reqWrapper.setPjp(pjp);
			reqWrapper.setQueueName(queueName);
			reqWrapper.setUuid(queueName);
			Consumer consumer = new Consumer(reqWrapper);
			ConsumerThreadPool.addTask(queueName,consumer);
			
			return null;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}

	
	@Override
	public void destroy() throws Exception {
		SecurityExitReqQueue.securityExit();
	}
}
