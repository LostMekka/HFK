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
		img = Resources.getImage("plasmamachinegun.png");
		flippedImg = Resources.getImage("plasmamachinegun.png", true);
	}

	@Override
	public float getScreenRecoilAmount() {
		return 0.08f;
	}

	@Override
	public Shot initShot(Shot s) {
		s.size = 0.09f;
		s.img = Resources.getImage("shot.png");
		s.hit = Resources.getSound("hit1.wav");
		return s;
	}

	@Override
	public WeaponStatsCard getDefaultWeaponStats() {
		WeaponStatsCard s = new WeaponStatsCard();
		int plasmaround = Weapon.AmmoType.plasmaround.ordinal();
		s.clipSize[plasmaround] = 80;
		s.reloadTimes[plasmaround] = 5000;
		s.ammoPerShot[plasmaround] = 1;
		s.shotsPerBurst = 1;
		s.burstInterval = 100;
		s.maxScatter = 25f;
		s.scatterCoolRate = 15f;
		s.scatterPerShot = 2.2f;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = new DamageCard(1);
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
