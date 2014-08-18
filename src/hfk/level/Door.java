/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level;

import hfk.PointI;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.mobs.Mob;
import hfk.net.NetState;
import hfk.net.NetStatePart;
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
		size = 0.6f;
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

	@Override
	public void update(int time) {
		if(timer >= 0){
			timer += time;
			if(timer >= ANIMATION_TIME * 3){
				timer = -1;
				open = !open;
			}
			GameController.get().recalcVisibleTiles = true;
			updateImg();
		}
	}

	@Override
	public boolean damage(int dmg) {
		boolean destroyed = super.damage(dmg);
		if(damaged) return destroyed;
		if(destroyed){
			if((timer < 0 && open) || 
					(open && timer >= ANIMATION_TIME * 3 / 2) ||
					(!open && timer >= 0 && timer < ANIMATION_TIME * 3 / 2)) return true;
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
		return timer < 0 ? !open : false;
	}

	@Override
	public boolean blocksMovement() {
		// movement is blocked if closed, closing or opening
		return timer < 0 ? !open : true;
	}

	@Override
	public boolean canUse(Mob m) {
		return !damaged && timer < 0;
	}

	@Override
	public boolean useInternal(Mob m) {
		GameController.get().recalcVisibleTiles = true;
		GameController.get().playSoundAt(sound, pos.toFloat());
		timer = 0;
		updateImg();
		return true;
	}

	@Override
	public NetStatePart fillStateParts(NetStatePart part, NetState state) {
		part = super.fillStateParts(part, state);
		part.setBoolean(0, vertical);
		part.setBoolean(1, open);
		part.setBoolean(2, damaged);
		return part;
	}

	@Override
	public void updateFromStatePart(NetStatePart part, NetState state) {
		super.updateFromStatePart(part, state);
		vertical = part.getBoolean(0);
		open = part.getBoolean(1);
		damaged = part.getBoolean(2);
		updateImg();
	}
	
}
