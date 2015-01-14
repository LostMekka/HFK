/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory;

import hfk.Box;
import hfk.PointCloud;
import hfk.PointF;
import hfk.PointI;
import hfk.Shape;
import hfk.game.GameController;
import hfk.items.InventoryItem;
import hfk.level.ExplosiveBarrel;
import hfk.level.Level;
import hfk.level.Stairs;
import hfk.level.tiles.Tile;
import hfk.mobs.Mob;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public abstract class LevelFactory extends LevelGenerator {
	
	public static final int LEVEL_BORDER = 10;
	public static final float LEVEL_MINMOBDISTANCE = 12f;
	public static final String PROP_TILEVARIANT = "tileVariant";
	
	public PropertyMap barrelChance = null;
	
	private final PointI size;

	public LevelFactory(int width, int height) {
		super(null);
		size = new PointI(width, height);
	}
	
	public abstract Shape getLegalSpawnArea();
	public abstract Tile getDefaultTile();
	
	public Level create(int difficulty, int rarity){
		// generate tiles
		Level l = new Level(size.x, size.y, getDefaultTile());
		generate(l, new Box(0, 0, size.x, size.y));
		// add spawn
		PointCloud s = new PointCloud(getLegalSpawnArea());
		PointI spawn = s.getRandomPointInside();
		l.setSpawnPoint(spawn);
		s.remove(spawn);
		// add other stuff
		LinkedList<PointI> ex = new LinkedList<>();
		addStairs(l, s, ex);
		addItems(l, s, rarity, ex);
		addMobs(l, s, difficulty, ex);
		if(barrelChance != null) addBarrels(l, s, ex);
		return l;
	}

	@Override
	public PointI getLevelSize() {
		return size;
	}
	
	public Box getLevelBox(){
		return new Box(0, 0, size.x, size.y);
	}
	
	public void addBarrels(Level l, Shape area, LinkedList<PointI> ex){
		for(PointI p : area) if(!l.isWallTile(p.x, p.y) && !ex.contains(p)){
			if(GameController.random.nextFloat() <= barrelChance.getFloatAt(p)){
				ex.add(p);
				l.items.add(new ExplosiveBarrel(p));
			}
		}
	}

	public void addStairs(Level l, Shape area, LinkedList<PointI> ex){
		PointI stairsPos = l.getNextFreeField(area.getRandomPointInside(), ex);
		ex.add(stairsPos);
		l.items.add(new Stairs(stairsPos));
	}
	
	public void addMobs(Level l, Shape area, int diff, LinkedList<PointI> ex){
		//System.out.println("\n--- level ----------------");
		GameController ctrl = GameController.get();
		Random ran = GameController.random;
		PointF plpF = l.getSpawnPoint().toFloat();
		LinkedList<PointI> ex2 = new LinkedList<>();
		ex2.addAll(ex);
		for(int x=0; x<l.getWidth(); x++){
			for(int y=0; y<l.getHeight(); y++){
				PointF p = new PointF(x, y);
				if(p.squaredDistanceTo(plpF) <= LEVEL_MINMOBDISTANCE*LEVEL_MINMOBDISTANCE){
					ex2.add(p.round());
				}
			}
		}
		Mob m;
		int d = diff, mc = Integer.MAX_VALUE, stack = 0;
		PointI p = null;
		for(;;){
			if(mc >= stack){
				mc = 0;
				p = area.getRandomPointInside();
				stack = ran.nextInt(ran.nextInt(ran.nextInt(10)+1)+1);
			}
			int maxD = Math.min(d, Math.round(diff*GameController.MOBGENERATION_MAXDIFF_FACTOR));
			m = Mob.createMob(new PointF(), maxD, ctrl.getLevelCount());
			if(m == null) break;
			//System.out.println("   mob: " + m.getDisplayName());
			mc++;
			d -= m.getDifficultyScore();
			PointI p2 = l.getNextFreeField(p, ex2);
			if(p2 == null) break;
			m.pos = p2.toFloat();
			ex.add(p2);
			ex2.add(p2);
			ctrl.mobs.add(m);
		}
	}
	
	public void addItems(Level l, Shape area, int rar, LinkedList<PointI> ex){
		GameController ctrl = GameController.get();
		Random ran = GameController.random;
		InventoryItem i;
		int r = rar, mc = Integer.MAX_VALUE, stack = 0;
		PointI p = null;
		for(;;){
			if(mc >= stack){
				mc = 0;
				p = area.getRandomPointInside();
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
