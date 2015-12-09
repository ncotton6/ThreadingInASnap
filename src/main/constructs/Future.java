package main.constructs;

/**
 * The Future class is essentially a container to hold an object. This is the
 * tool that is used to connect return types to solve the return problem.
 * 
 * @author Nathaniel Cotton
 * 
 * @param <T>
 */
public class Future<T> {

	private T obj = null;
	private boolean done = false;

	public Future() {
	}

	/**
	 * Asks the object if it has been filled.
	 * 
	 * @return
	 */
	public boolean isReady() {
		return done;
	}

	/**
	 * Retrieves the contained object.
	 * 
	 * @return
	 */
	public T get() {
		return obj;
	}

	/**
	 * Sets the contained object
	 * 
	 * @param obj
	 */
	public void set(T obj) {
		this.obj = obj;
	}

	/**
	 * Marks the object as being filled.
	 */
	public void markDone() {
		done = true;
	}

}
