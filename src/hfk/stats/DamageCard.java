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
	private float areaRadius = 0f;			// multiplied on applyBonus
	private boolean isBonusCard = false;

	public static final DamageCard createNormal(){
		DamageCard ans = new DamageCard();
		ans.isBonusCard = false;
		Arrays.fill(ans.dieCount, 1);
		return ans;
	}
	
	public static final DamageCard createBonus(){
		DamageCard ans = new DamageCard();
		ans.isBonusCard = true;
		return ans;
	}
	
	private DamageCard() {}
	
	public void add(DamageCard c){
		if(!isBonusCard || !c.isBonusCard) throw new RuntimeException(
				"trying to add a " + 
				(c.isBonusCard ? "bonus" : "normal") + 
				" card to a " +
				(isBonusCard ? "bonus" : "normal") + 
				" card!");
		for(int t=0; t<dieCount.length; t++){
			dieCount[t] += c.dieCount[t];
			eyeCount[t] += c.eyeCount[t];
			dmgBonus[t] += c.dmgBonus[t];
		}
		areaRadius += c.areaRadius;
	}
	
	public void applyBonus(DamageCard c){
		if(isBonusCard || !c.isBonusCard) throw new RuntimeException(
				"trying to apply a " + 
				(c.isBonusCard ? "bonus" : "normal") + 
				" card to a " +
				(isBonusCard ? "bonus" : "normal") + 
				" card!");
		for(int t=0; t<dieCount.length; t++){
			dieCount[t] += c.dieCount[t];
			eyeCount[t] += c.eyeCount[t];
			dmgBonus[t] += c.dmgBonus[t];
		}
		areaRadius *= 1f + c.areaRadius;
	}
	
	public DamageCard clone(){
		DamageCard ans = new DamageCard();
		ans.isBonusCard = isBonusCard;
		ans.areaRadius = areaRadius;
		System.arraycopy(dieCount, 0, ans.dieCount, 0, dieCount.length);
		System.arraycopy(eyeCount, 0, ans.eyeCount, 0, eyeCount.length);
		System.arraycopy(dmgBonus, 0, ans.dmgBonus, 0, dmgBonus.length);
		return ans;
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

	public boolean isBonusCard() {
		return isBonusCard;
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

	public Damage createDamage(){
		Damage d = new Damage();
		d.setAreaRadius(areaRadius);
		for(int t=0; t<dieCount.length; t++) if(eyeCount[t] > 0){
			int sum = 0;
			for(int i=0; i<dieCount[t]; i++) sum += GameController.random.nextInt(eyeCount[t]) + 1;
			sum += Math.round(sum * dmgBonus[t]);
			d.setDmgPoints(t, sum);
		}
		return d;
	}
	
}
