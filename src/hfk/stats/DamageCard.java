/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.stats;

import hfk.game.GameController;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author LostMekka
 */
public class DamageCard {
	
	private final int[] dieCount = new int[Damage.DAMAGE_TYPE_COUNT];
	private final int[] eyeCount = new int[Damage.DAMAGE_TYPE_COUNT];
	private final float[] dmgBonus = new float[Damage.DAMAGE_TYPE_COUNT];
	private float areaRadius = 0f;
	private boolean isUnique = false;

	public DamageCard() {}
	public DamageCard(int defaultEyeCount) {
		Arrays.fill(eyeCount, 1);
	}

	public int getDieCount(int type) {
		return dieCount[type];
	}

	public void setDieCount(int type, int dieCount) {
		this.dieCount[type] = dieCount;
	}

	public int getEyeCount(int type) {
		return eyeCount[type];
	}

	public void setEyeCount(int type, int eyeCount) {
		this.eyeCount[type] = eyeCount;
	}

	public float getDmgBonus(int type) {
		return dmgBonus[type];
	}

	public void setDmgBonus(int type, float dmgBonus) {
		this.dmgBonus[type] = dmgBonus;
	}

	public float getAreaRadius() {
		return areaRadius;
	}

	public void setAreaRadius(float areaRadius) {
		this.areaRadius = areaRadius;
	}

	public boolean isUnique() {
		return isUnique;
	}
	
	public boolean doesDamage(int type){
		return eyeCount[type] > 0 && dieCount[type] > 0;
	}
	
	public String getShortDamageString(int type, boolean forceDisplayBonus){
		String s = String.format("%dD%d", dieCount[type], eyeCount[type]);
		float b = dmgBonus[type];
		if(forceDisplayBonus || b > 0f){
			if(b >= 0f) s += "+";
			s += (int)(b*100f) + "%";
		}
		s += ":" + Damage.DamageType.values()[type].getShortID();
		return s;
	}

	public String getLongDamageString(int type, boolean forceDisplayBonus){
		String s = String.format("%dD%d", dieCount[type], eyeCount[type]);
		float b = dmgBonus[type];
		if(forceDisplayBonus || b > 0f){
			if(b >= 0f) s += "+";
			s += (int)(b*100f) + "%";
		}
		s += " as " + Damage.DamageType.values()[type];
		return s;
	}

	public DamageCard cloneUnique(){
		DamageCard udc = new DamageCard();
		udc.areaRadius = areaRadius;
		System.arraycopy(dieCount, 0, udc.dieCount, 0, dieCount.length);
		System.arraycopy(eyeCount, 0, udc.eyeCount, 0, eyeCount.length);
		System.arraycopy(dmgBonus, 0, udc.dmgBonus, 0, dmgBonus.length);
		udc.isUnique = true;
		return udc;
	}

	public void add(DamageCard augCard){
		for(int t=0; t<dieCount.length; t++){
			dieCount[t] += augCard.dieCount[t];
			eyeCount[t] += augCard.eyeCount[t];
			dmgBonus[t] += augCard.dmgBonus[t];
		}
		areaRadius += augCard.areaRadius;
	}
	
	public Damage createDamage(){
		return createDamageInternal(dieCount, eyeCount, dmgBonus, areaRadius);
	}
	
	public Damage createDamage(Collection<DamageCard> augmentations){
		int[] dice = new int[Damage.DAMAGE_TYPE_COUNT];
		int[] eyes = new int[Damage.DAMAGE_TYPE_COUNT];
		float[] boni = new float[Damage.DAMAGE_TYPE_COUNT];
		for(int t=0; t<dice.length; t++){
			dice[t] = dieCount[t];
			eyes[t] = eyeCount[t];
			boni[t] = dmgBonus[t];
		}
		float area = areaRadius;
		for(DamageCard a : augmentations){
			for(int t=0; t<dice.length; t++){
				dice[t] += a.dieCount[t];
				eyes[t] += a.eyeCount[t];
				boni[t] += a.dmgBonus[t];
			}
			area += a.areaRadius;
		}
		return createDamageInternal(dice, eyes, boni, area);
	}
	
	private Damage createDamageInternal(int[] dice, int[] eyes, float[] boni, float area){
		Damage d = new Damage();
		d.setAreaRadius(area);
		for(int t=0; t<dice.length; t++){
			int sum = 0;
			for(int i=0; i<dice[t]; i++) sum += GameController.random.nextInt(eyes[t]) + 1;
			d.setDmgPoints(t, Math.round(sum * (1 + boni[t])));
		}
		return d;
	}
	
}
