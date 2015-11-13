package main;

import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.*;

import main.annotations.*;
import main.constructs.*;

public aspect SnapThread {

	private static final String error_str = "Shouldn't have hit this point";

	// inject shell objects
	declare parents : (@Shell *) implements ShellObj;
	declare parents : (@Shell *..*) implements ShellObj;
	public Future<ShellObj> ShellObj.__shellObject = null;

	// async methods
	pointcut async_void() : execution(@Async void *..*(..));

	pointcut async_shell() : execution(@Async ShellObj+ *..*(..));

	pointcut async_future() : execution(@Async Future *..*(..));

	pointcut async_other() : execution(@Async (!void && !ShellObj+ && !Future) *..*(..));

	Object around(): async_void(){
		final ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cs.acquire();
					proceed();
				} catch (InterruptedException e) {
				} finally {
					cs.release();
				}
			}
		});
		ThreadTree.get().addThread(t);
		t.start();
		return null;
	}

	Object around(): async_shell(){
		final ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		Class<?> returnable = ((MethodSignature) thisJoinPointStaticPart
				.getSignature()).getMethod().getReturnType();
		ShellObj shell = null;
		try {
			shell = (ShellObj) returnable.getConstructor().newInstance();
		} catch (InvocationTargetException e) {
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		} catch (NoSuchMethodException e) {
		}
		final Future<ShellObj> future = new Future<ShellObj>();
		shell.__shellObject = future;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cs.acquire();
					ShellObj obj = (ShellObj) proceed();
					future.set(obj);
					future.markDone();
				} catch (InterruptedException e) {
				} finally {
					cs.release();
				}
			}
		});
		ThreadTree.get().addThread(t);
		t.start();
		return shell;
	}

	Object around(): async_future(){
		final ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		final Future<Object> future = new Future<Object>();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cs.acquire();
					Object ret = proceed();
					future.set(ret);
					future.markDone();
				} catch (InterruptedException e) {
				} finally {
					cs.release();
				}
			}
		});
		ThreadTree.get().addThread(t);
		t.start();
		return future;
	}

	Object around(): async_other(){
		final ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		Class<?> returnable = ((MethodSignature) thisJoinPointStaticPart
				.getSignature()).getReturnType();
		if (returnable.isInterface()) {
			// proxy
			final Future<Object> future = new Future<Object>();
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try{
						cs.acquire();
						Object ret = proceed();
						future.set(ret);
						future.markDone();
					}catch(InterruptedException e){}
					finally{
						cs.release();
					}
				}
			});
			ThreadTree.get().addThread(t);
			t.start();
			Object prox = Proxy.newProxyInstance(returnable.getClassLoader(), new Class<?>[]{returnable}, new ProxyHandler(future));
			return prox;
		}else{
			// regular object, nothing we can do with it
			return proceed();
		}
	}

	// sync methods
	pointcut sync() : execution(@Sync * *..*(..));

	Object around() : sync(){
		ContextSemaphore sp = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		try {
			sp.acquire();
			return proceed();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			sp.release();
		}
		throw new RuntimeException(error_str);
	}

	// sync fields

	// order methods
	pointcut order() : execution(@Order * *..*(..));

	Object around(): order(){
		while (!ThreadTree.get().threadReady(Thread.currentThread())) {
			Thread.yield();
		}
		ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		try {
			cs.acquire();
			return proceed();
		} catch (InterruptedException e) {
		} finally {
			cs.release();
		}
		throw new RuntimeException(error_str);
	}

	// Using a Shell
	pointcut shell(ShellObj shell) : execution(* ShellObj+.*(..)) 
									&& target(shell);

	before(ShellObj shell) : shell(shell) {
		if (shell.__shellObject != null) {
			while (!shell.__shellObject.isReady())
				Thread.yield();
			ShellObj actual = shell.__shellObject.get();
			makeEqual(shell, actual);
			shell.__shellObject = null;
		}
	}

	private static void makeEqual(ShellObj shell, ShellObj actual) {
		Class<?> clazz = shell.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				f.set(shell, f.get(actual));
			} catch (IllegalAccessException e) {
				// e.printStackTrace();
			} catch (Exception e) {
				System.err.println("EHHHHHHHH!!!!");
				e.printStackTrace();
				throw new RuntimeException("Cannot Move Data Over");
			}
		}
	}

	private static ContextSemaphore getSemaphore(MethodSignature sig) {
		String method = sig.toLongString();
		ContextSemaphore cs = ThreadingConstraints.get().getSemaphore(method);
		if (cs == null) {
			int threadCount = 1;
			Async a = sig.getMethod().getAnnotation(Async.class);
			if(a != null)
				threadCount = a.threads();
			cs = ThreadingConstraints.get().createSemaphore(method,
					threadCount <= 0 ? Integer.MAX_VALUE : threadCount);
		}
		return cs;
	}
}
