/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.skills;

import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;

/**
 *
 * @author LostMekka
 */
public class Skill implements StatsModifier {
	
	public Weapon.WeaponType weaponType = null;
	public boolean isSuperSkill = false;
	public String name, description;
	public int level = 0;
	public Skill[] needs, blocks;
	public DamageCard[] damageCards;
	public WeaponStatsCard[] weaponStatsCards;
	public MobStatsCard[] mobStatsCards;
	public int[] costs;
	public float[] customValues = null;
	
	private int maxLevel;
	private Mob parent;
	
	public Skill(Mob parent, String name, String description, Skill[] needs, Skill[] blocks, int maxLevel) {
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.needs = needs;
		this.blocks = blocks;
		this.maxLevel = maxLevel;
		damageCards = new DamageCard[maxLevel];
		weaponStatsCards = new WeaponStatsCard[maxLevel];
		mobStatsCards = new MobStatsCard[maxLevel];
		costs = new int[maxLevel];
		customValues = new float[maxLevel];
	}

	public void setDamageCard(int i, DamageCard c) {
		damageCards[i] = c;
	}

	public void setWeaponStatsCard(int i, WeaponStatsCard c) {
		weaponStatsCards[i] = c;
	}

	public void setMobStatsCard(int i, MobStatsCard c) {
		mobStatsCards[i] = c;
	}

	public void setCustomValues(int i, int cost) {
		costs[i] = cost;
	}

	public void setCustomValues(int i, float value) {
		customValues[i] = value;
	}

	public boolean levelUp(){
		int c = getCost();
		if(level >= maxLevel || parent.xp < c) return false;
		parent.xp -= c;
		level++;
		parent.skills.skillsChanged(this);
		parent.recalculateCards();
		return true;
	}
	
	public float getCustomValue(){
		if(level <= 0 || customValues == null) return 0f;
		return customValues[level-1];
	}

	public DamageCard getDamageCard(){
		if(level <= 0) return null;
		return damageCards[level-1];
	}

	public WeaponStatsCard getWeaponStatsCard(){
		if(level <= 0) return null;
		return weaponStatsCards[level-1];
	}
	
	public MobStatsCard getMobStatsCard(){
		if(level <= 0) return null;
		return mobStatsCards[level-1];
	}
	
	public int getCost(){
		if(level >= maxLevel) return -1;
		return costs[level];
	}
	@Override
	public void addDamageCardEffects(DamageCard card, Weapon w, Mob m) {
		DamageCard c = getDamageCard();
		if(c != null && (weaponType == null || weaponType == w.type)) card.add(c);
	}

	@Override
	public void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m) {
		WeaponStatsCard c = getWeaponStatsCard();
		if(c != null && (weaponType == null || weaponType == w.type)) card.add(c);
	}

	@Override
	public void addMobStatsCardEffects(MobStatsCard card, Mob m) {
		MobStatsCard c = getMobStatsCard();
		if(c != null) card.add(c);
	}

}
