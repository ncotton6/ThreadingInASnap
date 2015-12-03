package test.dbquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DBSim2 {

	static Random rand = new Random();
	static long start;
	static Thread prev = null;

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		for (int i = 0; i < 50; ++i) {
			getResult(String.valueOf(i));
		}
		try {
			prev.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Time Taken: "
				+ (System.currentTimeMillis() - start));
	}

	private static void getResult(String string) {
		final String temp = string;
		final Thread thread = prev;
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(rand.nextInt(5000));
					if (thread != null)
						thread.join();
					System.out.println(temp);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		prev = t;
		t.start();
	}
}
