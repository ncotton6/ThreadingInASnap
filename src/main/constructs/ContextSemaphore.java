package main.constructs;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class ContextSemaphore extends Semaphore {

	private static final long serialVersionUID = 1L;
	private Set<Thread> currents = new HashSet<Thread>();

	public ContextSemaphore(int permits) {
		super(permits);
	}

	public ContextSemaphore(int permits, boolean fair) {
		super(permits, fair);
	}

	@Override
	public void acquire() throws InterruptedException {
		Thread current = Thread.currentThread();
		if (!currents.contains(current)) {
			super.acquire();
			currents.add(current);
		}
	}

	@Override
	public void release() {
		if (currents.contains(Thread.currentThread())) {
			super.release();
			currents.remove(Thread.currentThread());
		}
	}

}
