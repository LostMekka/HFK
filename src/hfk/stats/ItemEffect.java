/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.stats;

import hfk.items.ItemType;


/**
 *
 * @author LostMekka
 */
public abstract class ItemEffect {
	
	public ItemType itemType = null;
	
	public MobStatsCard msc = MobStatsCard.createBonus();
	public WeaponStatsCard wsc = WeaponStatsCard.createBonus();
	public DamageCard dc = DamageCard.createBonus();
	
	public abstract String[] getDisplayStrings();
	public abstract long getRarity();
	
}
