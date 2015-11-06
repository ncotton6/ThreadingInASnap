package test.store;

import java.util.ArrayList;
import java.util.List;

import main.annotations.Order;

public class StoreSim {

	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		List<Cart> carts = new ArrayList<Cart>();
		List<Checkout> checkoutAreas = new ArrayList<Checkout>();
		// add carts
		for(int i = 0; i < 100; ++i){
			carts.add(new Cart(5));
		}
		// add checkoutAreas
		for(int i = 0; i < 3; ++i){
			checkoutAreas.add(new Checkout(250));
		}
		
		for(int i = 0; i < carts.size(); ++i){
			Checkout co = checkoutAreas.get(i%checkoutAreas.size());
			co.addCart(carts.get(i));
		}
		
		for(Checkout co : checkoutAreas){
			co.start();
		}
		DisplayTime(start);
	}

	@Order
	private static void DisplayTime(long start) {
		System.out.println("Execution Time: " + (System.currentTimeMillis() - start));
	}

}
