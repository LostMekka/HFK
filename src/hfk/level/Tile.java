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
import hfk.net.NetState;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Tile{
	
	public static final int FLOORTYPES = 10;
	public static final int WALLTYPES = 10;
	public static final int SUBTYPES = 3;
	public static final int VARIANTS = 3;
	
	private static final int WALLOFFSET = FLOORTYPES;
	private static final int CRATEOFFSET = FLOORTYPES + WALLTYPES;
	private static final int SPECIALOFFSET = SUBTYPES * VARIANTS;

	private static SpriteSheet sheet = null;
	
	public final PointI[] imgPos;
	public final PointF[] size;
	public int[] hp, armor;
	public int[][] animationLength;
	public float[] dps;
	private byte index = 0, animationIndex;
	private int animationTimer = 0, animationMaxTimer = -1, 
			floorAnimationMaxTimer, combinedAnimationMaxTimer;
	private PointI animationPos[][];

	public static Tile createFloor(int type, int subtype, int variant){
		Tile t = new Tile(1);
		t.imgPos[0] = new PointI(type, SUBTYPES*subtype + variant);
		t.size[0] = new PointF();
		return t;
	}
	
	public static Tile createWall(int type, int subtype, int variant, 
			int floorType, int floorSubtype, int floorVariant){
		Tile t = new Tile(2);
		t.imgPos[0] = new PointI(WALLOFFSET + type, SUBTYPES*subtype + variant);
		t.size[0] = new PointF(1f, 1f);
		t.hp[0] = 200;
		t.imgPos[1] = new PointI(floorType, SUBTYPES*floorSubtype + floorVariant);
		t.size[1] = new PointF();
		return t;
	}
	
	public static Tile createCrate(int type, int subtype, int variant, 
			int floorType, int floorSubtype, int floorVariant){
		Tile t = new Tile(2);
		t.imgPos[0] = new PointI(CRATEOFFSET + type, SUBTYPES*subtype + variant);
		t.size[0] = new PointF(0.875f, 0.875f);
		t.hp[0] = 100;
		t.imgPos[1] = new PointI(floorType, SUBTYPES*floorSubtype + floorVariant);
		t.size[1] = new PointF();
		return t;
	}
	
	public static Tile createSleepChamber(int floorType, int floorSubtype, int floorVariant){
		Tile t = new Tile(3);
		t.animationPos[0] = new PointI[]{
				new PointI(0, SPECIALOFFSET), 
				new PointI(1, SPECIALOFFSET), 
				new PointI(0, SPECIALOFFSET+1), 
				new PointI(1, SPECIALOFFSET+1)};
		t.animationLength[0] = new int[]{600,600,600,600};
		t.size[0] = new PointF(1f, 0.57f);
		t.hp[0] = 60;
		t.animationPos[1] = new PointI[]{
				new PointI(2, SPECIALOFFSET), 
				new PointI(2, SPECIALOFFSET+1), 
				new PointI(2, SPECIALOFFSET), 
				new PointI(2, SPECIALOFFSET+1)};
		t.animationLength[1] = new int[]{200,200,800,500};
		t.size[1] = new PointF(1f, 0.57f);
		t.hp[1] = 60;
		t.imgPos[1] = new PointI(floorType, SUBTYPES*floorSubtype + floorVariant);
		t.size[1] = new PointF();
		return t;
	}
	
	private Tile(int layers){
		if(sheet == null) sheet = Resources.getSpriteSheet("tiles.png");
		imgPos = new PointI[layers];
		size = new PointF[layers];
		hp = new int[layers];
		hp[layers-1] = -1;
		armor = new int[layers];
		dps = new float[layers];
		animationPos = new PointI[layers][];
		animationLength = new int[layers][];
	}

	public Tile(PointI[] imgPos, PointF[] size, int[] hp, int[] armor, int[][] animationLength, float[] dps, PointI[][] animationPos) {
		this.imgPos = imgPos;
		this.size = size;
		this.hp = hp;
		this.armor = armor;
		this.dps = dps;
		this.animationLength = animationLength;
		this.animationPos = animationPos;
	}
	
	public PointF getSize(){
		return size[index];
	}
	
	public float getSizeX(){
		return size[index].x;
	}
	
	public float getSizeY(){
		return size[index].y;
	}
	
	public boolean damage(int dmg){
		if(hp[index] < 0) return false;
		dmg = Math.max(dmg-armor[index], 0);
		hp[index] -= dmg;
		if(hp[index] <= 0){
			index++;
			updateMaxTimer();
			return true;
		}
		return false;
	}
	
	public boolean isWall() {
		return !size[index].isZero();
	}
	
	public Image getImage(){
		PointI p = imgPos[index];
		if(animationPos[index] != null) p = animationPos[index][animationTimer % animationMaxTimer];
		return sheet.getSprite(p.x, p.y);
	}
	
	public Image getFloorImage(){
		int i = imgPos.length-1;
		PointI p = imgPos[i];
		if(animationPos[i] != null) p = animationPos[i][animationTimer % floorAnimationMaxTimer];
		return sheet.getSprite(p.x, p.y);
	}
	
	public void draw(PointF pos){
		if(index < imgPos.length-1 && (size[index].x < 1f || size[index].y < 1f)){
			GameController.get().renderer.drawImage(getFloorImage(), pos, true);
		}
		GameController.get().renderer.drawImage(getImage(), pos, true);
	}
	
	public void update(int time){
		if(animationMaxTimer == -1) updateMaxTimer();
		if(combinedAnimationMaxTimer != 0){
			animationTimer = (animationTimer+time) % combinedAnimationMaxTimer;
		}
	}
	
	private void updateMaxTimer(){
		int t = 0;
		if(animationPos[index] != null) for(int i=0; i<animationPos[index].length; i++){
			t += animationLength[index][i];
		}
		int last = imgPos.length-1;
		int tf = 0;
		if(animationPos[last] != null) for(int i=0; i<animationPos[last].length; i++){
			tf += animationLength[last][i];
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
