package main;

import java.util.Map;
import java.util.HashMap;

import main.constructs.ContextSemaphore;

public class ThreadingConstraints {

	private static ThreadingConstraints tc = null;
	private Map<String, ContextSemaphore> lockMap = new HashMap<String, ContextSemaphore>();
	private Integer numberOfRunningThreads = 0;

	private ThreadingConstraints() {
	}

	public static ThreadingConstraints get() {
		if (tc == null)
			synchronized (ThreadingConstraints.class) {
				if (tc == null) {
					tc = new ThreadingConstraints();
				}
			}
		return tc;
	}

	public ContextSemaphore getSemaphore(String method) {
		return lockMap.get(method);
	}

	public synchronized ContextSemaphore createSemaphore(String method,
			int threadCount) {
		ContextSemaphore cs = null;
		if ((cs = lockMap.get(method)) == null) {
			cs = new ContextSemaphore(threadCount, true);
			lockMap.put(method, cs);
		}
		return cs;
	}

	public void incThreadCount() {
		synchronized (numberOfRunningThreads) {
			numberOfRunningThreads += 1;
		}
	}

	public void decThreadCount() {
		synchronized (numberOfRunningThreads) {
			numberOfRunningThreads -= 1;
		}
	}

	public int getThreadCount() {
		return numberOfRunningThreads;
	}

}
