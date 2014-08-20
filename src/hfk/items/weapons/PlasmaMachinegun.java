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
public class PlasmaMachinegun extends Weapon {
	
	public PlasmaMachinegun(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("shot1.wav");
		setImg("w_plasmamachinegun.png");
		type = WeaponType.plasmaMachinegun;
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
		int plasma = Weapon.AmmoType.plasmaRound.ordinal();
		s.clipSize[plasma] = 80;
		s.reloadCount[plasma] = s.clipSize[plasma];
		s.reloadTimes[plasma] = 5000;
		s.ammoPerShot[plasma] = 1;
		s.shotsPerBurst = 1;
		s.burstInterval = 100;
		s.maxScatter = 25f;
		s.scatterCoolRate = 15f;
		s.scatterPerShot = 2.2f;
		s.shotVel = 7f;
		s.isAutomatic = true;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int plasma = Damage.DamageType.plasma.ordinal();
		d.setDieCount(plasma, 2);
		d.setEyeCount(plasma, 4);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Plasma Machinegun";
	}

	@Override
	public long getRarityScore() {
		return 15000;
	}
	
}
