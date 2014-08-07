/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.items.weapons;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.Resources;
import hfk.items.InventoryItem;
import hfk.mobs.Mob;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.WeaponStatsCard;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;

/**
 *
 * @author LostMekka
 */
public abstract class Weapon extends InventoryItem {

	public static enum WeaponType{
		pistol,
		machinegun,
		shotgun,
		plasmaWeapon{
			@Override
			public String toString() {
				return "plasma weapon";
			}},
		sniperRifle{
			@Override
			public String toString() {
				return "sniper rifle";
			}},
		rocketLauncher{
			@Override
			public String toString() {
				return "rocket launcher";
			}},
		grenadeLauncher{
			@Override
			public String toString() {
				return "grenade launcher";
			}},
	}
	
	public static enum AmmoType {
		bullet {
			@Override public String getShortID(){ return "b"; }
		}, 
		shell {
			@Override public String getShortID(){ return "sh"; }
		}, 
		plasmaRound {
			@Override public String getShortID(){ return "p"; }
			@Override public String toString() { return "plasma round"; }
		}, 
		sniperRound {
			@Override public String getShortID(){ return "sr"; }
			@Override public String toString() { return "sniper round"; }
		}, 
		grenade {
			@Override public String getShortID(){ return "g"; }
		}, 
		rocket {
			@Override public String getShortID(){ return "r"; }
		}, 
		energy {
			@Override public String getShortID(){ return "e"; }
		}, 
		;
		public abstract String getShortID();
	}
	public static final int AMMO_TYPE_COUNT = AmmoType.values().length;
	
	public enum WeaponState { ready, cooldownShot, cooldownBurst, cooldownReload }
	
	public WeaponType type;
	public Mob bionicParent = null;
	public float currentScatter;
	public float weaponLength = 0.5f, lengthOffset = 0.3f;
	public Image img, flippedImg;
	public Sound shotSound = null, burstSound = null;
	public WeaponStatsCard basicStats, totalStats;
	public DamageCard basicDamageCard, totalDamageCard;
	public Shot.Team shotTeam = Shot.Team.dontcare;
	public Sound[] reloadStartSound = new Sound[AMMO_TYPE_COUNT];
	public Sound[] reloadEndSound = new Sound[AMMO_TYPE_COUNT];
	public Color displayColor = Color.yellow;
	
	private final int[] clips = new int[AMMO_TYPE_COUNT];
	private final float[] ammoRegenCounter = new float[AMMO_TYPE_COUNT];
	private WeaponState state = WeaponState.ready;
	private int clipToReload = -1, reloadAmount = 0, timer = 0, timerStart = 1, burstShotCount = 0;
	private boolean reloadAll = true;

	public abstract Shot initShot(Shot s);
	public abstract WeaponStatsCard getDefaultWeaponStats();
	public abstract DamageCard getDefaultDamageCard();
	public abstract String getWeaponName();
	
	public float getScreenShakeAmount(){ return 0.1f; };
	public float getScreenRecoilAmount(){ return 0.2f; };
	
	public Weapon(float angle, PointF position) {
		super(position);
		this.angle = angle;
		basicStats = getDefaultWeaponStats();
		totalStats = basicStats.clone();
		currentScatter = totalStats.minScatter;
		System.arraycopy(totalStats.clipSize, 0, clips, 0, AMMO_TYPE_COUNT);
		basicDamageCard = getDefaultDamageCard();
		totalDamageCard = basicDamageCard.clone();
		reloadStartSound[AmmoType.plasmaRound.ordinal()] = Resources.getSound("reload_s_pr.wav");
		reloadEndSound[AmmoType.plasmaRound.ordinal()] = Resources.getSound("reload_e_pr.wav");
		destroyWhenUsed = false;
	}
	
	public void setImg(String ref){
		img = Resources.getImage(ref);
		flippedImg = Resources.getImage(ref, true);
	}
	
	private void setState(int timer, WeaponState s){
		this.timer = timer;
		timerStart = timer;
		state = s;
	}
	
	private void setReady(){
		state = WeaponState.ready;
		timer = 0;
		timerStart = 1;
		clipToReload = -1;
		reloadAmount = 0;
		burstShotCount = 0;
		reloadAll = false;
	}

	@Override
	public Color getDisplayColor() {
		return displayColor;
	}
	
