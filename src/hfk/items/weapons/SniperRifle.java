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
public class SniperRifle extends Weapon {

	public SniperRifle(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_sniperrifle.png");
		type = WeaponType.sniperRifle;
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
		int sniper = Weapon.AmmoType.sniperRound.ordinal();
		s.clipSize[sniper] = 5;
		s.reloadCount[sniper] = 1;
		s.reloadTimes[sniper] = 800;
		s.ammoPerShot[sniper] = 1;
		s.shotsPerBurst = 1;
		s.burstInterval = 1000;
		s.maxScatter = 10f;
		s.scatterCoolRate = 10f;
		s.scatterPerShot = 6f;
		s.shotVel = 12f;
		s.weaponZoom = 5f;
		s.isAutomatic = false;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int physical = Damage.DamageType.physical.ordinal();
		d.setDieCount(physical, 7);
		d.setEyeCount(physical, 7);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Sniper Rifle";
	}

	@Override
	public long getRarityScore() {
		return 17000;
	}
	
}
