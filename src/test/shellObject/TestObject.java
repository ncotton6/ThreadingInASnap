package test.shellObject;

import main.annotations.Shell;

@Shell
public class TestObject {

	public static int instanceCount = 0;
	public int id = instanceCount++;
	public String name;
	public float cash;
	
	@Override
	public String toString() {
		return "TestObject [id=" + id + ", name=" + name + ", cash=" + cash
				+ "]";
	}
	
	public int getId(){
		return id;
	}
	
	
}
