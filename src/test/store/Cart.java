package test.store;

public class Cart {
	public static int totalCarts = 0;
	
	public int cartNumber;
	public int[] items;

	public Cart(int items) {
		this.items = new int[items];
		for(int i = 0; i < this.items.length; ++i){
			this.items[i] = i+1;
		}
		this.cartNumber = ++totalCarts;
	}
}