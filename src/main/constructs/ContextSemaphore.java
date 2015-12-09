package main.constructs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * ContextSemaphore extends the functionality of a Semaphore, by allowing
 * threads that already have access to continue execution. Therefore multiple
 * calls to acquire by one thread will allow the thread to continue execution.
 * 
 * @author Nathaniel Cotton
 * 
 */
public class ContextSemaphore extends Semaphore {

	private static final long serialVersionUID = 1L;
	private Map<Thread,Integer> currents = new HashMap<Thread,Integer>();

	public ContextSemaphore(int permits) {
		super(permits);
	}

	public ContextSemaphore(int permits, boolean fair) {
		super(permits, fair);
	}

	@Override
	public void acquire() throws InterruptedException {
		Thread current = Thread.currentThread();
		if (!currents.containsKey(current)) {
			super.acquire();
			currents.put(current,1);
		}else{
			currents.put(current, currents.get(current)+1);
		}
	}

	@Override
	public void release() {
		Thread thread = Thread.currentThread();
		if (currents.containsKey(thread)) {
			int count = currents.get(thread);
			currents.put(thread, count-1);
			if(count == 1){
				currents.remove(thread);
				super.release();
			}				
		}
	}

}
