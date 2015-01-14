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
public class HealthPack extends InventoryItem {

	public enum Type { small, medium, big }
	
	public int hp;
	public Type t;

	public HealthPack(PointF pos, Type t) {
		super(pos);
		this.t = t;
		switch(t){
			case small: hp = 25; break;
			case medium: hp = 50; break;
			case big: hp = 100; break;
		}
		image = Resources.getImage("healthpack.png");
	}

	@Override
	public String getDisplayName() {
		return "health pack: " + hp;
	}

	@Override
	public long getRarityScore() {
		return 30 * hp;
	}

	@Override
	public Color getDisplayColor() {
		return Color.red;
	}

	@Override
	public boolean use(Mob m, boolean fromInventory) {
		return m.heal(hp);
	}

	@Override
	public void render() {
		GameController.get().renderer.drawImage(image, pos, true, GameRenderer.LayerType.items);
	}
	
}
