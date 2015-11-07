package test.shellObject;

import main.annotations.Async;
import main.annotations.Order;

public class ShellObject {

	private static int lastCount = 0;
	private static Integer count = 0;

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		for(int i = 0; i < 10; ++i){
			TestObject to = generateObject();
			output(to);
		}
		outputEnd(start);
	}

	@Order
	private static void outputEnd(long start) {
		System.out.println("Took: " + (System.currentTimeMillis()-start));
	}

	@Async
	private static void output(TestObject to) {
		System.out.println("\t"+to.toString());
		synchronized (count) {
			System.out.println(count++);
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
