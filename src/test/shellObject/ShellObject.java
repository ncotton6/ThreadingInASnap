package test.shellObject;

import main.annotations.Async;
import main.annotations.Order;
import main.constructs.Future;

public class ShellObject {

	private static int lastCount = 0;

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; ++i) {
			Future<TestObject> to = generateObject();
			output(to);
		}
		outputEnd(start);
	}

	@Order
	private static void outputEnd(long start) {
		System.out.println("Took: " + (System.currentTimeMillis() - start));
	}

	@Async
	private static void output(Future<TestObject> to) {
		while(!to.isReady())
			Thread.yield();
		//to.get().toString();
		System.out.println("\t"+to.get().toString());
	}

	@Async
	private static Future<TestObject> generateObject() throws InterruptedException {
		Thread.sleep(200);
		TestObject to = new TestObject();
		to.cash = lastCount += 2;
		to.name = "Jar Jar Binks"; // idk doesn't matter
		Future f = new Future<TestObject>();
		f.set(to);
		return f;
	}

}
