/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.mobs;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.stats.MobStatsCard;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Sound;

/**
 *
 * @author LostMekka
 */
public abstract class Mob {

	//stuff that can be set by extending classes
	public PointF pos;
	public boolean showCollisionDebug = false;
	public boolean showPathDebug = false;
	
	public boolean allowPlayerPosNotify = true;
	public boolean autoSetPath = true;
	public boolean autoFollowPath = true;
	public boolean autoFollowPlayer = true;
	public boolean autoFollowPlayerOnHit = true;
	public boolean autoFollowPlayerOnNotify = true;
	public boolean autoLockOnPlayer = true;
	public boolean autoUseWeapon = true;
	public boolean autoReloadWeapon = true;
	public boolean moveWhilePlayerVisible = true;
	public boolean moveWhileShooting = false;
	public float minDistanceToPlayer = 2f;
	public int barrageTimeOnHit = 0;
	public int barrageTimeOnLost = 0;
	public int barrageTimeOnNotify = 0;
	public int timeToFeelSafe = 15000;
	public int pathLength = 5;
	public float size = 0.8f;
	public Animation animation = null;
	public Sound hitSound = null, deathSound = null, alertSound = null;
	public Weapon bionicWeapon = null;
	public LinkedList<PointF> path = new LinkedList<>();
	// stuff that normally will not be set by extending classes
	public MobStatsCard stats;
	public int hp;
	public Inventory inventory;
	private PointF lastPlayerPos = null;
	private int lastPlayerTime = -1;
	private float lookAngle = 0f;
	private int barrageTimer = 0;
	private int safetyTimer = 0;
	private int stateTextTimer = 0;
	private String stateText = "";
	private boolean playerVisible = false;
	
	public Mob(PointF pos){
		this.pos = pos.clone();
		stats = getDefaultMobStatsCard();
		// init inventory after msc, because inventory needs the msc!!!
		inventory = new Inventory(this);
		hp = stats.getMaxHP();
	}
	
	public static Mob createMob(PointF pos, int maxDifficulty, int level){
		// TODO: think of a less wasteful way to do this!!!
		LinkedList<Mob> l = new LinkedList<>();
		l.add(new Star(pos));
		l.add(new Scout(pos));
		l.add(new Freak(pos, Freak.LoadOutType.pistol));
		l.add(new Freak(pos, Freak.LoadOutType.shotgun));
		l.add(new Freak(pos, Freak.LoadOutType.energypistol));
		l.add(new Freak(pos, Freak.LoadOutType.plasmaminigun));
		float p = 0f;
		ListIterator<Mob> iter = l.listIterator();
		while(iter.hasNext()) if(iter.next().getDifficultyScore() > maxDifficulty) iter.remove();
		if(l.isEmpty()) return null;
		
		for(Mob m : l) p += m.getProbabilityModifier();
		float r = p * GameController.random.nextFloat();
		for(Mob m : l){
			p = m.getProbabilityModifier();
			if(p > r) return m;
			r -= p;
		}
		System.out.println("WARNING: createMob should not end up here!");
		return l.getLast();
	}
	
	public abstract MobStatsCard getDefaultMobStatsCard();
	public abstract int getDifficultyScore();
	
	public float getProbabilityModifier(){ return 1f; }
	public void mobOnLostSightOfPlayer(PointF playerPos){}
	public void mobOnGetSightOfPlayer(PointF playerPos){}
	public void mobOnLostMemoryOfPlayer(){}
	public void mobUpdate(int time, boolean playerVisible){}
	public void mobOnDeath(Shot s){}
	public void mobOnHit(int dmg, Shot s){}

	public boolean isAlive(){
		return hp > 0;
	}
	
	public boolean heal(int amount){
		if(hp >= stats.getMaxHP()) return false;
		hp = Math.min(hp + amount, stats.getMaxHP());
		return true;
	}
	
	public float getLookAngle() {
		return lookAngle;
	}

	public Weapon getActiveWeapon(){
		if(bionicWeapon != null) return bionicWeapon;
		return inventory.getActiveWeapon();
	}
	
	public void lookAt(PointF p){
		lookInDirection(pos.angleTo(p));
	}
	
