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
public class Machinegun extends Weapon {
	
	public Machinegun(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_machinegun.png");
		type = ItemType.wMachinegun;
	}

	@Override
	public float getScreenRecoilAmount() {
		return 0.08f;
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
		s.clipSize[bullet] = 100;
		s.reloadCount[bullet] = s.clipSize[bullet];
		s.reloadTimes[bullet] = 4000;
		s.ammoPerShot[bullet] = 1;
		s.shotsPerBurst = 1;
		s.burstInterval = 100;
		s.maxScatter = 20f;
		s.scatterCoolRate = 15f;
		s.scatterPerShot = 1.9f;
		s.shotVel = 7f;
		s.isAutomatic = true;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int phys = Damage.DamageType.physical.ordinal();
		d.setDieCount(phys, 2);
		d.setEyeCount(phys, 4);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Machinegun";
	}

	@Override
	public long getRarityScore() {
		return 9000;
	}
	
}
