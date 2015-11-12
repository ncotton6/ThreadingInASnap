package main;

import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Semaphore;
import java.lang.reflect.*;

import main.annotations.*;
import main.constructs.*;

public aspect SnapThread {

	// inject shell objects
	declare parents : (@Shell *) implements ShellObj;
	declare parents : (@Shell *..*) implements ShellObj;

	public Future<ShellObj> ShellObj.__shellObject = null;

	// async methods
	pointcut async() : execution(@Async * *..*(..));

	Object around(): async(){
		// Generate semaphore
		// only creates the semaphore on the first
		// encounter
		ThreadingConstraints tc = ThreadingConstraints.get();
		MethodSignature ms = (MethodSignature) thisJoinPointStaticPart
				.getSignature();
		final Class<?> returnType = ms.getReturnType();
		final String key = ms.toLongString();
		if (tc.lockMap.get(key) == null) {
			synchronized (tc) {
				if (tc.lockMap.get(key) == null) {
					Async a = ms.getMethod().getAnnotation(Async.class);
					// create a semaphore for this method
					ContextSemaphore s = new ContextSemaphore(
							a.threads() <= 0 ? Integer.MAX_VALUE : a.threads(),
							true);
					tc.lockMap.put(key, s);
				}
			}
		}

		if (/*
			 * tc.getThreadCount() >= Runtime.getRuntime().availableProcessors()
			 * * 3 ||
			 */((!returnType.equals(Void.TYPE) && (!ShellObj.class
				.isAssignableFrom(returnType))))) {
			// create a constraint on the number of actively running threads.
			return proceed();
		} else if (returnType.equals(Void.TYPE)) {
			ThreadingConstraints.get().incThreadCount();
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					Semaphore sp = ThreadingConstraints.get().lockMap.get(key);
					try {
						sp.acquire();
						proceed();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} finally {
						sp.release();
						ThreadingConstraints.get().decThreadCount();
					}
				}
			});
			ThreadTree.get().addThread(Thread.currentThread(), t);
			t.start();
		} else {
			// create shell object
			final Future<ShellObj> future = new Future<ShellObj>();
			ShellObj ret = null;
			if (!returnType.equals(Void.TYPE)) {
				try {
					ret = (ShellObj) returnType.getConstructor().newInstance();
					ret.__shellObject = future;
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				}
			}
			ThreadingConstraints.get().incThreadCount();
			Thread t = new Thread(new Runnable() {
				public void run() {
					Semaphore sp = ThreadingConstraints.get().lockMap.get(key);
					try {
						try {
							sp.acquire();
							Object ret = proceed();
							future.set((ShellObj) ret);
							future.markDone();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						future.markDone();
					} finally {
						ThreadingConstraints.get().decThreadCount();
						sp.release();
					}
				}
			});
			ThreadTree.get().addThread(Thread.currentThread(), t);
			t.start();
			return ret;
		}
		return null;
	}

	// sync methods
	pointcut sync() : execution(@Sync * *..*(..));

	Object around() : sync(){
		String key = thisJoinPointStaticPart.getSignature().toLongString();
		ThreadingConstraints tc = ThreadingConstraints.get();
		ContextSemaphore sp = null;
		if ((sp = tc.lockMap.get(key)) == null) {
			synchronized (tc) {
				if ((sp = tc.lockMap.get(key)) == null) {
					sp = new ContextSemaphore(1);
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

	// Using a Shell
	pointcut shell(ShellObj shell) : (execution(* ShellObj+.*(..)))
			//|| set(ShellObj+.*) || get(ShellObj+.*)) 
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
				System.exit(1);
			}
		}
	}

}
