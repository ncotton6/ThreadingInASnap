package main;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class ThreadingConstraints {

	private static ThreadingConstraints tc = null;
	public Map<String,Semaphore> lockMap = new HashMap<String,Semaphore>();
	
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
	
}
