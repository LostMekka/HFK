/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory.generators;

import hfk.Box;
import hfk.PointCloud;
import hfk.PointI;
import hfk.Shape;
import hfk.game.GameController;
import hfk.level.Door;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author LostMekka
 */
public class RoomsGenerator extends LevelGenerator {

	private static Random ran = GameController.random;
	
	private class Connection{
		public Room r1, r2;
		public Connection(Room r1, Room r2) {
			this.r1 = r1;
			this.r2 = r2;
		}
		public Room getOther(Room r){
			return r1 == r ? r2 : r1;
		}
		public boolean connectsRoom(Room r){
			return r1 == r || r2 == r;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass())  return false;
			final Connection c = (Connection) obj;
			return (r1==c.r1 && r2==c.r2) || (r1==c.r2 && r2==c.r1);
		}
	}
	
	private final class Area{
		LinkedList<Room> rooms = new LinkedList<>();
		LinkedList<Connection> outer = new LinkedList<>();
		LinkedList<Connection> innerWall = new LinkedList<>();
		LinkedList<Connection> innerNoWall = new LinkedList<>();
		PointCloud shape = new PointCloud();
		public Area(Room r) {
			rooms.add(r);
			outer.addAll(r.connections);
			shape.addShape(r.box);
		}
		public void add(Connection location){
			if(innerWall.contains(location)){
				innerWall.remove(location);
				innerNoWall.add(location);
				shape.addShape(getConnectableArea(location, true));
			}
		}
		public void add(Area a, Connection location){
			shape.addShape(a.shape);
			shape.addShape(getConnectableArea(location, true));
			innerWall.addAll(a.innerWall);
			innerNoWall.addAll(a.innerNoWall);
			innerNoWall.add(location);
			for(Connection c : a.outer) if(c != location){
				boolean connects = false;
				for(Room r : rooms)	if(c.connectsRoom(r)){
					connects = true;
					break;
				}
				if(connects){
					outer.remove(c);
					innerWall.add(c);
				} else {
					outer.add(c);
				}
			}
			for(Room r : a.rooms) rooms.add(r);
		}
	}
	
	private final class Room{
		public Box box;
		public LinkedList<Connection> connections = new LinkedList<>();
		public Room(Box box) {
			this.box = box;
		}
		public void connect(Room r){
			for(Connection c : connections){
				if(c.r1 == r || c.r2 == r) return;
			}
			Connection c = new Connection(this, r);
			connections.add(c);
			r.connections.add(c);
		}
		public void disconnect(Room r){
			Connection cc = null;
			for(Connection c : connections){
				if(c.r1 == r || c.r2 == r){
					cc = c;
					break;
				}
			}
			if(cc == null) return;
			connections.remove(cc);
			r.connections.remove(cc);
		}
		public Room getRoom(Connection c){
			return c.getOther(this);
		}
	}
	
	public PropertyMap minWMap = null;
	public PropertyMap maxWMap = null;
	public PropertyMap minHMap = null;
	public PropertyMap maxHMap = null;
	public PropertyMap noWallChance = null;
	public PropertyMap additionalDoorChance = null;
	
	public LevelGenerator walls = null;
	public LevelGenerator floors = null;
	public LevelGenerator empty = null;

	public RoomsGenerator(LevelGenerator parent) {
		super(parent);
	}

	@Override
	public void generateDefaultPropertyMaps(int width, int height) {
		minWMap = PropertyMap.createRandom(width, height, 7, 1, 3.5f, 10f);
		maxWMap = PropertyMap.createRandom(width, height, 7, 1, 8f, 20f);
		minHMap = PropertyMap.createRandom(width, height, 7, 1, 3.5f, 10f);
		maxHMap = PropertyMap.createRandom(width, height, 7, 1, 8f, 20f);
		noWallChance = PropertyMap.createRandom(width, height, 9, 0, 0f, 0.6f);
		additionalDoorChance = PropertyMap.createRandom(width, height, 9, 0, 0f, 0.6f);
	}

	@Override
	public void generate(Level l, Shape s) {
		LinkedList<Room> rooms = split(s.getBoundingBox());
		LinkedList<Connection> allConnections = getAllConnections(rooms);
		LinkedList<Area> areas = createAreas(rooms, allConnections);
		// connect rooms
		LinkedList<Connection> doorConnections = new LinkedList<>();
		LinkedList<Area> combined = new LinkedList<>();
		LinkedList<Area> singles = new LinkedList<>();
		singles.addAll(areas);
		combined.add(singles.removeFirst());
		ListIterator<Area> iter = null;
		while(!singles.isEmpty()){
			if(iter == null || !iter.hasNext()) iter = combined.listIterator();
			Area a1 = iter.next(), a2 = null;
			for(Connection c : a1.outer){
				Room r = a1.rooms.contains(c.r1) ? c.r2 : c.r1;
				for(Area a : singles) if(a.rooms.contains(r)){
					a2 = a;
					break;
				}
				if(a2 != null){
					doorConnections.add(c);
					break;
				}
			}
			if(a2 != null){
				singles.remove(a2);
				iter.add(a2);
			}
		}
		// create some more connections if needed
		for(Connection c : allConnections) if(!doorConnections.contains(c)){
			// connection is not needed. connect anyways?
			float p = additionalDoorChance.getAverageFloat(getConnectableArea(c, false));
			if(ran.nextFloat() <= p) doorConnections.add(c);
		}
		// gather shape for walls
		PointCloud wallShape = new PointCloud();
		for(Connection c : allConnections){
			Box b = getConnectableArea(c, true);
			wallShape.addShape(b);
		}
		for(Area a : areas) a.shape.remove(wallShape);
		// create doors
		PointCloud floorOnlyShape = new PointCloud();
		for(Connection c : doorConnections){
			Box b = getConnectableArea(c, false);
			Door d;
			PointI p;
			if(connectsVertical(c)){
				int posY = b.y + ran.nextInt(b.h);
				p = new PointI(b.x, posY);
				d = new Door(p, true);
			} else {
				int posX = b.x + ran.nextInt(b.w);
				p = new PointI(posX, b.y);
				d = new Door(p, false);
			}
			l.items.add(d);
			floorOnlyShape.addPoint(p);
			wallShape.remove(p);
		}
		// generate areas inside of rooms
		for(Area a : areas){
			if(a.shape.getPointCount() > 20){
				// generate room with inner generator
				Shape border = a.shape.subtractBorder(1);
				floorOnlyShape.addShape(border);
				getRandomInnerGenerator().generate(l, a.shape);
			} else {
				// do not generate with inner generator. clear room instead
				empty.generate(l, a.shape);
			}
		}
		drawDebug(rooms, areas, null, wallShape, "a");
		// generate wall and door area
		floors.generate(l, wallShape);
		floors.generate(l, floorOnlyShape);
		walls.generate(l, wallShape);
	}
	
	private LinkedList<Room> split(Box b){
		// split every box into sub boxes until they are small enough
		LinkedList<Room> roomsToSplit = new LinkedList<>();
		LinkedList<Room> finalRooms = new LinkedList<>();
		roomsToSplit.add(new Room(b));
		while(!roomsToSplit.isEmpty()){
			Room r1 = roomsToSplit.remove(ran.nextInt(roomsToSplit.size()));
			int minW = minWMap.getMinInt(r1.box);
			int maxW = maxWMap.getMaxInt(r1.box);
			int minH = minHMap.getMinInt(r1.box);
			int maxH = maxHMap.getMaxInt(r1.box);
			boolean vert = r1.box.w >= 2*minW + 1 && r1.box.h < 3*r1.box.w;
			boolean horiz = r1.box.h >= 2*minH + 1 && r1.box.w < 3*r1.box.h;
			if((!vert && !horiz) || (r1.box.w <= maxW && r1.box.h <= maxH && ran.nextFloat() <= 0.2f)){
				// do not split anymore
				finalRooms.add(r1);
				continue;
			}
			boolean dir = vert ? (horiz ? ran.nextBoolean() : false) : true;
			Room r2;
			if(dir){
				// horizontal split
				int h = r1.box.h;
				r1.box.h = minH + ran.nextInt(h - 2*minH);
				r2 = new Room(new Box(r1.box.x, r1.box.y + r1.box.h + 1, r1.box.w, h - r1.box.h - 1));
			} else {
				// vertical split
				int w = r1.box.w;
				r1.box.w = minW + ran.nextInt(w - 2*minW);
				r2 = new Room(new Box(r1.box.x + r1.box.w + 1, r1.box.y, w - r1.box.w - 1, r1.box.h));
			}
			if(ran.nextBoolean()){
				roomsToSplit.add(r1);
				roomsToSplit.add(r2);
			} else {
				roomsToSplit.add(r2);
				roomsToSplit.add(r1);
			}
		}
		for(int i1=0; i1<finalRooms.size(); i1++){
			Room r1 = finalRooms.get(i1);
			for(int i2=i1+1; i2<finalRooms.size(); i2++){
				Room r2 = finalRooms.get(i2);
				if(canConnect(r1, r2)) r1.connect(r2);
			}
		}
		return finalRooms;
	}
	
	private LinkedList<Connection> getAllConnections(LinkedList<Room> rooms){
		LinkedList<Connection> ans = new LinkedList<>();
		for(Room r : rooms) for(Connection c : r.connections){
			if(!ans.contains(c)) ans.add(c);
		}
		return ans;
	}
	
	private void drawDebug(LinkedList<Room> rooms, LinkedList<Area> areas, Connection c, Shape walls, String name){
		int w = getLevelSize().x;
		int h = getLevelSize().y;
		int pix = 10;
		BufferedImage i = new BufferedImage(w*pix, h*pix, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = i.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, w*pix, h*pix);
		g.setColor(new Color(1f, 0f, 0f, 0.5f));
		for(Room r : rooms){
			g.fillRect(r.box.x*pix, r.box.y*pix, r.box.w*pix, r.box.h*pix);
		}
		g.setColor(new Color(0f, 1f, 0f, 0.5f));
		for(Area a : areas) for(PointI p : a.shape){
			g.fillRect(p.x*pix, p.y*pix, pix, pix);
		}
		g.setColor(new Color(0f, 0f, 1f, 0.5f));
		if(c != null) for(PointI p : getConnectableArea(c, false)){
			g.fillRect(p.x*pix, p.y*pix, pix, pix);
		}
		g.setColor(new Color(0f, 0f, 1f, 0.5f));
		if(walls != null) for(PointI p : walls){
			g.fillRect(p.x*pix, p.y*pix, pix, pix);
		}
		g.dispose();
		try{
			ImageIO.write(i, "png", new File("RoomsDebug_" + name + ".png"));
		} catch(IOException e){}
	}
	
	private void drawDebug(LinkedList<Room> rooms, LinkedList<Shape> shapes, String name){
		int w = getLevelSize().x;
		int h = getLevelSize().y;
		int pix = 10;
		BufferedImage i = new BufferedImage(w*pix, h*pix, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = i.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, w*pix, h*pix);
		g.setColor(new Color(1f, 0f, 0f, 0.5f));
		for(Room r : rooms){
			g.fillRect(r.box.x*pix, r.box.y*pix, r.box.w*pix, r.box.h*pix);
		}
		g.setColor(new Color(0f, 1f, 0f, 0.5f));
		for(Shape s : shapes) for(PointI p : s){
			g.fillRect(p.x*pix, p.y*pix, pix, pix);
		}
		g.setColor(new Color(0f, 0f, 1f, 0.5f));
		g.dispose();
		try{
			ImageIO.write(i, "png", new File("RoomsDebug_" + name + ".png"));
		} catch(IOException e){}
	}
	
	private void removeWall(LinkedList<Area> areas, Connection c, 
			LinkedList<Connection> noWallConnections, LinkedList<Connection> saveForLater){
		Room r1 = c.r1;
		Area a1 = null;
		for(Area a : areas) if(a.rooms.contains(r1)){
			a1 = a;
			break;
		}
		Room r2 = c.r2;
		Area a2 = null;
		for(Area a : areas) if(a.rooms.contains(r2)){
			a2 = a;
			break;
		}
		if(a1 != a2){
			if(getConnectableArea(c, false).getPointCount() > 2){
				a1.add(a2, c);
				noWallConnections.add(c);
				areas.remove(a2);
			} else {
				// wall to remove is a really tiny one.
				if(saveForLater != null){
					// tiny walls should only be removed if they stand by themselves
					// after all other walls are removed.
					// --> save this wall for later!
					saveForLater.add(c);
				}
			}
		} else {
			if(getConnectableArea(c, false).getPointCount() > 2){
				a1.add(c);
				noWallConnections.add(c);
			} else {
				// this is a tiny inner wall. remove it only if it stands alone
				LinkedList<Connection> list = new LinkedList<>();
				list.addAll(r1.connections);
				list.addAll(r2.connections);
				list.remove(c);
				list.remove(c);
				Box cb = getConnectableArea(c, true);
				boolean alone = true;
				for(Connection c2 : list){
					if(cb.touchesBox(getConnectableArea(c2, true))){
						alone = false;
						break;
					}
				}
				if(alone){
					a1.add(c);
					noWallConnections.add(c);
				}
			}
		}
	}
	
	private LinkedList<Area> createAreas(LinkedList<Room> rooms, LinkedList<Connection> allCon){
		// create 1 area for each room
		LinkedList<Area> areas = new LinkedList<>();
		for(Room r : rooms) areas.add(new Area(r));
		// clear some walls
		LinkedList<Connection> noWallConnections = new LinkedList<>();
		LinkedList<Connection> tinyConnections = new LinkedList<>();
		for(Connection c : allCon){
			float p = noWallChance.getAverageFloat(getConnectableArea(c.r1, c.r2, false));
			if(ran.nextFloat() <= p){
				removeWall(areas, c, noWallConnections, tinyConnections);
			}
		}
		// try again removing single tile walls, they will be removed if they are inner walls now.
		for(Connection c : tinyConnections){
			removeWall(areas, c, noWallConnections, null);
		}
		// remove single tile artifacts in area shapes
		for(Area a : areas){
			for(PointI p : a.shape.getBoundingBox()) if(!a.shape.contains(p)){
				if(		a.shape.contains(new PointI(p.x-1, p.y)) &&
						a.shape.contains(new PointI(p.x+1, p.y)) &&
						a.shape.contains(new PointI(p.x, p.y-1)) &&
						a.shape.contains(new PointI(p.x, p.y+1))){
					a.shape.addPoint(p);
				}
			}
		}
		// done
		allCon.removeAll(noWallConnections);
		return areas;
	}
	
	private boolean canConnect(Room r1, Room r2){
		return canConnect(r1.box, r2.box);
	}
	
	private boolean canConnect(Box r1, Box r2){
		int dx = r2.x > r1.x ? r2.x-r1.x-r1.w : r1.x-r2.x-r2.w;
		int dy = r2.y > r1.y ? r2.y-r1.y-r1.h : r1.y-r2.y-r2.h;
		return (dx == 1 && dy < 0) || (dy == 1 && dx < 0);
	}
	
	private Boolean connectsVertical(Connection c){
		return connectsVertical(c.r1.box, c.r2.box);
	}
	
	private Boolean connectsVertical(Room r1, Room r2){
		return connectsVertical(r1.box, r2.box);
	}
	
	private Boolean connectsVertical(Box r1, Box r2){
		int dx = r2.x > r1.x ? r2.x-r1.x-r1.w : r1.x-r2.x-r2.w;
		int dy = r2.y > r1.y ? r2.y-r1.y-r1.h : r1.y-r2.y-r2.h;
		if(dx == 1 && dy < 0) return true;
		if(dy == 1 && dx < 0) return false;
		return null;
	}
	
	private Box getConnectableArea(Connection c, boolean make1bigger){
		return getConnectableArea(c.r1.box, c.r2.box, make1bigger);
	}
	
	private Box getConnectableArea(Room r1, Room r2, boolean make1bigger){
		return getConnectableArea(r1.box, r2.box, make1bigger);
	}
	
	private Box getConnectableArea(Box r1, Box r2, boolean make1bigger){
		boolean ox = r2.x > r1.x;
		boolean oy = r2.y > r1.y;
		int dx = ox? r2.x-r1.x-r1.w : r1.x-r2.x-r2.w;
		int dy = oy? r2.y-r1.y-r1.h : r1.y-r2.y-r2.h;
		if(dx == 1 && dy < 0){
			// vertical wall
			int posX = ox ? r2.x-1 : r1.x-1;
			int startY = oy ? r2.y : r1.y;
			int endY = r1.y+r1.h > r2.y+r2.h ? r2.y+r2.h : r1.y+r1.h;
			if(make1bigger){
				startY--;
				endY++;
			}
			return new Box(posX, startY, 1, endY - startY);
		}
		if(dy == 1 && dx < 0){
			// horizontal wall
			int posY = oy ? r2.y-1 : r1.y-1;
			int startX = ox ? r2.x : r1.x;
			int endX = r1.x+r1.w > r2.x+r2.w ? r2.x+r2.w : r1.x+r1.w;
			if(make1bigger){
				startX--;
				endX++;
			}
			return new Box(startX, posY, endX - startX, 1);
		}
		return null;
	}
	
}