	@Override
	public String getDisplayName(){
		return String.format("%s %s %s", getWeaponName(), getShortAmmoString(false), getShortDamageString());
	}
	
	public String getShortAmmoString(boolean includeInventory){
		String s = "";
		boolean first = true;
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			if(totalStats.clipSize[i] > 0){
				if(first){
					first = false;
				} else {
					s += " ";
				}
				s += getShortAmmoString(i, includeInventory);
			}
		}
		return s;
	}
	
	public String getShortAmmoString(int ammoType, boolean includeInventory){
		if(totalStats.clipSize[ammoType] > 0){
			String ans = AmmoType.values()[ammoType].getShortID() + "(" + 
					clips[ammoType] + "/" + totalStats.clipSize[ammoType];
			if(includeInventory && parentInventory != null && ammoType != AmmoType.energy.ordinal()){
				ans += "/" + parentInventory.getAmmoCount(AmmoType.values()[ammoType]);
			}
			ans += ")";
			return ans;
		} else {
			return null;
		}
	}
	
	public String getAmmoString(int ammoType, boolean includeInventory){
		if(totalStats.clipSize[ammoType] > 0){
			String ans = AmmoType.values()[ammoType].toString() + ": (" + 
					clips[ammoType] + "/" + totalStats.clipSize[ammoType];
			if(includeInventory && parentInventory != null && ammoType != AmmoType.energy.ordinal()){
				ans += "/" + parentInventory.getAmmoCount(AmmoType.values()[ammoType]);
			}
			ans += ")";
			return ans;
		} else {
			return null;
		}
	}
	
	public String getShortDamageString(){
		String s = "";
		boolean first = true;
		for(int i=0; i<Damage.DAMAGE_TYPE_COUNT; i++){
			if(totalDamageCard.doesDamage(i)){
				if(first){
					first = false;
				} else {
					s += ", ";
				}
				s += totalDamageCard.getShortDamageString(i, false);
			}
		}
		return s;
	}

	@Override
	public boolean use(Mob m, boolean fromInventory) {
		if(fromInventory){
			return m.inventory.equipWeaponFromInventory(this);
		} else {
			return m.inventory.equipWeaponFromGround(this);
		}
	}

	public boolean isBionic(){
		return bionicParent != null;
	}
	
	public boolean isReloading(){
		return state == WeaponState.cooldownReload;
	}
	
	public float getProgress(){
		if(state == WeaponState.ready) return 0f;
		return (float)(timerStart - timer) / (float) timerStart;
	}
	
	public int getAmmoCount(AmmoType t){
		return clips[t.ordinal()];
	}
	
	public void aimAt(PointF target) {
		angle = (float)Math.atan2(target.y - pos.y, target.x - pos.x);
	}
	
	public boolean canFire(){
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			int a = Math.round(totalStats.ammoPerBurst[i]) + totalStats.shotsPerBurst * Math.round(totalStats.ammoPerShot[i]);
			if(clips[i] < a) return false;
		}
		return isReady();
	}
	
	public boolean isReady(){
		return (state == WeaponState.ready);
	}
	
	public boolean reload(){
		if(this instanceof SniperRifle){
			System.out.println("");
		}
		if(!isReady()) return false;
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			if(canReload(i)){
				reloadAll = true;
				reloadInternal(i);
				return true;
			}
		}
		return false;
	}
	
	public boolean reload(AmmoType t){
		if(!isReady()) return false;
		int i = t.ordinal();
		if(canReload(i)){
			reloadInternal(t);
			return true;
		}
		return false;
	}
	
	private void reloadInternal(int i){
		reloadInternalBOTHVALUES(i, AmmoType.values()[i]);
	}
	
	private void reloadInternal(AmmoType t){
		reloadInternalBOTHVALUES(t.ordinal(), t);
	}
	
	private void reloadInternalBOTHVALUES(int i, AmmoType t){
		clipToReload = i;
		reloadAmount = Math.round(Math.min(totalStats.clipSize[i] - clips[i], totalStats.reloadCount[i]));
		if(!isBionic() && parentInventory != null){
			reloadAmount = Math.min(reloadAmount, parentInventory.getAmmoCount(t));
			parentInventory.removeAmmo(AmmoType.values()[i], reloadAmount);
		}
		setState(Math.round(totalStats.reloadTimes[i]), WeaponState.cooldownReload);
		Sound sound = reloadStartSound[i];
		if(sound != null) GameController.get().playSoundAt(sound, pos);
	}
	
	public boolean mustReload(){
		return mustReload(true, true);
	}
	
	public boolean mustReload(boolean ignoreRegenerating, boolean ignoreUnreloadable){
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			if(ignoreRegenerating && totalStats.ammoRegenerationRates[i] > 0) continue;
			if(ignoreUnreloadable && totalStats.reloadCount[i] <= 0) continue;
			if(clips[i] < totalStats.ammoPerBurst[i] + totalStats.shotsPerBurst * totalStats.ammoPerShot[i]) return true;
		}
		return false;
	}
	
	public boolean canReload(){
		if(!isReady()) return false;
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			if(i == AmmoType.energy.ordinal()) continue;
			if(canReload(i)) return true;
		}
		return false;
	}
	
	private boolean canReload(int i){
		return (totalStats.reloadCount[i] > 0 && clips[i] < totalStats.clipSize[i]
				&& (parentInventory == null || parentInventory.hasAmmo(AmmoType.values()[i])));
	}
	
	public void pullAlternativeTrigger(){
		Mob m = getParentMob();
		if(m == null || m.skills.canAltFire(this)) pullAlternativeTriggerInternal();
	}
	
	public void pullAlternativeTriggerInternal(){
		if(type == WeaponType.grenadeLauncher){
			boolean foundLevel2Shot = false;
			for(Shot shot : GameController.get().shots){
				if(!GameController.get().shotsToRemove.contains(shot) && shot.parent == this){
					if(shot.manualDetonateLevel == 1) shot.hit();
					if(shot.manualDetonateLevel == 2 && ! foundLevel2Shot){
						shot.hit();
						foundLevel2Shot = true;
					}
				}
			}
		}
	}
	
	public boolean pullTrigger(){
		if(canFire()){
			burstShotCount = totalStats.shotsPerBurst;
			for(int i=0; i<AMMO_TYPE_COUNT; i++) clips[i] -= totalStats.ammoPerBurst[i];
			if(burstSound != null) burstSound.play();
			shootInternal();
			return true;
		}
		return false;
	}
	
	public float getScatteredAngle(){
		return angle + currentScatter / 180f * (float)Math.PI * (GameController.random.nextFloat() - 0.5f);
	}
	
	public void shootInternal(){
		shootInternal(true);
	}
	
	public void shootInternal(boolean playSound){
		if(getParentMob() == GameController.get().player){
			GameController.get().cameraShake(getScreenShakeAmount());
			GameController.get().cameraRecoil(angle + (float)Math.PI, getScreenRecoilAmount());
		}
		burstShotCount--;
		for(int i=0; i<AMMO_TYPE_COUNT; i++) clips[i] -= totalStats.ammoPerShot[i];
		if(playSound && shotSound != null) GameController.get().playSoundAt(shotSound, pos);
		for(int i=0; i<totalStats.projectilesPerShot; i++){
			Shot s = new Shot(this, null, null, 0.1f);
			s = initShot(s);
			s.dmg = totalDamageCard.createDamage();
			s.team = shotTeam;
			s.isGrenade = type == WeaponType.grenadeLauncher;
			s.bounceCount = totalStats.shotBounces;
			s.parent = this;
			Mob m = getParentMob();
			if(m != null) s = m.skills.modifyShot(s, this, m);
			GameController.get().shots.add(s);
		}
		if(burstShotCount > 0){
			setState(totalStats.shotInterval, WeaponState.cooldownShot);
		} else {
			setState(mustReload() ? 0 : totalStats.burstInterval, WeaponState.cooldownBurst);
		}
		currentScatter = Math.min(0, Math.max(totalStats.maxScatter, currentScatter + totalStats.scatterPerShot));
	}
	
	public Mob getParentMob(){
		Mob m = null;
		if(bionicParent != null){
			m = bionicParent;
		} else if(parentInventory != null) m = parentInventory.getParent();
		return (m != null && m.isAlive()) ? m : null;
	}
	
	@Override
	public void update(int time){
		if(getParentMob() == null){
			// fancy weapon updates are only done when the weapon is in an inventory or it is a bionic weapon
			super.update(time);
			return;
		}
		currentScatter -= time / 1000f * totalStats.scatterCoolRate;
		if(currentScatter < totalStats.minScatter) currentScatter = totalStats.minScatter;
		if(currentScatter > totalStats.maxScatter) currentScatter = totalStats.maxScatter;
		// regenerate ammo
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			float rate = totalStats.ammoRegenerationRates[i];
			if(rate != 0f && (state != WeaponState.cooldownReload || clipToReload != i)){
				ammoRegenCounter[i] += rate * time / 1000f;
				int a = (int)ammoRegenCounter[i];
				ammoRegenCounter[i] -= a;
				clips[i] += a;
				if(clips[i] > totalStats.clipSize[i]) clips[i] = Math.round(totalStats.clipSize[i]);
			}
		}
		// from here on: cooldown stuff, only when not ready!
		if(state == WeaponState.ready) return;
		timer -= time;
		if(timer <= 0){
			switch(state){
				case cooldownReload:
					clips[clipToReload] += reloadAmount;
					Sound sound = reloadEndSound[clipToReload];
					if(sound != null) GameController.get().playSoundAt(sound, pos);
					if(reloadAll){
						// try to reload other clips as well
						int t = -1;
						for(int i=clipToReload+1; i<AMMO_TYPE_COUNT; i++){
							if(canReload(i)){
								t = i;
								break;
							}
						}
						if(t != -1){
							reloadInternal(t);
							break;
						}
					}
					// nothing more to reload
					setReady();
					break;
				case cooldownShot:
					if(burstShotCount == 0) throw new RuntimeException("we are in the wrong state here!");
					shootInternal();
					break;
				case cooldownBurst:
					setReady();
					break;
			}
		}
	}
	
	@Override
	public void render(){
		PointF p = pos.clone();
		p.x += lengthOffset * Math.cos(angle);
		p.y += lengthOffset * Math.sin(angle);
		if(Math.abs(angle) > Math.PI/2){
			GameController.get().renderer.drawImage(flippedImg, p, 1f, angle);
		} else {
			GameController.get().renderer.drawImage(img, p, 1f, angle);
		}
	}
	
	public void renderInformation(int x, int y, boolean colored){
		GameRenderer r = GameController.get().renderer;
		Color c = colored ? getDisplayColor() : GameRenderer.COLOR_TEXT_NORMAL;
		r.drawStringOnScreen("weapon : " + getWeaponName(), x, y, c, 1f);
		y += GameRenderer.MIN_TEXT_HEIGHT;
		DamageCard dc = totalDamageCard;
		x += 25;
		int l = 0;
		for(int i=0; i<Damage.DAMAGE_TYPE_COUNT; i++){
			if(colored) c = totalDamageCard.doesDamage(i) ? GameRenderer.COLOR_TEXT_NORMAL : GameRenderer.COLOR_TEXT_INACTIVE;
			String s = dc.getLongDamageString(i, true);
			int projectiles = totalStats.projectilesPerShot;
			if(projectiles > 1) s = "" + projectiles + " x " + s;
			l = Math.max(l, r.getStringWidth(s));
			r.drawStringOnScreen(s, x, y+i*GameRenderer.MIN_TEXT_HEIGHT, c, 1f);
		}
		x += l + 35;
		if(colored){
			if(isReady()){
				c = Color.green;
				if(canReload()) c = GameRenderer.COLOR_TEXT_NORMAL;
				if(mustReload(false, false)) c = Color.red;
			} else {
				c = GameRenderer.COLOR_TEXT_INACTIVE;
			}
		}
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
			String s = getAmmoString(i, true);
			if(s == null) continue;
			r.drawStringOnScreen(s, x, y, c, 1f);
			y += GameRenderer.MIN_TEXT_HEIGHT;
		}
	}
	
	public float getScatter(){
		return (currentScatter - totalStats.minScatter) / (totalStats.maxScatter - totalStats.minScatter);
	}
	
	public void resetStats(){
		totalDamageCard = basicDamageCard.clone();
		totalStats = basicStats.clone();
	}
	
	public void recalculateStats(){
		resetStats();
		Mob p = getParentMob();
		if(p == null) return;
		// calculate damage card
		DamageCard dcEffect = DamageCard.createBonus();
		p.addDamageCardEffects(dcEffect, this, p);
		totalDamageCard.applyBonus(dcEffect);
		// calculate weapon stats card
		WeaponStatsCard wscEffect = WeaponStatsCard.createBonus();
		p.addWeaponStatsCardEffects(wscEffect, this, p);
		totalStats.applyBonus(wscEffect);
	}
	
}
