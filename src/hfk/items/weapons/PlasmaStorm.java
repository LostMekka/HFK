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
public class PlasmaStorm extends Weapon {

	public PlasmaStorm(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("shot1.wav");
		setImg("w_plasmaStorm.png");
		type = WeaponType.plasmaStorm;
	}

	@Override
	public float getScreenRecoilAmount() {
		return 0.07f;
	}

	@Override
	public Shot initShot(Shot s) {
		s.lifetime = GameController.random.nextInt(650) + 650;
		s.friction = 2f + 9f * GameController.random.nextFloat();
		s.size = 0.09f;
		s.img = Resources.getImage("shot_plasma.png");
		s.hitSound = Resources.getSound("hit1.wav");
		return s;
	}

	@Override
	public WeaponStatsCard getDefaultWeaponStats() {
		WeaponStatsCard s = WeaponStatsCard.createNormal();
		int plasma = Weapon.AmmoType.plasmaRound.ordinal();
		s.clipSize[plasma] = 200;
		s.reloadTimes[plasma] = 6000;
		s.reloadCount[plasma] = s.clipSize[plasma];
		s.ammoPerShot[plasma] = 2;
		s.shotsPerBurst = 1;
		s.projectilesPerShot = 3;
		s.burstInterval = 150;
		s.minScatter = 40f;
		s.maxScatter = 110f;
		s.scatterCoolRate = 9;
		s.scatterPerShot = 1f;
		s.shotVel = 7.1f;
		s.isAutomatic = true;
		s.bounceProbability = 0.5f;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int i = Damage.DamageType.plasma.ordinal();
		d.setDieCount(i, 2);
		d.setEyeCount(i, 4);
		i = Damage.DamageType.shock.ordinal();
		d.setDieCount(i, 1);
		d.setEyeCount(i, 7);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Plasma Storm";
	}

	@Override
	public long getRarityScore() {
		return 40000;
	}

}
