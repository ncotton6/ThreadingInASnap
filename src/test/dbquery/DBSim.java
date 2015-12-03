package test.dbquery;

import java.util.Random;

import main.annotations.Async;
import main.annotations.Order;
import main.annotations.Service;
import main.annotations.Sync;

public class DBSim {

	static Random rand = new Random();
	@Sync
	static long start;

	public static void main(String[] args) throws InterruptedException {
		//printingService();
		start = System.currentTimeMillis();
		for (int i = 0; i < 50; ++i) {
			getResult(String.valueOf(i));
		}
		timeTaken();
	}
	
	@Service(daemon = true)
	private static void printingService() throws InterruptedException{
		while(true){
			System.out.println("hello World");
			Thread.sleep(1000);
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

	@Order
	private static void timeTaken() {
		System.out.println("Time Taken: "
				+ (System.currentTimeMillis() - start));
	}
}