	public void lookInDirection(float angle){
		lookAngle = angle;
		Weapon w = getActiveWeapon();
		if(w != null) w.angle = angle;
	}
	
	public boolean reloadActiveWeapon(){
		Weapon w = getActiveWeapon();
		if(w == null) return false;
		return w.reload();
	}
	
	public boolean holdsWeapon(){
		return getActiveWeapon() != null;
	}
	
	public void randomizeAnimationState(){
		int t = 0;
		for(int i=0; i<animation.getFrameCount(); i++) t += animation.getDuration(i);
		animation.update(GameController.random.nextInt(t));
	}
	
	public void startBarrage(PointF target, int time){
		barrageTimer = Math.max(time, barrageTimer);
		if(target != null) lastPlayerPos = target.clone();
	}
	
	public void notifyPlayerPosition(PointF playerPos, int time){
		if(!allowPlayerPosNotify || (lastPlayerTime >= 0 && time > lastPlayerTime)) return;
		lastPlayerPos = playerPos;
		lastPlayerTime = 1;
		alert(true, true);
		if(barrageTimeOnNotify > 0) startBarrage(null, barrageTimeOnNotify);
		if(autoFollowPlayerOnNotify){
			path = GameController.get().level.getPathTo(this.pos, playerPos, 20, true);
			path.removeFirst();
		}
	}
	
	public final void alert(boolean forceSound, boolean forceText){
		if(this == GameController.get().player) return;
		if(alertSound != null && (forceSound || lastPlayerTime < 0)) GameController.get().playSoundAt(alertSound, pos);
		if(forceText || lastPlayerTime < 0) setStateText("!");
		safetyTimer = 0;
	}
	
	public void setStateText(String s){
		if(stateTextTimer <= 0){
			GameController ctrl = GameController.get();
			PointF p = pos.clone();
			float scale = 1.3f;
			p.y -= 0.5f;
			ctrl.addFloatingText(s, scale, p, Color.red, null);
			stateText = s;
			stateTextTimer = 800;
		}
	}
	
	public boolean canSeePlayer(){
		return playerVisible;
	}
	
	public final void update(int time){
		GameController ctrl = GameController.get();
		stateTextTimer = Math.max(0, stateTextTimer - time);
		safetyTimer = Math.min(safetyTimer + time, timeToFeelSafe);
		animation.update(time);
		Weapon w = getActiveWeapon();
		if(w != null){
			w.update(time);
			w.pos = pos;
			// reload if: clip empty or not quite empty but feeling safe
			if(autoReloadWeapon && (w.mustReload() || (safetyTimer >= timeToFeelSafe && w.canReload()))) w.reload();
		}
		if(this == ctrl.player) return;
		boolean playerWasVisible = playerVisible;
		playerVisible = ctrl.playerIsAlive() && ctrl.level.isVisibleFrom(this, ctrl.player, stats.getSightRange());
		if(playerVisible){
			lastPlayerPos = ctrl.player.pos.clone();
			if(!playerWasVisible){
				alert(false, false);
				lastPlayerTime = 0;
				mobOnGetSightOfPlayer(lastPlayerPos);
			}
			barrageTimer = 0;
			safetyTimer = 0;
			if(autoLockOnPlayer){
				lookInDirection(pos.angleTo(ctrl.player.pos));
				path.clear();
				if(moveWhilePlayerVisible) path.add(ctrl.player.pos.clone());
				if(autoUseWeapon && w != null) w.pullTrigger();
			}
		} else {
			// handle last known player position
			if(lastPlayerTime == 0){
				if(autoFollowPlayer){
					path = ctrl.level.getPathTo(pos, lastPlayerPos, 30, true);
					path.removeFirst();
					startBarrage(null, barrageTimeOnLost);
				}
				mobOnLostSightOfPlayer(lastPlayerPos);
			}
			if(barrageTimer > 0){
				barrageTimer -= time;
				if(barrageTimer < 0){
					barrageTimer = 0;
				} else {
					lookAt(lastPlayerPos);
					if(w != null) w.pullTrigger();
				}
			}
			if(lastPlayerTime >= 0){
				lastPlayerTime += time;
				if(lastPlayerTime > stats.getMemoryTime()){
					lastPlayerPos = null;
					lastPlayerTime = -1;
					mobOnLostMemoryOfPlayer();
				}
			}
		}
		// move
		if((moveWhileShooting || (barrageTimer <= 0 && (w == null || w.isReady()))) && 
			 (moveWhilePlayerVisible || !playerVisible || lastPlayerPos.squaredDistanceTo(pos) < minDistanceToPlayer*minDistanceToPlayer)){
			if(autoSetPath && path.isEmpty()) path = ctrl.level.getRandomPath(pos, pathLength, true);
			if(autoFollowPath && !path.isEmpty()){
				if(goInDirectionOf(path.getFirst(), time)) path.removeFirst();
			}
		}
		mobUpdate(time, playerVisible);
	}
	
