/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.skills;

import hfk.Shot;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class SkillSet implements StatsModifier {
	
	private int superCount = 0, superMax = 1;
	private Mob parent;
	private LinkedList<Skill> skills = new LinkedList<>();
	
	// special skills (need special handling)
	private Skill pistolVel, pistolAuto;
	private Skill shotgunBounce, shotgunRange;
	private Skill machinegunRate;
	private Skill plasmaOverDmgSplash;
	private Skill grenadeRange, grenadeManual, grenadeSmart;
	private Skill spiderSenses;
	private Skill superMore;
	
	public SkillSet(Mob parent) {
		this.parent = parent;
		// TODO: init skills
		grenadeSmart = new Skill(parent, 2, "smart grenade", "grenade instantly explode when they touch any living thing. level 2 grenades even avoid friendly fire!");
		grenadeSmart.costs = new int[] { 10, 20 };
		grenadeManual = new Skill(parent, 2, "grenade trigger", "grenades are detonated manually! dat skill, many control, much kapow.");
		grenadeManual.costs = new int[] { 10, 20 };
		spiderSenses = new Skill(parent, 2, "spider senses", "sense information about enemies. level 1 shows reload status, level 2 shows health bars.");
		spiderSenses.costs = new int[] { 10, 20 };
		skills.add(grenadeSmart);
		skills.add(grenadeManual);
		skills.add(spiderSenses);
		grenadeManual.blocks.add(grenadeSmart);
		grenadeSmart.blocks.add(grenadeManual);
		grenadeSmart.addRequirement(1, spiderSenses, 2);
	}

	public Mob getParent() {
		return parent;
	}
	
	public int size(){
		return skills.size();
	}
	
	public boolean canLevelUp(Skill s){
		return canLevelUp(s, false);
	}
	
	public boolean canLevelUp(Skill s, boolean ignoreCost){
		return (s.level != 0 || !s.isSuperSkill || superCount < superMax) && s.requirementsMet(ignoreCost);
	}
	
	public boolean canAddSuperSkill(){
		return superCount < superMax;
	}

	public int getSuperSkillCount() {
		return superCount;
	}

	public int getSuperSkillMax() {
		return superMax;
	}
	
	public void skillsChanged(Skill changedSkill){
		if(changedSkill == superMore) superMax++;
		if(changedSkill.isSuperSkill && changedSkill.level == 1){
			superCount++;
			if(superCount > superMax) throw new RuntimeException("super skill limit exceeded");
		}
	}
	
	public LinkedList<Skill> getSkillList(){
		return skills;
	}
	
	public int getSkillLevel(String name){
		for(Skill s : skills){
			if(s.name.equals(name)) return s.level;
		}
		throw new RuntimeException("Skill \"" + name + "\" not in skill tree!");
	}
	
	@Override
	public void addDamageCardEffects(DamageCard card, Weapon w, Mob m) {
		for(Skill s : skills) s.addDamageCardEffects(card, w, m);
	}

	@Override
	public void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m) {
		for(Skill s : skills) s.addWeaponStatsCardEffects(card, w, m);
	}

	@Override
	public void addMobStatsCardEffects(MobStatsCard card, Mob m) {
		for(Skill s : skills) s.addMobStatsCardEffects(card, m);
	}
	
	public Shot modifyShot(Shot s, Weapon w, Mob m){
		if(m != parent || w.type == null) return s;
		switch(w.type){
			case grenadeLauncher:
				s.smartGrenadeLevel = Math.max(grenadeSmart.level, s.smartGrenadeLevel);
				s.manualGrenadeLevel = Math.max(grenadeManual.level, s.manualGrenadeLevel);
				break;
		}
		return s;
	}
	
	public boolean shouldRenderReloadBar(Mob m){
		return spiderSenses.level > 0;
	}
	
	public boolean shouldRenderHealthBar(Mob m){
		return spiderSenses.level > 1;
	}
	
}
