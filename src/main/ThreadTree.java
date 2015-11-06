package main;

import java.util.ArrayList;
import java.util.List;

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
		//System.out.println(parent.getName() + " :: >> " + spawn.getName());
		ThreadNode tn = find(parent, base);
		ThreadNode newNode = new ThreadNode(spawn);
		newNode.parent = tn;
		tn.add(newNode);
	}

	private ThreadNode find(Thread thread, ThreadNode node) {
		if (node.thread.equals(thread))
			return node;
		for (ThreadNode tn : node.spawn) {
			ThreadNode ret = find(thread, tn);
			if (ret != null)
				return ret;
		}
		return null;
	}

	public boolean threadReady(Thread thread) {
		ThreadNode tn = find(thread, base);
		boolean done = true;
		for (ThreadNode child : tn.spawn) {
			done = allDone(child);
			if (!done)
				break;
		}
		
		// the following code will execute for everything except for the main
		if (tn.parent != null) {
			List<ThreadNode> currentLevel = tn.parent.spawn;
			for (ThreadNode prevNode : currentLevel) {
				// you don't have to check the current thread or anything past
				// it
				if (prevNode.thread.equals(thread))
					break;
				done = allDone(prevNode);
				if (!done)
					break;
			}
		}
		return done;
	}

	private boolean allDone(ThreadNode child) {
		if (child.thread.isAlive())
			return false;
		for (ThreadNode tn : child.spawn) {
			if (!allDone(tn))
				return false;
		}
		return true;
	}

	private class ThreadNode {
		public ThreadNode parent;
		public Thread thread;
		public List<ThreadNode> spawn = new ArrayList<ThreadNode>();

		public ThreadNode(Thread parent) {
			this.thread = parent;
		}

		public void add(ThreadNode spawn) {
			this.spawn.add(spawn);
		}

	}

}
