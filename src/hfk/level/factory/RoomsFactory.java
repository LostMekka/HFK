/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory;

import hfk.PointI;
import hfk.game.GameController;
import hfk.level.Door;
import hfk.level.Level;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class RoomsFactory extends LevelFactory {

	public static final String PROP_MINROOMWIDTH = "minRoomWidth";
	public static final String PROP_MAXROOMWIDTH = "maxRoomWidth";
	public static final String PROP_MINROOMHEIGHT = "minRoomHeight";
	public static final String PROP_MAXROOMHEIGHT = "maxRoomHeight";
	
	private final LevelFactory emptyFactory;
	private final PropertyMap minWMap, maxWMap, minHMap, maxHMap;
	
	public RoomsFactory(int width, int height) {
		super(width, height);
		emptyFactory = new EmptyFactory(width, height);
		minWMap = getrandomizedPropertyMap(3.5f, 20f, 3f);
		maxWMap = getrandomizedPropertyMap(8f, 30f, 3f);
		minHMap = getrandomizedPropertyMap(3.5f, 20f, 3f);
		maxHMap = getrandomizedPropertyMap(8f, 30f, 3f);
	}

	@Override
	public void generate(Level l, Box b) {
		// split every box into sub boxes until they are small enough
		Random ran = GameController.random;
		LinkedList<Box> roomsToSplit = new LinkedList<>();
		LinkedList<Box> finalRooms = new LinkedList<>();
		roomsToSplit.add(new Box(b));
		while(!roomsToSplit.isEmpty()){
			Box r1 = roomsToSplit.removeFirst();
			int minW = minWMap.getMinInt(r1);
			int maxW = maxWMap.getMaxInt(r1);
			int minH = minHMap.getMinInt(r1);
			int maxH = maxHMap.getMaxInt(r1);
			boolean vert = r1.w >= 2*minW + 1 && r1.h < 3*r1.w;
			boolean horiz = r1.h >= 2*minH + 1 && r1.w < 3*r1.h;
			if((!vert && !horiz) || (r1.w <= maxW && r1.h <= maxH && ran.nextFloat() <= 0.2f)){
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
		// generate rooms
		for(Box b1 : finalRooms){
			if(b1.w * b1.h > 40){
				// generate room with inner factory
				getRandomInnerFactory().generate(l, b1);
			} else {
				// do not generate boxes. clear room instead
				emptyFactory.generate(l, b1);
			}
		}
		// create doors
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
					l.setTile(posX, posY, 4+getTileVariation(posX, posY));
					l.items.add(door);
				}
				if(dy == 1 && dx < 0){
					int posY = oy ? r2.y-1 : r1.y-1;
					int startX = ox ? r2.x : r1.x;
					int endX = r1.x+r1.w > r2.x+r2.w ? r2.x+r2.w : r1.x+r1.w;
					int posX = startX + ran.nextInt(endX - startX);
					PointI p = new PointI(posX, posY);
					Door door = new Door(p, false);
					l.setTile(posX, posY, 4+getTileVariation(posX, posY));
					l.items.add(door);
				}
			}
		}
		// fill everything else with walls
		for(int x=b.x; x<b.x+b.w; x++){
			for(int y=b.y; y<b.y+b.h; y++){
				if(!l.hasTile(x, y)) l.setTile(x, y, getTileVariation(x, y));
			}
		}
	}
	
}
