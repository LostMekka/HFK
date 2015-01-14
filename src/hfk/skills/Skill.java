/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.skills;

import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class Skill implements StatsModifier {
	
	public static enum SkillAvailability { available, cantAfford, isMaxed, isBlocked, reqNeeded, superMaxed }
	
	public Weapon.WeaponType weaponType = null;
	public boolean isSuperSkill = false;
	public String name, description;
	public LinkedList<Skill> blocks = new LinkedList<>();
	public DamageCard[] damageCards;
	public WeaponStatsCard[] weaponStatsCards;
	public MobStatsCard[] mobStatsCards;
	public int[] costs;
	public float[] customValues = null;
	
	private int level = 0, maxLevel;
	private ArrayList<HashMap<Skill, Integer>> requirements;
	private Mob parent;
	
	public Skill(Mob parent, int maxLevel, String name, String description) {
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.maxLevel = maxLevel;
		damageCards = new DamageCard[maxLevel];
		weaponStatsCards = new WeaponStatsCard[maxLevel];
		mobStatsCards = new MobStatsCard[maxLevel];
		costs = new int[maxLevel];
		customValues = new float[maxLevel];
		requirements = new ArrayList<>(maxLevel);
		for(int i=0; i<maxLevel; i++) requirements.add(i, new HashMap<Skill, Integer>());
	}

	public int getLevel() {
		return level;
	}

	public int getMaxLevel() {
		return maxLevel;
	}
	
	public boolean isMaxed(){
		return level >= maxLevel;
	}

	public String getDisplayName(){
		return isSuperSkill ? "(S) " + name + " (" + level + ")" : name + " (" + level + ")";
	}
	
	public void addRequirement(int ownLevel, Skill req, int reqLevel){
		HashMap<Skill, Integer> map = requirements.get(ownLevel-1);
		if(map.containsKey(req)) level = Math.max(reqLevel, map.get(req));
		map.put(req, reqLevel);
	}
	
	public void addBlock(Skill s){
		if(!blocks.contains(s)) blocks.add(s);
		if(!s.blocks.contains(this)) s.blocks.add(this);
	}
	
	public boolean canLevelUp(){
		return getSkillAvailability() == SkillAvailability.available;
	}
	
	public SkillAvailability getSkillAvailability(){
		if(maxLevel <= level) return SkillAvailability.isMaxed;
		if(parent.xp < getCost()) return SkillAvailability.cantAfford;
		if(isSuperSkill && level == 0 && !parent.skills.canAddSuperSkill()) return SkillAvailability.superMaxed;
		HashMap<Skill, Integer> map = getRequrirements();
		for(Skill s : map.keySet()) if(s.level < map.get(s)) return SkillAvailability.reqNeeded;
		for(Skill s : blocks) if(s.level > 0) return SkillAvailability.isBlocked;
		return SkillAvailability.available;
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

	public void setCost(int i, int cost) {
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
		parent.skills.skillLeveledUp(this, c);
		parent.recalculateCards();
		return true;
	}
	
	public HashMap<Skill, Integer> getRequrirements(){
		if(level < 0 || level >= maxLevel) return new HashMap<>();
		return requirements.get(level);
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
		return Math.round(costs[level] * parent.skills.getSkillCostMultiplier());
	}
	@Override
	public void addDamageCardEffects(DamageCard card, Weapon w, Mob m) {
		DamageCard c = getDamageCard();
		if(c != null && (weaponType == null || w.type.isSubTypeOf(weaponType))) card.add(c);
	}

	@Override
	public void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m) {
		WeaponStatsCard c = getWeaponStatsCard();
		if(c != null && (weaponType == null || w.type.isSubTypeOf(weaponType))) card.add(c);
	}

	@Override
	public void addMobStatsCardEffects(MobStatsCard card, Mob m) {
		MobStatsCard c = getMobStatsCard();
		if(c != null) card.add(c);
	}

}
