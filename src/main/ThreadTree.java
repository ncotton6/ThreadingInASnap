package main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nathaniel
 * 
 */
public class ThreadTree {

	private ThreadNode base;
	private static ThreadTree tt = null;

	private ThreadTree() {
		this.base = new ThreadNode(Thread.currentThread());
	}

	public static ThreadTree get() {
		if (tt == null) {
			synchronized (ThreadTree.class) {
				if (tt == null) {
					tt = new ThreadTree();
				}
			}
		}
		return tt;
	}

	public void addThread(Thread parent, Thread spawn) {
		ThreadNode tn = find(parent,base);
		if(tn != null)
			new ThreadNode(spawn, tn);
	}

	private ThreadNode find(Thread thread, ThreadNode node) {
		if(thread.equals(node.thread.get()))
			return node;
		for(ThreadNode tn : node.spawn){
			return find(thread,tn);
		}
		return null;
	}

	public boolean threadReady(Thread thread) {
		ThreadNode tn = find(thread,base);
		if(tn == null)
			
	}

	private boolean allDone(ThreadNode child) {

	}

	/**
	 * Inner class used to keep track of the relationship between threads.
	 * 
	 * @author Nathaniel Cotton
	 * 
	 */
	private class ThreadNode {
		/* Local Variables */
		public long id;
		public ThreadNode parent;
		public WeakReference<Thread> thread;
		public List<ThreadNode> spawn = new ArrayList<ThreadNode>();

		
		
		/**
		 * Simplest constructor for the ThreadNode
		 * 
		 * @param thread
		 */
		public ThreadNode(Thread thread){
			this.thread = new WeakReference<Thread>(thread);
		}
		
		/**
		 * Constructor to contain the relationship between threads.
		 * 
		 * @param Thread
		 *            the current thread
		 * @param ThreadNode
		 *            the thread that created the thread
		 */
		public ThreadNode(Thread thread, ThreadNode parent) {
			this.thread = new WeakReference<Thread>(thread);
			this.parent = parent;
			this.id = thread.getId();
			parent.spawn.add(this);
		}

	}

}
