/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.skills;

import hfk.Shot;
import hfk.game.GameController;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.stats.Damage;
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
	
	private int superCount = 0, superMax = 1, levelCount = 0;
	private final Mob parent;
	private final LinkedList<Skill> skills = new LinkedList<>();
	
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
		Skill s = new Skill(parent, 8, "explosive bullets", "all pistols deal extra fire damage.");
		s.weaponType = Weapon.WeaponType.pistol;
		for(int i=0; i<s.getMaxLevel(); i++){
			DamageCard dc = DamageCard.createBonus();
			dc.setDieCount(Damage.DamageType.fire.ordinal(), (i+1)/2);
			dc.setEyeCount(Damage.DamageType.fire.ordinal(), (i+2)/2);
			s.damageCards[i] = dc;
			s.costs[i] = 10 + i>5 ? 10*i : 5*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 2, "ricochet shells", "nothing says \"in your face\" more than a shotgun. you modified your shells so that pellets bounce off walls and travel further, which makes you the ultimate close-quarter badass.");
		s.weaponType = Weapon.WeaponType.shotgun;
		for(int i=0; i<s.getMaxLevel(); i++){
			WeaponStatsCard wsc = WeaponStatsCard.createBonus();
			wsc.shotBounces = 2*i + 1;
			wsc.shotVel = 1f + 0.33f * i;
			s.weaponStatsCards[i] = wsc;
			s.costs[i] = 10 + 5*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 5, "weapon juggler", "if surviving in a foreign universe has taught you one thing, it is that you need all the weapons you can get... at the same time! for each level of this skill you gain an extra weapon slot.");
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setQuickSlotCount(i+1);
			s.mobStatsCards[i] = msc;
			s.costs[i] = 25 + 10*i;
		}
		skills.add(s);
		
		grenadeSmart = new Skill(parent, 2, "smart grenades", "grenades are fun, especially when they dont kill you. for that reason you installed an a.i. module on your grenades that makes sure you stay safe. level 1 of this skill lets grenades bounce off walls instead of exploding on impact, while level 2 installs a friend/foe detection system to avoid friendly fire.");
		grenadeSmart.costs = new int[] { 15, 30 };
		skills.add(grenadeSmart);

		grenadeManual = new Skill(parent, 2, "grenade trigger", "grenades are detonated manually! so skill, many control, much kapow.");
		grenadeManual.costs = new int[] { 10, 20 };
		skills.add(grenadeManual);

		spiderSenses = new Skill(parent, 2, "spider senses", "you evolved to sense information about your enemies. how did you even do that??? level 1 shows reload status, level 2 shows health bars.");
		spiderSenses.costs = new int[] { 10, 20 };
		skills.add(spiderSenses);
		
		// TODO: set blocks and requirements
		grenadeManual.addBlock(grenadeSmart);
		grenadeSmart.addRequirement(1, spiderSenses, 2);
	}

	public Skill getSkill(String name){
		for(Skill s : skills){
			if(s.name.equalsIgnoreCase(name)) return s;
		}
		return null;
	}
	
	public float getSkillCostMultiplier(){
		return 1f + GameController.get().getSkillCostIncreaseRate() * levelCount;
	}
	
	public Mob getParent() {
		return parent;
	}
	
	public int size(){
		return skills.size();
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
		if(changedSkill.isSuperSkill && changedSkill.getLevel() == 1){
			superCount++;
			if(superCount > superMax) throw new RuntimeException("super skill limit exceeded");
		}
		levelCount = 0;
		for(Skill s : skills) levelCount += s.getLevel();
	}
	
	public LinkedList<Skill> getSkillList(){
		return skills;
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
				s.smartGrenadeLevel = Math.max(grenadeSmart.getLevel(), s.smartGrenadeLevel);
				s.manualDetonateLevel = Math.max(grenadeManual.getLevel(), s.manualDetonateLevel);
				break;
		}
		return s;
	}
	
	public boolean shouldRenderReloadBar(Mob m){
		return spiderSenses.getLevel() > 0;
	}
	
	public boolean shouldRenderHealthBar(Mob m){
		return spiderSenses.getLevel() > 1;
	}
	
}
