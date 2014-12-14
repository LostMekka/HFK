/*
 */
package hfk;

import hfk.game.GameController;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 *
 * @author LostMekka
 */
public class PointCloud extends Shape {

	private LinkedList<PointI> l = new LinkedList<>();
	private Box b = null;

	public PointCloud() {
	}

	public PointCloud(PointCloud c) {
		for(PointI p : c.l) l.add(new PointI(p));
		if(c.b != null) b = new Box(c.b);
	}
	
	public PointCloud(Shape s) {
		for(PointI p : s) l.add(new PointI(p));
	}
	
	public void addPoint(PointI p){
		if(!l.contains(p)){
			l.add(p);
			if(b == null){
				b = new Box(p.x, p.y, 1, 1);
			} else {
				if(p.x >= b.x + b.w) b.w = p.x - b.x + 1;
				if(p.x < b.x){ b.w += b.x - p.x; b.x = p.x; }
				if(p.y >= b.y + b.h) b.w = p.y - b.y + 1;
				if(p.y < b.y){ b.h += b.y - p.y; b.y = p.y; }
			}
		}
	}
	
	public void remove(PointI p){
		if(l.remove(p)) b = null; // mark as dirty
	}
	
	public void remove(Shape s){
		for(PointI p : s) remove(p);
	}
	
	public void addShape(Shape s){
		for(PointI p : s) addPoint(p);
	}

	@Override
	public Shape clone() {
		return new PointCloud(this);
	}

	@Override
	public Shape subtractBorder(int border) {
		PointCloud ans = new PointCloud();
		PointI p1 = new PointI();
		for(PointI p2 : this){
			int s = ans.l.size();
			for(p1.x=p2.x-border; p1.x<=p2.x+border; p1.x++){
				for(p1.y=p2.y-border; p1.y<=p2.y+border; p1.y++){
					if(!contains(p1)){
						ans.addPoint(p2.clone());
						break;
					}
				}
				if(ans.l.size() != s) break;
			}
		}
		for(PointI p : ans) remove(p);
		return ans;
	}
	
	@Override
	public PointI getRandomPointInside() {
		if(l.isEmpty()) throw new NoSuchElementException();
		return l.get(GameController.random.nextInt(l.size()));
	}

	@Override
	public int getPointCount() {
		return l.size();
	}

	@Override
	public boolean contains(PointI p) {
		return l.contains(p);
	}

	@Override
	public Box getBoundingBox() {
		if(l.isEmpty()) throw new RuntimeException("PointCloud is empty!");
		if(b == null){
			PointI q = l.getFirst();
			b = new Box(q.x, q.y, 1, 1);
			for(PointI p : l){
				if(p.x >= b.x + b.w) b.w = p.x - b.x + 1;
				if(p.x < b.x){ b.w += b.x - p.x; b.x = p.x; }
				if(p.y >= b.y + b.h) b.w = p.y - b.y + 1;
				if(p.y < b.y){ b.h += b.y - p.y; b.y = p.y; }
			}
		}
		return new Box(b);
	}

	@Override
	public Iterator<PointI> iterator() {
		return l.iterator();
	}
	
}
