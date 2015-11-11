package main;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import main.constructs.ShellObj;

public class ObjectPool {

	private static ObjectPool pool = null;
	private Map<UUID, ShellObj> objectPool = new WeakHashMap<UUID, ShellObj>();
	private HashSet<UUID> errorPool = new HashSet<UUID>();

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
		errorPool.add(id);
	}

	public void addObject(UUID id, Object ret) {
		if (ret instanceof ShellObj) {
			if (objectPool.containsKey(id)) {
				System.err
						.println("::::::::::::::::::\nAlready Contained\n:::::::::::::::::");
			}
			//System.out.println("Adding: " + id);
			objectPool.put(id, (ShellObj) ret);
		} else {
			addError(id);
		}
	}

	public ShellObj get(UUID uuid) {
		while (true) {
			if (errorPool.contains(uuid))
				throw new RuntimeException("She's Not Coming :(");
			if (objectPool.containsKey(uuid))
				break;
			Thread.yield();
		}

		ShellObj ret = objectPool.get(uuid);
		objectPool.remove(uuid);
		System.out.println("Removing: " + uuid);
		if (ret == null) {
			System.err.println("Cannot Find: " + uuid);
		}
		return ret;
	}

}
