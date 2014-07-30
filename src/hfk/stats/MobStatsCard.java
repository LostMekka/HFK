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
	private float maxSpeed, maxPickupRange, sightRange, hearRange;
	private int maxHP, inventorySize, quickSlotCount, memoryTime;

	public MobStatsCard() {
		this(false);
	}

	public MobStatsCard(boolean empty) {
		if(empty) return;
		maxSpeed = 1f;
		maxPickupRange = 1f;
		sightRange = 4f;
		hearRange = 4f;
		maxHP = 100;
		inventorySize = 10;
		quickSlotCount = 2;
		memoryTime = 5000;
	}

	public float getHearRange() {
		return hearRange;
	}

	public void setHearRange(float hearRange) {
		this.hearRange = hearRange;
	}

	public float getSightRange() {
		return sightRange;
	}

	public void setSightRange(float sightRange) {
		this.sightRange = sightRange;
	}

	public int getMemoryTime() {
		return memoryTime;
	}

	public void setMemoryTime(int memoryTime) {
		this.memoryTime = memoryTime;
	}

	public float getMaxPickupRange() {
		return maxPickupRange;
	}

	public void setMaxPickupRange(float maxPickupRange) {
		this.maxPickupRange = maxPickupRange;
	}

	public int getQuickSlotCount() {
		return quickSlotCount;
	}

	public void setQuickSlotCount(int quickSlotCount) {
		this.quickSlotCount = quickSlotCount;
	}
	
	public int getResistance(int type){
		return resistances[type];
	}
	
	public void setResistance(int type, int resistance){
		resistances[type] = resistance;
	}

	public int getAmmoSlotSize(int type){
		return ammoSlotSizes[type];
	}
	
	public void setAmmoSlotSize(int type, int ammoSlotSize){
		ammoSlotSizes[type] = ammoSlotSize;
	}

	public int getInventorySize() {
		return inventorySize;
	}

	public void setInventorySize(int inventorySize) {
		this.inventorySize = inventorySize;
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(float maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public int getMaxHP() {
		return maxHP;
	}

	public void setMaxHP(int maxHP) {
		this.maxHP = maxHP;
	}
	
	public void add(MobStatsCard c){
		maxHP += c.maxHP;
		maxSpeed += c.maxSpeed;
		inventorySize += c.inventorySize;
		quickSlotCount += c.quickSlotCount;
		maxPickupRange += c.maxPickupRange;
		memoryTime += c.memoryTime;
		sightRange = c.sightRange;
		for(int i=0; i<resistances.length; i++) resistances[i] += c.resistances[i];
		for(int i=0; i<ammoSlotSizes.length; i++) ammoSlotSizes[i] += c.ammoSlotSizes[i];
	}
	
	@Override
	public MobStatsCard clone(){
		MobStatsCard ans = new MobStatsCard(true);
		ans.maxHP = maxHP;
		ans.maxSpeed = maxSpeed;
		ans.inventorySize = inventorySize;
		ans.quickSlotCount = quickSlotCount;
		ans.maxPickupRange = maxPickupRange;
		ans.memoryTime = memoryTime;
		ans.sightRange = sightRange;
		System.arraycopy(resistances, 0, ans.resistances, 0, Damage.DAMAGE_TYPE_COUNT);
		System.arraycopy(ammoSlotSizes, 0, ans.ammoSlotSizes, 0, Weapon.AMMO_TYPE_COUNT);
		return ans;
	}
	
}
