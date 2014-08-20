/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.stats;

import hfk.items.weapons.Weapon;
import java.util.Arrays;

/**
 *
 * @author LostMekka
 */
public class WeaponStatsCard {
	
	public int[] clipSize = new int[Weapon.AMMO_TYPE_COUNT];
	public int[] reloadTimes = new int[Weapon.AMMO_TYPE_COUNT];
	public int[] reloadCount = new int[Weapon.AMMO_TYPE_COUNT];
	public int[] ammoPerShot = new int[Weapon.AMMO_TYPE_COUNT];
	public int[] ammoPerBurst = new int[Weapon.AMMO_TYPE_COUNT];
	public float[] ammoRegenerationRates = new float[Weapon.AMMO_TYPE_COUNT];
	public int projectilesPerShot;
	public int shotsPerBurst;
	public int burstInterval;
	public int shotInterval;
	public int shotBounces;
	public float minScatter;
	public float maxScatter;
	public float scatterPerShot;
	public float scatterCoolRate;
	public float shotVel;
	public float weaponZoom;
	public float overDamageSplashRadius;
	public float maxEnergyLossOnBounce;
	public float bounceProbability;
	public boolean isAutomatic;
	
	private boolean isBonusCard;
	
	public static final WeaponStatsCard createNormal(){
		WeaponStatsCard ans = new WeaponStatsCard();
		ans.isBonusCard = false;
		ans.projectilesPerShot = 1;
		ans.shotsPerBurst = 1;
		ans.burstInterval = 100;
		ans.shotInterval = 300;
		ans.minScatter = 0f;
		ans.maxScatter = 10f;
		ans.scatterPerShot = 1f;
		ans.scatterCoolRate = 5f;
		ans.shotVel = 9f;
		ans.weaponZoom = 1f;
		ans.maxEnergyLossOnBounce = 0.4f;
		ans.bounceProbability = 0.01f;
		return ans;
	}
	
	public static final WeaponStatsCard createBonus(){
		WeaponStatsCard ans = new WeaponStatsCard();
		ans.isBonusCard = true;
		return ans;
	}
	
	private WeaponStatsCard(){}
	
	public void add(WeaponStatsCard c){
		if(!isBonusCard || !c.isBonusCard) throw new RuntimeException("add on non bonus card!");
		// add
		minScatter += c.minScatter;
		maxScatter += c.maxScatter;
		scatterPerShot += c.scatterPerShot;
		scatterCoolRate += c.scatterCoolRate;
		shotVel += c.shotVel;
		projectilesPerShot += c.projectilesPerShot;
		shotsPerBurst += c.shotsPerBurst;
		burstInterval += c.burstInterval;
		shotInterval += c.shotInterval;
		weaponZoom += c.weaponZoom;
		shotBounces += c.shotBounces;
		overDamageSplashRadius += c.overDamageSplashRadius;
		maxEnergyLossOnBounce += c.maxEnergyLossOnBounce;
		bounceProbability += c.bounceProbability;
		// other
		isAutomatic |= c.isAutomatic;
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
			clipSize[i] += c.clipSize[i];
			reloadTimes[i] += c.reloadTimes[i];
			reloadCount[i] += c.reloadCount[i];
			ammoPerShot[i] += c.ammoPerShot[i];
			ammoPerBurst[i] += c.ammoPerBurst[i];
			ammoRegenerationRates[i] *= 1f + c.ammoRegenerationRates[i];
		}
	}
	
	public void applyBonus(WeaponStatsCard c){
		if(isBonusCard || !c.isBonusCard) throw new RuntimeException("apply on bonus card or applied a non bonus card!");
		// multiply
		minScatter *= 1f + c.minScatter;
		maxScatter *= 1f + c.maxScatter;
		scatterPerShot *= 1f + c.scatterPerShot;
		scatterCoolRate *= 1f + c.scatterCoolRate;
		shotVel *= 1f + c.shotVel;
		weaponZoom *= 1f + c.weaponZoom;
		maxEnergyLossOnBounce *= 1f + c.maxEnergyLossOnBounce;
		// add
		bounceProbability += c.bounceProbability;
		projectilesPerShot += c.projectilesPerShot;
		shotsPerBurst += c.shotsPerBurst;
		burstInterval += c.burstInterval;
		shotInterval += c.shotInterval;
		shotBounces += c.shotBounces;
		overDamageSplashRadius += c.overDamageSplashRadius;
		// other
		isAutomatic |= c.isAutomatic;
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
			clipSize[i] += c.clipSize[i];
			reloadTimes[i] -= c.reloadTimes[i];
			reloadCount[i] += c.reloadCount[i];
			ammoPerShot[i] += c.ammoPerShot[i];
			ammoPerBurst[i] += c.ammoPerBurst[i];
			ammoRegenerationRates[i] *= 1f + c.ammoRegenerationRates[i];
		}
		// limit
		minScatter = Math.max(0f, minScatter);
		maxScatter = Math.max(minScatter, maxScatter);
		shotVel = Math.max(2f, shotVel);
		maxEnergyLossOnBounce = Math.min(1f, Math.max(0f, maxEnergyLossOnBounce));
		bounceProbability = Math.min(1f, Math.max(0f, bounceProbability));
	}
	
	@Override
	public WeaponStatsCard clone(){
		WeaponStatsCard ans = new WeaponStatsCard();
		ans.projectilesPerShot = projectilesPerShot;
		ans.shotsPerBurst = shotsPerBurst;
		ans.burstInterval = burstInterval;
		ans.shotInterval = shotInterval;
		ans.minScatter = minScatter;
		ans.maxScatter = maxScatter;
		ans.scatterPerShot = scatterPerShot;
		ans.scatterCoolRate = scatterCoolRate;
		ans.shotVel = shotVel;
		ans.weaponZoom = weaponZoom;
		ans.isAutomatic = isAutomatic;
		ans.shotBounces = shotBounces;
		ans.overDamageSplashRadius = overDamageSplashRadius;
		ans.bounceProbability = bounceProbability;
		ans.maxEnergyLossOnBounce = maxEnergyLossOnBounce;
		System.arraycopy(clipSize, 0, ans.clipSize, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(reloadTimes, 0, ans.reloadTimes, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(reloadCount, 0, ans.reloadCount, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(ammoPerShot, 0, ans.ammoPerShot, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(ammoPerBurst, 0, ans.ammoPerBurst, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(ammoRegenerationRates, 0, ans.ammoRegenerationRates, 0, Weapon.AMMO_TYPE_COUNT);
		return ans;
	}
	
}
