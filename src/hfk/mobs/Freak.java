/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.mobs;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.items.weapons.EnergyPistol;
import hfk.items.weapons.Pistol;
import hfk.items.weapons.PlasmaMachinegun;
import hfk.items.weapons.Shotgun;
import hfk.items.weapons.Weapon;
import hfk.stats.Damage;
import hfk.stats.MobStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Freak extends Mob {

	public enum LoadOutType { pistol, shotgun, energypistol, plasmaminigun }
	
	private LoadOutType t;
	
	public Freak(PointF pos, LoadOutType t) {
		super(pos);
		this.t = t;
		SpriteSheet sheet = Resources.getSpriteSheet("e_star.png");
		animation = new Animation(sheet, new int[]{1,0,0,0,1,1,0,0}, new int[]{300,300,300,300});
		randomizeAnimationState();
		hitSound = Resources.getSound("e_star_hit.wav");
		deathSound = Resources.getSound("e_star_die.wav");
		alertSound = Resources.getSound("e_star_alert.wav");
		switch(t){
			case pistol:
				barrageTimeOnHit = 2000;
				barrageTimeOnLost = 1000;
				inventory.equipWeaponFromGround(new Pistol(0f, pos.clone()));
				inventory.addAmmo(Weapon.AmmoType.bullet, GameController.random.nextInt(200) + 100);
				break;
			case shotgun:
				barrageTimeOnLost = 500;
				moveWhileShooting = true;
				moveWhilePlayerVisible = true;
				inventory.equipWeaponFromGround(new Shotgun(0f, pos.clone()));
				inventory.addAmmo(Weapon.AmmoType.shell, GameController.random.nextInt(100) + 50);
				break;
			case energypistol:
				barrageTimeOnHit = 2000;
				barrageTimeOnLost = 1000;
				inventory.equipWeaponFromGround(new EnergyPistol(0f, pos.clone()));
				inventory.addAmmo(Weapon.AmmoType.bullet, GameController.random.nextInt(200) + 100);
				break;
			case plasmaminigun:
				barrageTimeOnHit = 4000;
				barrageTimeOnLost = 1000;
				barrageTimeOnNotify = 4000;
				inventory.equipWeaponFromGround(new PlasmaMachinegun(0f, pos.clone()));
				inventory.addAmmo(Weapon.AmmoType.plasmaround, GameController.random.nextInt(300) + 300);
				break;
			default: throw new RuntimeException("loadout type not recognized");
		}
	}

	@Override
	public int getDifficultyScore() {
		switch(t){
			case pistol: return 4;
			case shotgun: return 6;
			case energypistol: return 8;
			case plasmaminigun: return 16;
			default: throw new RuntimeException("loadout type not recognized");
		}
	}

	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setAmmoSlotSize(Weapon.AmmoType.bullet.ordinal(), 100);
		c.setAmmoSlotSize(Weapon.AmmoType.plasmaround.ordinal(), 150);
		c.setAmmoSlotSize(Weapon.AmmoType.shell.ordinal(), 50);
		c.setMaxHP(70);
		c.setSightRange(4.6f);
		c.setMaxSpeed(1.5f);
		c.setResistance(Damage.DamageType.physical.ordinal(), 2);
		return c;
	}
	
}
