package test.callcount;

import main.annotations.Async;
import main.annotations.Order;
import main.annotations.Sync;

public class CallCount {

	private static int callCount = 0;

	public static void main(String[] args) {
		for (int i = 0; i < 4; ++i)
			start();
		output();
	}

	@Order
	private static void output() {
		System.out.println("Call Count Should be 100");
		System.out.println("Call Count: " + callCount);
	}

	@Async
	private static void start() {
		for (int i = 0; i < 25; ++i) {
			callMethod();
		}
	}

	@Sync
	private static void callMethod() {
		int currentValue = callCount;
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		callCount = currentValue + 1;
	}

}
