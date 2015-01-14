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
public class RocketLauncher extends Weapon {

	public RocketLauncher(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_rocketlauncher.png");
		type = WeaponType.rocketLauncher;
	}

	@Override
	public Shot initShot(Shot s) {
		s.size = 0.09f;
		s.img = Resources.getImage("s_rocket.png");
		s.hitSound = Resources.getSound("s_grenade_hit.wav");
		return s;
	}

	@Override
	public WeaponStatsCard getDefaultWeaponStats() {
		WeaponStatsCard s = WeaponStatsCard.createNormal();
		int rocket = Weapon.AmmoType.rocket.ordinal();
		s.clipSize[rocket] = 1;
		s.reloadCount[rocket] = s.clipSize[rocket];
		s.reloadTimes[rocket] = 1400;
		s.ammoPerShot[rocket] = 1;
		s.shotsPerBurst = 1;
		s.maxScatter = 30f;
		s.scatterCoolRate = 10f;
		s.scatterPerShot = 6f;
		s.shotVel = 9f;
		s.isAutomatic = false;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int phys = Damage.DamageType.physical.ordinal();
		int fire = Damage.DamageType.fire.ordinal();
		d.setDieCount(phys, 20);
		d.setEyeCount(phys, 5);
		d.setDieCount(fire, 20);
		d.setEyeCount(fire, 5);
		d.setAreaRadius(2.5f);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Rocket Launcher";
	}

	@Override
	public long getRarityScore() {
		return 25000;
	}
	
}
