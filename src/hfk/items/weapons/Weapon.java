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
import hfk.items.AmmoItem;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.ItemType;
import hfk.mobs.Mob;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.ItemEffect;
import hfk.stats.WeaponStatsCard;
import java.util.ArrayList;
import java.util.Arrays;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;

/**
 *
 * @author LostMekka
 */
public abstract class Weapon extends InventoryItem {

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
	
	public enum WeaponState { ready, cooldownShot, cooldownBurst, reloading, unloading }
	
	public Mob bionicParent = null;
	public float currentScatter;
	public float weaponLength = 0.5f, lengthOffset = 0.3f;
	public Image img, flippedImg;
	public Sound shotSound = null, burstSound = null;
	public WeaponStatsCard basicStats, totalStats;
	public DamageCard basicDamageCard, totalDamageCard;
	public Shot.Team shotTeam = Shot.Team.dontcare;
	public Sound reloadStartSound = null;
	public Sound reloadEndSound = null;
	public Color displayColor = Color.yellow;
	public ItemEffect zoomEffect = null;
	
	private final int[] clips = new int[AMMO_TYPE_COUNT];
	private final float[] ammoRegenCounter = new float[AMMO_TYPE_COUNT];
	private WeaponState state = WeaponState.ready;
	private int timer = 0, timerStart = 1, burstShotCount = 0, burstTimeBetweenShots = -1;
	private boolean zoom = false;
	private int[] reloadAmounts = new int[AMMO_TYPE_COUNT];
	private Inventory loadTarget = null;
	

	public abstract Shot initShot(Shot s);
	public abstract WeaponStatsCard getDefaultWeaponStats();
	public abstract DamageCard getDefaultDamageCard();
	public abstract String getWeaponName();
	
	public float getScreenShakeAmount(){ return 0.1f; };
	public float getScreenRecoilAmount(){ return 0.2f; };
	
	public void weaponSelected(){}
	
	public void weaponUnSelected(){
		Mob m = getParentMob();
		// cancel reloading
		if(state == WeaponState.reloading){
			for(AmmoType t : AmmoType.values()){
				int ti = t.ordinal();
				if(t == AmmoType.energy || reloadAmounts[ti] <= 0) continue;
				InventoryItem i = new AmmoItem(pos.clone(), t, reloadAmounts[ti]);
				if(m != null && m.skills.shouldKeepAmmoOnCancelReload()) i = m.inventory.addItem(i);
				if(i != null) GameController.get().dropItem(i, null, false);
			}
			setReady();
		}
		// do weapon specific stuff
		if(type.isSubTypeOf(ItemType.wZoomable)){
			if(m != null && zoom){
				zoom = false;
				effects.remove(zoomEffect);
				m.recalculateCards();
				if(m == GameController.get().player) GameController.get().recalcVisibleTiles = true;
			}
		}
	}
	
	public Weapon(float angle, PointF position) {
		super(position);
		this.angle = angle;
		basicStats = getDefaultWeaponStats();
		totalStats = basicStats.clone();
		currentScatter = totalStats.minScatter;
		System.arraycopy(totalStats.clipSize, 0, clips, 0, AMMO_TYPE_COUNT);
		basicDamageCard = getDefaultDamageCard();
		totalDamageCard = basicDamageCard.clone();
		destroyWhenUsed = false;
	}
	
	public void setImg(String ref){
		img = Resources.getImage(ref);
		flippedImg = Resources.getImage(ref, true);
	}
	
	private void setState(int timer, WeaponState s, boolean absolute){
		this.timer = absolute ? timer : (timer + this.timer);
		timerStart = this.timer;
		state = s;
	}
	
	private void setReady(){
		state = WeaponState.ready;
		timer = 0;
		timerStart = 1;
		burstShotCount = 0;
		Arrays.fill(reloadAmounts, 0);
		loadTarget = null;
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
		return state == WeaponState.reloading;
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
		return canFire(totalStats.shotsPerBurst);
	}
	
