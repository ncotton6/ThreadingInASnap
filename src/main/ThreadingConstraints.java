package main;

import java.util.Map;
import java.util.HashMap;

import main.constructs.ContextSemaphore;

/**
 * ThreadingConstraints class is a singleton that will hold a map of
 * ContextSemaphores that will constrain execution within portions of an
 * application.
 * 
 * @author Nathaniel Cotton
 * 
 */
public class ThreadingConstraints {

	// Local Variables
	private static ThreadingConstraints tc = null;
	private Map<String, ContextSemaphore> lockMap = new HashMap<String, ContextSemaphore>();
	private Integer numberOfRunningThreads = 0;

	private ThreadingConstraints() {
	}

	/**
	 * Gets the singleton instance of the ThreadingConstraints
	 * */
	public static ThreadingConstraints get() {
		if (tc == null)
			synchronized (ThreadingConstraints.class) {
				if (tc == null) {
					tc = new ThreadingConstraints();
				}
			}
		return tc;
	}

	/**
	 * Gets a ContextSemaphore created for a particular joinpoint.
	 */
	public ContextSemaphore getSemaphore(String joinpointSignature) {
		return lockMap.get(joinpointSignature);
	}

	/**
	 * Creates a new ContextSemaphore and places it in the map.
	 * 
	 * @param method
	 * @param threadCount
	 * @return
	 */
	public synchronized ContextSemaphore createSemaphore(String method,
			int threadCount) {
		ContextSemaphore cs = null;
		if ((cs = lockMap.get(method)) == null) {
			cs = new ContextSemaphore(threadCount, true);
			lockMap.put(method, cs);
		}
		return cs;
	}

	/**
	 * Increments the number of threads running
	 */
	public void incThreadCount() {
		synchronized (numberOfRunningThreads) {
			numberOfRunningThreads += 1;
		}
	}

	/**
	 * Decrements the number of threads running
	 */
	public void decThreadCount() {
		synchronized (numberOfRunningThreads) {
			numberOfRunningThreads -= 1;
		}
	}

	/**
	 * Retrieves the number of running threads
	 */
	public int getThreadCount() {
		return numberOfRunningThreads;
	}

}
