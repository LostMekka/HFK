/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items;

import hfk.PointF;
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
		return Color.darkGray;
	}

	@Override
	public boolean use(Mob m, boolean fromInventory) {
		return false;
	}

}
