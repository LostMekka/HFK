/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level;

import hfk.PointI;
import hfk.game.Resources;
import hfk.mobs.Player;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Door extends UsableLevelItem {

	private final SpriteSheet sheet;
	private final boolean vertical;
	private boolean open = false, damaged = false;
	
	public Door(PointI pos, boolean isVertical) {
		super(pos);
		vertical = isVertical;
		sheet = Resources.getSpriteSheet("door.png");
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
	public boolean use(Player p) {
		if(damaged) return false;
		open = !open;
		updateImg();
		return true;
	}
	
}
