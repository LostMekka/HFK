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
import hfk.mobs.Mob;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.ItemEffect;
import hfk.stats.MobStatsCard;
import hfk.stats.WeaponStatsCard;

/**
 *
 * @author LostMekka
 */
public class SniperRifle extends Weapon {

	private ItemEffect zoomEffect;
	private boolean zoom = false;
	
	public SniperRifle(float angle, PointF position) {
		super(angle, position);
		shotSound = Resources.getSound("w_p_s.wav");
		setImg("w_sniperrifle.png");
		type = WeaponType.sniperRifle;
		zoomEffect = new ItemEffect() {
			@Override
			public String[] getDisplayStrings() {
				return new String[]{"sniper zoom"};
			}
			@Override
			public long getRarity() {
				return 1;
			}
		};
		zoomEffect.msc = MobStatsCard.createBonus();
		zoomEffect.msc.setVisionAngle(-300f);
		zoomEffect.msc.setSightRange(5f);
		zoomEffect.wsc = WeaponStatsCard.createBonus();
		zoomEffect.wsc.weaponZoom = 6f;
	}

	@Override
	public void weaponUnSelected() {
		Mob m = getParentMob();
		if(m == null || !zoom) return;
		zoom = false;
		effects.remove(zoomEffect);
		m.recalculateCards();
		if(m == GameController.get().player) GameController.get().recalcVisibleTiles = true;
	}

	@Override
	public void pullAlternativeTriggerInternal() {
		Mob m = getParentMob();
		if(m == null) return;
		if(zoom){
			effects.remove(zoomEffect);
		} else {
			effects.add(zoomEffect);
		}
		zoom = !zoom;
		m.recalculateCards();
		if(m == GameController.get().player) GameController.get().recalcVisibleTiles = true;
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
		s.reloadTimes[sniper] = 1100;
		s.ammoPerShot[sniper] = 1;
		s.shotsPerBurst = 1;
		s.burstInterval = 1000;
		s.maxScatter = 10f;
		s.scatterCoolRate = 10f;
		s.scatterPerShot = 6f;
		s.shotVel = 20f;
		s.weaponZoom = 1f;
		s.isAutomatic = false;
		return s;
	}

	@Override
	public DamageCard getDefaultDamageCard() {
		DamageCard d = DamageCard.createNormal();
		int physical = Damage.DamageType.physical.ordinal();
		d.setDieCount(physical, 10);
		d.setEyeCount(physical, 5);
		d.setDmgBonus(physical, 0.2f);
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
