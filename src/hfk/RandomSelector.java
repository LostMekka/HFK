
package hfk;

import hfk.game.GameController;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 *
 * @author LostMekka
 */
public class RandomSelector<T> {
	
	private class Item{
		public T i;
		public float p;
		public Item(T t, float p) {
			this.i = t;
			this.p = p;
		}
	}
	
	private final LinkedList<Item> l = new LinkedList<>();
	private float pSum = 0f;
	
	public void addItem(T item, float p){
		l.add(new Item(item, p));
		pSum += p;
	}
	
	public T getRandomItem(){
		float f = GameController.random.nextFloat()*pSum;
		Item item = null;
		for(Item i : l){
			if(f <= i.p){
				item = i;
				break;
			}
			f -= i.p;
		}
		if(item != null){
			return item.i;
		} else {
			throw new NoSuchElementException();
		}
	}
	
}
