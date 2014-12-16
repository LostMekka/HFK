package hfk.level.factory.generators;

import hfk.Box;
import hfk.PointCloud;
import hfk.PointI;
import hfk.Shape;
import hfk.game.GameController;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class CaveGenerator extends LevelGenerator {

	private class ConnectNode{
		public PointI loc;
		public ConnectNode last;
		public PointCloud parent;
		public ConnectNode(PointI loc, ConnectNode last, PointCloud parent) {
			this.loc = loc;
			this.last = last;
			this.parent = parent;
		}
	}
	
	public PropertyMap wallMap = null;
	
	public LevelGenerator walls = null;
	public LevelGenerator floor = null;
	public LevelGenerator tunnelWalls = null;
	public LevelGenerator tunnelFloor = null;
	public float minFloorRatio = 0.8f;
	public float additionalTunnelChance = 0f;
	public PointCloud lastSpawnArea = null;
	
	public CaveGenerator(LevelGenerator parent) {
		super(parent);
	}

	@Override
	public void generateDefaultPropertyMaps(int width, int height) {
		wallMap = PropertyMap.createRandom(width, height, 7, 1, -1.4f, 1f);
		wallMap.fadeOutEllypse(0.3f, 1f, 1f);
	}
	
	private class Connection{
		public PointCloud a1, a2, path;
		public Connection(PointCloud a1, PointCloud a2, PointCloud path) {
			this.a1 = a1;
			this.a2 = a2;
			this.path = path;
		}
	}
	
	@Override
	public void generate(Level l, Shape s) {
		Random r = GameController.random;
		// get list of separate areas
		boolean[][] marks = new boolean[l.getWidth()][l.getHeight()];
		boolean[][] isFloor = new boolean[l.getWidth()][l.getHeight()];
		boolean[][] isTunnel = new boolean[l.getWidth()][l.getHeight()];
		LinkedList<PointCloud> areas = new LinkedList<>();
		int totalSize = 0;
		PointI p = new PointI();
		for(p.x=0; p.x<l.getWidth(); p.x++){
			for(p.y=0; p.y<l.getHeight(); p.y++){
				PointCloud pc = getArea(p, marks, isFloor);
				if(pc != null){
					areas.add(pc);
					totalSize += pc.getPointCount();
				}
			}
		}
		// sort areas by size
		Collections.sort(areas, new Comparator<PointCloud>() {
			@Override
			public int compare(PointCloud o1, PointCloud o2) {
				return o1.getPointCount() - o2.getPointCount();
			}
		});
		// remove smallest areas as long as we have enough space left to spare
		int minSize = (int)(l.getWidth() * l.getHeight() * minFloorRatio);
		while(areas.size() > 1){
			int size = areas.getLast().getPointCount();
			if(totalSize - size < minSize) break;
			totalSize -= size;
			PointCloud pc = areas.removeLast();
			mark(marks, pc, false);
			mark(isFloor, pc, false);
			if(totalSize == minSize) break;
		}
		// connect remaining areas to form one big one
		LinkedList<Connection> connections = getConnections(areas, marks);
		LinkedList<PointCloud> connectedAreas = new LinkedList<>();
		connectedAreas.add(areas.removeFirst());
		while(!areas.isEmpty()){
			LinkedList<Connection> valid = new LinkedList<>();
			for(Connection c : connections) if(connectsToOneOf(c, connectedAreas)) valid.add(c);
			if(valid.isEmpty()){
				int i=0;
			}
			Connection c = valid.get(r.nextInt(valid.size()+1));
			connections.remove(c);
			markTunnel(isFloor, isTunnel, c.path);
			PointCloud a = connectedAreas.contains(c.a1) ? c.a2 : c.a1;
			areas.remove(a);
			connectedAreas.add(a);
		}
		// add additional connections, if wanted
		for(Connection c : connections) if(r.nextFloat() <= additionalTunnelChance){
			markTunnel(isFloor, isTunnel, c.path);
		}
		// assemble final areas
		PointCloud floorArea = new PointCloud();
		PointCloud tunnelArea = new PointCloud();
		PointCloud wallArea = new PointCloud();
		PointCloud tunnelWallArea = new PointCloud();
		for(PointI sp : s){
			if(isFloor[sp.x][sp.y]){
				if(isTunnel[sp.x][sp.y]){
					tunnelArea.addPoint(sp);
				} else {
					floorArea.addPoint(sp);
				}
			} else {
				if(isTunnel[sp.x][sp.y]){
					tunnelWallArea.addPoint(sp);
				} else {
					wallArea.addPoint(sp);
				}
			}
		}
		// let the inner generators do all the dirty work
		walls.generate(l, wallArea);
		floor.generate(l, floorArea);
		tunnelFloor.generate(l, tunnelArea);
		tunnelWalls.generate(l, tunnelWallArea);
		lastSpawnArea = floorArea;
	}
	
	private void mark(boolean[][] a, PointCloud s, boolean val){
		for(PointI p : s) a[p.x][p.y] = val;
	}
	
	private void markTunnel(boolean[][] isFloor, boolean[][] isTunnel, PointCloud path){
		for(PointI p : path){
			isFloor[p.x][p.y] = true;
			if(!isFloor[p.x+1][p.y]) isTunnel[p.x+1][p.y] = true;
			if(!isFloor[p.x-1][p.y]) isTunnel[p.x-1][p.y] = true;
			if(!isFloor[p.x][p.y+1]) isTunnel[p.x][p.y+1] = true;
			if(!isFloor[p.x][p.y-1]) isTunnel[p.x][p.y-1] = true;
		}
	}
	
	private boolean connectsToOneOf(Connection c, LinkedList<PointCloud> areas){
		return areas.contains(c.a1) || areas.contains(c.a2);
	}
	
	private LinkedList<Connection> getConnections(LinkedList<PointCloud> areas, boolean[][] marks){
		LinkedList<Connection> connections = new LinkedList<>();
		LinkedList<ConnectNode> toExpand = getNodes(areas, marks);
		while(!toExpand.isEmpty()){
			ConnectNode n = toExpand.removeFirst();
			// make sure that the first direction to expand is the same as last time
			int dx = n.loc.x - n.last.loc.x;
			int dy = n.loc.y - n.last.loc.y;
			PointI[] pa = new PointI[]{new PointI(n.loc), new PointI(n.loc), new PointI(n.loc)};
			if(dx != 0){
				pa[0].x += dx; pa[1].y++; pa[2].y--;
			}
			if(dy != 0){
				pa[0].y += dy; pa[1].x++; pa[2].x--;
			}
			for(PointI pe : pa) if(wallMap.getFloatAt(pe) < 0f){
				if(marks[pe.x][pe.y]){
					// connection found! is it the first for this area pair?
					ConnectNode n2 = getNode(pe, toExpand);
					// if n2 is null that means it is already expanded!
					// that means the connection was already found and we do nothing here.
					if(n2 != null && !ConnectionExists(n.parent, n2.parent, connections)){
						// connection is the first one between these two areas!
						PointCloud path = new PointCloud();
						fillPath(n, path);
						fillPath(n2, path);
						connections.add(new Connection(n.parent, n2.parent, path));
					}
				} else {
					marks[pe.x][pe.y] = true;
					toExpand.addLast(new ConnectNode(pe, n, n.parent));
				}
			}
		}
		return connections;
	}
	
	private void fillPath(ConnectNode n, PointCloud path){
		while(n != null){
			path.addPoint(n.loc);
			n = n.last;
		}
	}
	
	private boolean ConnectionExists(PointCloud a1, PointCloud a2, LinkedList<Connection> connections){
		for(Connection c : connections){
			if((c.a1 == a1 && c.a2 == a2) || (c.a1 == a2 && c.a2 == a1)){
				return true;
			}
		}
		return false;
	}
	
	private ConnectNode getNode(PointI p, LinkedList<ConnectNode> nodes){
		for(ConnectNode n : nodes) if(n.loc.equals(p)) return n;
		return null;
	}
	
	private LinkedList<ConnectNode> getNodes(LinkedList<PointCloud> areas, boolean[][] marks){
		LinkedList<ConnectNode> nodes = new LinkedList<>();
		for(PointCloud c : areas) for(PointI p1 : c){
			PointI[] pa = new PointI[]{new PointI(p1), new PointI(p1), new PointI(p1), new PointI(p1)};
			pa[0].x++; pa[1].x--; pa[2].y++; pa[3].y--;
			for(PointI p : pa){
				if(p.x<0 || p.y<0 || p.x>=marks.length || p.y>=marks[0].length) continue;
				if(!marks[p.x][p.y] && wallMap.getFloatAt(p) < 0f){
					nodes.add(new ConnectNode(p, null, c));
				}
			}
		}
		return nodes;
	}
	
	private PointCloud getArea(PointI p, boolean[][] marks, boolean[][] isFloor){
		if(marks[p.x][p.y] || wallMap.getFloatAt(p) >= 0f) return null;
		// found a point that has not been flooded. flood it!
		PointCloud ans = new PointCloud();
		ans.addPoint(p);
		LinkedList<PointI> toExpand = new LinkedList<>();
		toExpand.add(p);
		marks[p.x][p.y] = true;
		isFloor[p.x][p.y] = true;
		while(!toExpand.isEmpty()){
			PointI pe = toExpand.remove();
			PointI[] pa = new PointI[]{new PointI(pe), new PointI(pe), new PointI(pe), new PointI(pe)};
			pa[0].x++; pa[1].x--; pa[2].y++; pa[3].y--;
			for(PointI pp : pa){
				if(pp.x<0 || pp.y<0 || pp.x>=marks.length || pp.y>=marks[0].length) continue;
				if(!marks[pp.x][pp.y] && wallMap.getFloatAt(pp) < 0f){
					ans.addPoint(pp);
					toExpand.add(pp);
					marks[pp.x][pp.y] = true;
					isFloor[pp.x][pp.y] = true;
				}
			}
		}
		return ans;
	}
	
}
