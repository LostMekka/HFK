/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.mobs;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.items.weapons.DamagedHuntingGun;
import hfk.items.weapons.Weapon;
import hfk.stats.MobStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Hunter extends Mob {

	public Hunter(PointF pos) {
		super(pos);
		canOpenDoors = true;
		SpriteSheet sheet = Resources.getSpriteSheet("e_star.png");
		animation = new Animation(sheet, new int[]{1,0,0,0,1,1,0,0}, new int[]{300,300,300,300});
		randomizeAnimationState();
		hitSound = Resources.getSound("e_star_hit.wav");
		deathSound = Resources.getSound("e_star_die.wav");
		alertSound = Resources.getSound("e_star_alert.wav");
		barrageTimeOnLost = 500;
		moveWhilePlayerVisible = true;
		moveWhileShooting = true;
		canOpenDoors = false;
		inventory.equipWeaponFromGround(new DamagedHuntingGun(0f, pos.clone()));
		inventory.addAmmo(Weapon.AmmoType.shell, GameController.random.nextInt(11) + 5);
	}

	@Override
	public float getSpawnProbabilityModifier() {
		return 0.1f;
	}

	@Override
	public int getDifficultyScore() {
		return 2;
	}

	@Override
	public String getDisplayName() {
		return "Hunter";
	}

	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setAmmoSlotSize(Weapon.AmmoType.shell.ordinal(), 5);
		c.setMaxHP(35);
		c.setSightRange(4.6f);
		c.setMaxSpeed(1f);
		return c;
	}
	
}
