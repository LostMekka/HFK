/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.game.slickstates.GameplayState;
import hfk.items.InventoryItem;
import hfk.mobs.Mob;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class Level {
	
	public static class Factory{
		private static Random ran = GameController.random;
		private Factory(){}
		private static class Box{
			public int x,y,w,h;
			public Box() {}
			public Box(int x, int y, int w, int h) {
				this.x = x;
				this.y = y;
				this.w = w;
				this.h = h;
			}
		}
		private static boolean boxesTouch(Box b1, Box b2){
			return	b1.x+b1.w >= b2.x && 
					b1.x <= b2.x+b2.w && 
					b1.y+b1.h >= b2.y && 
					b1.y <= b2.y+b2.h;
		}
		public static Level createTestArena(int sx, int sy, int difficulty, int itemScore){
			GameController ctrl = GameController.get();
			Level l = new Level();
			l.tiles = new Tile[sx][sy];
			int min = ran.nextInt(10)+3;
			int max = ran.nextInt(20)+10;
			generateRoomLevel(l, 0, 0, sx, sy, min, max, min, max);
			PointI plp = PointI.random(sx, sy);
			ctrl.player.pos = l.getNextFreeField(plp, null).toFloat();
			l.items.add(new Stairs(l.getNextFreeField(PointI.random(sx, sy), null)));
			addMobs(difficulty, plp, l);
			addItems(itemScore, l);
			l.defaultTile = Tile.Factory.createTile(new PointI(0, 0), Tile.TileType.blueWall);
			return l;
		}
		private static void generateRoomLevel(Level l, int lx, int ly, int lw, int lh, int minW, int maxW, int minH, int maxH){
			GameController ctrl = GameController.get();
			LinkedList<Box> roomsToSplit = new LinkedList<>();
			LinkedList<Box> finalRooms = new LinkedList<>();
			roomsToSplit.add(new Box(lx, ly, lw, lh));
			while(!roomsToSplit.isEmpty()){
				Box r1 = roomsToSplit.removeFirst();
				boolean vert = r1.w >= 2*minW + 1;
				boolean horiz = r1.h >= 2*minH + 1;
				if((!vert && !horiz) || (r1.w <= maxW && r1.h <= maxH && ran.nextFloat() <= 0.4f)){
					// do not split anymore
					finalRooms.add(r1);
					continue;
				}
				boolean dir = vert ? (horiz ? ran.nextBoolean() : false) : true;
				if(dir){
					// horizontal split
					int h = r1.h;
					r1.h = minH + ran.nextInt(h - 2*minH);
					Box r2 = new Box(r1.x, r1.y + r1.h + 1, r1.w, h - r1.h - 1);
					roomsToSplit.add(r1);
					roomsToSplit.add(r2);
				} else {
					// vertical split
					int w = r1.w;
					r1.w = minW + ran.nextInt(w - 2*minW);
					Box r2 = new Box(r1.x + r1.w + 1, r1.y, w - r1.w - 1, r1.h);
					roomsToSplit.add(r1);
					roomsToSplit.add(r2);
				}
			}
			for(int x=lx; x<lx+lw; x++){
				for(int y=ly; y<ly+lh; y++){
					Tile.TileType t = ran.nextFloat()>0.2 ? Tile.TileType.blueWall : Tile.TileType.blueWallB;
					l.tiles[x][y] = Tile.Factory.createTile(new PointI(x, y), t);
				}
			}
			for(Box b : finalRooms){
				if(b.w * b.h > 40){
					generateBoxLevel(l, b.x, b.y, b.w, b.h, 2, 3, 2, 3, 0.04f);
				} else {
					for(int x=b.x; x<b.x+b.w; x++){
						for(int y=b.y; y<b.y+b.h; y++){
							Tile.TileType t = ran.nextFloat()>0.4 ? Tile.TileType.blueFloor : Tile.TileType.blueFloorB;
							l.tiles[x][y] = Tile.Factory.createTile(new PointI(x, y), t);
						}
					}
				}
			}
			int n = finalRooms.size();
			for(int i1=0; i1<n; i1++){
				Box r1 = finalRooms.get(i1);
				for(int i2=i1+1; i2<n; i2++){
					Box r2 = finalRooms.get(i2);
					boolean ox = r2.x > r1.x;
					boolean oy = r2.y > r1.y;
					int dx = ox? r2.x-r1.x-r1.w : r1.x-r2.x-r2.w;
					int dy = oy? r2.y-r1.y-r1.h : r1.y-r2.y-r2.h;
					if(dx != 1 && dy != 1) continue;
					if(dx == 1 && dy < 0){
						int posX = ox ? r2.x-1 : r1.x-1;
						int startY = oy ? r2.y : r1.y;
						int endY = r1.y+r1.h > r2.y+r2.h ? r2.y+r2.h : r1.y+r1.h;
						int posY = startY + ran.nextInt(endY - startY);
						PointI p = new PointI(posX, posY);
						Door door = new Door(p, true);
						l.tiles[posX][posY] = Tile.Factory.createTile(p, Tile.TileType.blueFloor);
						l.items.add(door);
					}
					if(dy == 1 && dx < 0){
						int posY = oy ? r2.y-1 : r1.y-1;
						int startX = ox ? r2.x : r1.x;
						int endX = r1.x+r1.w > r2.x+r2.w ? r2.x+r2.w : r1.x+r1.w;
						int posX = startX + ran.nextInt(endX - startX);
						PointI p = new PointI(posX, posY);
						Door door = new Door(p, false);
						l.tiles[posX][posY] = Tile.Factory.createTile(p, Tile.TileType.blueFloor);
						l.items.add(door);
					}
				}
			}
		}
		private static void generateBoxLevel(Level l, int lx, int ly, int lw, int lh, int minW, int maxW, int minH, int maxH, float rate){
			if(minW > lw-2 || minH > lh-2) return; // boxes do not fit!
			LinkedList<Box> bl = new LinkedList<>();
			for(int i=0; i<lh*lw*rate; i++){
				Box b1 = new Box();
				b1.w = Math.min(ran.nextInt(maxW-minW+1)+minW, lw-2);
				b1.h = Math.min(ran.nextInt(maxH-minH+1)+minH, lh-2);
				b1.x = ran.nextInt(lw-b1.w-1)+1+lx;
				b1.y = ran.nextInt(lh-b1.h-1)+1+ly;
				boolean touch = false;
				for(Box b2 : bl) if(boxesTouch(b1, b2)){
					touch = true;
					break;
				}
				if(!touch) bl.add(b1);
			}
			for(int x=lx; x<lx+lw; x++){
				for(int y=ly; y<ly+lh; y++){
					Tile.TileType t = ran.nextFloat()>0.2 ? Tile.TileType.blueFloor : Tile.TileType.blueFloorB;
					l.tiles[x][y] = Tile.Factory.createTile(new PointI(x, y), t);
				}
			}
			for(Box b : bl){
				Tile.TileType t = null;
				switch(ran.nextInt(4)){
					case 0: t = Tile.TileType.boxA; break;
					case 1: t = Tile.TileType.boxB; break;
					case 2: t = Tile.TileType.boxC; break;
					case 3: t = Tile.TileType.boxD; break;
				}
				for(int x=b.x; x<b.x+b.w; x++){
					for(int y=b.y; y<b.y+b.h; y++){
						l.tiles[x][y] = Tile.Factory.createTile(new PointI(x, y), t);
					}
				}
			}
		}
		private static void addMobs(int diff, PointI plp, Level l){
//			System.out.println("\n--- level ----------------");
			GameController ctrl = GameController.get();
			LinkedList<PointI> ex = new LinkedList<>();
			Mob m;
			int d = diff, mc = Integer.MAX_VALUE, stack = 0;
			PointI p = null;
			for(;;){
				if(mc >= stack){
					mc = 0;
					do {
						p = PointI.random(l.getWidth(), l.getHeight());
					} while(p.hammingDistanceTo(plp) < 10);
					stack = ran.nextInt(ran.nextInt(ran.nextInt(12)+1)+1);
				}
				m = Mob.createMob(new PointF(), Math.min(d, Math.round(diff/6f)), ctrl.getLevelCount());
				if(m == null) break;
//				System.out.println("   mob: " + m.getDisplayName());
				mc++;
				d -= m.getDifficultyScore();
				PointI p2 = l.getNextFreeField(p, ex);
				if(p2 == null) break;
				m.pos = p2.toFloat();
				ex.add(p2);
				ctrl.mobs.add(m);
			}
		}
		private static void addItems(int rar, Level l){
			GameController ctrl = GameController.get();
			LinkedList<PointI> ex = new LinkedList<>();
			InventoryItem i;
			int r = rar, mc = Integer.MAX_VALUE, stack = 0;
			PointI p = null;
			for(;;){
				if(mc >= stack){
					mc = 0;
					p = PointI.random(l.getWidth(), l.getHeight());
					stack = ran.nextInt(ran.nextInt(8)+1);
				}
				i = InventoryItem.create(new PointF(), r);
				if(i == null) break;
				mc++;
				r -= i.getRarityScore();
				PointI p2 = l.getNextFreeField(p, ex);
				if(p2 == null) break;
				i.pos = p2.toFloat();
				ex.add(p2);
				ctrl.addItem(i);
			}
		}
	}
	
	private Tile[][] tiles;
	private Tile defaultTile;
	private LinkedList<UsableLevelItem> items = new LinkedList<>();
	
	private Level(){}
	
	public int getWidth(){
		return tiles.length;
	}
	
	public int getHeight(){
		return tiles[0].length;
	}
	
	public UsableLevelItem getUsableLevelItem(PointF p){
		PointI pp = p.round();
		for(UsableLevelItem i : items){
			if(i.pos.equals(pp)) return i;
		}
		return null;
	}
	
	public PointI getNextFreeField(PointI source, List<PointI> exclusions){
		if(!isWall(source.x, source.y) && (exclusions == null || !exclusions.contains(source))) return source;
		int d = Integer.MAX_VALUE;
		LinkedList<PointI> l = new LinkedList<>();
		for(int x=0; x<getWidth(); x++){
			for(int y=0; y<getHeight(); y++){
				if(!isWall(x, y)){
					PointI p = new PointI(x, y);
					if(exclusions != null && exclusions.contains(p)) continue;
					int d2 = p.hammingDistanceTo(source);
					if(d2 == d){
						l.add(p);
					} else if(d2 < d){
						l.clear();
						l.add(p);
						d = d2;
					}
				}
			}
		}
		if(l.isEmpty()) return null;
		PointI ans = l.get(GameController.random.nextInt(l.size()));
		if(isWall(ans.x, ans.y)) System.out.println("WARNING: getnextfreefield returned a wall tile!");
		return ans;
	}
	
	public boolean isInLevel(PointI p){
		return p.x >= 0 && p.x < getWidth() && p.y >= 0 && p.y < getHeight();
	}
	
	public void damageTile(PointI pos, int dmg){
		if(!isInLevel(pos)) return;
		if(tiles[pos.x][pos.y].isWall()){
			tiles[pos.x][pos.y] = tiles[pos.x][pos.y].damage(dmg);
		} else {
			UsableLevelItem toDelete = null;
			for(UsableLevelItem i : items){
				if(i.pos.equals(pos)){
					if(i.damage(dmg)) toDelete = i;
				}
			}
			items.remove(toDelete); // if toDelete is null, nothing is removed
		}
	}
	
	public boolean isWall(int x, int y){
		if(x<0 || y<0 || x>=getWidth() || y>=getHeight()) return true;
		if(tiles[x][y].isWall()) return true;
		for(UsableLevelItem i : items) if(i instanceof Door && i.pos.x == x && i.pos.y == y && !((Door)i).isOpen()) return true;
		return false;
	}
	
	public void draw(int x1, int y1, int x2, int y2){
		for(int x=x1; x<=x2; x++){
			for(int y=y1; y<=y2; y++){
				if(x >= 0 && x < getWidth() && y >= 0 && y < getHeight()){
					tiles[x][y].draw();
				} else {
					defaultTile.move(x, y);
					defaultTile.draw();
				}
			}
		}
		for(UsableLevelItem i : items) i.draw();
	}
	
	private LinkedList<PointI> getDirectlyReachableTiles(PointI p){
		LinkedList<PointI> ans = new LinkedList<>();
		if(!isWall(p.x+1, p.y)) ans.add(new PointI(p.x+1, p.y));
		if(!isWall(p.x-1, p.y)) ans.add(new PointI(p.x-1, p.y));
		if(!isWall(p.x, p.y+1)) ans.add(new PointI(p.x, p.y+1));
		if(!isWall(p.x, p.y-1)) ans.add(new PointI(p.x, p.y-1));
		return ans;
	}
	
	private void insert(PointI p, PointI[] a){
		for(int i=0; i<a.length; i++){
			if(a[i] == null){
				a[i] = p;
				return;
			}
		}
	}
	
	private PointI getRandom(PointI[] a){
		int i;
		for(i=0; i<a.length; i++){
			if(a[i] == null) break;
		}
		if(i == 0) return null;
		return a[GameController.random.nextInt(i)];
	}
	
	private void insert(WayPoint p, WayPoint[] a){
		if(a[0] == null){
			a[0] = p;
		} else {
			if(a[0].dist < p.dist) return;
			if(a[0].dist > p.dist){
				a[0] = p;
				for(int i=1; i<a.length; i++) a[i] = null;
			}
			for(int i=0; i<a.length; i++){
				if(a[i] == null){
					a[i] = p;
					return;
				}
			}
		}
	}
	
	private WayPoint getRandom(WayPoint[] a){
		int i;
		for(i=0; i<a.length; i++){
			if(a[i] == null) break;
		}
		if(i == 0) return null;
		return a[GameController.random.nextInt(i)];
	}
	
	public LinkedList<PointF> getRandomPath(PointF start, int len, boolean shorten){
		PointI[][][] flowField = new PointI[getWidth()][getHeight()][4];
		LinkedList<PointI> expanded = new LinkedList<>();
		LinkedList<PointI> toExpand = new LinkedList<>();
		LinkedList<PointI> justExpanded = new LinkedList<>();
		toExpand.add(start.round());
		
		for(int i=0; i<=len; i++){
			justExpanded.clear();
			expanded.addAll(toExpand);
			for(PointI p1 : toExpand) for(PointI p2 : getDirectlyReachableTiles(p1)){
				if(expanded.contains(p2)) continue;
				if(!justExpanded.contains(p2)) justExpanded.add(p2);
				insert(p1, flowField[p2.x][p2.y]);
			}
			if(justExpanded.isEmpty()){
				// no path with that length found! break out early
				justExpanded.addAll(toExpand);
				break;
			}
			toExpand.clear();
			toExpand.addAll(justExpanded);
		}
		
		LinkedList<PointF> ans = new LinkedList<>();
		PointI p = justExpanded.get(GameController.random.nextInt(justExpanded.size()));
		while(p != null){
			ans.addFirst(p.toFloat());
			p = getRandom(flowField[p.x][p.y]);
		}
		ans.set(0, start);
		if(shorten) removeTrivialWaypoints(ans);
		return ans;
	}
	
	private class WayPoint{
		public PointI p;
		public int score, dist;
		public WayPoint(PointI p, int score, int dist) {
			this.p = p;
			this.score = score;
			this.dist = dist;
		}
		@Override
		public String toString() {
			return "WP{" + p.x + "," + p.y + " / " + score + ", " + dist + '}';
		}
	}
	
	public LinkedList<PointF> getPathAwayFrom(PointF start, PointF target, int maxLength, boolean shorten){
		PointI s = start.round();
		PointI e = target.round();
		WayPoint[][][] flowField = new WayPoint[getWidth()][getHeight()][4];
		LinkedList<WayPoint> expanded = new LinkedList<>();
		LinkedList<WayPoint> toExpand = new LinkedList<>();
		toExpand.add(new WayPoint(s, -s.hammingDistanceTo(e), 0));
		
		while(!toExpand.isEmpty()){
			WayPoint wp1 = toExpand.removeFirst();
			// if maximum path length is reached, return path to that point instead
			if(wp1.dist >= maxLength){
				e = wp1.p;
				break;
			}
			expanded.add(wp1);
			for(PointI p2 : getDirectlyReachableTiles(wp1.p)){
				if(contains(p2, expanded)) continue;
				WayPoint wp2 = new WayPoint(p2, wp1.dist + 1 - p2.hammingDistanceTo(e), wp1.dist + 1);
				if(!contains(p2, toExpand)) insertSorted(wp2, toExpand);
				insert(wp1, flowField[wp2.p.x][wp2.p.y]);
			}
		}
		// get point that has largest distance
		LinkedList<PointI> best = new LinkedList<>();
		int d = Integer.MAX_VALUE;
		for(WayPoint w : expanded){
			int d2 = w.p.hammingDistanceTo(e);
			if(d2 == d){
				best.add(w.p);
			} else if(d2 < d || best.isEmpty()){
				best.clear();
				best.add(w.p);
				d = d2;
			}
		}
		e = best.get(GameController.random.nextInt(best.size()));
		
		LinkedList<PointF> ans = new LinkedList<>();
		WayPoint w = new WayPoint(e, 0, 0);
		while(w != null){
			ans.addFirst(w.p.toFloat());
			w = getRandom(flowField[w.p.x][w.p.y]);
		}
		ans.set(0, start);
		if(shorten) removeTrivialWaypoints(ans);
		return ans;
	}
	
	public LinkedList<PointF> getPathTo(PointF start, PointF end, int maxLength, boolean shorten){
		PointI s = start.round();
		PointI e = end.round();
		WayPoint[][][] flowField = new WayPoint[getWidth()][getHeight()][4];
		LinkedList<WayPoint> expanded = new LinkedList<>();
		LinkedList<WayPoint> toExpand = new LinkedList<>();
		toExpand.add(new WayPoint(s, s.hammingDistanceTo(e), 0));
		
		boolean done = false;
		while(!done && !toExpand.isEmpty()){
			WayPoint wp1 = toExpand.removeFirst();
			// if maximum path length is reached, return path to that point instead
			if(wp1.dist >= maxLength){
				e = wp1.p;
				break;
			}
			expanded.add(wp1);
			for(PointI p2 : getDirectlyReachableTiles(wp1.p)){
				if(contains(p2, expanded)) continue;
				WayPoint wp2 = new WayPoint(p2, wp1.dist + 1 + p2.hammingDistanceTo(e), wp1.dist + 1);
				if(!contains(p2, toExpand)) insertSorted(wp2, toExpand);
				insert(wp1, flowField[wp2.p.x][wp2.p.y]);
				if(p2.equals(e)){
					done = true;
					break;
				}
			}
		}
		if(!done){
			// point is not reachable. go to nearest point instead!
			LinkedList<PointI> best = new LinkedList<>();
			int d = Integer.MIN_VALUE;
			for(WayPoint w : expanded){
				int d2 = w.p.hammingDistanceTo(e);
				if(d2 == d){
					best.add(w.p);
				} else if(d2 > d || best.isEmpty()){
					best.clear();
					best.add(w.p);
					d = d2;
				}
			}
			e = best.get(GameController.random.nextInt(best.size()));
		}
		
		LinkedList<PointF> ans = new LinkedList<>();
		WayPoint w = new WayPoint(e, 0, 0);
		while(w != null){
			ans.addFirst(w.p.toFloat());
			w = getRandom(flowField[w.p.x][w.p.y]);
		}
		ans.set(0, start);
		if(shorten) removeTrivialWaypoints(ans);
		return ans;
	}
	
	private boolean contains(PointI p, LinkedList<WayPoint> l){
		for(WayPoint wp : l) if(wp.p.equals(p)) return true;
		return false;
	}
	
	private void insertSorted(WayPoint p, LinkedList<WayPoint> l){
		Iterator<WayPoint> iter = l.iterator();
		int i = 0;
		while(iter.hasNext()){
			if(p.score < iter.next().score) break;
			i++;
		}
		l.add(i, p);
	}
	
	public void removeTrivialWaypoints(LinkedList<PointF> path){
		if(path.size() <= 2) return;
		ListIterator<PointF> iter = path.listIterator();
		PointF p = iter.next();
		while(iter.hasNext()){
			iter.next();
			int len = 0;
			boolean visible = true;
			while(visible && iter.hasNext()){
				PointF p2 = iter.next();
				if(Float.isNaN(p2.x)) throw new RuntimeException("p2 x is NaN");
				if(Float.isNaN(p2.y)) throw new RuntimeException("p2 y is NaN");
				if(isVisibleFrom(p, p2, Float.MAX_VALUE)){
					len++;
				} else {
					visible = false;
				}
			}
			if(!visible) iter.previous();
			iter.previous();
			for(int i=0; i<len; i++){
				iter.previous();
				iter.remove();
			}
			p = iter.next();
		}
	}
	
	public PointI testCollision(PointF center, float size){
		int ix = Math.round(center.x);
		int iy = Math.round(center.y);
		int cap = (int)Math.ceil(size);
		for(int x=-cap; x<=cap; x++){
			for(int y=-cap; y<=cap; y++){
				if(isWall(x+ix, y+iy)){
					PointI p = new PointI(x+ix, y+iy);
					if(testSingleWallCollision(center, size/2f, p)){
						return p;
					}
				}
			}
		}
		return null;
	}
	
	// --- !!! --- SIMPLE COLLISION! SIZE MUST NOT BE GREATER THAN 1 !!!
	public PointF doCollision(PointF center, float size){
		if(size > 1) throw new IllegalArgumentException("size is greater than 1. this algorithm cannot handle that!!!");
		int ix = Math.round(center.x);
		int iy = Math.round(center.y);
		LinkedList<PointF> history = new LinkedList<>();
		int collisionCount = 0;
		ColAns ans = new ColAns();
		for(int x=-1; x<=1; x++){
			for(int y=-1; y<=1; y++){
				if(isWall(x+ix, y+iy)){
					PointF newCorr = doSingleWallCollision(center, size/2f, new PointI(x+ix, y+iy));
					if(newCorr.x != 0 || newCorr.y != 0){
						// new collision
						history.add(newCorr);
						collisionCount++;
						addCollisionEffect(ans, newCorr, center, history);
					}
				}
			}
		}
		if(collisionCount != history.size()){
			// some collisions were discarded to avoid false correction.
			// recalculate correction vector from history!
			ans = new ColAns();
			for(PointF newCorr : history) addCollisionEffect(ans, newCorr, center, history);
		}
		// if stuck, ignore any noclip axis (may be both)
		if(ans.noClipX) ans.oldCorr.x = 0f;
		if(ans.noClipY) ans.oldCorr.y = 0f;
		return ans.oldCorr;
	}
	
	private class ColAns{
		public PointF oldCorr = new PointF();
		public PointF lastCorrXInfl = null, lastCorrYInfl = null;
		public boolean noClipX = false, noClipY = false;
	}
	
	private void addCollisionEffect(ColAns ans, PointF newCorr, PointF center, LinkedList<PointF> history){
		// handle x axis
		if(newCorr.x != 0 && !ans.noClipY){
			if(ans.oldCorr.x == 0){
				ans.oldCorr.x = newCorr.x;
				ans.lastCorrXInfl = newCorr;
			} else {
				if(Math.signum(ans.oldCorr.x) == Math.signum(newCorr.x)){
					// same direction. take maximum, discard sideways movement of smaller one
					// this is important! sideway movements are visible otherwise!
					if(Math.abs(newCorr.x) > Math.abs(ans.oldCorr.x)){
						ans.oldCorr.x = newCorr.x;
						// delete old point from history, main routine will recalculate later
						history.remove(ans.lastCorrXInfl);
						ans.lastCorrXInfl = newCorr;
					} else {
						// delete new one from history
						history.remove(newCorr);
					}
				} else {
					// opposite direction, we are stuck!!
					// go between the two walls
					// and make other axis noclip to move out
					ans.oldCorr.x = (float)Math.floor(center.x) - center.x + 0.5f;
					ans.noClipY = true;
				}
			}
		}
		// handle y axis
		if(newCorr.y != 0 && !ans.noClipX){
			if(ans.oldCorr.y == 0){
				ans.oldCorr.y = newCorr.y;
				ans.lastCorrYInfl = newCorr;
			} else {
				if(Math.signum(ans.oldCorr.y) == Math.signum(newCorr.y)){
					// same direction. take maximum, discard sideways movement of smaller one
					// this is important! sideway movements are visible otherwise!
					if(Math.abs(newCorr.y) > Math.abs(ans.oldCorr.y)){
						ans.oldCorr.y = newCorr.y;
						// delete old point from history, main routine will recalculate later
						history.remove(ans.lastCorrYInfl);
						ans.lastCorrYInfl = newCorr;
					} else {
						// delete new one from history
						history.remove(newCorr);
					}
				} else {
					// opposite direction, we are stuck!!
					// go between the two walls
					// and make other axis noclip to move out
					ans.oldCorr.y = (float)Math.floor(center.y) - center.y + 0.5f;
					ans.noClipX = true;
				}
			}
		}
	}
	
	private boolean testSingleWallCollision(PointF cp, float cr, PointI sp){
		PointF p = cp.clone();
		if(p.x < sp.x-0.5f) p.x = sp.x-0.5f;
		if(p.x > sp.x+0.5f) p.x = sp.x+0.5f;
		if(p.y < sp.y-0.5f) p.y = sp.y-0.5f;
		if(p.y > sp.y+0.5f) p.y = sp.y+0.5f;
		p.x -= cp.x;
		p.y -= cp.y;
		return (p.x*p.x + p.y*p.y < cr*cr);
	}
	
	private PointF doSingleWallCollision(PointF cp, float cr, PointI sp){
		float dx = cp.x - sp.x;
		float dy = cp.y - sp.y;
		float sx = Math.signum(dx);
		float sy = Math.signum(dy);
		float mx = (0.5f - Math.abs(dx) + cr) * sx;
		float my = (0.5f - Math.abs(dy) + cr) * sy;
		boolean inX = (Math.abs(dx) < 0.5f);
		boolean inY = (Math.abs(dy) < 0.5f);
		if(inX && inY){
			// circle center is in the square.
			// move in direction that has smallest move distance
			if(Math.abs(mx) < Math.abs(my)){
				return new PointF(mx, 0f);
			} else {
				return new PointF(0f, my);
			}
		}
		if(inX && Math.abs(dy) < 0.5f+cr){
			// needs to move vertically
			return new PointF(0f, my);
		}
		if(inY && Math.abs(dx) < 0.5f+cr){
			// needs to move horizontally
			return new PointF(mx, 0f);
		}
		// test for collision with one corner
		dx -= 0.5f * Math.signum(dx);
		dy -= 0.5f * Math.signum(dy);
		float d = dx*dx + dy*dy;
		if(d == 0f) return new PointF(sx * cr / GameController.SQRT2, sy * cr / GameController.SQRT2);
		if(d < cr*cr){
			// collision with corner. we need a sqrt here!
			float mul = cr / (float)Math.sqrt(d);
			return new PointF(dx * mul - dx, dy * mul - dy);
		}
		// no collision.
		return new PointF(0f, 0f);
	}
	
	public PointF doRectCollision(float x1, float y1, float w1, float h1, float x2, float y2, float w2, float h2){
		PointF ans = new PointF();
		if(x1+w1<=x2 || x2+w2<=x1 || y1+h1<=y2 || y2+h2<=y1) return ans;
		// collision is happening.
		float dx1 = (x2-w1)-x1;
		float dx2 = (x2+w2)-x1;
		float dx = Math.abs(dx1)<Math.abs(dx2) ? dx1 : dx2;
		float dy1 = (y2-h1)-y1;
		float dy2 = (y2+h2)-y1;
		float dy = Math.abs(dy1)<Math.abs(dy2) ? dy1 : dy2;
		if(Math.abs(dx) < Math.abs(dy)){
			ans.x = dx;
		} else {
			ans.y = dy;
		}
		return ans;
	}
	
	public UsableLevelItem getUsableItemOnLine(PointF p1, PointF p2, float maxDistance, boolean ignoreWalls){
		for(PointI p : getTilesOnLine(p1, p2, maxDistance + 1f)){
			UsableLevelItem i = getUsableLevelItem(p.toFloat());
			if(i != null) return i;
			if(isWall(p.x, p.y) && !ignoreWalls) return null;
		}
		return null;
	}
	
	public LinkedList<PointI> getTilesOnLine(PointF p1, PointF p2, float maxDistance){
		LinkedList<PointI> ans = new LinkedList<>();
		if(p1.squaredDistanceTo(p2) > maxDistance*maxDistance){
			// set p2 to the point that is on the line and is maxDistance away from p1
			PointF tmp = p2.clone();
			tmp.subtract(p1);
			tmp.multiply(maxDistance / tmp.length());
			p2 = p1.clone();
			p2.add(tmp);
		}
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		int sx = (int)Math.signum(dx);
		int sy = (int)Math.signum(dy);
		if(Math.round(p1.x) == Math.round(p2.x) && Math.round(p1.y) == Math.round(p2.y)){
			ans.add(p1.round());
			return ans;
		}
		if(dx == 0){
			int y1 = Math.round(p1.y);
			int y2 = Math.round(p2.y);
			int x = Math.round(p1.x);
			for(int y=y1; y*sy<=y2*sy; y+=sy) ans.add(new PointI(x, y));
			return ans;
		}
		float m = dy / dx;
		float n = p1.y - m * p1.x;
		int x1 = Math.round(p1.x);
		int x2 = Math.round(p2.x);
		if(dy == 0){
			for(int x=x1; x*sx<=x2*sx; x+=sx){
				ans.add(new PointI(x, Math.round(p1.y)));
			}
		} else {
			for(int x=x1; x*sx<=x2*sx; x+=sx){
				int y1 = x == x1 ? Math.round(p1.y) : Math.round(m * (x - sx * 0.5f) + n - sy*0.001f);
				int y2 = x == x2 ? Math.round(p2.y) : Math.round(m * (x + sx * 0.5f) + n + sy*0.001f);
				for(int y=y1; y*sy<=y2*sy; y+=sy) ans.add(new PointI(x, y));
			}
		}
		return ans;
	}
	
	public boolean isVisibleFrom(PointF p1, PointF p2, float maxDistance){
		float dx = p2.x - p1.x;
		float dy = p2.y - p1.y;
		int sx = (int)Math.signum(dx);
		int sy = (int)Math.signum(dy);
		if(Math.round(p1.x) == Math.round(p2.x) && Math.round(p1.y) == Math.round(p2.y)) return true;
		if(dx*dx + dy*dy > maxDistance*maxDistance) return false;
		if(dx == 0){
			int y1 = Math.round(p1.y);
			int y2 = Math.round(p2.y);
			int x = Math.round(p1.x);
			for(int y=y1; y*sy<=y2*sy; y+=sy) if(isWall(x, y)) return false;
			return true;
		}
		float m = dy / dx;
		float n = p1.y - m * p1.x;
		int x1 = Math.round(p1.x);
		int x2 = Math.round(p2.x);
		for(int x=x1; x*sx<=x2*sx; x+=sx){
			if(dy == 0){
				if(isWall(x, Math.round(p1.y))) return false;
			} else {
				int y1 = x == x1 ? Math.round(p1.y) : Math.round(m * (x - sx * 0.5f) + n - sy*0.001f);
				int y2 = x == x2 ? Math.round(p2.y) : Math.round(m * (x + sx * 0.5f) + n + sy*0.001f);
				for(int y=y1; y*sy<=y2*sy; y+=sy) if(isWall(x, y)) return false;
			}
		}
		return true;
	}
	
	public boolean isVisibleFrom(Mob m1, Mob m2, float maxDistance){
		return isVisibleFrom(m1.pos, m2.pos, maxDistance);
	}
	
}
