/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.stats;

/**
 *
 * @author LostMekka
 */
public abstract class ItemEffect {
	
	public MobStatsCard msc = new MobStatsCard();
	public WeaponStatsCard wsc = new WeaponStatsCard();
	public DamageCard dc = new DamageCard();
	
	public abstract String[] getDisplayStrings();
	public abstract long getRarity();
	
}
