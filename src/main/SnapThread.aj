package main;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.Semaphore;

import main.annotations.*;

public aspect SnapThread {

	// async methods
	pointcut async() : execution(@Async * *..*(..));

	Object around(): async(){
		ThreadingConstraints tc = ThreadingConstraints.get();
		final String key = thisJoinPointStaticPart.getSignature()
				.toLongString();
		Semaphore sp = null;
		if ((sp = tc.lockMap.get(key)) == null) {
			synchronized (tc) {
				if ((sp = tc.lockMap.get(key)) == null) {
					MethodSignature ms = (MethodSignature) thisJoinPointStaticPart
							.getSignature();
					Async a = ms.getMethod().getAnnotation(Async.class);
					// create a semaphore for this method
					Semaphore s = new Semaphore(
							a.threads() <= 0 ? Integer.MAX_VALUE : a.threads(),
							true);
					tc.lockMap.put(key, s);
				}
			}
		}
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					Semaphore sp = ThreadingConstraints.get().lockMap.get(key);
					sp.acquire();
					proceed();
					sp.release();
				} catch (InterruptedException e) {
					System.out.println("Interuppeted exception");
					e.printStackTrace();
				}
			}
		});
		ThreadTree.get().addThread(Thread.currentThread(), t);
		t.start();
		return new Object();
	}

	// sync methods
	pointcut sync() : execution(@Sync * *..*(..));

	Object around() : sync(){
		String key = thisJoinPointStaticPart.getSignature().toLongString();
		ThreadingConstraints tc = ThreadingConstraints.get();
		Semaphore sp = null;
		if ((sp = tc.lockMap.get(key)) == null) {
			synchronized (tc) {
				if ((sp = tc.lockMap.get(key)) == null) {
					sp = new Semaphore(1);
					tc.lockMap.put(key, sp);
				}
			}
		}
		try {
			sp.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Object ret = proceed();
		sp.release();
		return ret;
	}

	// sync fields

	// order methods
	pointcut order() : execution(@Order * *..*(..));

	Object around(): order(){
		while (!ThreadTree.get().threadReady(Thread.currentThread())) {
			Thread.yield();
		}
		return proceed();
	}

}
