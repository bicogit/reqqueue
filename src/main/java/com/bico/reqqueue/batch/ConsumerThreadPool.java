package com.bico.reqqueue.batch;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class ConsumerThreadPool {

	private static final Map<String,ThreadPoolTaskExecutor> threadPool = new ConcurrentHashMap<>();
	
	public static boolean hasThreadPool(String queueName) {
		ThreadPoolTaskExecutor executorService = threadPool.get(queueName);
		if(Objects.isNull(executorService)) {
			return false;
		}
		
		return true;
	}
	
	public synchronized static void initThreadPool(String queueName,int maxThreadNum) {
		
		ThreadPoolTaskExecutor executorService = threadPool.get(queueName);
		if(Objects.isNull(executorService)) {
			//ThreadPoolExecutor fixedThreadPool = (ThreadPoolExecutor)Executors.newFixedThreadPool(maxThreadNum);
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setCorePoolSize(maxThreadNum);
			executor.setMaxPoolSize(maxThreadNum);
	        executor.setQueueCapacity(Integer.MAX_VALUE);
	        executor.setKeepAliveSeconds(0);
	        executor.setThreadNamePrefix("TaskExecutePool-");
	        executor.setWaitForTasksToCompleteOnShutdown(true);
	        executor.initialize();
	        
			threadPool.put(queueName, executor);
		}
	}
	
	public static void addTask(String queueName,Consumer consumer) {
		ThreadPoolTaskExecutor executorService = threadPool.get(queueName);
		executorService.execute(consumer);
	}
	
	public static int taskNum(String queueName) {
		ThreadPoolTaskExecutor executorService = threadPool.get(queueName);
		return executorService.getQueueSize();
	}
	
	public static void stopTask() {
		threadPool.forEach((queueName,rhreadPoolExecutor) -> {
			rhreadPoolExecutor.shutdown();
		});
	}
}
