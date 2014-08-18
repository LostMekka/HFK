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
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Door extends UsableLevelItem {

	private static SpriteSheet sheet = null;
	private boolean vertical;
	private boolean open = false, damaged = false;
	
	public Door(PointI pos, boolean isVertical) {
		super(pos);
		vertical = isVertical;
		if(sheet == null) sheet = Resources.getSpriteSheet("door.png");
		updateImg();
		hp = 70;
	}
	
	private void updateImg(){
		int x = open ? 1 : 0;
		int y = vertical ? 0 : 1;
		if(damaged) x = 2;
		img = sheet.getSprite(x, y);
	}

	@Override
	public boolean damage(int dmg) {
		boolean destroyed = super.damage(dmg);
		if(damaged) return destroyed;
		if(destroyed){
			if(open) return true;
			hp = 200;
			damaged = true;
			updateImg();
		}
		return false;
	}

	public boolean isOpen(){
		return open;
	}
	
	@Override
	public String getDisplayName() {
		return damaged ? "damaged door" : "door";
	}

	@Override
	public boolean canUse(Mob m) {
		return !damaged;
	}

	@Override
	public boolean useInternal(Mob m) {
		GameController.get().recalcVisibleTiles = true;
		open = !open;
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
