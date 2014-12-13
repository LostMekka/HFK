/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.game.Resources;
import java.io.Serializable;
import java.util.LinkedList;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Tile{
	
	public static class TileData implements Serializable{
		public PointI imgPos;
		public PointF size;
		public int hp, armor;
		public float[] dps;
		public int[] aniLen;
		public PointI[] aniPos;
	}
	
	public static final int FLOORTYPES = 10;
	public static final int WALLTYPES = 10;
	public static final int SUBTYPES = 3;
	public static final int VARIANTS = 3;
	
	private static final int WALLOFFSET = FLOORTYPES;
	private static final int CRATEOFFSET = FLOORTYPES + WALLTYPES;
	private static final int SPECIALOFFSET = SUBTYPES * VARIANTS;

	private static SpriteSheet sheet = null;
	
	public LinkedList<TileData> data = new LinkedList<>();
	private TileData curr = null, nextFull = null;
	private int animationTimer = 0, animationMaxTimer = -1, 
			floorAnimationMaxTimer, combinedAnimationMaxTimer;

	public void addFloor(int type, int subtype, int variant){
		TileData d = new TileData();
		d.imgPos = new PointI(type, SUBTYPES*subtype + variant);
		d.size = new PointF();
		data.addLast(d);
		updateData();
	}
	
	public void addWall(int type, int subtype, int variant){
		TileData d = new TileData();
		d.imgPos = new PointI(WALLOFFSET + type, SUBTYPES*subtype + variant);
		d.size = new PointF(1f, 1f);
		d.hp = 200;
		data.addFirst(d);
		updateData();
	}
	
	public void addCrate(int type, int subtype, int variant){
		TileData d = new TileData();
		d.imgPos = new PointI(CRATEOFFSET + type, SUBTYPES*subtype + variant);
		d.size = new PointF(0.875f, 0.875f);
		d.hp = 100;
		data.addFirst(d);
		updateData();
	}
	
	public void addSleepChamber(boolean damaged){
		TileData d = new TileData();
		d.aniPos = new PointI[]{
				new PointI(2, SPECIALOFFSET), 
				new PointI(2, SPECIALOFFSET+1), 
				new PointI(2, SPECIALOFFSET), 
				new PointI(2, SPECIALOFFSET+1)};
		d.aniLen = new int[]{200,800,500,200};
		d.size = new PointF(1f, 0.57f);
		d.hp = 60;
		data.addFirst(d);
		if(!damaged){
			d = new TileData();
			d.aniPos = new PointI[]{
					new PointI(0, SPECIALOFFSET), 
					new PointI(1, SPECIALOFFSET), 
					new PointI(0, SPECIALOFFSET+1), 
					new PointI(1, SPECIALOFFSET+1)};
			d.aniLen = new int[]{600,600,600,600};
			d.size = new PointF(1f, 0.57f);
			d.hp = 60;
			data.addFirst(d);
		}
		updateData();
		animationTimer = GameController.random.nextInt(animationMaxTimer);
	}
	
	public Tile(){
		if(sheet == null) sheet = Resources.getSpriteSheet("tiles.png");
	}

	public PointF getSize(){
		return curr.size;
	}
	
	public float getSizeX(){
		return curr.size.x;
	}
	
	public float getSizeY(){
		return curr.size.y;
	}
	
	public boolean damage(int dmg){
		if(curr.hp < 0) return false;
		dmg = Math.max(dmg-curr.armor, 0);
		curr.hp -= dmg;
		if(curr.hp <= 0){
			data.removeFirst();
			updateData();
			return true;
		}
		return false;
	}
	
	public boolean isWall() {
		return !curr.size.isZero();
	}
	
	private int getAnimationIndex(int timer, int maxTime, int[] times){
		int t = timer % maxTime;
		for(int i=0; i<times.length; i++){
			if(t <= times[i]) return i;
			t -= times[i];
		}
		return 0;
	}
	
	public Image getImage(){
		PointI p = curr.imgPos;
		if(curr.aniPos != null) p = curr.aniPos[getAnimationIndex(animationTimer, animationMaxTimer, curr.aniLen)];
		return sheet.getSprite(p.x, p.y);
	}
	
	public Image getFloorImage(){
		PointI p = nextFull.imgPos;
		if(nextFull.aniPos != null) p = nextFull.aniPos[getAnimationIndex(animationTimer, floorAnimationMaxTimer, nextFull.aniLen)];
		return sheet.getSprite(p.x, p.y);
	}
	
	public void draw(PointF pos){
		if(curr != nextFull && (curr.size.x < 1f || curr.size.y < 1f)){
			GameController.get().renderer.drawImage(getFloorImage(), pos, true);
		}
		GameController.get().renderer.drawImage(getImage(), pos, true);
	}
	
	public void update(int time){
		if(animationMaxTimer == -1) updateData();
		if(combinedAnimationMaxTimer != 0){
			animationTimer = (animationTimer+time) % combinedAnimationMaxTimer;
		}
	}
	
	private void updateData(){
		curr = data.getFirst();
		nextFull = data.getLast();
		for(TileData d : data){
			if(d.size.x >= 1f && d.size.y >= 1f){
				nextFull = d;
				break;
			}
		}
		int t = 0;
		if(curr.aniPos != null) for(int i=0; i<curr.aniPos.length; i++){
			t += curr.aniLen[i];
		}
		int tf = 0;
		if(nextFull.aniPos != null) for(int i=0; i<nextFull.aniPos.length; i++){
			tf += nextFull.aniLen[i];
		}
		animationMaxTimer = t;
		floorAnimationMaxTimer = tf;
		if(t == 0){
			combinedAnimationMaxTimer = tf;
		} else if(tf == 0){
			combinedAnimationMaxTimer = t;
		} else {
			combinedAnimationMaxTimer = t * tf;
		}
		if(combinedAnimationMaxTimer != 0){
			animationTimer %= combinedAnimationMaxTimer;
		}
	}
	
}
