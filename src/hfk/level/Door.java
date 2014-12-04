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
import hfk.mobs.Mob;
import hfk.net.NetState;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Door extends UsableLevelItem {

	private static final int ANIMATION_TIME = 200;
	private static SpriteSheet sheet = null;
	private static Sound sound = null;
	private boolean vertical;
	private boolean open = false, damaged = false;
	private int timer = -1;
	
	public Door(PointI pos, boolean isVertical) {
		super(pos);
		if(sheet == null){
			sheet = Resources.getSpriteSheet("door.png");
			sound = Resources.getSound("door.wav");
		}
		vertical = isVertical;
		updateImg();
		hp = 70;
		if(vertical){
			size.x = 0.6f;
			size.y = 1f;
		} else {
			size.x = 1f;
			size.y = 0.6f;
		}
	}
	
	private void updateImg(){
		int x, y = vertical ? 0 : 2;
		if(damaged){
			x = 2;
		} else {
			if(timer < 0){
				x = open ? 1 : 0;
			} else {
				y++;
				x = open ? 2-timer/ANIMATION_TIME : timer/ANIMATION_TIME;
			}
		}
		img = sheet.getSprite(x, y);
	}
	
	private void notifyController(){
		GameController.get().collisionStateChanged(
				new PointF(pos.x - size.x/2f, pos.y - size.y/2f), 
				new PointF(pos.x + size.x/2f, pos.y + size.y/2f));
	}

	@Override
	public void update(int time) {
		if(timer >= 0){
			timer += time;
			if(timer >= ANIMATION_TIME * 3){
				timer = -1;
				open = !open;
				if(open){
					notifyController();
				} else {
					GameController.get().recalcVisibleTiles = true;
				}
			}
			updateImg();
		}
	}

	@Override
	public boolean damage(int dmg) {
		boolean destroyed = super.damage(dmg);
		if(damaged) return destroyed;
		if(destroyed){
			if(timer < 0 && open) return true;
			if((open && timer >= 0 && timer < ANIMATION_TIME * 3 / 2) ||
					(!open && timer >= ANIMATION_TIME * 3 / 2)){
				open = true;
				timer = -1;
				notifyController();
				return true;
			}
			hp = 200;
			open = false;
			damaged = true;
			timer = -1;
			updateImg();
		}
		return false;
	}

	public boolean isVertical() {
		return vertical;
	}

	public boolean isOpen(){
		return open && timer < 0;
	}
	
	public boolean isOpening(){
		return !open && timer >= 0;
	}
	
	public boolean isClosing(){
		return open && timer >= 0;
	}

	public boolean isMoving(){
		return timer >= 0;
	}

	public boolean isDamaged() {
		return damaged;
	}
	
	@Override
	public String getDisplayName() {
		return damaged ? "damaged door" : "door";
	}

	@Override
	public boolean isSquare() {
		return true;
	}

	@Override
	public boolean blocksSight() {
		// sight is blocked only if closed
		return timer < 0 && !open;
	}

	@Override
	public boolean blocksMovement() {
		// movement is blocked if closed, closing or opening
		return timer >= 0 || !open;
	}

	@Override
	public boolean canUse(Mob m) {
		return !damaged && timer < 0;
	}

	@Override
	public boolean useInternal(Mob m) {
		timer = 0;
		updateImg();
		if(open){
			notifyController();
		} else {
			GameController.get().recalcVisibleTiles = true;
		}
		GameController.get().playSoundAt(sound, pos.toFloat());
		return true;
	}

	@Override
	public void fillStateFields(int[] ints, int intOffset, long[] longs, int longOffset, float[] floats, int floatOffset, boolean[] bools, int boolOffset) {
		bools[boolOffset+0] = vertical;
		bools[boolOffset+1] = open;
		bools[boolOffset+2] = damaged;
		super.fillStateFields(ints, intOffset, longs, longOffset, floats, floatOffset, bools, boolOffset+3);
	}

	@Override
	public void applyFromStateFields(NetState state, int[] ints, int intOffset, long[] longs, int longOffset, float[] floats, int floatOffset, boolean[] bools, int boolOffset) {
		vertical = bools[boolOffset+0];
		open = bools[boolOffset+1];
		damaged = bools[boolOffset+2];
		super.applyFromStateFields(state, ints, intOffset, longs, longOffset, floats, floatOffset, bools, boolOffset+3);
	}

	@Override
	public int getBoolCount() {
		return super.getBoolCount() + 3;
	}

}
