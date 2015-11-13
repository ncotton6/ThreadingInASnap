package test.shellObject;

import main.annotations.Async;
import main.annotations.Order;

public class ShellObject {

	private static int lastCount = 0;

	public static void main(String[] args) throws InterruptedException {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000; ++i) {
			TestObject to = generateObject();
			output(to);
		}
		outputEnd(start);
	}

	@Order
	private static void outputEnd(long start) {
		System.out.println("Took: " + (System.currentTimeMillis() - start));
	}

	@Async
	private static void output(TestObject to) {
		//to.toString();
		System.out.println("\t"+to.toString());
	}

	@Async
	private static TestObject generateObject() throws InterruptedException {
		Thread.sleep(200);
		TestObject to = new TestObject();
		to.cash = lastCount += 2;
		to.name = "Jar Jar Binks"; // idk doesn't matter
		return to;
	}

}
