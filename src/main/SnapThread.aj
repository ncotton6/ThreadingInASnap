package main;

import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.*;

import main.annotations.*;
import main.constructs.*;

/**
 * The SnapThread aspect will capture methods marked with Async, Order, Service,
 * and Sync methods, to turn a single-threaded system into a multi-threaded one.
 * 
 * It will also prepare objects marked with Shell to semi-seamlessly interact
 * with the system.
 * 
 * @author Nathaniel Cotton
 * 
 */
public aspect SnapThread {

	private static final String error_str = "Shouldn't have hit this point";

	// inject shell objects
	declare parents : (@Shell *) implements ShellObject;
	declare parents : (@Shell *..*) implements ShellObject;
	public Future<ShellObject> ShellObject.__ShellObjectect = null;

	// async methods
	pointcut async_void() : execution(@Async void *..*(..));

	pointcut async_shell() : execution(@Async ShellObject+ *..*(..));

	pointcut async_future() : execution(@Async Future *..*(..));

	pointcut async_other() : execution(@Async (!void && !ShellObject+ && !Future) *..*(..));

	/**
	 * A method marked with Async, and has void as the return type is the
	 * easiest case. The proceed is wrapped up in a thread and kicked off. No
	 * return problem here :)
	 * 
	 * @return
	 */
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

	/**
	 * Sometimes the return type of an async method is an implementor of the
	 * ShellObject interface. Which indicates that the return type has had a
	 * Future field introduced. This method will create an instance of the
	 * return type class, as well as a future to be placed within the shell
	 * object. This future will be filled once the thread finishes execution. At
	 * this point once the object is used the values will be pulled out of the
	 * future and pumped into the shell object returned, essentially making them
	 * the same.
	 * 
	 * @return
	 */
	Object around(): async_shell(){
		final ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		Class<?> returnable = ((MethodSignature) thisJoinPointStaticPart
				.getSignature()).getMethod().getReturnType();
		ShellObject shell = null;
		try {
			shell = (ShellObject) returnable.getConstructor().newInstance();
		} catch (InvocationTargetException e) {
		} catch (IllegalAccessException e) {
		} catch (InstantiationException e) {
		} catch (NoSuchMethodException e) {
		}
		final Future<ShellObject> future = new Future<ShellObject>();
		shell.__ShellObjectect = future;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cs.acquire();
					ShellObject obj = (ShellObject) proceed();
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

	/**
	 * At this point the return type has already been determined to be a Future.
	 * In order to proceed following code will return an empty future, and once
	 * the thread execution of the method comes back with a value the Future
	 * will be set, and marked.
	 * 
	 * @return An empty Future
	 */
	Object around(): async_future(){
		final ContextSemaphore cs = getSemaphore((MethodSignature) thisJoinPointStaticPart
				.getSignature());
		final Future<Object> future = new Future<Object>();
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					cs.acquire();
					Future<?> ret = (Future<?>) proceed();
					future.set(ret.get());
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

	/**
	 * This is the final option for an async method, where we don't know what
	 * the return type is. If the return type pulled off of the MethodSignature
	 * is an interface we will create a proxy for the returnable. Once the
	 * thread has completed the proxy will be filled with the actual returned
	 * object, and then invoke methods off of that when needed.
	 * 
	 * On the other hand if the return type is a solid object we have no choice
	 * we must execute the method synchronously, and return the value given.
	 * 
	 * @return actual object, or proxy
	 */
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
			Object prox = Proxy.newProxyInstance(returnable.getClassLoader(),
					new Class<?>[] { returnable }, new ProxyHandler(future));
			return prox;
		} else {
			// regular object, nothing we can do with it
			return proceed();
		}
	}

	// sync methods
	pointcut sync() : execution(@Sync * *..*(..));

	/**
	 * Any method or field that is marked with the Sync annotation is
	 * essentially acts as synchronized block or method marker, in that it will
	 * only let one thread in at a time. It uses the ContextSemaphore to
	 * accomplish this.
	 * 
	 * @return
	 */
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

	// service methods
	pointcut service():execution(@Service void *..*(..));

	/**
	 * A method marked with the Service annotation receives special treatment.
	 * It will wrap the method into a thread and kick off execution, very
	 * similar to the Async annotation. With the difference that a service
	 * method will start of as a new base node within the ThreadTree data
	 * structure. This makes a service method act very similar to a main method
	 * when an application starts.
	 * 
	 * Their is a field held within the annotation that indicates whether or not
	 * the service should be considered a daemon service, or user service. If
	 * marked true, all threads created by the service will be marked as daemon.
	 * Which will allow the JVM to exit once only daemon threads are running.
	 * 
	 * @return null
	 */
	Object around(): service(){
		Service s = ((MethodSignature) thisJoinPointStaticPart.getSignature())
				.getMethod().getAnnotation(Service.class);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				proceed();
			}
		});
		ThreadTree.get().addService(t);
		t.setDaemon(s.daemon());
		t.start();
		return null;
	}

	// order methods
	pointcut order() : execution(@Order * *..*(..));

	/**
	 * Any method marked with the Order annotation will be surrounded with the
	 * following code. With the following code which will force the current
	 * executing thread to wait until all previously created threads have
	 * finished, and all threads created by those threads have completed, and so
	 * on and so forth. Once this thread has reached it's turn it will be
	 * allowed to proceed. Well once it has gained access to the
	 * ContextSemaphore, which is more of a formality given the prior
	 * constraint.
	 * 
	 * @return
	 */
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
	pointcut shell(ShellObject shell) : execution(* ShellObject+.*(..)) 
									&& target(shell);

	/**
	 * Before a ShellObject can be used it must have the values held in the
	 * actual object moved into its fields.
	 * 
	 * @param shell
	 */
	before(ShellObject shell) : shell(shell) {
		if (shell.__ShellObjectect != null) {
			while (!shell.__ShellObjectect.isReady())
				Thread.yield();
			ShellObject actual = shell.__ShellObjectect.get();
			makeEqual(shell, actual);
			shell.__ShellObjectect = null;
		}
	}

	/**
	 * This method takes two ShellObjects, one of which is the actual object,
	 * and the other is the shell object that is being used for assignment. It
	 * will then proceed to move over all field values from the actual object to
	 * the shell object. Thus making the shell object the actual object.
	 * 
	 * @param shell
	 * @param actual
	 */
	private static void makeEqual(ShellObject shell, ShellObject actual) {
		Class<?> clazz = shell.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				f.set(shell, f.get(actual));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method is given the method signature of the requesting method. With
	 * this signature it will look up the corresponding ContextSemaphore for
	 * constraining threads. If the ContextSemaphore doesn't exist this method
	 * will create the ContextSemaphore and associate it with the method
	 * signature.
	 * 
	 * @param sig
	 * @return
	 */
	private static ContextSemaphore getSemaphore(MethodSignature sig) {
		String method = sig.toLongString();
		ContextSemaphore cs = ThreadingConstraints.get().getSemaphore(method);
		if (cs == null) {
			int threadCount = 1;
			Async a = sig.getMethod().getAnnotation(Async.class);
			if (a != null)
				threadCount = a.threads();
			cs = ThreadingConstraints.get().createSemaphore(method,
					threadCount <= 0 ? Integer.MAX_VALUE : threadCount);
		}
		return cs;
	}

	// declarable errors
	declare error: 
		(@annotation(Async) && @annotation(Order))
		||(@annotation(Async) && @annotation(Service))
		||(@annotation(Async) && @annotation(Sync))
		||(@annotation(Order) && @annotation(Service))
		||(@annotation(Order) && @annotation(Sync))
		||(@annotation(Service) && @annotation(Sync))
	:"Only one threading annotation per method.";

	declare error: execution(@Service !void *..*(..))
	: "A Service method may not return a value.";

	/*
	 * declare error: @annotation(Shell) && (execution(*.new(..)) &&
	 * !execution(*.new())) :
	 * "A Shell object must have a parameterless constructor.";
	 */
}
