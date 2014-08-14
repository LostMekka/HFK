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
public class MobStatsCard {
	
	private final int[] resistances = new int[Damage.DAMAGE_TYPE_COUNT];
	private final int[] ammoSlotSizes = new int[Weapon.AMMO_TYPE_COUNT];
	private float maxSpeed;				// multiplied on applyBonus
	private float maxPickupRange;		// multiplied on applyBonus
	private float sightRange;			// multiplied on applyBonus
	private float hearRange;			// multiplied on applyBonus
	private float memoryTime;			// multiplied on applyBonus
	private float turnRate;
	private float visionAngle;
	private int maxHP;
	private int inventorySize;
	private int quickSlotCount;

	private boolean isBonusCard;
	
	public static MobStatsCard createNormal(){
		MobStatsCard ans = new MobStatsCard();
		ans.isBonusCard = false;
		ans.maxSpeed = 1f;
		ans.maxPickupRange = 1f;
		ans.sightRange = 4f;
		ans.hearRange = 4f;
		ans.maxHP = 100;
		ans.inventorySize = 10;
		ans.quickSlotCount = 2;
		ans.memoryTime = 5000;
		ans.visionAngle = (float)Math.PI / 4f;
		ans.turnRate = (float)Math.PI * 2f;
		return ans;
	}
	
	public static MobStatsCard createBonus(){
		MobStatsCard ans = new MobStatsCard();
		ans.isBonusCard = true;
		return ans;
	}
	
	private MobStatsCard() {}

	public void add(MobStatsCard c){
		if(!isBonusCard || !c.isBonusCard) throw new RuntimeException(
				"trying to add a " + 
				(c.isBonusCard ? "bonus" : "normal") + 
				" card to a " +
				(isBonusCard ? "bonus" : "normal") + 
				" card!");
		maxHP += c.maxHP;
		maxSpeed += c.maxSpeed;
		inventorySize += c.inventorySize;
		quickSlotCount += c.quickSlotCount;
		maxPickupRange += c.maxPickupRange;
		memoryTime += c.memoryTime;
		sightRange += c.sightRange;
		hearRange += c.hearRange;
		turnRate += c.turnRate;
		visionAngle += visionAngle;
		for(int i=0; i<resistances.length; i++) resistances[i] += c.resistances[i];
		for(int i=0; i<ammoSlotSizes.length; i++) ammoSlotSizes[i] += c.ammoSlotSizes[i];
	}
	
	public void applyBonus(MobStatsCard c){
		if(isBonusCard || !c.isBonusCard) throw new RuntimeException(
				"trying to apply a " + 
				(c.isBonusCard ? "bonus" : "normal") + 
				" card to a " +
				(isBonusCard ? "bonus" : "normal") + 
				" card!");
		// apply by multiplication
		maxSpeed *= 1f + c.maxSpeed;
		maxPickupRange *= 1f + c.maxPickupRange;
		memoryTime *= 1f + c.memoryTime;
		sightRange *= 1f + c.sightRange;
		hearRange *= 1f + c.hearRange;
		// apply by addition
		maxHP += c.maxHP;
		inventorySize += c.inventorySize;
		quickSlotCount += c.quickSlotCount;
		turnRate += c.turnRate;
		visionAngle += visionAngle;
		for(int i=0; i<resistances.length; i++) resistances[i] += c.resistances[i];
		for(int i=0; i<ammoSlotSizes.length; i++) ammoSlotSizes[i] += c.ammoSlotSizes[i];
	}
	
	@Override
	public MobStatsCard clone(){
		MobStatsCard ans = new MobStatsCard();
		ans.isBonusCard = isBonusCard;
		ans.maxHP = maxHP;
		ans.maxSpeed = maxSpeed;
		ans.inventorySize = inventorySize;
		ans.quickSlotCount = quickSlotCount;
		ans.maxPickupRange = maxPickupRange;
		ans.memoryTime = memoryTime;
		ans.sightRange = sightRange;
		ans.hearRange = hearRange;
		ans.turnRate = turnRate;
		ans.visionAngle = visionAngle;
		System.arraycopy(resistances, 0, ans.resistances, 0, Damage.DAMAGE_TYPE_COUNT);
		System.arraycopy(ammoSlotSizes, 0, ans.ammoSlotSizes, 0, Weapon.AMMO_TYPE_COUNT);
		return ans;
	}

	public float getTurnRate() {
		return turnRate;
	}

	public void setTurnRate(float turnRate) {
		this.turnRate = turnRate;
	}

	public float getVisionAngle() {
		return visionAngle;
	}

	public void setVisionAngle(float visionAngle) {
		this.visionAngle = visionAngle;
	}

	public int getResistance(int damageType) {
		return resistances[damageType];
	}

	public void setResistance(int damageType, int resistance) {
		this.resistances[damageType] = resistance;
	}

	public int getAmmoSlotSize(int ammoType) {
		return ammoSlotSizes[ammoType];
	}

	public void setAmmoSlotSize(int ammoType, int ammoSlotSize) {
		this.ammoSlotSizes[ammoType] = ammoSlotSize;
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public float getMaxPickupRange() {
		return maxPickupRange;
	}

	public void setMaxPickupRange(float maxPickupRange) {
		this.maxPickupRange = maxPickupRange;
	}

	public float getSightRange() {
		return sightRange;
	}

	public void setSightRange(float sightRange) {
		this.sightRange = sightRange;
	}

	public float getHearRange() {
		return hearRange;
	}

	public void setHearRange(float hearRange) {
		this.hearRange = hearRange;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}

	public int getMemoryTime() {
		return Math.round(memoryTime);
	}

	public void setMemoryTime(int memoryTime) {
		this.memoryTime = memoryTime;
	}

	public int getInventorySize() {
		return inventorySize;
	}

	public void setInventorySize(int inventorySize) {
		this.inventorySize = inventorySize;
	}

	public int getQuickSlotCount() {
		return quickSlotCount;
	}

	public void setQuickSlotCount(int quickSlotCount) {
		this.quickSlotCount = quickSlotCount;
	}

	public boolean isIsBonusCard() {
		return isBonusCard;
	}
	
}
