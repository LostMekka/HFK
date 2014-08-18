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
import hfk.mobs.Player;

/**
 *
 * @author LostMekka
 */
public class Stairs extends UsableLevelItem {

	public Stairs(PointI pos) {
		super(pos);
		img = Resources.getImage("stairs.png");
		hp = -1;
	}

	@Override
	public boolean damage(int dmg) {
		return false;
	}

	@Override
	public String getDisplayName() {
		return "Stairs";
	}

	@Override
	public boolean isSquare() {
		return false;
	}

	@Override
	public boolean blocksSight() {
		return false;
	}

	@Override
	public boolean blocksMovement() {
		return false;
	}

	@Override
	public boolean canUse(Mob m) {
		return m instanceof Player;
	}

	@Override
	public boolean useInternal(Mob m) {
		GameController.get().nextLevel();
		return true;
	}
	
}
