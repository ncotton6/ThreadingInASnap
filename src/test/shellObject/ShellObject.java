package test.shellObject;

import main.annotations.Async;

public class ShellObject {

	private static int lastCount = 0;

	public static void main(String[] args) throws InterruptedException {
		for(int i = 0; i < 10; ++i){
			TestObject to = generateObject();
			System.out.println(to.toString());
		}
	}

	@Async
	private static TestObject generateObject() throws InterruptedException {
		Thread.sleep(200);
		TestObject to = new TestObject();
		to.cash = lastCount  + 2;
		to.name = "Lord Nuts"; //idk doesn't matter
		return to;
	}

}
