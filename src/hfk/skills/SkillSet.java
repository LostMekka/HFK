/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.skills;

import hfk.Shot;
import hfk.game.GameController;
import hfk.items.weapons.CheatRifle;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;
import java.util.Arrays;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class SkillSet implements StatsModifier {
	
	private int superCount = 0, superMax = 1, levelCount = 0;
	private final Mob parent;
	private final LinkedList<Skill> skills = new LinkedList<>();
	private int totalXpSpent = 0;
	
	// special skills (need special handling)
	private Skill pistolVel, pistolAuto;
	private Skill shotgunDouble;
	private Skill machinegunRate;
	private Skill plasmaOverDmgSplash;
	private Skill grenadeRange, grenadeManual, grenadeSmart;
	private Skill spiderSenses;
	private Skill quickHandsReload;
	private Skill superMore;
	
	public SkillSet(Mob parent) {
		this.parent = parent;
		// TODO: init skills
		Skill s = new Skill(parent, 8, "health boost", "health is important! especially when you are hunted by an army of aliens! with each level of this skill you get +25 max hp.");
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setMaxHP((i+1)*25);
			s.mobStatsCards[i] = msc;
			s.costs[i] = 15 + 8*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 8, "explosive bullets", "all pistols deal extra fire damage.");
		s.weaponType = Weapon.WeaponType.pistol;
		for(int i=0; i<s.getMaxLevel(); i++){
			DamageCard dc = DamageCard.createBonus();
			dc.setDieCount(Damage.DamageType.fire.ordinal(), (i+1)/2);
			dc.setEyeCount(Damage.DamageType.fire.ordinal(), (i+2)/2);
			s.damageCards[i] = dc;
			s.costs[i] = i>0 ? s.costs[i-1] + 2*i+5 : 12;
		}
		skills.add(s);
		
		s = new Skill(parent, 8, "ricochet shells", "nothing says \"in your face\" more than a shotgun. you modified your shells so that pellets bounce off walls and travel further, which makes you the ultimate close-quarter badass.");
		s.weaponType = Weapon.WeaponType.shotgun;
		for(int i=0; i<s.getMaxLevel(); i++){
			WeaponStatsCard wsc = WeaponStatsCard.createBonus();
			wsc.shotBounces = i + 1;
			wsc.shotVel = 0.04f * (i+1);
			wsc.maxEnergyLossOnBounce = -0.1f * (i+1);
			s.weaponStatsCards[i] = wsc;
			s.costs[i] = i>0 ? s.costs[i-1] + 7 : 10;
		}
		skills.add(s);
		
		shotgunDouble = new Skill(parent, 2, "dual barrel", "enables the alternative fire of double barrel shotguns to shoot both shells at once. level 2 even works with all other shotguns!");
		shotgunDouble.costs[0] = 30;
		shotgunDouble.costs[1] = 40;
		skills.add(shotgunDouble);
		
		s = new Skill(parent, 8, "rapid fire", "the machinegun is your best friend. why someone would use any other weapons is beyond your comprehension... this skill raises the fire rate of all machineguns.");
		s.weaponType = Weapon.WeaponType.machinegun;
		for(int i=0; i<s.getMaxLevel(); i++){
			WeaponStatsCard wsc = WeaponStatsCard.createBonus();
			wsc.shotInterval = -0.05f*(i+2);
			wsc.burstInterval = -0.05f*(i+2);
			s.weaponStatsCards[i] = wsc;
			s.costs[i] = 17 + 9*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 5, "weapon juggler", "if surviving in a foreign universe has taught you one thing, it is that you need all the weapons you can get... at the same time! for each level of this skill you gain an extra weapon slot.");
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setQuickSlotCount(i+1);
			s.mobStatsCards[i] = msc;
			s.costs[i] = 25 + 8*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 6, "reloader", "you optimized the reload routine. thats nice! every level gives you a speed boost when reloading any weapon type.\n\"my mom always said: honey, every second is precious, especially when you need to shoot someone.\"");
		for(int i=0; i<s.getMaxLevel(); i++){
			WeaponStatsCard wsc = WeaponStatsCard.createBonus();
			wsc.reloadTimes = new float[Weapon.AMMO_TYPE_COUNT];
			Arrays.fill(wsc.reloadTimes, -0.05f*(i+2));
			s.weaponStatsCards[i] = wsc;
			s.costs[i] = 14 + 7*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 1, "double reloader", "reloading every single bullet by itself annoys you so much that you just squeeze in a second one with each reload step. \n\"who cares if it is breaking the weapon, i have no time!\"");
		s.weaponType = Weapon.WeaponType.singleReload;
		WeaponStatsCard wsc = WeaponStatsCard.createBonus();
		wsc.reloadCount = new int[Weapon.AmmoType.values().length];
		for(int i=0; i<wsc.reloadCount.length; i++) wsc.reloadCount[i] = 1;
		s.weaponStatsCards[0] = wsc;
		s.costs[0] = 32;
		skills.add(s);
		
		quickHandsReload = new Skill(parent, 1, "quick hands", "even in the heat of battle, when you switch your weapon that is not done reloading, you put the ammo to reload back in your bag instead of dropping it.");
		quickHandsReload.costs = new int[] { 14 };
		skills.add(quickHandsReload);
		
		grenadeSmart = new Skill(parent, 2, "smart grenades", "grenades are fun, especially when they dont kill you. for that reason you installed an a.i. module on your grenades that makes sure you stay safe. level 1 of this skill lets grenades bounce off walls instead of exploding on impact, while level 2 installs a friend/foe detection system to avoid friendly fire.");
		grenadeSmart.costs = new int[] { 15, 25 };
		skills.add(grenadeSmart);

		grenadeManual = new Skill(parent, 2, "grenade trigger", "grenades are detonated manually! so skill, many control, much kapow.");
		grenadeManual.costs = new int[] { 28, 38 };
		skills.add(grenadeManual);

		spiderSenses = new Skill(parent, 8, "spider senses", "you evolved to sense your surroundings and enemies from further away than you can see. how did you even do that??? the greater the level of this skill, the further you can sense stuff. if you are close enough, you can even sense reload status or health of an enemy!\n\n\"think of a number from one to a million...\"");
		for(int i=0; i<spiderSenses.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setBasicSenseRange(i+3);
			if(i > 0) msc.setReloadSenseRange(i+2);
			if(i > 1) msc.setHealthSenseRange(i);
			spiderSenses.mobStatsCards[i] = msc;
			spiderSenses.costs[i] = i>0 ? spiderSenses.costs[i-1] + 2*i+6 : 22;
		}
		skills.add(spiderSenses);
		
		s = new Skill(parent, 4, "hellrunner", "congratulations, you mastered the art of running away! each level of this skill makes you a bit faster.");
		float speedup = 0.1f;
		float totalspeed = speedup;
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setMaxSpeed(totalspeed);
			speedup *= 0.5f;
			totalspeed += speedup;
			s.mobStatsCards[i] = msc;
			s.costs[i] = 23 + 10*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 10, "mule", "+1 inventory slot, makes a bit slower");
		speedup = -0.05f;
		totalspeed = speedup;
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setInventorySize(i+1);
			msc.setMaxSpeed(totalspeed);
			speedup *= 0.5f;
			totalspeed += speedup;
			s.mobStatsCards[i] = msc;
			s.costs[i] = 25 + 9*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 3, "thick skin", "+1 resistance against physical and piercing damage per level");
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setResistance(Damage.DamageType.physical.ordinal(), i+1);
			msc.setResistance(Damage.DamageType.piercing.ordinal(), i+1);
			s.mobStatsCards[i] = msc;
			s.costs[i] = 25 + 10*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 3, "elemental armor", "+1 resistance against fire and ice damage per level");
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setResistance(Damage.DamageType.fire.ordinal(), i+1);
			msc.setResistance(Damage.DamageType.ice.ordinal(), i+1);
			s.mobStatsCards[i] = msc;
			s.costs[i] = 25 + 10*i;
		}
		skills.add(s);
		
		s = new Skill(parent, 3, "energy armor", "+1 resistance against shock and plasma damage per level");
		for(int i=0; i<s.getMaxLevel(); i++){
			MobStatsCard msc = MobStatsCard.createBonus();
			msc.setResistance(Damage.DamageType.shock.ordinal(), i+1);
			msc.setResistance(Damage.DamageType.plasma.ordinal(), i+1);
			s.mobStatsCards[i] = msc;
			s.costs[i] = 25 + 10*i;
		}
		skills.add(s);
		
		// TODO: set blocks and requirements
		grenadeManual.addBlock(grenadeSmart);
		grenadeSmart.addRequirement(1, spiderSenses, 2);
	}
	
	public void printSkillBalanceInfo(){
		float r = GameController.get().getSkillCostIncreaseRate();
		for(Skill s1 : skills){
			System.out.println(s1.name + "," + s1.getMaxLevel() + ":");
			System.out.print("    cost:");
			for(int i=0; i<s1.costs.length; i++){
				Math.round(s1.costs[i] * (1f + i*r));
				System.out.print(" " + (Math.round(s1.costs[i] * (1f + i*r))));
			}
			System.out.print("\n    total:");
			int txp = 0;
			for(int i=0; i<s1.costs.length; i++){
				txp += Math.round(s1.costs[i] * (1f + i*r));
				System.out.print(" " + txp);
			}
			System.out.println();
		}
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

	public int getTotalXpSpent() {
		return totalXpSpent;
	}
	
	public int getSkillLearnedCount() {
		return levelCount;
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
	
	public void skillLeveledUp(Skill changedSkill, int cost){
		if(changedSkill == superMore) superMax++;
		if(changedSkill.isSuperSkill && changedSkill.getLevel() == 1){
			superCount++;
			if(superCount > superMax) throw new RuntimeException("super skill limit exceeded");
		}
		if(changedSkill == spiderSenses) GameController.get().recalcVisibleTiles = true;
		totalXpSpent += cost;
		levelCount++;
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
		if(w.type.isSubTypeOf(Weapon.WeaponType.grenadeLauncher)){
			s.smartGrenadeLevel = Math.max(grenadeSmart.getLevel(), s.smartGrenadeLevel);
			s.manualDetonateLevel = Math.max(grenadeManual.getLevel(), s.manualDetonateLevel);
		}
		return s;
	}
	
	public boolean shouldRenderReloadBar(Mob m){
		return spiderSenses.getLevel() > 0;
	}
	
	public boolean shouldRenderHealthBar(Mob m){
		return spiderSenses.getLevel() > 1;
	}
	
	public boolean shouldKeepAmmoOnCancelReload(){
		return quickHandsReload.getLevel() > 0;
	}
	
	public boolean canAltFire(Weapon w){
		if(w instanceof CheatRifle) return true;
		if(w.type.isSubTypeOf(Weapon.WeaponType.zoomable)) return true;
		if(w.type.isSubTypeOf(Weapon.WeaponType.grenadeLauncher)) return grenadeManual.getLevel() > 0;
		if(w.type.isSubTypeOf(Weapon.WeaponType.shotgun)) switch(shotgunDouble.getLevel()){
			case 0: return false;
			case 1: return w.type.isSubTypeOf(Weapon.WeaponType.doubleBarrelShotgun);
			case 2: return true;
		}
		return true;
	}
	
}