	private boolean canFire(int shotsPerBurst){
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			int a = Math.round(totalStats.ammoPerBurst[i]) + shotsPerBurst * Math.round(totalStats.ammoPerShot[i]);
			if(clips[i] < a) return false;
		}
		return isReady();
	}
	
	public boolean isReady(){
		return (state == WeaponState.ready);
	}
	
	public boolean unloadToGround(){
		return unloadToInventory(null);
	}

	public boolean unloadToInventory(Inventory inv){
		if(!isReady()) return false;
		loadTarget = inv;
		float time = 0;
		int sum = 0;
		for(AmmoType t : AmmoType.values()){
			if(t == AmmoType.energy) continue;
			int i = t.ordinal();
			int amount = clips[i];
			if(loadTarget != null){
				int room = loadTarget.getAmmoRoomLeft(t);
				if(amount > room) amount = room;
			}
			time += totalStats.reloadTimes[i];
			sum += amount;
		}
		// only start unload if there is room and something to reload
		if(sum <= 0) return false;
		setState(Math.round(time), WeaponState.unloading, true);
		return true;
	}
	
	public void stopUnloading(){
		if(state == WeaponState.unloading) setReady();
	}
	
	private void finishUnloading(){
		for(AmmoType t : AmmoType.values()){
			if(t == AmmoType.energy) continue;
			int i = t.ordinal();
			if(loadTarget == null){
				AmmoItem a = new AmmoItem(pos.clone(), t, clips[i]);
				GameController.get().dropItem(a, null, false);
				clips[i] = 0;
			} else {
				clips[i] = loadTarget.addAmmo(t, clips[i]);
			}
		}
		// TODO: do this via listeners or something more graceful
		if(loadTarget != null) GameController.get().inventorySubState.populateInventoryList();
		setReady();
		initLabel();
	}

	public boolean reload(){
		if(!isReady() || !canReload()) return false;
		float time = 0;
		for(AmmoType t : AmmoType.values()){
			if(t == AmmoType.energy) continue;
			int i = t.ordinal();
			reloadAmounts[i] = Math.round(Math.min(totalStats.clipSize[i] - clips[i], totalStats.reloadCount[i]));
			if(!isBionic() && parentInventory != null){
				reloadAmounts[i] = Math.min(reloadAmounts[i], parentInventory.getAmmoCount(t));
				parentInventory.removeAmmo(t, reloadAmounts[i]);
			}
			time += totalStats.reloadTimes[i];
		}
		setState(Math.round(time), WeaponState.reloading, true);
		if(reloadStartSound != null) GameController.get().playSoundAt(reloadStartSound, pos);
		return true;
	}
	
	private void finishReloading(){
		for(AmmoType t : AmmoType.values()){
			int i = t.ordinal();
			clips[i] += reloadAmounts[i];
		}
		if(reloadEndSound != null) GameController.get().playSoundAt(reloadEndSound, pos);
		setReady();
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
	
	public final void pullAlternativeTrigger(){
		Mob m = getParentMob();
		if(m == null || m.skills.canAltFire(this)) pullAlternativeTriggerInternal();
	}
	
	public void pullAlternativeTriggerInternal(){
		if(type.isSubTypeOf(ItemType.wGrenadeLauncher)){
			boolean foundLevel2Shot = false;
			for(Shot shot : GameController.get().shots){
				if(!GameController.get().isMarkedForRemoval(shot) && shot.parent == this){
					if(shot.manualDetonateLevel == 1) shot.hit();
					if(shot.manualDetonateLevel == 2 && ! foundLevel2Shot){
						shot.hit();
						foundLevel2Shot = true;
					}
				}
			}
		} else if(type.isSubTypeOf(ItemType.wShotgun)){
			pullTrigger(2, 100);
		} else if(type.isSubTypeOf(ItemType.wZoomable) && zoomEffect != null){
			Mob m = getParentMob();
			if(m != null){
				if(zoom){
					effects.remove(zoomEffect);
				} else {
					effects.add(zoomEffect);
				}
				zoom = !zoom;
				m.recalculateCards();
				if(m == GameController.get().player) GameController.get().recalcVisibleTiles = true;
			}
		}
	}
	
	public boolean pullTrigger(){
		return pullTrigger(totalStats.shotsPerBurst, Math.round(totalStats.shotInterval));
	}
	
	private boolean pullTrigger(int shotsPerBurst, int shotInterval){
		if(canFire(shotsPerBurst)){
			burstShotCount = shotsPerBurst;
			burstTimeBetweenShots = shotInterval;
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
		Mob parent = getParentMob();
		if(parent == GameController.get().player){
			GameController.get().cameraShake(getScreenShakeAmount());
			GameController.get().cameraRecoil(angle + (float)Math.PI, getScreenRecoilAmount());
		}
		burstShotCount--;
		for(int i=0; i<AMMO_TYPE_COUNT; i++) clips[i] -= totalStats.ammoPerShot[i];
		if(shotSound != null) GameController.get().playSoundAt(shotSound, pos);
		for(int i=0; i<totalStats.projectilesPerShot; i++){
			Shot s = new Shot(this, null, null, 0.1f);
			s = initShot(s);
			s.dmg = totalDamageCard.createDamage();
			s.team = parent == null ? Shot.Team.dontcare : shotTeam;
			s.isGrenade = type.isSubTypeOf(ItemType.wGrenadeLauncher);
			s.bounceCount = totalStats.shotBounces;
			s.bounceProbability = totalStats.bounceProbability;
			s.parent = this;
			if(parent != null) s = parent.skills.modifyShot(s, this, parent);
			GameController.get().shots.add(s);
		}
		if(burstShotCount > 0){
			setState(burstTimeBetweenShots, WeaponState.cooldownShot, false);
		} else {
			setState(mustReload() ? 0 : Math.round(totalStats.burstInterval), WeaponState.cooldownBurst, false);
		}
		currentScatter = Math.max(0, Math.min(totalStats.maxScatter, currentScatter + totalStats.scatterPerShot));
	}
	
	public Mob getParentMob(){
		Mob m = null;
		if(bionicParent != null){
			m = bionicParent;
		} else if(parentInventory != null) m = parentInventory.getParent();
		return (m != null && m.isAlive()) ? m : null;
	}
	
	@Override
	public void update(int time, boolean isEquipped, boolean isHeld){
		if(!isHeld) super.update(time, isEquipped, isHeld);
		// accuracy cooldown
		currentScatter -= time / 1000f * totalStats.scatterCoolRate;
		if(currentScatter < totalStats.minScatter) currentScatter = totalStats.minScatter;
		if(currentScatter > totalStats.maxScatter) currentScatter = totalStats.maxScatter;
		// regenerate ammo
		for(int i=0; i<AMMO_TYPE_COUNT; i++){
			float rate = totalStats.ammoRegenerationRates[i];
			if(rate != 0f && state != WeaponState.reloading){
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
				case reloading:
					finishReloading();
					break;
				case unloading:
					finishUnloading();
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
		Mob m = getParentMob();
		boolean drawOutside = m == null || GameController.get().shouldDrawMobOutsideVisionRange(m);
		Image i = Math.abs(angle) > Math.PI/2 ? flippedImg : img;
		GameRenderer.LayerType l = parentInventory == null ? 
				GameRenderer.LayerType.items : GameRenderer.LayerType.mob2;
		GameController.get().renderer.drawImage(i, p, 1f, angle, drawOutside, l);
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
