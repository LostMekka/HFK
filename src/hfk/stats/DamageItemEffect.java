/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.stats;

import hfk.game.GameController;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class DamageItemEffect extends ItemEffect {

	private static final int BASE_RARITY = 1000;
	
	public static DamageItemEffect createDIE(long maxRarity, boolean[] usedTypes){
		DamageItemEffect die = createRandomSingle(usedTypes);
		while(die.r < maxRarity){
			DamageItemEffect die2 = createRandomSingle(usedTypes);
			if(die2.r + die.r > maxRarity){
				break;
			} else {
				die.add(die2);
			}
		}
		return die;
	}

	public static DamageItemEffect create(Damage.DamageType t, int dice, int eyes, float bonus){
		DamageItemEffect die = new DamageItemEffect();
		die.dc.setDieCount(t.ordinal(), dice);
		die.dc.setEyeCount(t.ordinal(), eyes);
		die.dc.setDmgBonus(t.ordinal(), bonus);
		die.r = Math.round(BASE_RARITY * (dice + eyes + bonus / 0.05f));
		return die;
	}
	
	public static DamageItemEffect createRandomSingle() {
		DamageItemEffect die = new DamageItemEffect();
		int t = GameController.random.nextInt(Damage.DAMAGE_TYPE_COUNT);
		switch(GameController.random.nextInt(3)){
			case 0: die.dc.setDieCount(t, 1); break;
			case 1: die.dc.setEyeCount(t, 1); break;
			case 2: die.dc.setDmgBonus(t, 0.05f); break;
		}
		die.r = BASE_RARITY;
		return die;
	}

	public static DamageItemEffect createRandomSingle(boolean[] usedTypes) {
		int r = 0;
		for(int i=0; i<Damage.DAMAGE_TYPE_COUNT; i++) if(usedTypes[i]) r++;
		if(r == 0) throw new IllegalArgumentException("must allow at least one damage type");
		int t = GameController.random.nextInt(r);
		for(int i=0; i<Damage.DAMAGE_TYPE_COUNT; i++) if(!usedTypes[i]) t++;
		DamageItemEffect die = new DamageItemEffect();
		switch(GameController.random.nextInt(3)){
			case 0: die.dc.setDieCount(t, 1); break;
			case 1: die.dc.setEyeCount(t, 1); break;
			case 2: die.dc.setDmgBonus(t, 0.05f); break;
		}
		die.r = BASE_RARITY;
		return die;
	}

	private long r = 1;
	private String[] display = null;
	
	private DamageItemEffect() {}
	
	@Override
	public String[] getDisplayStrings() {
		if(display == null){
			// create display string
			LinkedList<String> lines = new LinkedList<>();
			for(int i=0; i<Damage.DAMAGE_TYPE_COUNT; i++){
				int d = dc.getDieCount(i);
				int e = dc.getEyeCount(i);
				float b = dc.getDmgBonus(i);
				if(d != 0 || e != 0 || b != 0f){
					String l = "";
					if(d != 0 || e != 0){
						if(d >= 0) l += "+";
						l += d + "D";
						if(e >= 0) l += "+";
						l += e + " ";
					}
					if(b != 0){
						if(b > 0) l += "+";
						l += Math.round(b * 100f) + "% ";
					}
					l += Damage.DamageType.values()[i].name();
					lines.add(l);
				}
			}
			display = (String[])lines.toArray();
		}
		return display;
	}

	@Override
	public long getRarity() {
		return r;
	}
	
	public void add(DamageItemEffect die){
		dc.add(die.dc);
		r += die.r;
		display = null;
	}
	
}
