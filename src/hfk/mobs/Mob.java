/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.mobs;

import hfk.ExpRandom;
import hfk.PointF;
import hfk.PointI;
import hfk.Shot;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.items.ExperienceOrb;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.level.Door;
import hfk.level.UsableLevelItem;
import hfk.skills.SkillSet;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;
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
public abstract class Mob implements StatsModifier {

	// debug stuff
	public static final boolean showCollisionDebug = false;
	public static final boolean showPathDebug = false;
	public static final boolean debugBlind = false;
	
	//stuff that can be set by extending classes
	public PointF pos;
	public int xp = 0;
	public SkillSet skills = new SkillSet(this);
	
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
	public boolean canOpenDoors = false;
	public boolean autoCloseDoors = false;
	public float minDistanceToPlayer = 2f;
	public int minPullTriggerDelay = 50;
	public int maxPullTriggerDelay = 450;
	public int barrageTimeOnHit = 0;
	public int barrageTimeOnLost = 0;
	public int barrageTimeOnNotify = 0;
	public int timeToFeelSafe = 15000;
	public int minPathLength = 2;
	public int maxPathLength = 7;
	public float size = 0.8f;
	public Animation animation = null;
	public Sound hitSound = null, deathSound = null, alertSound = null;
	public LinkedList<PointF> path = new LinkedList<>();
	// stuff that normally will not be set by extending classes
	public boolean givesXP = true;
	public MobStatsCard basicStats, totalStats;
	public int hp;
	public int totalDamageTaken = 0;
	public int totalHpHealed = 0;
	public Inventory inventory;
	public PointF lastPlayerPos = null;
	public int lastPlayerTime = -1;
	private float lookAngle = 0f, desiredLookAngle = 0f;
	private int barrageTimer = 0;
	private int pullTriggerDelayTimer = 0;
	private int safetyTimer = 0;
	private int stateTextTimer = 0;
	private String stateText = "";
	private boolean playerVisible = false;
	private Weapon bionicWeapon = null;
	private Door lastDoor = null;
	private boolean enteredLastDoor = false;
	private PointF lastDoorExitPoint = null;
	private Weapon weaponToUnload = null;
	
	public static Mob createMob(PointF pos, int maxDifficulty, int level){
		// TODO: think of a less wasteful way to do this!!!
		LinkedList<Mob> l = new LinkedList<>();
		l.add(new Star(pos));
		l.add(new Kamikaze(pos));
		l.add(new KamikazeMaster(pos));
		l.add(new Scout(pos));
		l.add(new Hunter(pos));
		l.add(new Freak(pos, Freak.LoadOutType.pistol));
		l.add(new Freak(pos, Freak.LoadOutType.shotgun));
		l.add(new Freak(pos, Freak.LoadOutType.energypistol));
		l.add(new Freak(pos, Freak.LoadOutType.plasmaminigun));
		l.add(new Grunt(pos));
		l.add(new Brute(pos));
		float p = 0f;
		ListIterator<Mob> iter = l.listIterator();
		while(iter.hasNext()) if(iter.next().getDifficultyScore() > maxDifficulty) iter.remove();
		if(l.isEmpty()) return null;
		
		for(Mob m : l) p += getP(m);
		float r = p * GameController.random.nextFloat();
		for(Mob m : l){
			p = getP(m);
			if(p > r){
				if(GameController.random.nextFloat() < 0.1f){
					ExpRandom ran = new ExpRandom(0.6);
					float f = ran.getNextFloat();
					int x = (int)(f*m.getDifficultyScore()*2f);
					if(x >= 5) m.inventory.addItem(new ExperienceOrb(pos.clone(), x));
				}
				return m;
			}
			r -= p;
		}
		System.out.println("WARNING: createMob should not end up here!");
		return l.getLast();
	}
	private static float getP(Mob m){
		return m.getSpawnProbabilityModifier() + m.getDifficultyScore()/30f;
	}
	
	public Mob(PointF pos){
		this.pos = pos.clone();
		basicStats = getDefaultMobStatsCard();
		totalStats = basicStats.clone();
		// init inventory after msc, because inventory needs the msc!!!
		inventory = new Inventory(this);
		recalculateCards();
		hp = totalStats.getMaxHP();
	}
	
	public abstract MobStatsCard getDefaultMobStatsCard();
	public abstract int getDifficultyScore();
	public abstract String getDisplayName();
	
	public float getSpawnProbabilityModifier(){ return 1f; }
	public void mobOnLostSightOfPlayer(PointF playerPos){}
	public void mobOnGetSightOfPlayer(PointF playerPos){}
	public void mobOnLostMemoryOfPlayer(){}
	public void mobUpdate(int time, boolean playerVisible){}
	public boolean mobOnDeath(Shot s){ return true; }
	public void mobOnHit(int dmg, Shot s){}

