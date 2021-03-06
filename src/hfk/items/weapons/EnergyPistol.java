/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items.weapons;

import hfk.PointF;
import hfk.Shot;
import hfk.game.Resources;
import hfk.items.ItemType;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.WeaponStatsCard;

/**
 *
 * @author LostMekka
 */
public class EnergyPistol extends Weapon {

	public EnergyPistol(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_energypistol.png");
		type = ItemType.wEnergyPistol;
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
		int energy = Weapon.AmmoType.energy.ordinal();
		s.clipSize[bullet] = 8;
		s.reloadCount[bullet] = s.clipSize[bullet];
		s.reloadTimes[bullet] = 1500;
		s.ammoPerShot[bullet] = 1;
		s.clipSize[energy] = 50;
		s.reloadTimes[energy] = -1;
		s.ammoPerShot[energy] = 5;
		s.ammoRegenerationRates[energy] = 3.5f;
		s.shotsPerBurst = 1;
		s.burstInterval = 250;
		s.maxScatter = 30f;
		s.scatterCoolRate = 10f;
		s.scatterPerShot = 6f;
		s.shotVel = 7.8f;
		s.isAutomatic = false;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int physical = Damage.DamageType.physical.ordinal();
		int mental = Damage.DamageType.mental.ordinal();
		d.setDieCount(physical, 3);
		d.setEyeCount(physical, 5);
		d.setDieCount(mental, 1);
		d.setEyeCount(mental, 5);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Energy Pistol";
	}

	@Override
	public long getRarityScore() {
		return 10000;
	}
	
}
