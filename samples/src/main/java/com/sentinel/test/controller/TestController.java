package com.sentinel.test.controller;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bico.reqqueue.annotation.ReqQueue;
import com.bico.reqqueue.aspectj.ReqWrapper;
import com.bico.reqqueue.utils.ResponseUtil;

@RestController
public class TestController {
	
	@GetMapping(value = "/get")
	@ReqQueue(value = "get",handleHandler = "getHandleHandler")
	public void get(HttpServletRequest req,HttpServletResponse resp) throws InterruptedException {
		ResponseUtil.out(resp, "Hello World error");
	}
	
	public void getHandleHandler(HttpServletRequest req,HttpServletResponse resp,ReqWrapper reqWrapper) throws InterruptedException {

		AsyncContext asyncContext = reqWrapper.getAsyncContext();
		ResponseUtil.out(resp, "Hello World");
		asyncContext.complete();
	}
}
