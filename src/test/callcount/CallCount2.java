package test.callcount;

import main.annotations.Async;
import main.annotations.Order;

public class CallCount2 {

	@Order
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
			int val = callCount;
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			callCount = val + 1;
		}
	}
}
