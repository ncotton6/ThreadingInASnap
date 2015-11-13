package main.constructs;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyHandler implements InvocationHandler {

	private Future<Object> container = null;
	
	public ProxyHandler(Future<Object> future){
		this.container = future;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		while(!container.isReady())
			Thread.yield();
		return method.invoke(container.get(), args);
	}

}
