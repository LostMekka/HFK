/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.items.InventoryItem;
import hfk.mobs.Mob;
import hfk.net.NetState;
import hfk.net.NetStateObject;
import hfk.net.NetStatePart;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public class Level implements NetStateObject{
	
	public LinkedList<UsableLevelItem> items = new LinkedList<>();
	private int tileSet;
	private Tile[][] tiles;
	private boolean[][] visible;
	private boolean[][] scouted;
	private PointI scoutedMin = null;
	private PointI scoutedMax = null;
	private PointI spawnPoint = null;
	private Tile defaultTile;
	private LinkedList<UsableLevelItem> itemsToRemove = new LinkedList<>();
	
	public Level(int sx, int sy, int tileSet){
		this.tileSet = tileSet;
		defaultTile = new Tile(0, tileSet);
		tiles = new Tile[sx][sy];
		visible = new boolean[sx+2][sy+2];
		scouted = new boolean[sx+2][sy+2];
		id = GameController.get().createIdFor(this);
	}
	
	public void printTilesInfo(){
		for(int y=0; y<getHeight(); y++){
			for(int x=0; x<getWidth(); x++){
				System.out.format("%2d ", tiles[x][y].getTileType());
			}
			System.out.println();
		}
	}

	public void print(){
		for(int y=0; y<getHeight(); y++){
			for(int x=0; x<getWidth(); x++){
				System.out.print(tiles[x][y].isWall() ? "[]" : "  ");
			}
			System.out.println();
		}
	}

	public PointI getNextFreeSpawnPoint() {
		return getNextFreeField(spawnPoint, null);
	}

	public PointI getSpawnPoint() {
		return spawnPoint.clone();
	}

	public void setSpawnPoint(PointI spawnPoint) {
		this.spawnPoint = getNextFreeField(spawnPoint, null);
	}

	public boolean setTile(int x, int y, Tile t){
		if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return false;
		tiles[x][y] = t;
		return true;
	}
	
	public boolean setTile(int x, int y, int tileType){
		if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) return false;
		tiles[x][y] = new Tile(tileType, tileSet);
		return true;
	}
	
	public boolean hasTile(int x, int y){
		return tiles[x][y] != null;
	}
	
	public int getTileSet() {
		return tileSet;
	}
	
	public void clearScouted(){
		scoutedMin = null;
		scoutedMax = null;
		for(boolean[] a : scouted) Arrays.fill(a, false);
	}
	
	public void clearVisible(){
		for(boolean[] a : visible) Arrays.fill(a, false);
	}
	
	public boolean isScouted(PointI pos){
		if(pos.x < -1 || pos.y < -1 || pos.x >= getWidth()+2 || pos.y >= getHeight()+2) return false;
		return scouted[pos.x+1][pos.y+1];
	}
	
	public boolean isVisible(PointI pos){
		if(pos.x < -1 || pos.y < -1 || pos.x >= getWidth()+2 || pos.y >= getHeight()+2) return false;
		return visible[pos.x+1][pos.y+1];
	}
	
	public void setScouted(PointI pos){
		if(pos.x < -1 || pos.y < -1 || pos.x >= getWidth()+2 || pos.y >= getHeight()+2) return;
		scouted[pos.x+1][pos.y+1] = true;
		if(scoutedMin == null){
			scoutedMin = pos.clone();
			scoutedMax = pos.clone();
		} else {
			scoutedMin.x = Math.min(pos.x, scoutedMin.x);
			scoutedMax.x = Math.max(pos.x, scoutedMax.x);
			scoutedMin.y = Math.min(pos.y, scoutedMin.y);
			scoutedMax.y = Math.max(pos.y, scoutedMax.y);
		}
	}
	
	public void setVisible(PointI pos){
		if(pos.x < -1 || pos.y < -1 || pos.x >= getWidth()+2 || pos.y >= getHeight()+2) return;
		visible[pos.x+1][pos.y+1] = true;
		scouted[pos.x+1][pos.y+1] = true;
		if(scoutedMin == null){
			scoutedMin = pos.clone();
			scoutedMax = pos.clone();
		} else {
			scoutedMin.x = Math.min(pos.x, scoutedMin.x);
			scoutedMax.x = Math.max(pos.x, scoutedMax.x);
			scoutedMin.y = Math.min(pos.y, scoutedMin.y);
			scoutedMax.y = Math.max(pos.y, scoutedMax.y);
		}
	}
	
	public boolean hasScoutedTiles(){
		return scoutedMin != null;
	}
	
	public PointI getScoutedMin(){
		return scoutedMin == null ? new PointI(-1, -1) : scoutedMin;
	}
	
	public PointI getScoutedMax(){
		return scoutedMax == null ? new PointI(-1, -1) : scoutedMax;
	}
	
	public int getScoutedMinX(){
		return scoutedMin == null ? -1 : scoutedMin.x;
	}
	
	public int getScoutedMinY(){
		return scoutedMin == null ? -1 : scoutedMin.y;
	}
	
	public int getScoutedMaxX(){
		return scoutedMax == null ? -1 : scoutedMax.x;
	}
	
	public int getScoutedMaxY(){
		return scoutedMax == null ? -1 : scoutedMax.y;
	}
	
	public void update(int time){
		for(UsableLevelItem i : items) i.update(time);
		items.removeAll(itemsToRemove);
		itemsToRemove.clear();
		for(int x=0; x<getWidth(); x++){
			for(int y=0; y<getHeight(); y++){
				tiles[x][y].update(time);
			}
		}
	}

	public void requestDeleteItem(UsableLevelItem i) {
		itemsToRemove.add(i);
	}
	
	public boolean isMarkedForRemoval(UsableLevelItem i){
		return itemsToRemove.contains(i);
	}
	
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
		if(!isImpassable(source.x, source.y) && (exclusions == null || !exclusions.contains(source))) return source;
		int d = Integer.MAX_VALUE;
		LinkedList<PointI> l = new LinkedList<>();
		for(int x=0; x<getWidth(); x++){
			for(int y=0; y<getHeight(); y++){
				if(!isImpassable(x, y)){
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
		if(isImpassable(ans.x, ans.y)) System.out.println("WARNING: getnextfreefield returned a wall tile!");
		return ans;
	}
	
	public boolean isInLevel(PointI p){
		return p.x >= 0 && p.x < getWidth() && p.y >= 0 && p.y < getHeight();
	}
	
	public void damageTile(PointI pos, int dmg, PointF shotPos){
		if(!isInLevel(pos)) return;
		if(tiles[pos.x][pos.y].isWall()){
			Tile n = tiles[pos.x][pos.y].damage(dmg);
			int bx = Math.round(16f * (1f - tiles[pos.x][pos.y].size.x));
			int by = Math.round(16f * (1f - tiles[pos.x][pos.y].size.y));
			if(tiles[pos.x][pos.y] != n){
				GameController.get().createDebris(pos.toFloat(), tiles[pos.x][pos.y].getImage(), bx, by, 0.5f);
				GameController.get().recalcVisibleTiles = true;
				tiles[pos.x][pos.y] = n;
			} else {
				addDebrisFromDamage(pos, shotPos, dmg, bx, by, tiles[pos.x][pos.y].getImage());
			}
		} else {
			for(UsableLevelItem i : items) if(!isMarkedForRemoval(i)){
				if(i.pos.equals(pos)){
					int bx = Math.round(16f * (1f - i.size.x));
					int by = Math.round(16f * (1f - i.size.y));
					if(i.damage(dmg)){
						requestDeleteItem(i);
						GameController.get().createDebris(pos.toFloat(), i.img, bx, by, 0.5f);
						GameController.get().recalcVisibleTiles = true;
					} else {
						addDebrisFromDamage(pos, shotPos, dmg, bx, by, i.img);
					}
				}
			}
		}
	}
	private static final float DEBRIS_POINT_DISTANCE = 0.52f;
	private void addDebrisFromDamage(PointI pos, PointF shotPos, int dmg, int bx, int by, Image img){
		int n = getDebrisCountOnDamage(dmg);
		if(n <= 0) return;
		PointF diff = shotPos.clone();
		diff.subtract(pos.toFloat());
		PointF p = diff.clone();
		Float dir;
		float pi = (float)Math.PI;
		float sdx = Math.signum(diff.x);
		float sdy = Math.signum(diff.y);
		if(Math.abs(diff.x) > Math.abs(diff.y)){
			p.x = DEBRIS_POINT_DISTANCE * sdx;
			p.y = p.x * diff.y / diff.x;
			dir = GameController.random.nextFloat() * pi*0.6f + pi*0.2f - sdx * pi/2f;
		} else if(Math.abs(diff.x) < Math.abs(diff.y)){
			p.y = DEBRIS_POINT_DISTANCE * sdy;
			p.x = p.y * diff.x / diff.y;
			dir = GameController.random.nextFloat() * pi*0.6f + pi*0.2f + (sdx+1) / 2f * pi;
		} else {
			p.x = DEBRIS_POINT_DISTANCE * sdx;
			p.y = DEBRIS_POINT_DISTANCE * sdy;
			if(diff.x == 0f){
				dir = null;
			} else {
				dir = GameController.random.nextFloat() * pi;
				dir += pi/4f * sdx * sdy;
				dir -= (sdy+1) / 2f * pi/2f * sdx;
			}
		}
		p.add(pos.toFloat());
		if(dir == null){
			GameController.get().createDebris(p, img, bx, by, n, n, 0f);
		} else {
			GameController.get().createDebris(p, (float)dir, img, bx, by, n, n, 0f);
		}
	}
	private int getDebrisCountOnDamage(int dmg){
		int n = 0;
		float f = 7f;
		for(int i=0; i<10; i++){
			if(GameController.random.nextFloat() * dmg > f) n++;
			f *= 1.8f;
		}
		return n;
	}
	
	public boolean isWallTile(int x, int y){
		if(x<0 || y<0 || x>=getWidth() || y>=getHeight()) return true;
		return tiles[x][y].isWall();
	}
	
	public boolean isImpassable(int x, int y){
		return isImpassable(x, y, false);
	}
	
	public boolean isImpassable(int x, int y, boolean ignoreDoors){
		if(x<0 || y<0 || x>=getWidth() || y>=getHeight()) return true;
		if(tiles[x][y].isWall()) return true;
		for(UsableLevelItem i : items) if(i.pos.x == x && i.pos.y == y){
			if(ignoreDoors && i instanceof Door) return false;
			return i.blocksMovement();
		}
		return false;
	}
	
	public boolean isSightBlocking(int x, int y){
		if(x<0 || y<0 || x>=getWidth() || y>=getHeight()) return true;
		if(tiles[x][y].isWall()) return true;
		for(UsableLevelItem i : items) if(i.pos.x == x && i.pos.y == y){
			return i.blocksSight();
		}
		return false;
	}
	
	public void draw(int x1, int y1, int x2, int y2){
		for(int x=x1; x<=x2; x++){
			for(int y=y1; y<=y2; y++){
				PointF p = new PointF(x, y);
				if(x >= 0 && x < getWidth() && y >= 0 && y < getHeight()){
					tiles[x][y].draw(p);
				} else {
					defaultTile.draw(p);
				}
			}
		}
		for(UsableLevelItem i : items) i.draw();
	}
	
	private LinkedList<PointI> getDirectlyReachableTiles(PointI p, boolean ignoreDoors){
		LinkedList<PointI> ans = new LinkedList<>();
		if(!isImpassable(p.x+1, p.y, ignoreDoors)) ans.add(new PointI(p.x+1, p.y));
		if(!isImpassable(p.x-1, p.y, ignoreDoors)) ans.add(new PointI(p.x-1, p.y));
		if(!isImpassable(p.x, p.y+1, ignoreDoors)) ans.add(new PointI(p.x, p.y+1));
		if(!isImpassable(p.x, p.y-1, ignoreDoors)) ans.add(new PointI(p.x, p.y-1));
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
	
	private void insert(Level.WayPoint p, Level.WayPoint[] a){
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
	
	private Level.WayPoint getRandom(Level.WayPoint[] a){
		int i;
		for(i=0; i<a.length; i++){
			if(a[i] == null) break;
		}
		if(i == 0) return null;
		return a[GameController.random.nextInt(i)];
	}
	
	public LinkedList<PointF> getRandomPath(PointF start, int len, boolean shorten, boolean canOpenDoors){
		PointI[][][] flowField = new PointI[getWidth()][getHeight()][4];
		LinkedList<PointI> expanded = new LinkedList<>();
		LinkedList<PointI> toExpand = new LinkedList<>();
		LinkedList<PointI> justExpanded = new LinkedList<>();
		toExpand.add(start.round());
		
		for(int i=0; i<=len; i++){
			justExpanded.clear();
			expanded.addAll(toExpand);
			for(PointI p1 : toExpand) for(PointI p2 : getDirectlyReachableTiles(p1, canOpenDoors)){
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
		if(shorten){
			removeTrivialWaypoints(ans, canOpenDoors);
			ans.removeFirst();
		}
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
	
	public LinkedList<PointF> getPathAwayFrom(PointF start, PointF target, int maxLength, boolean shorten, boolean canOpenDoors){
		PointI s = start.round();
		PointI e = target.round();
		Level.WayPoint[][][] flowField = new Level.WayPoint[getWidth()][getHeight()][4];
		LinkedList<Level.WayPoint> expanded = new LinkedList<>();
		LinkedList<Level.WayPoint> toExpand = new LinkedList<>();
		toExpand.add(new Level.WayPoint(s, -s.hammingDistanceTo(e), 0));
		
		while(!toExpand.isEmpty()){
			Level.WayPoint wp1 = toExpand.removeFirst();
			// if maximum path length is reached, return path to that point instead
			if(wp1.dist >= maxLength){
				e = wp1.p;
				break;
			}
			expanded.add(wp1);
			for(PointI p2 : getDirectlyReachableTiles(wp1.p, canOpenDoors)){
				if(contains(p2, expanded)) continue;
				Level.WayPoint wp2 = new Level.WayPoint(p2, wp1.dist + 1 - p2.hammingDistanceTo(e), wp1.dist + 1);
				if(!contains(p2, toExpand)) insertSorted(wp2, toExpand);
				insert(wp1, flowField[wp2.p.x][wp2.p.y]);
			}
		}
		// get point that has largest distance
		LinkedList<PointI> best = new LinkedList<>();
		int d = Integer.MAX_VALUE;
		for(Level.WayPoint w : expanded){
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
		Level.WayPoint w = new Level.WayPoint(e, 0, 0);
		while(w != null){
			ans.addFirst(w.p.toFloat());
			w = getRandom(flowField[w.p.x][w.p.y]);
		}
		ans.set(0, start);
		if(shorten){
			removeTrivialWaypoints(ans, canOpenDoors);
			ans.removeFirst();
		}
		return ans;
	}
	
	public LinkedList<PointF> getPathTo(PointF start, PointF end, int maxLength, boolean shorten, boolean canOpenDoors, boolean nullIfUnreachable){
		PointI s = start.round();
		PointI e = end.round();
		Level.WayPoint[][][] flowField = new Level.WayPoint[getWidth()][getHeight()][4];
		LinkedList<Level.WayPoint> expanded = new LinkedList<>();
		LinkedList<Level.WayPoint> toExpand = new LinkedList<>();
		toExpand.add(new Level.WayPoint(s, s.hammingDistanceTo(e), 0));
		
		boolean done = false;
		while(!done && !toExpand.isEmpty()){
			Level.WayPoint wp1 = toExpand.removeFirst();
			// if maximum path length is reached, return path to that point instead
			if(wp1.dist >= maxLength){
				e = wp1.p;
				break;
			}
			expanded.add(wp1);
			for(PointI p2 : getDirectlyReachableTiles(wp1.p, canOpenDoors)){
				if(contains(p2, expanded)) continue;
				Level.WayPoint wp2 = new Level.WayPoint(p2, wp1.dist + 1 + p2.hammingDistanceTo(e), wp1.dist + 1);
				if(!contains(p2, toExpand)) insertSorted(wp2, toExpand);
				insert(wp1, flowField[wp2.p.x][wp2.p.y]);
				if(p2.equals(e)){
					done = true;
					break;
				}
			}
		}
		if(!done){
			if(nullIfUnreachable) return null;
			// point is not reachable. go to nearest point instead!
			LinkedList<PointI> best = new LinkedList<>();
			int d = Integer.MIN_VALUE;
			for(Level.WayPoint w : expanded){
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
		Level.WayPoint w = new Level.WayPoint(e, 0, 0);
		while(w != null){
			ans.addFirst(w.p.toFloat());
			w = getRandom(flowField[w.p.x][w.p.y]);
		}
		ans.set(0, start);
		if(shorten){
			removeTrivialWaypoints(ans, canOpenDoors);
			ans.removeFirst();
		}
		return ans;
	}
	
	private boolean contains(PointI p, LinkedList<Level.WayPoint> l){
		for(Level.WayPoint wp : l) if(wp.p.equals(p)) return true;
		return false;
	}
	
	private void insertSorted(Level.WayPoint p, LinkedList<Level.WayPoint> l){
		Iterator<Level.WayPoint> iter = l.iterator();
		int i = 0;
		while(iter.hasNext()){
			if(p.score < iter.next().score) break;
			i++;
		}
		l.add(i, p);
	}
	
	public void removeTrivialWaypoints(LinkedList<PointF> path, boolean ignoreDoors){
		if(path.size() <= 2) return;
		ListIterator<PointF> iter = path.listIterator();
		PointF p = iter.next();
		while(iter.hasNext()){
			iter.next();
			int len = 0;
			boolean walkable = true;
			while(walkable && iter.hasNext()){
				PointF p2 = iter.next();
				if(Float.isNaN(p2.x)) throw new RuntimeException("p2 x is NaN");
				if(Float.isNaN(p2.y)) throw new RuntimeException("p2 y is NaN");
				if(isWalkableFrom(p, p2, Float.MAX_VALUE, ignoreDoors)){
					len++;
				} else {
					walkable = false;
				}
			}
			if(!walkable) iter.previous();
			iter.previous();
			for(int i=0; i<len; i++){
				iter.previous();
				iter.remove();
			}
			p = iter.next();
		}
	}
	
	public static class CollisionAnswer{
		public PointF corr = null;
		public PointI collidingTilePos = null;
	}
	// --- !!! --- SIMPLE COLLISION! SIZE MUST NOT BE GREATER THAN 1 !!!
	public Level.CollisionAnswer doCollision(PointF center, float size){
		// TODO: rewrite collision to make use ofaith size > 1
		if(size > 1) throw new IllegalArgumentException("size is greater than 1. this algorithm cannot handle that!!!");
		Level.CollisionAnswer answer = new Level.CollisionAnswer();
		float ansLen = -1f;
		int ix = Math.round(center.x);
		int iy = Math.round(center.y);
		LinkedList<PointF> history = new LinkedList<>();
		int collisionCount = 0;
		Level.ColAns ans = new Level.ColAns();
		// collide with level items
		for(UsableLevelItem i : items){
			if(i.blocksMovement()){
				PointF newCorr;
				if(i instanceof Door){
					// limit correction movement
					Door d = (Door)i;
					newCorr = doSingleWallCollision(center, size/2f, i.pos, 
							d.size.x, d.size.y, !d.isVertical(), d.isVertical());
				} else {
					newCorr = doSingleWallCollision(center, size/2f, i.pos, i.size.x, i.size.y, false, false);
				}
				if(!newCorr.isZero()){
					float len = newCorr.length();
					if(ansLen < len){
						answer.collidingTilePos = i.pos.clone();
						ansLen = len;
					}
					history.add(newCorr);
					collisionCount++;
					addCollisionEffect(ans, newCorr, center, history);
				}
			}
		}
		// collide with walls
		for(int x=-1; x<=1; x++){
			for(int y=-1; y<=1; y++){
				if(isWallTile(x+ix, y+iy)){
					PointI pi = new PointI(x+ix, y+iy);
					PointF newCorr = doSingleWallCollision(center, size/2f, pi);
					if(newCorr.x != 0 || newCorr.y != 0){
						// new collision
						float len = newCorr.squaredLength();
						if(ansLen < len){
							answer.collidingTilePos = pi;
							ansLen = len;
						}
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
			ans = new Level.ColAns();
			LinkedList<PointF> oldHistory = history;
			history = new LinkedList<>();
			for(PointF newCorr : oldHistory) addCollisionEffect(ans, newCorr, center, history);
		}
		// if stuck, ignore any noclip axis (may be both)
		if(ans.noClipX) ans.oldCorr.x = 0f;
		if(ans.noClipY) ans.oldCorr.y = 0f;
		answer.corr = ans.oldCorr;
		return answer;
	}
	
	private class ColAns{
		public PointF oldCorr = new PointF();
		public PointF lastCorrXInfl = null, lastCorrYInfl = null;
		public boolean noClipX = false, noClipY = false;
	}
	
	private void addCollisionEffect(Level.ColAns ans, PointF newCorr, PointF center, LinkedList<PointF> history){
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
		return doSingleWallCollision(cp, cr, sp, 1f, 1f, false, false);
	}
	
	private PointF doSingleWallCollision(PointF cp, float cr, PointI sp, float wallSize){
		return doSingleWallCollision(cp, cr, sp, wallSize, wallSize, false, false);
	}
	
	private PointF doSingleWallCollision(PointF cp, float cr, PointI sp, float wallSizeX, float wallSizeY, 
			boolean ignoreHorizontal, boolean ignoreVertical){
		wallSizeX /= 2f;
		wallSizeY /= 2f;
		float dx = cp.x - sp.x;
		float dy = cp.y - sp.y;
		float sx = Math.signum(dx);
		float sy = Math.signum(dy);
		float mx = (wallSizeX - Math.abs(dx) + cr) * sx;
		float my = (wallSizeY - Math.abs(dy) + cr) * sy;
		boolean inX = (Math.abs(dx) < wallSizeX);
		boolean inY = (Math.abs(dy) < wallSizeY);
		if(inX && inY){
			// circle center is in the square.
			// move in direction that has smallest move distance
			if(Math.abs(mx) < Math.abs(my)){
				return ignoreHorizontal ? new PointF(0f, my) : new PointF(mx, 0f);
			} else {
				return ignoreVertical ? new PointF(mx, 0f) : new PointF(0f, my);
			}
		}
		if(inX && Math.abs(dy) < wallSizeY+cr){
			// needs to move vertically
			return ignoreVertical ? new PointF(mx, 0f) : new PointF(0f, my);
		}
		if(inY && Math.abs(dx) < wallSizeX+cr){
			// needs to move horizontally
			return ignoreHorizontal ? new PointF(0f, my) : new PointF(mx, 0f);
		}
		// test for collision with one corner
		dx -= wallSizeX * sx;
		dy -= wallSizeY * sy;
		float d = dx*dx + dy*dy;
		if(d == 0f){
			// point is directly on corner!
			if(ignoreHorizontal) return new PointF(0f, sy * (float)Math.sqrt(cr*cr-dx*dx));
			if(ignoreVertical) return new PointF(sx * (float)Math.sqrt(cr*cr-dy*dy), 0f);
			return new PointF(sx * cr / GameController.SQRT2, sy * cr / GameController.SQRT2);
		}
		if(d < cr*cr){
			// point is in arc around corner. we need a sqrt here!
			if(ignoreHorizontal) return new PointF(0f, dy);
			if(ignoreVertical) return new PointF(dx, 0f);
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
		UsableLevelItem ans = null;
		boolean isFirstTile = true;
		for(PointI p : getTilesOnLine(p1, p2, maxDistance + 1f)){
			UsableLevelItem i = getUsableLevelItem(p.toFloat());
			if(i != null){
				if(p1.squaredDistanceToRect(i.getTopLeftPoint(), i.getBottomRightPoint()) < maxDistance*maxDistance){
					// if the item is stairs or this is the first tile, save it for later.
					// if nothing else comes up later, we return the saved item.
					if(isFirstTile || (ans == null && i instanceof Stairs)){
						ans = i;
					} else {
						return i;
					}
				}
			}
			isFirstTile = false;
			if(isSightBlocking(p.x, p.y) && !ignoreWalls) return ans;
		}
		return ans;
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
	
	public boolean isVisibleFrom(Mob m1, Mob m2, float maxDistance){
		return isVisibleFrom(m1.pos, m2.pos, maxDistance);
	}

	public boolean isVisibleFrom(PointF p1, PointF p2, float maxDistance){
		if(p1.squaredDistanceTo(p2) > maxDistance * maxDistance) return false;
		for(PointI p : getTilesOnLine(p1, p2, maxDistance)){
			if(isSightBlocking(p.x, p.y)) return false;
		}
		return true;
	}
	
	public boolean isWalkableFrom(Mob m1, Mob m2, float maxDistance, boolean ignoreDoors){
		return isWalkableFrom(m1.pos, m2.pos, maxDistance, ignoreDoors);
	}

	public boolean isWalkableFrom(PointF p1, PointF p2, float maxDistance, boolean ignoreDoors){
		if(p1.squaredDistanceTo(p2) > maxDistance * maxDistance) return false;
		for(PointI p : getTilesOnLine(p1, p2, maxDistance)){
			if(isImpassable(p.x, p.y, ignoreDoors)) return false;
		}
		return true;
	}
	
	private long id;
	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID(long id) {
		this.id = id;
	}

	@Override
	public int getIntCount() {
		return getWidth() * getHeight() * 3 + 4;
	}

	@Override
	public int getLongCount() {
		return items.size();
	}

	@Override
	public int getFloatCount() {
		return 0;
	}

	@Override
	public int getBoolCount() {
		return getWidth() * getHeight() * 2;
	}

	@Override
	public void fillStateFields(int[] ints, int intOffset, long[] longs, int longOffset, float[] floats, int floatOffset, boolean[] bools, int boolOffset) {
		ints[intOffset+0] = getWidth();
		ints[intOffset+1] = getHeight();
		ints[intOffset+2] = tileSet;
		ints[intOffset+3] = defaultTile.getTileType();
		intOffset += 4;
		int i = 0;
		for(int x=0; x<getWidth(); x++){
			for(int y=0; y<getHeight(); y++){
				ints[intOffset+3*i+0] = tiles[x][y].hp;
				ints[intOffset+3*i+1] = tiles[x][y].armor;
				ints[intOffset+3*i+2] = tiles[x][y].getTileType();
				bools[boolOffset+2*i] = visible[x][y];
				bools[boolOffset+2*i+1] = scouted[x][y];
				i++;
			}
		}
		i = 0;
		for(UsableLevelItem item : items){
			longs[longOffset+i] = item.getID();
			i++;
		}
	}

	@Override
	public void applyFromStateFields(NetState state, int[] ints, int intOffset, long[] longs, int longOffset, float[] floats, int floatOffset, boolean[] bools, int boolOffset) {
		int w = ints[intOffset+0];
		int h = ints[intOffset+1];
		if(tiles == null || w != getWidth() || h != getHeight()){
			// level changed!!
			if(tiles != null) System.out.println("WARNING: Level size changed!");
			tiles = new Tile[w][h];
			visible = new boolean[w][h];
			scouted = new boolean[w][h];
			tileSet = ints[intOffset+2];
			defaultTile = new Tile(ints[intOffset+3], tileSet);
			intOffset += 4;
			int i = 0;
			for(int x=0; x<getWidth(); x++){
				for(int y=0; y<getHeight(); y++){
					tiles[x][y] = new Tile(ints[intOffset+3*i+2], tileSet);
					tiles[x][y].hp = ints[intOffset+3*i+0];
					tiles[x][y].armor = ints[intOffset+3*i+1];
					visible[x][y] = bools[boolOffset+2*i+1];
					scouted[x][y] = bools[boolOffset+2*i+2];
					i++;
				}
			}
		} else {
			// level is still the same.
			int ts = ints[intOffset+2];
			boolean tsChanged = tileSet != ts;
			tileSet = ts;
			defaultTile.setTileType(ints[intOffset+3], tileSet, tsChanged);
			intOffset += 4;
			int i = 0;
			for(int x=0; x<getWidth(); x++){
				for(int y=0; y<getHeight(); y++){
					tiles[x][y].hp = ints[intOffset+3*i+0];
					tiles[x][y].armor = ints[intOffset+3*i+1];
					tiles[x][y].setTileType(ints[intOffset+3*i+2], tileSet, tsChanged);
					visible[x][y] = bools[boolOffset+2*i+1];
					scouted[x][y] = bools[boolOffset+2*i+2];
					i++;
				}
			}
		}
		// remaining longs are ids for level items
		items.clear();
		for(int i=0; i<longs.length; i++){
			items.add((UsableLevelItem)state.getObject(longs[longOffset+i]));
		}
	}

}