	public boolean goInDirectionOf(PointF target, int time){
		lookInDirection(pos.angleTo(target));
		PointF vel = new PointF(getLookAngle());
		float dd = pos.squaredDistanceTo(target);
		float speed = stats.getMaxSpeed();
		float reach = speed * time / 1000f;
		boolean arrived = false;
		if(dd < reach*reach){
			speed = (float)Math.sqrt(dd) / time * 1000f;
			arrived = true;
		}
		GameController.get().moveMob(this, vel.x * speed, vel.y * speed, time);
		return arrived;
	}
	
	public void drawPathDebugInfo(){
		GameController ctrl = GameController.get();
		Graphics g = ctrl.renderer.getGraphics();
		g.setColor(Color.yellow);
		PointF p1 = ctrl.transformTilesToScreen(pos), p2;
		Iterator<PointF> pi = path.iterator();
		while(pi.hasNext()){
			p2 = ctrl.transformTilesToScreen(pi.next());
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = p2;
		}
	}
	
	public void drawCollisionDebugInfo(){
		GameController ctrl = GameController.get();
		Graphics g = ctrl.renderer.getGraphics();
		g.setColor(Color.white);
		float s = ctrl.transformTilesToScreen(size);
		PointF p = ctrl.transformTilesToScreen(new PointF(pos.x - size/2f, pos.y - size/2f));
		g.drawOval(p.x, p.y, s, s);
		PointF p1 = ctrl.transformTilesToScreen(pos);
		PointF p2 = new PointF(getLookAngle());
		p2.add(pos);
		p2 = ctrl.transformTilesToScreen(p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	public void draw(){
		GameController ctrl = GameController.get();
		ctrl.renderer.drawImage(animation.getCurrentFrame(), pos);
		Weapon w = inventory.getActiveWeapon();
		if(w != null && w != bionicWeapon) w.render();
		// reloading progress bar
		if(w != null && w.isReloading() && ctrl.shouldDrawReloadBar(this)){
			PointF p = pos.clone();
			p.x -= 0.5f;
			p.y -=0.6f;
			p = ctrl.transformTilesToScreen(p);
			float len = ctrl.transformTilesToScreen(1f);
			Graphics g = ctrl.renderer.getGraphics();
			g.setColor(Color.white);
			g.drawRect(p.x, p.y, len, 7);
			len -= 3;
			g.fillRect(p.x + 2, p.y + 2, len * w.getProgress(), 4);
		}
//		if(stateTextTimer > 0 && this != ctrl.player){
//			PointF p = pos.clone();
//			p.y -= 0.5f;
//			ctrl.renderer.drawString(stateText, p, Color.red, 1f, true);
//		}
		if(showCollisionDebug) drawCollisionDebugInfo();
		if(showPathDebug) drawPathDebugInfo();
	}
	
	public final void onDeath(Shot s){
		if(deathSound != null) GameController.get().playSoundAt(deathSound, pos);
		for(InventoryItem i : inventory.removeAll()){
			GameController.get().dropItem(i, this, false);
		}
		mobOnDeath(s);
	}
	
	public final void onHit(int dmg, Shot s){
		alert(false, false);
		if(hitSound != null) GameController.get().playSoundAt(hitSound, pos);
		lastPlayerPos = s.origin.clone();
		if(autoFollowPlayerOnHit && s.team == Shot.Team.friendly) lastPlayerTime = 0;
		startBarrage(null, barrageTimeOnHit);
		mobOnHit(dmg, s);
	}
	
}
