/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.stats;


/**
 *
 * @author LostMekka
 */
public class Damage {
	
	public static enum DamageType{
		physical {
			@Override public String getShortID(){return "ph";}
		}, 
		piercing {
			@Override public String getShortID(){return "pr";}
		}, 
		fire {
			@Override public String getShortID(){return "f";}
		}, 
		ice {
			@Override public String getShortID(){return "i";}
		}, 
		shock {
			@Override public String getShortID(){return "s";}
		}, 
		plasma {
			@Override public String getShortID(){return "pl";}
		}, 
		mental {
			@Override public String getShortID(){return "m";}
		},
		;
		public abstract String getShortID();
	}
	public static final int DAMAGE_TYPE_COUNT = DamageType.values().length;
	
	private final int[] dmgPoints = new int[DAMAGE_TYPE_COUNT];
	private float areaRadius = 0;

	public int getDmgPoints(int type) {
		return dmgPoints[type];
	}

	public void setDmgPoints(int type, int dmgPoints) {
		this.dmgPoints[type] = dmgPoints;
	}

	public float getAreaRadius() {
		return areaRadius;
	}

	public void setAreaRadius(float areaRadius) {
		this.areaRadius = areaRadius;
	}
	
	public int calcFinalDamage(){
		int d = 0;
		for(int i=0; i<DAMAGE_TYPE_COUNT; i++) d += dmgPoints[i];
		return d;
	}
	
	public int calcFinalDamage(MobStatsCard res){
		int d = 0;
		for(int i=0; i<DAMAGE_TYPE_COUNT; i++){
			int di = dmgPoints[i] - res.getResistance(i);
			if(di > 0) d += di;
		}
		return d;
	}
	
}
