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
	public abstract boolean contains(PointI p);
	public abstract Box getBoundingBox();
	public abstract Shape subtractBorder(int border);
	public abstract Shape clone();
	
	public boolean contains(Shape s){
		for(PointI p : s){
			if(!contains(p)) return false;
		}
		return true;
	}
	
}
