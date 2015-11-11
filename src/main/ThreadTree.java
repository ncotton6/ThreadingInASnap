package main;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The ThreadTree has the job of maintaining order amongst all of the threads,
 * to ensure order of execution is maintained.
 * 
 * @author Nathaniel Cotton
 * 
 */
public class ThreadTree {

	// Local Variables
	private ThreadNode base;
	private static ThreadTree tt = null;

	/**
	 * Private constructor to induce the singleton functionality.
	 */
	private ThreadTree() {
		if (tt != null)
			throw new RuntimeException(
					"ThreadTree is a singleton, cannot create more than one");
		this.base = new ThreadNode(Thread.currentThread());
	}

	/**
	 * Gets the singleton instances of the ThreadTree to maintian order of the
	 * threads.
	 * 
	 * @return
	 */
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

	/**
	 * Adds a newly created thread to the tree
	 * 
	 * @param parent
	 * @param spawn
	 */
	public void addThread(Thread parent, Thread spawn) {
		ThreadNode tn = find(parent, base);
		if (tn != null)
			new ThreadNode(spawn, tn);
	}

	/**
	 * Uses DFS to search the tree of threads to find the node that represents
	 * the current thread.
	 * 
	 * @param thread
	 * @param node
	 * @return
	 */
	private ThreadNode find(Thread thread, ThreadNode node) {
		if (thread.equals(node.thread.get()))
			return node;
		for (ThreadNode tn : node.spawn) {
			return find(thread, tn);
		}
		return null;
	}

	/**
	 * This method determines if the thread is ready to proceed. A thread is
	 * given the right to proceed if and only if it doesn't have any child
	 * threads running.
	 * 
	 * @param thread
	 * @return
	 */
	public boolean threadReady(Thread thread) {
		ThreadNode tn = find(thread, base);
		if (tn != null) {
			// Check to make sure that all of the children of the node
			// have completed
			Iterator<ThreadNode> iter = tn.spawn.iterator();
			while (iter.hasNext()) {
				ThreadNode node = iter.next();
				if (allDone(node)) {
					// prune dead branches of the tree
					iter.remove();
				} else {
					return false;
				}
			}
			// Check to make sure all threads in the queue in front of
			// the node have finished.
			if (tn.parent != null) {
				Iterator<ThreadNode> queue = tn.parent.spawn.iterator();
				while (queue.hasNext()) {
					ThreadNode node = queue.next();
					if (node.equals(tn))
						break;
					else {
						if (allDone(node))
							queue.remove();
						else
							return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * This method will use DFS to determine if all of the children and the
	 * current thread have completed.
	 * 
	 * @param threadNode
	 * @return
	 */
	private boolean allDone(ThreadNode threadNode) {
		Thread t = threadNode.thread.get();
		if (threadNode.spawn.size() == 0
				&& (t == null || (t != null && !t.isAlive())))
			return true;
		if (t.isAlive())
			return false;
		for (ThreadNode tn : threadNode.spawn) {
			if (!allDone(tn))
				return false;
		}
		return true;
	}

	/**
	 * Inner class used to keep track of the relationship between threads.
	 * 
	 * @author Nathaniel Cotton
	 * 
	 */
	private class ThreadNode {
		// Local Variables
		public long id;
		public ThreadNode parent;
		public WeakReference<Thread> thread;
		public List<ThreadNode> spawn = new ArrayList<ThreadNode>();

		/**
		 * Simplest constructor for the ThreadNode
		 * 
		 * @param thread
		 */
		public ThreadNode(Thread thread) {
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

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ThreadNode other = (ThreadNode) obj;
			if (parent == null) {
				if (other.parent != null)
					return false;
			} else if (!parent.equals(other.parent))
				return false;
			if (spawn == null) {
				if (other.spawn != null)
					return false;
			} else if (!spawn.equals(other.spawn))
				return false;
			if (thread == null) {
				if (other.thread != null)
					return false;
			} else if (!thread.equals(other.thread))
				return false;
			return true;
		}
	}

}
