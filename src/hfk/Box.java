package hfk;

import hfk.game.GameController;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author LostMekka
 */
public class Box extends Shape{
	
	public static Box[] createOutsideBorder(Box b, int border){
		Box[] ans = new Box[4];
		ans[0] = new Box(b.x + border, b.y, b.w - 2*border, border);
		ans[1] = new Box(b.x + border, b.y + b.h - border, b.w - 2*border, border);
		ans[2] = new Box(b.x, b.y, border, b.h);
		ans[3] = new Box(b.x + b.w - border, b.y, border, b.h);
		return ans;
	}

	public static Box[] createOutsideBorder(int x, int y, int width, int height, int border){
		Box[] ans = new Box[4];
		ans[0] = new Box(x + border, y, width - 2*border, border);
		ans[1] = new Box(x + border, y + height - border, width - 2*border, border);
		ans[2] = new Box(x, y, border, height);
		ans[3] = new Box(x + width - border, y, border, height);
		return ans;
	}

	public int x,y,w,h;
	
	public Box() {}
	
	public Box(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	public Box(Box b) {
		this.x = b.x;
		this.y = b.y;
		this.w = b.w;
		this.h = b.h;
	}

	@Override
	public Shape clone() {
		return new Box(this);
	}

	@Override
	public Shape subtractBorder(int border) {
		PointCloud ans = new PointCloud();
		for(Box b : createOutsideBorder(this, border)) ans.addShape(b);
		x += border;
		y += border;
		w -= 2*border; if(w<0) w=0;
		h -= 2*border; if(h<0) h=0;
		return ans;
	}
	
	@Override
	public Box getBoundingBox() {
		return new Box(this);
	}
	
	@Override
	public PointI getRandomPointInside() {
		return new PointI(GameController.random.nextInt(h)+x, GameController.random.nextInt(h)+y);
	}

	public boolean contains(Box b) {
		return x<=b.x && y<=b.y && x+w>=b.x+b.w && y+h>=b.y+b.h;
	}

	@Override
	public boolean contains(PointI p) {
		return isInside(p.x, p.y);
	}

	public boolean isInside(int px, int py){
		return	x+w > px && x <= px && y+h > py && y <= py;
	}

	public boolean touchesBox(Box b){
		return	x+w >= b.x && 
				x <= b.x+b.w && 
				y+h >= b.y && 
				y <= b.y+b.h;
	}

	@Override
	public int getPointCount() {
		return w*h;
	}

	@Override
	public Iterator<PointI> iterator() {
		return new Iterator<PointI>() {
			int ix = x, iy = y;
			@Override
			public boolean hasNext() {
				return ix < x+w && iy < y+h;
			}
			@Override
			public PointI next() {
				if(!hasNext()) throw new NoSuchElementException();
				PointI p = new PointI(ix, iy);
				ix++;
				if(ix >= x+w){
					ix = x;
					iy++;
				}
				return p;
			}
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported");
			}
		};
	}

}

