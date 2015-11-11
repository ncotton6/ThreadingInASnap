package test.dbquery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.annotations.Async;
import main.annotations.Order;

public class DBSim {

	static Random rand = new Random();

	public static void main(String[] args) {
		for (int i = 0; i < 50; ++i) {
			getResult(String.valueOf(i));
		}

	}

	@Async
	private static void getResult(String string) {
		try {
			Thread.sleep(rand.nextInt(5000));
			output(string);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Order
	private static void output(String string) {
		System.out.println(string);
	}

}
