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
	public int projectilesPerShot = 1;
	public int shotsPerBurst = 1;
	public int burstInterval = 100;
	public int shotInterval = 300;
	public float minScatter = 0f;
	public float maxScatter = 10f;
	public float scatterPerShot = 1f;
	public float scatterCoolRate = 5f;
	public float shotVel = 9f;
	public float weaponZoom = 1f;
	public boolean isAutomatic = true;
	
}
