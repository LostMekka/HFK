/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.stats;

import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;

/**
 *
 * @author LostMekka
 */
public interface StatsModifier {
	
	public abstract void addDamageCardEffects(DamageCard card, InventoryItem i, Mob m);
	public abstract void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m);
	public abstract void addMobStatsCardEffects(MobStatsCard card, Mob m);
	
}
