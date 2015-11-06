package main;

import main.annotations.*;

public aspect SnapThread {

	// async methods
	pointcut async() : execution(@Async * *..*(..));
	
	Object around(): async(){
		Thread t = new Thread(new Runnable(){
			public void run(){
				proceed();
			}
		});
		ThreadTree.get().addThread(Thread.currentThread(), t);
		t.start();
		return new Object();
	}
	
	// sync methods
	
	// sync fields
	
	// order methods
	pointcut order() : execution(@Order * *..*(..));
	
	Object around(): order(){
		while(!ThreadTree.get().threadReady(Thread.currentThread())){
			try{
				Thread.sleep(250);
			}catch(InterruptedException e){}
		}
		proceed();
		return new Object();
	}
	
}
