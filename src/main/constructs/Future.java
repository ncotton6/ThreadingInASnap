package main.constructs;

public class Future<T> {

	private T obj = null;
	private boolean done = false;

	public Future() {
	}

	public boolean isReady() {
		return done;
	}

	public T get() {
		return obj;
	}

	public void set(T obj) {
		this.obj = obj;
	}
	
	public void markDone(){
		done = true;
	}

}
