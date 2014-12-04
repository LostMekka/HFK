/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items.weapons;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.WeaponStatsCard;

/**
 *
 * @author LostMekka
 */
public class AutoShotgun extends Weapon {

	public AutoShotgun(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_sg_s.wav");
		setImg("w_autoShotgun.png");
		type = WeaponType.autoShotgun;
	}

	@Override
	public float getScreenRecoilAmount() {
		return 0.4f;
	}

	@Override
	public Shot initShot(Shot s) {
		s.lifetime = GameController.random.nextInt(550) + 450;
		s.friction = 2f + 8f * GameController.random.nextFloat();
		s.size = 0.09f;
		s.img = Resources.getImage("shot.png");
		s.hitSound = Resources.getSound("hit1.wav");
		return s;
	}

	@Override
	public WeaponStatsCard getDefaultWeaponStats() {
		WeaponStatsCard s = WeaponStatsCard.createNormal();
		int shells = Weapon.AmmoType.shell.ordinal();
		s.clipSize[shells] = 8;
		s.reloadCount[shells] = 1;
		s.reloadTimes[shells] = 750;
		s.ammoPerShot[shells] = 1;
		s.shotsPerBurst = 1;
		s.projectilesPerShot = 9;
		s.burstInterval = 350;
		s.minScatter = 32f;
		s.maxScatter = s.minScatter;
		s.shotVel = 6.5f;
		s.weaponZoom = 0.2f;
		s.isAutomatic = true;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int physical = Damage.DamageType.physical.ordinal();
		d.setDieCount(physical, 2);
		d.setEyeCount(physical, 5);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Auto Shotgun";
	}

	@Override
	public long getRarityScore() {
		return 25000;
	}

}
