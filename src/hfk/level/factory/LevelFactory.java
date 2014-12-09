/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.items.InventoryItem;
import hfk.level.ExplosiveBarrel;
import hfk.level.Level;
import hfk.level.Stairs;
import hfk.mobs.Mob;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public abstract class LevelFactory {
	
	public static final String PROP_TILEVARIANT = "tileVariant";
	
	public static class Box{
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
		public PointI getRandomPoint(){
			return new PointI(GameController.random.nextInt(h)+x, GameController.random.nextInt(h)+y);
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
	}

	private class Property{
		private PropertyMap map;
		private float value;
		public Property(float value) {
			this.value = value;
		}
		public Property(PropertyMap map) {
			this.map = map;
		}
		public PropertyMap getMap() {
			if(map == null) map = new PropertyMap(width, height, value);
			return map;
		}
		public void setMap(PropertyMap map) {
			this.map = map;
		}
		public float getValue() {
			return value;
		}
		public void setValue(float value) {
			map = null;
			this.value = value;
		}
		public float getFloatAt(int x, int y){
			return map == null ? value : map.getFloatAt(x, y);
		}
		public int getIntAt(int x, int y){
			return map == null ? Math.round(value) : map.getIntAt(x, y);
		}
	}
	
	private class InnerFactory{
		public LevelFactory factory;
		public float probabilityFactor;
		public InnerFactory(LevelFactory factory, float probabilityFactor) {
			this.factory = factory;
			this.probabilityFactor = probabilityFactor;
		}
	}
	
	private static HashMap<String, Property> properties = new HashMap<>();
	private int width, height;
	private LinkedList<InnerFactory> innerFactories = new LinkedList<>();

	public LevelFactory(int width, int height) {
		if(width < 3*LEVEL_BORDER || height < 3*LEVEL_BORDER) throw new RuntimeException("level is too small! (" + width + "," + height + ")");
		this.width = width;
		this.height = height;
	}
	
	public abstract void generate(Level l, Box b);

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public void setProperty(String name, float value){
		if(properties.containsKey(name)){
			properties.get(name).setValue(value);
		} else {
			properties.put(name, new Property(value));
		}
	}
	
	public void setProperty(String name, PropertyMap map){
		if(properties.containsKey(name)){
			properties.get(name).setMap(map);
		} else {
			properties.put(name, new Property(map));
		}
	}
	
	public float getFloatProperty(String name, int x, int y){
		Property p = properties.get(name);
		if(p == null) return 0f; // undefined properties return default value of 0f.
		return p.getFloatAt(x, y);
	}
	
	public int getIntProperty(String name, int x, int y){
		Property p = properties.get(name);
		if(p == null) return 0; // undefined properties return default value of 0.
		return p.getIntAt(x, y);
	}
	
	public float getFloatProperty(String name, Box b){
		Property p = properties.get(name);
		if(p == null) return 0f; // undefined properties return default value of 0f.
		if(p.map == null) return p.value;
		return p.map.getAverageFloat(b);
	}
	
	public int getIntProperty(String name, Box b){
		Property p = properties.get(name);
		if(p == null) return 0; // undefined properties return default value of 0.
		if(p.map == null) return Math.round(p.value);
		return p.map.getAverageInt(b);
	}
	
	public void addInnerFactory(LevelFactory f, float probabilityFactor){
		innerFactories.add(new InnerFactory(f, probabilityFactor));
	}
	
	public LevelFactory getRandomInnerFactory(){
		if(innerFactories.isEmpty()) return null;
		float r = 0f;
		for(InnerFactory i : innerFactories) r += i.probabilityFactor;
		r *= GameController.random.nextFloat();
		for(InnerFactory i : innerFactories){
			if(r <= i.probabilityFactor) return i.factory;
			r -= i.probabilityFactor;
		}
		// this should never happen!
		throw new RuntimeException("getRandomInnerFactory() failed!");
	}
	
	public int getTileVariation(int x, int y){
		int ans = getFloatProperty(PROP_TILEVARIANT, x, y) > 0f ? 2 : 0;
		if(GameController.random.nextFloat() <= 0.07f) ans++;
		return ans;
	}
	
	public void randomizeTileVariants(float chunkSize){
		setProperty(PROP_TILEVARIANT, PropertyMap.createRandom(width, height, 4, 1, -1f, 1f));
	}
	
	public PropertyMap getrandomizedPropertyMap(float minValue, float maxValue, float chunkSize){
		Random ran = GameController.random;
		PropertyMap map = new PropertyMap(width, height);
		int n = (int)(width * height * 0.4f);
		for(int i=0; i<n; i++){
			map.putValue(ran.nextInt(width), ran.nextInt(height), 1);
			map.putValue(ran.nextInt(width), ran.nextInt(height), -1);
		}
		map.blur(chunkSize);
		map.normalize(minValue, maxValue);
		return map;
	}
	
	public static final int LEVEL_BORDER = 10;
	public static final float LEVEL_MINMOBDISTANCE = 12f;
	public Level generate(int difficulty, int rarity, int tileSet){
		Random ran = GameController.random;
		Level l = new Level(width, height, tileSet);
		Box inside = new Box(LEVEL_BORDER, LEVEL_BORDER, width - 2*LEVEL_BORDER, height - 2*LEVEL_BORDER);
		new BorderFactory(width, height).generate(l, inside);
		generate(l, inside);
		l.setSpawnPoint(inside.getRandomPoint());
		LinkedList<PointI> ex = new LinkedList<>();
		addStairs(l, inside, ex);
		float barr = ran.nextFloat() * ran.nextFloat() * ran.nextFloat() * ran.nextFloat();
		addBarrels(l, inside, (int)(width * height * 0.08f * barr), ex);
		addItems(l, inside, rarity, ex);
		addMobs(l, inside, difficulty, ex);
		return l;
	}
	
	public void addStairs(Level l, Box box, LinkedList<PointI> ex){
		PointI stairsPos = l.getNextFreeField(box.getRandomPoint(), ex);
		ex.add(stairsPos);
		l.items.add(new Stairs(stairsPos));
	}
	
	public void addMobs(Level l, Box box, int diff, LinkedList<PointI> ex){
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
				p = box.getRandomPoint();
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
	
	public void addItems(Level l, Box box, int rar, LinkedList<PointI> ex){
		GameController ctrl = GameController.get();
		Random ran = GameController.random;
		InventoryItem i;
		int r = rar, mc = Integer.MAX_VALUE, stack = 0;
		PointI p = null;
		for(;;){
			if(mc >= stack){
				mc = 0;
				p = box.getRandomPoint();
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
	
	public void addBarrels(Level l, Box box, int count, LinkedList<PointI> ex){
		for(int i=0; i<count; i++){
			PointI p = l.getNextFreeField(box.getRandomPoint(), ex);
			if(p == null) break;
			l.items.add(new ExplosiveBarrel(p));
			ex.add(p);
		}
	}
	
}