	public Weapon getBionicWeapon() {
		return bionicWeapon;
	}

	public void setBionicWeapon(Weapon w) {
		if(bionicWeapon != null) bionicWeapon.bionicParent = null;
		this.bionicWeapon = w;
		bionicWeapon.bionicParent = this;
	}

	public boolean isAlive(){
		return hp > 0;
	}
	
	public boolean heal(int amount){
		if(hp >= totalStats.getMaxHP()) return false;
		amount = Math.min(amount, totalStats.getMaxHP()-hp);
		totalHpHealed += amount;
		hp += amount;
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
		if(this instanceof Player){
			lookAngle = angle;
			Weapon w = getActiveWeapon();
			if(w != null) w.angle = angle;
		} else {
			desiredLookAngle = angle;
		}
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
		// TODO: only start barrage if mob has clear line of fire. else move into position first
		barrageTimer = Math.max(time, barrageTimer);
		if(target != null) lastPlayerPos = target.clone();
	}
	
	public void notifyPlayerPosition(PointF playerPos, int time){
		if(!allowPlayerPosNotify || (lastPlayerTime >= 0 && time > lastPlayerTime)) return;
		lastPlayerPos = playerPos;
		lastPlayerTime = 1;
		alert(true, true);
		if(barrageTimeOnNotify > 0) startBarrage(null, barrageTimeOnNotify);
		if(autoFollowPlayerOnNotify) createPathToPlayer();
	}
	
