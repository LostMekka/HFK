/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items;

import hfk.PointF;
import hfk.game.GameRenderer;
import hfk.mobs.Mob;
import org.newdawn.slick.Color;

/**
 *
 * @author LostMekka
 */
public class EmptyItem extends InventoryItem {

	public EmptyItem(PointF pos) {
		super(pos);
	}

	@Override
	public String getDisplayName() {
		return "(EMPTY)";
	}

	@Override
	public long getRarityScore() {
		return 0;
	}

	@Override
	public Color getDisplayColor() {
		return GameRenderer.COLOR_TEXT_INACTIVE;
	}

	@Override
	public InventoryItem use(Mob m) {
		throw new RuntimeException("used empty item!");
	}

}
