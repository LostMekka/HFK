/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.stats;

import hfk.items.weapons.Weapon;

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
	public boolean isAutomatic;
	
	public WeaponStatsCard(){
		this(false);
	}
	
	public WeaponStatsCard(boolean empty){
		if(empty) return;
		projectilesPerShot = 1;
		shotsPerBurst = 1;
		burstInterval = 100;
		shotInterval = 300;
		minScatter = 0f;
		maxScatter = 10f;
		scatterPerShot = 1f;
		scatterCoolRate = 5f;
		shotVel = 9f;
		weaponZoom = 1f;
		isAutomatic = true;
	}
	
	public void add(WeaponStatsCard c){
		projectilesPerShot += c.projectilesPerShot;
		shotsPerBurst += c.shotsPerBurst;
		burstInterval += c.burstInterval;
		shotInterval += c.shotInterval;
		minScatter += c.minScatter;
		maxScatter += c.maxScatter;
		scatterPerShot += c.scatterPerShot;
		scatterCoolRate += c.scatterCoolRate;
		shotVel += c.shotVel;
		weaponZoom += c.weaponZoom;
		isAutomatic |= c.isAutomatic;
		shotBounces += c.shotBounces;
		overDamageSplashRadius += c.overDamageSplashRadius;
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
			clipSize[i] += c.clipSize[i];
			reloadTimes[i] += c.reloadTimes[i];
			reloadCount[i] += c.reloadCount[i];
			ammoPerShot[i] += c.ammoPerShot[i];
			ammoPerBurst[i] += c.ammoPerBurst[i];
			ammoRegenerationRates[i] += c.ammoRegenerationRates[i];
		}
	}
	
	public void apply(WeaponStatsCard c){
		projectilesPerShot += c.projectilesPerShot;
		shotsPerBurst += c.shotsPerBurst;
		burstInterval += c.burstInterval;
		shotInterval += c.shotInterval;
		minScatter /= c.minScatter;
		maxScatter /= c.maxScatter;
		scatterPerShot /= c.scatterPerShot;
		scatterCoolRate *= c.scatterCoolRate;
		shotVel *= c.shotVel;
		weaponZoom *= c.weaponZoom;
		isAutomatic |= c.isAutomatic;
		shotBounces += c.shotBounces;
		overDamageSplashRadius += c.overDamageSplashRadius;
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
			clipSize[i] += c.clipSize[i];
			reloadTimes[i] -= c.reloadTimes[i];
			reloadCount[i] += c.reloadCount[i];
			ammoPerShot[i] += c.ammoPerShot[i];
			ammoPerBurst[i] += c.ammoPerBurst[i];
			ammoRegenerationRates[i] += c.ammoRegenerationRates[i];
		}
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
		System.arraycopy(clipSize, 0, ans.clipSize, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(reloadTimes, 0, ans.reloadTimes, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(reloadCount, 0, ans.reloadCount, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(ammoPerShot, 0, ans.ammoPerShot, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(ammoPerBurst, 0, ans.ammoPerBurst, 0, Weapon.AMMO_TYPE_COUNT);
		System.arraycopy(ammoRegenerationRates, 0, ans.ammoRegenerationRates, 0, Weapon.AMMO_TYPE_COUNT);
		return ans;
	}
	
}
