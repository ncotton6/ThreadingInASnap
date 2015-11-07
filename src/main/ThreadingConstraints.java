package main;

import java.util.Map;
import java.util.HashMap;

import main.constructs.ContextSemaphore;

public class ThreadingConstraints {

	private static ThreadingConstraints tc = null;
	Map<String,ContextSemaphore> lockMap = new HashMap<String,ContextSemaphore>();
	private Integer numberOfRunningThreads = 0;
	
	private ThreadingConstraints(){}
	
	public static ThreadingConstraints get(){
		if(tc == null)
			synchronized (ThreadingConstraints.class) {
				if(tc == null){
					tc = new ThreadingConstraints();
				}
			}
		return tc;
	}
	
	public void incThreadCount(){
		synchronized (numberOfRunningThreads) {
			numberOfRunningThreads += 1;
		}
	}
	
	public void decThreadCount(){
		synchronized (numberOfRunningThreads) {
			numberOfRunningThreads -= 1;
		}
	}
	
	public int getThreadCount(){
		return numberOfRunningThreads;
	}
	
}
