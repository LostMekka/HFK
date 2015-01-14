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
public class CheatRifle extends Weapon {

	public CheatRifle(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_grenadelauncher.png");
		type = WeaponType.cheatRifle;
	}

	@Override
	public Shot initShot(Shot s) {
		s.size = 0.09f;
		s.img = Resources.getImage("s_grenade.png");
		s.hitSound = Resources.getSound("s_grenade_hit.wav");
		s.bounceSound = Resources.getSound("s_grenade_bounce.wav");
		s.friction = 5f;
		s.lifetime = 2000;
		return s;
	}

	@Override
	public WeaponStatsCard getDefaultWeaponStats() {
		WeaponStatsCard s = WeaponStatsCard.createNormal();
		int grenade = Weapon.AmmoType.grenade.ordinal();
		s.clipSize[grenade] = 1;
		s.reloadCount[grenade] = s.clipSize[grenade];
		s.reloadTimes[grenade] = 1100;
		s.shotsPerBurst = 1;
		s.maxScatter = 30f;
		s.scatterCoolRate = 10f;
		s.scatterPerShot = 6f;
		s.shotVel = 9f;
		s.isAutomatic = true;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int phys = Damage.DamageType.physical.ordinal();
		int fire = Damage.DamageType.fire.ordinal();
		d.setDieCount(phys, 10);
		d.setEyeCount(phys, 10);
		d.setDieCount(fire, 10);
		d.setEyeCount(fire, 10);
		d.setAreaRadius(1f);
		return d;
	}

	@Override
	public String getWeaponName() {
		return "Grenade Launcher";
	}

	@Override
	public long getRarityScore() {
		return 12000;
	}

	@Override
	public void pullAlternativeTriggerInternal() {
		GameController.get().nextLevel();
	}
	
}