	public final void alert(boolean forceSound, boolean forceText){
		if(this instanceof Player) return;
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
	
	private void tryToShoot(){
		Weapon w = getActiveWeapon();
		if(w == null) return;
		if(Math.abs(GameController.getAngleDiff(lookAngle, desiredLookAngle)) <= (float)Math.PI/8f && 
				(w.totalStats.isAutomatic || pullTriggerDelayTimer <= 0)){
			boolean b = w.pullTrigger();
			if(b){
				pullTriggerDelayTimer = minPullTriggerDelay;
				if(minPullTriggerDelay <= maxPullTriggerDelay){
					pullTriggerDelayTimer += GameController.random.nextInt(maxPullTriggerDelay - minPullTriggerDelay);
				}
			}
		}
	}
	
	public boolean unloadWeapon(Weapon w){
		Weapon aw = getActiveWeapon();
		if(aw != null && !aw.isReady()) return false; // using equipped weapon right now
		if(weaponToUnload != null) return false; // already unloading something
		if(!w.unloadToInventory(inventory)) return false;
		weaponToUnload = w;
		return true;
	}
	
	public final void update(int time){
		GameController ctrl = GameController.get();
		pullTriggerDelayTimer = Math.max(0, pullTriggerDelayTimer - time);
		stateTextTimer = Math.max(0, stateTextTimer - time);
		safetyTimer = Math.min(safetyTimer + time, timeToFeelSafe);
		animation.update(time);
		inventory.update(time);
		if(bionicWeapon != null) bionicWeapon.update(time, true, true);
		if(weaponToUnload != null){
			float d = totalStats.getMaxPickupRange();
			if(weaponToUnload.pos.squaredDistanceTo(pos) > d*d){
				weaponToUnload.stopUnloading();
				weaponToUnload = null;
			} else if(weaponToUnload.isReady()){
				weaponToUnload = null;
			} else {
				// do nothing while unloading a weapon
				return;
			}
		}
		Weapon w = getActiveWeapon();
		if(w != null){
			w.pos.set(pos);
			// reload if: clip empty or not quite empty but feeling safe
			if(autoReloadWeapon && (w.mustReload() || (safetyTimer >= timeToFeelSafe && w.canReload()))) w.reload();
		}
		// non players only from here on
		if(this instanceof Player) return;
		float angleToPlayer = pos.angleTo(ctrl.player.pos);
		boolean playerWasVisible = playerVisible;
		playerVisible = !debugBlind && ctrl.playerIsAlive() && 
				Math.abs(GameController.getAngleDiff(lookAngle, angleToPlayer)) <= totalStats.getVisionAngle()/2f &&
				ctrl.level.isVisibleFrom(this, ctrl.player, totalStats.getSightRange());
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
				lookInDirection(angleToPlayer);
				path.clear();
				if(moveWhilePlayerVisible) path.add(ctrl.player.pos.clone());
				if(autoUseWeapon) tryToShoot();
			}
		} else {
			float sr = Math.max(totalStats.getBasicSenseRange(), (ctrl.player.size + size) / 2f);
			if(ctrl.player.pos.squaredDistanceTo(pos) <= sr*sr){
				// can sense player! update path but dont set barrage
				lastPlayerPos = ctrl.player.pos.clone();
				lastPlayerTime = 1;
				if(autoSetPath && autoFollowPlayer && (path.isEmpty() ||
				   path.getLast().squaredDistanceTo(ctrl.player.pos) >= 4f)){ 
					createPathToPlayer();
				}
			}
			// handle last known player position
			if(lastPlayerTime == 0){
				if(autoFollowPlayer){
					createPathToPlayer();
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
					tryToShoot();
				}
			}
			if(lastPlayerTime >= 0){
				lastPlayerTime += time;
				if(lastPlayerTime > totalStats.getMemoryTime()){
					lastPlayerPos = null;
					lastPlayerTime = -1;
					mobOnLostMemoryOfPlayer();
				}
			}
		}
		// look
		if(!(this instanceof Player) && lookAngle != desiredLookAngle){
			float diff = GameController.getAngleDiff(lookAngle, desiredLookAngle);
			if(Math.abs(diff) <= totalStats.getTurnRate()*time/1000f){
				lookAngle = desiredLookAngle;
			} else {
				lookAngle += Math.signum(diff) * totalStats.getTurnRate()*time/1000f;
			}
			if(w != null) w.angle = lookAngle;
		}
		// move
		if((moveWhileShooting || (barrageTimer <= 0 && (w == null || w.isReady()))) && 
			 (moveWhilePlayerVisible || !playerVisible || lastPlayerPos.squaredDistanceTo(pos) < minDistanceToPlayer*minDistanceToPlayer)){
			if(autoSetPath && path.isEmpty()) createNewRandomPath();
			if(autoFollowPath && !path.isEmpty()) goToNextWaypoint(time);
		}
		mobUpdate(time, playerVisible);
	}
	
	public void createNewRandomPath(){
		int n = minPathLength;
		if(maxPathLength > minPathLength) n += GameController.random.nextInt(maxPathLength - minPathLength);
		path = GameController.get().level.getRandomPath(pos, n, true, canOpenDoors);
	}
	
	public void createPathToPlayer(){
		path = GameController.get().level.getPathTo(this.pos, lastPlayerPos, 20, true, canOpenDoors, true);
		if(path == null) createNewRandomPath();
	}
	
	public void goToNextWaypoint(int time){
		PointF target = path.getFirst();
		PointF newPos = pos.clone();
		boolean needToCloseDoor = lastDoorExitPoint != null && lastDoorExitPoint.squaredDistanceTo(pos) >= size*size/4f;
		if(!needToCloseDoor){
			lookInDirection(pos.angleTo(target));
			// don't move if you have to turn more than 45°
			// this prevents mobs from walking into a position where
			// the current waypoint cannot be reached in a straight line anymore
			if(Math.abs(GameController.getAngleDiff(lookAngle, desiredLookAngle)) > Math.PI / 4f) return;
			float timeF = time / 1000f;
			PointF vel = new PointF(getLookAngle());
			float dd = pos.squaredDistanceTo(target);
			float speed = totalStats.getMaxSpeed();
			float reach = speed * timeF;
			if(dd < reach*reach) speed = (float)Math.sqrt(dd) / timeF;
			// try moving. if a collision happens, try to do something about it
			if(GameController.get().moveThing(newPos, vel.x * speed, vel.y * speed, size, time, true)){
				// try opening a door
				float range = totalStats.getMaxPickupRange() + 1f;
				UsableLevelItem i = GameController.get().level.getUsableItemOnLine(pos, target, range, false);
				if(i instanceof Door){
					Door d = (Door)i;
					if(d.isOpening() || (canOpenDoors && d.isMoving())){
						// door is moving. wait until it is finished
						return;
					}
					if(canOpenDoors && !d.isOpen() && d.use(this)){
						lastDoor = d;
						enteredLastDoor = false;
						return;
					}
				}
				// are we stuck?
				if(newPos.squaredDistanceTo(pos) < 0.05f*0.05f*speed*speed*timeF*timeF){
					// stuck! generate a new path
					if(lastPlayerPos == null){
						createNewRandomPath();
					} else {
						createPathToPlayer();
					}
				}
			}
		}
		// close door if necessary
		if(autoCloseDoors && lastDoor != null){
			PointI p = pos.round();
			if(!enteredLastDoor && p.equals(lastDoor.pos)){
				enteredLastDoor = true;
			} else if(enteredLastDoor && lastDoorExitPoint == null && !p.equals(lastDoor.pos)){
				lastDoorExitPoint = pos.clone();
			} else if(needToCloseDoor){
				if(lastDoor.isOpen()){
					lookAt(lastDoor.pos.toFloat());
					if(Math.abs(GameController.getAngleDiff(lookAngle, desiredLookAngle)) > Math.PI/6){
						// need to turn towards the door first before using it
						return;
					}
					lastDoor.use(this);
				}
				lastDoor = null;
				enteredLastDoor = false;
				lastDoorExitPoint = null;
				if(pos.squaredDistanceTo(target) < 0.2f*0.2f) path.removeFirst();
				// dont move now, do it next frame
				return;
			}
		}
		pos = newPos;
		if(pos.squaredDistanceTo(target) < 0.05f*0.05f) path.removeFirst();
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
		PointF p2 = new PointF(getLookAngle() + totalStats.getVisionAngle() / 2f);
		p2.add(pos);
		p2 = ctrl.transformTilesToScreen(p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
		p2 = new PointF(getLookAngle() - totalStats.getVisionAngle() / 2f);
		p2.add(pos);
		p2 = ctrl.transformTilesToScreen(p2);
		g.drawLine(p1.x, p1.y, p2.x, p2.y);
	}
	
	public void draw(){
		GameController ctrl = GameController.get();
		ctrl.renderer.drawImage(animation.getCurrentFrame(), pos, ctrl.shouldDrawMobOutsideVisionRange(this), GameRenderer.LayerType.mob1);
		Weapon w = getActiveWeapon();
		if(w != null && w != bionicWeapon){
			w.pos.set(pos);
			w.render();
		}
	}
	
	public void drawStatus(){
		GameController ctrl = GameController.get();
		Weapon w = getActiveWeapon();
		// reloading / unloading progress bar
		boolean relBar = false;
		if(weaponToUnload != null || w != null && w.isReloading() && ctrl.shouldDrawReloadBar(this)){
			PointF p = pos.clone();
			p.x -= 0.5f;
			p.y -= 0.6f;
			p = ctrl.transformTilesToScreen(p);
			float len = ctrl.transformTilesToScreen(Math.max(size, 1f));
			Graphics g = ctrl.renderer.getGraphics();
			g.setColor(Color.white);
			g.drawRect(p.x, p.y, len, 7);
			len -= 3;
			g.fillRect(p.x + 2, p.y + 2, len * (weaponToUnload != null ? weaponToUnload : w).getProgress(), 4);
			relBar = true;
		}
		// health bar
		if(ctrl.shouldDrawHealthBar(this)){
			PointF p = pos.clone();
			p.x -= 0.5f;
			p.y -= relBar ? 0.7f : 0.6f;
			p = ctrl.transformTilesToScreen(p);
			float len = ctrl.transformTilesToScreen(Math.max(size, 1f));
			Graphics g = ctrl.renderer.getGraphics();
			g.setColor(Color.red);
			g.drawRect(p.x, p.y, len, 7);
			len -= 3;
			g.fillRect(p.x + 2, p.y + 2, len * Math.max(0f, Math.min(1f, (float)hp / totalStats.getMaxHP())), 4);
		}
		if(stateTextTimer > 0 && this != ctrl.player){
			PointF p = pos.clone();
			p.y -= 0.5f;
			ctrl.renderer.drawString(stateText, p, Color.red, 1f, true);
		}
		if(showCollisionDebug) drawCollisionDebugInfo();
		if(showPathDebug) drawPathDebugInfo();
	}
	
	public final boolean onDeath(Shot s){
		if(mobOnDeath(s)){
			if(deathSound != null) GameController.get().playSoundAt(deathSound, pos);
			for(InventoryItem i : inventory.removeAll()){
				GameController.get().dropItem(i, this, false);
			}
			if(!(this instanceof Player) && givesXP) GameController.get().player.xp += getDifficultyScore();
			return true;
		}
		return false;
	}
	
	public final void onHit(int dmg, Shot s){
		alert(false, false);
		if(hitSound != null) GameController.get().playSoundAt(hitSound, pos);
		if(s != null && s.team == Shot.Team.friendly){
			if(autoFollowPlayerOnHit){
				lastPlayerTime = 0;
				lastPlayerPos = s.origin.clone();
			}
			startBarrage(null, barrageTimeOnHit);
		}
		mobOnHit(dmg, s);
	}
	
	public final void recalculateCards(){
		totalStats = basicStats.clone();
		MobStatsCard c = MobStatsCard.createBonus();
		addMobStatsCardEffects(c, this);
		totalStats.applyBonus(c);
		inventory.recalculateStats();
		hp = Math.min(hp, totalStats.getMaxHP());
	}

	@Override
	public void addDamageCardEffects(DamageCard card, InventoryItem i, Mob m) {
		if(m != this) return;
		skills.addDamageCardEffects(card, i, m);
		inventory.addDamageCardEffects(card, i, m);
	}

	@Override
	public void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m) {
		if(m != this) return;
		skills.addWeaponStatsCardEffects(card, w, m);
		inventory.addWeaponStatsCardEffects(card, w, m);
	}

	@Override
	public void addMobStatsCardEffects(MobStatsCard card, Mob m) {
		if(m != this) return;
		skills.addMobStatsCardEffects(card, m);
		inventory.addMobStatsCardEffects(card, m);
	}
	
}
