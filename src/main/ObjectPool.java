package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import main.constructs.ShellObj;

public class ObjectPool {

	private static ObjectPool pool = null;
	private HashMap<String, ShellObj> objectPool = new HashMap<String, ShellObj>();
	private HashSet<String> errorPool = new HashSet<String>();

	private ObjectPool() {

	}

	public static ObjectPool get() {
		if (pool == null) {
			synchronized (ObjectPool.class) {
				if (pool == null) {
					pool = new ObjectPool();
				}
			}
		}
		return pool;
	}

	public void addError(UUID id) {
		errorPool.add(id.toString());
	}

	public void addObject(UUID id, Object ret) {
		if (ret instanceof ShellObj) {
			if(objectPool.containsKey(id.toString())){
				System.err.println("::::::::::::::::::\nAlready Contained\n:::::::::::::::::");
			}
			objectPool.put(id.toString(), (ShellObj) ret);
		} else {
			addError(id);
		}
	}

	public ShellObj get(UUID uuid) {
		String id = uuid.toString();
		while (true) {
			if(errorPool.contains(id))
				throw new RuntimeException("She's Not Coming :(");
			if(objectPool.containsKey(id))
				break;
		}
			
		ShellObj ret = objectPool.get(id);
		//objectPool.remove(id);
		if(ret == null){
			System.err.println(uuid);
			System.err.println("Containing object is null");
		}
		return ret;
	}

}
