/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items.weapons;

import hfk.PointF;
import hfk.Shot;
import hfk.game.Resources;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.WeaponStatsCard;

/**
 *
 * @author LostMekka
 */
public class Pistol extends Weapon {

	public Pistol(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_pistol.png");
		type = WeaponType.pistol;
	}

	@Override
	public Shot initShot(Shot s) {
		s.size = 0.09f;
		s.img = Resources.getImage("shot.png");
		s.hitSound = Resources.getSound("hit1.wav");
		return s;
	}

	@Override
	public WeaponStatsCard getDefaultWeaponStats() {
		WeaponStatsCard s = WeaponStatsCard.createNormal();
		int bullet = Weapon.AmmoType.bullet.ordinal();
		s.clipSize[bullet] = 8;
		s.reloadCount[bullet] = s.clipSize[bullet];
		s.reloadTimes[bullet] = 1200;
		s.ammoPerShot[bullet] = 1;
		s.shotsPerBurst = 1;
		s.burstInterval = 250;
		s.maxScatter = 30f;
		s.scatterCoolRate = 10f;
		s.scatterPerShot = 6f;
		s.shotVel = 6f;
		s.isAutomatic = false;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int physical = Damage.DamageType.physical.ordinal();
		d.setDieCount(physical, 3);
		d.setEyeCount(physical, 5);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Pistol";
	}

	@Override
	public long getRarityScore() {
		return 3000;
	}
	
}
