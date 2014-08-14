/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.mobs;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.items.weapons.Weapon;
import hfk.game.Resources;
import hfk.stats.MobStatsCard;
import org.newdawn.slick.Animation;

/**
 *
 * @author LostMekka
 */
public class Player extends Mob {

	public Player(PointF pos) {
		super(pos);
		animation = new Animation(Resources.getSpriteSheet("player.png"), 350);
		hitSound = Resources.getSound("p_hit.wav");
		deathSound = Resources.getSound("p_die.wav");
		autoFollowPath = false;
		autoFollowPlayer = false;
		autoFollowPlayerOnHit = false;
		autoFollowPlayerOnNotify = false;
		autoLockOnPlayer = false;
		autoReloadWeapon = false;
		autoSetPath = false;
		autoUseWeapon = false;
	}

	@Override
	public int getDifficultyScore() {
		return -1;
	}

	@Override
	public String getDisplayName() {
		return "Player";
	}
	
	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setMaxHP(100);
		c.setSightRange(5.5f);
		c.setMaxSpeed(4f);
		c.setAmmoSlotSize(Weapon.AmmoType.bullet.ordinal(), 100);
		c.setAmmoSlotSize(Weapon.AmmoType.shell.ordinal(), 50);
		c.setAmmoSlotSize(Weapon.AmmoType.sniperRound.ordinal(), 20);
		c.setAmmoSlotSize(Weapon.AmmoType.grenade.ordinal(), 20);
		c.setAmmoSlotSize(Weapon.AmmoType.rocket.ordinal(), 5);
		c.setAmmoSlotSize(Weapon.AmmoType.plasmaRound.ordinal(), 150);
		c.setInventorySize(15);
		c.setHearRange(10);
		c.setQuickSlotCount(2);
		return c;
	}

	@Override
	public void mobOnDeath(Shot s) {
		GameController.get().playerDied();
	}
	
}
