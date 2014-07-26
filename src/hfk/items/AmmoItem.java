/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import org.newdawn.slick.Color;

/**
 *
 * @author LostMekka
 */
public class AmmoItem extends InventoryItem {

	private Weapon.AmmoType ammoType;
	private int ammoCount;

	public AmmoItem(PointF pos, Weapon.AmmoType ammoType, int ammoCount) {
		super(pos);
		this.ammoType = ammoType;
		this.ammoCount = ammoCount;
		switch(ammoType){
			case bullet: image = Resources.getImage("ammo_bullet.png"); break;
			case shell: image = Resources.getImage("ammo_shell.png"); break;
			case plasmaround: image = Resources.getImage("ammo_plasmaround.png"); break;
		}
	}

	public int getAmmoCount() {
		return ammoCount;
	}

	public void setAmmoCount(int a) {
		if(a != ammoCount){
			ammoCount = a;
			initLabel();
		}
	}

	public Weapon.AmmoType getAmmoType() {
		return ammoType;
	}

	@Override
	public String getDisplayName() {
		return ammoType + " x " + ammoCount;
		
	}

	@Override
	public Color getDisplayColor() {
		return Color.white;
	}

	@Override
	public long getRarityScore() {
		long r;
		switch(ammoType){
			case bullet: r = 10; break;
			case shell: r = 20; break;
			case plasmaround: r = 30; break;
			default: throw new RuntimeException("could not determine rarity!");
		}
		return r * ammoCount;
	}

	@Override
	public boolean use(Mob m, boolean fromInventory) {
		return false;
	}

	@Override
	public void render() {
		GameController.get().renderer.drawImage(image, pos, 0.5f);
	}
	
}
