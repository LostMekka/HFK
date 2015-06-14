/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.Resources;
import hfk.mobs.Mob;
import org.newdawn.slick.Color;

/**
 *
 * @author LostMekka
 */
public class ExperienceOrb extends InventoryItem {

	public int xp;

	public ExperienceOrb(PointF pos, int amount) {
		super(pos);
		useOnPickup = true;
		xp = amount;
		size = 0.3f;
		image = Resources.getImage("xporb.png");
	}

	@Override
	public String getDisplayName() {
		return "experience orb: " + xp;
	}

	@Override
	public long getRarityScore() {
		return 200 * xp;
	}

	@Override
	public Color getDisplayColor() {
		return Color.green;
	}

	@Override
	public InventoryItem use(Mob m) {
		m.xp += xp;
		return null;
	}

	@Override
	public void render() {
		GameController.get().renderer.drawImage(image, pos, true, GameRenderer.LayerType.items);
	}
	
}
