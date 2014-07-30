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
public abstract class ItemEffect {
	
	public Weapon.WeaponType weaponType = null;
	
	public MobStatsCard msc = new MobStatsCard();
	public WeaponStatsCard wsc = new WeaponStatsCard();
	public DamageCard dc = new DamageCard();
	
	public abstract String[] getDisplayStrings();
	public abstract long getRarity();
	
}
