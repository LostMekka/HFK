/*
 */
package hfk;

/**
 *
 * @author LostMekka
 */
public abstract class Shape implements Iterable<PointI>{
	
	public abstract PointI getRandomPointInside();
	public abstract int getPointCount();
	public abstract boolean isInside(PointI p);
	
	public boolean contains(Shape s){
		for(PointI p : s){
			if(!isInside(p)) return false;
		}
		return true;
	}
	
}
