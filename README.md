# ReqQueue

利用Servlet3.0提供的AsyncContext功能，将请求队列化的一个简单框架

## Annotation

`@ReqQueue` 注解需要进行队列化的请求

- `value`: 队列名，必须填写且全局惟一
- `maxQueueLen`: 最大队列长度，就是队列中未处理的请求最大的允许数
- `threadNum` : 处理队列的线程数，可根据自身的业务配置
- `handleHandler` :处理逻辑的方法名，线程池调用此方法来处理逻辑
- `handleHandlerClass`:指定处理逻辑方法所在的类

For example:

```java
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
```

## Configuration


### Spring AOP

If you are using Spring AOP, you should add a configuration to register the aspect
as a Spring bean:

```java
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
```

##将Jar部署至本地Maven仓库
mvn clean install -Dmaven.test.skip=true

## 感谢
部分代码来自于Sentinel项目，谢谢
