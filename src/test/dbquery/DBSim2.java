package test.dbquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DBSim2 {

	static Random rand = new Random();
	static long start;
	static List<Thread> lst = new ArrayList<Thread>();

	public static void main(String[] args) {
		start = System.currentTimeMillis();
		for (int i = 0; i < 50; ++i) {
			getResult(String.valueOf(i));
		}
		for (Thread t : lst) {
			t.start();
		}
		try {
			lst.get(lst.size()-1).join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Time Taken: "
				+ (System.currentTimeMillis() - start));
	}

	private static void getResult(String string) {
		final String temp = string;
		final Thread thread = lst.size() == 0 ? null : lst.get(lst.size() - 1);
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
		lst.add(t);
	}
}
