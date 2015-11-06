package test.store;

import java.util.ArrayList;
import java.util.List;

import main.annotations.Async;

public class Checkout {
	public long itemRate;
	public List<Cart> carts = new ArrayList<Cart>();

	public Checkout(long itemRate) {
		this.itemRate = itemRate;
	}
	
	public void processCart(Cart cart){
		System.out.println("Processing Cart #"+cart.cartNumber);
		for(int i : cart.items){
			System.out.println("\tItem#"+i);
			try {
				Thread.sleep(itemRate);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void addCart(Cart cart) {
		this.carts.add(cart);
	}
	
	@Async
	public void start(){
		for(Cart c : carts){
			processCart(c);
		}
	}
}