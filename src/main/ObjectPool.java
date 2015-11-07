package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import main.constructs.ShellObj;

public class ObjectPool {

	private static ObjectPool pool = null;
	private HashMap<UUID, ShellObj> objectPool = new HashMap<UUID, ShellObj>();
	private HashSet<UUID> errorPool = new HashSet<UUID>();

	private ObjectPool() {
		
	}
	
	public static ObjectPool get(){
		if(pool == null){
			synchronized (ObjectPool.class) {
				if(pool == null){
					pool = new ObjectPool();
				}
			}
		}
		return pool;
	}
	
	public void addError(UUID id){
		errorPool.add(id);
	}
	
	public void addObject(UUID id, Object ret){
		if(ret instanceof ShellObj){
			objectPool.put(id, (ShellObj)ret);
		}else{
			addError(id);
		}
	}
	
	public ShellObj get(UUID id){
		while(!errorPool.contains(id) && !objectPool.containsKey(id))
			Thread.yield();
		if(errorPool.contains(id))
			throw new RuntimeException("She's Not Coming :(");
		ShellObj ret = objectPool.get(id);
		objectPool.remove(id);
		return ret;
	}

}
