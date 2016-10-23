/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import hfk.ExpRandom;
import hfk.Explosion;
import hfk.config.HFKConfiguration;
import hfk.game.substates.GameSubState;
import hfk.IngameText;
import hfk.Particle;
import hfk.PointF;
import hfk.PointI;
import hfk.Shot;
import hfk.game.substates.ExchangeSubState;
import hfk.game.substates.GameOverSubState;
import hfk.game.substates.GameplaySubState;
import hfk.game.substates.InventorySubState;
import hfk.game.substates.OmniSubState;
import hfk.game.substates.OverviewSubState;
import hfk.game.substates.PauseSubState;
import hfk.game.substates.SkillsSubState;
import hfk.items.HealthPack;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.level.Level;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.concreteFactories.CaveAreaFactory;
import hfk.level.factory.concreteFactories.RoomsFactory;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import hfk.net.NetState;
import hfk.net.NetStateObject;
import hfk.skills.SkillSet;
import hfk.stats.Damage;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameController {
	
	public static boolean CHEAT_SCOUTED = false;
	public static boolean CHEAT_VISIBLE = false;
	
	public static final String VERSION = "0.1.6";
	
	public static final float SQRT2 = (float)Math.sqrt(2);
	public static final Random random = new Random();
	public static final float SPRITE_SIZE = 32f;
	public static final float EXPLODE_SHAKE_MULTIPLIER = 0.2f;
	public static final float MOBGENERATION_MAXDIFF_FACTOR = 0.1f;
	public static final float ITEMGENERATION_MAXRARITY_FACTOR = 0.166f;
	
	private static GameController currGC = null;
	
	public static GameController get(){
		return currGC;
	}
	public static void set(GameController gc){
		if(currGC != null && currGC.music != null){
			currGC.music.stop();
		}
		currGC = gc;
		if(gc.musicIsOn && gc.music != null) gc.startMusic();
	}
	
	public final LinkedList<Explosion> explosions = new LinkedList<>();
	public final LinkedList<Shot> shots = new LinkedList<>();
	public final LinkedList<Mob> mobs = new LinkedList<>();
	public final LinkedList<InventoryItem> items = new LinkedList<>();
	public final LinkedList<IngameText> texts = new LinkedList<>();
	public final LinkedList<Particle> particles = new LinkedList<>();
	private final LinkedList<Explosion> explosionsToRemove = new LinkedList<>();
	private final LinkedList<Shot> shotsToRemove = new LinkedList<>();
	private final LinkedList<Mob> mobsToRemove = new LinkedList<>();
	private final LinkedList<Mob> mobsToAdd = new LinkedList<>();
	private final LinkedList<InventoryItem> itemsToRemove = new LinkedList<>();

	public GameOverSubState gameOverState;
	public GameplaySubState gameplaySubState;
	public ExchangeSubState exchangeSubState;
	public InventorySubState inventorySubState;
	public OmniSubState omniSubState;
	public PauseSubState pauseSubState;
	public SkillsSubState skillsSubState;
	// payload: for development
	public OverviewSubState overviewSubState;

	public GameRenderer renderer;
	public GameSettings settings;
	public Level level = null;
	public Player player = null;
	public PointF mousePosInTiles = new PointF();
	public PointF screenPos = new PointF(-0.5f, -0.5f);
	public PointF screenPosOriginal = screenPos.clone();
	public PointF screenPosOffset = new PointF();
	public PointI mousePosInPixels = new PointI();
	public float screenShake = 0f;
	public int difficultyLevel = 1;
	public boolean musicIsOn = true, recalcVisibleTiles = false;
	
	private final InputMap inputMap;
	
	private GameSubState currSubState;
	private Music music = null;
	private float zoom = 3f;
	private boolean playerIsAlive = true;
	private int levelCount = 0;
	private long nextID = 0, timeStamp = 0;
	private HashMap<Long, NetStateObject> objectMap = new HashMap<>();

	public static float getAngleDiff(float a1, float a2) {
		float diff = (a2 - a1) % (2.0F * (float) Math.PI);
		if (diff > (float) Math.PI) {
			diff -= 2.0F * (float) Math.PI;
		}
		if (diff < -(float) Math.PI) {
			diff += 2.0F * (float) Math.PI;
		}
		return diff;
	}
	
	public GameController(GameContainer gc, GameSettings s) {
		settings = s;
		inputMap = new InputMap(gc.getInput());
		HFKConfiguration config = HFKConfiguration.fromFile();
		config.fillInputMap(inputMap);
		gameOverState = new GameOverSubState(inputMap);
		gameplaySubState = new GameplaySubState(inputMap);
		exchangeSubState = new ExchangeSubState(inputMap);
		inventorySubState = new InventorySubState(inputMap);
		omniSubState = new OmniSubState(inputMap);
		pauseSubState = new PauseSubState(inputMap);
		skillsSubState = new SkillsSubState(inputMap);
		overviewSubState = new OverviewSubState(inputMap);
		currSubState = gameplaySubState;
		renderer = new GameRenderer(gc);
	}

	public InputMap getInputMap() {
		return inputMap;
	}

	public long createIdFor(NetStateObject o){
		long id = nextID;
		nextID++;
		objectMap.put(id, o);
		return id;
	}
	
	public Music getMusic(){
		return music;
	}

	public GameSubState getCurrSubState() {
		return currSubState;
	}

	public void setCurrSubState(GameSubState s) {
		if(s instanceof OmniSubState) throw new RuntimeException("cant set sub state to omnisubstate!");
		currSubState = s;
	}
	
	public boolean isPaused(){
		return currSubState instanceof PauseSubState;
	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	public PointF getScreenPos(){
		return screenPos;
	}
	
	public PointF transformScreenToTiles(PointF p){
		return new PointF(
				transformScreenToTiles(p.x) + screenPos.x, 
				transformScreenToTiles(p.y) + screenPos.y);
	}
	
	public float transformScreenToTiles(float d){
		return d / zoom / SPRITE_SIZE;
	}
	
	public PointF transformTilesToScreen(PointF p){
		return new PointF(
				transformTilesToScreen(p.x - screenPos.x), 
				transformTilesToScreen(p.y - screenPos.y));
	}
	
	public float transformTilesToScreen(float d){
		return d * zoom * SPRITE_SIZE;
	}
	
	public void playSound(Sound s){
		s.play(1f, settings.sfxVolume);
	}
	
	public void playSoundAt(Sound s, PointF pos){
		float hr = player.totalStats.getHearRange();
		float v = 1f - pos.squaredDistanceTo(player.pos) / (hr*hr);
		if(v > 1f) v = 1f;
		if(v <= 0f) return;
		float x = (pos.x - player.pos.x) / hr;
		s.playAt(1f, settings.sfxVolume * v * (0.5f-x/2f), -1f, 0f, 0f);
		s.playAt(1f, settings.sfxVolume * v * (0.5f+x/2f), 1f, 0f, 0f);
	}
	
	public void startMusic(Music m){
		if(m != music){
			music.stop();
			music = m;
		}
		startMusic();
	}
	
	public void startMusic(){
		if(!music.playing()) music.loop(1f, settings.musicVolume);
		musicIsOn = true;
	}
	
	public void stopMusic(){
		music.stop();
		musicIsOn = false;
	}
	
	public void toggleMusic(){
		if(music.playing()){
			stopMusic();
		} else {
			startMusic();
		}
	}
	
	public void initAfterLoading(GameContainer gc){
		music = Resources.getMusic("music001.ogg");
		renderer.initAfterLoading();
		gameOverState.initAfterLoading(this, gc);
		gameplaySubState.initAfterLoading(this, gc);
		exchangeSubState.initAfterLoading(this, gc);
		inventorySubState.initAfterLoading(this, gc);
		omniSubState.initAfterLoading(this, gc);
		pauseSubState.initAfterLoading(this, gc);
		skillsSubState.initAfterLoading(this, gc);
		overviewSubState.initAfterLoading(this, gc);
		newGame();
	}
	
	public void newGame(){
		if(musicIsOn) startMusic();
		levelCount = 0;
		PointF pp = new PointF();
		player = new Player(pp);
		//printBalanceInfo();
		Weapon weapon = new hfk.items.weapons.Pistol(0, pp);
		player.inventory.equipWeapon(weapon);
		switch(difficultyLevel){
			case 0:
				player.inventory.addAmmoClips(weapon, 10);
				player.inventory.addItem(new HealthPack(pp, HealthPack.Type.medium));
				player.inventory.addItem(new HealthPack(pp, HealthPack.Type.medium));
				player.inventory.addItem(new HealthPack(pp, HealthPack.Type.medium));
				break;
			case 1:
				player.inventory.addAmmoClips(weapon, 4);
				player.inventory.addItem(new HealthPack(pp, HealthPack.Type.small));
				break;
			case 2:
				player.inventory.addAmmoClips(weapon, 2);
				break;
			case 3:
				player.inventory.addAmmoClips(weapon, 1);
				player.hp = 25;
				break;
		}
		currSubState = gameplaySubState;
		playerIsAlive = true;
		nextLevel();
	}
	
	public void nextLevel(){
		particles.clear();
		mobs.clear();
		mobs.add(player);
		items.clear();
		shots.clear();
		texts.clear();
		levelCount++;
		int s = 25 + levelCount + 2*LevelFactory.LEVEL_BORDER;
		int d = getLevelDifficultyLimit(levelCount, true);
		int r = getLevelRarityLimit(levelCount, true);
		LevelFactory f = new RoomsFactory(s, s);
//		LevelFactory f = new CaveAreaFactory(s, s);
		level = f.create(d, r);
		//level.print();
		player.pos = level.getNextFreeSpawnPoint().toFloat();
		currSubState.onNextLevel();
		recalcVisibleTiles = true;
	}
	
	public void printBalanceInfo(){
		player.skills.printSkillBalanceInfo();
		int totalXp = 0;
		for(int i=1; i<30; i++){
			int d = getLevelDifficultyLimit(i, false);
			totalXp += d;
			System.out.format("l %d: diff %d (%d total), rarity %d\n", i, d, 
					totalXp, getLevelRarityLimit(i, false));
		}
	}
	
	public int getLevelDifficultyLimit(int level, boolean addRandomPart){
		double d = Math.pow(level+3.4, 2.15);
		switch(difficultyLevel){
			case 0: d *= 0.7; break;
			case 2: d *= 1.5; break;
			case 3: d *= 2.5; break;
		}
		ExpRandom ran = new ExpRandom(0.75);
		if(addRandomPart) d *= 1 + 0.35 * ran.getNextDouble();
		return (int)d;
	}
	
	public int getLevelRarityLimit(int level, boolean addRandomPart){
		double r = 100 * Math.pow(level+5, 2.2);
		switch(difficultyLevel){
			case 0: r *= 0.8; break;
			case 2: r *= 1.4; break;
			case 3: r *= 1.8; break;
		}
		ExpRandom ran = new ExpRandom(0.9);
		if(addRandomPart) r *= 1 + 0.6 * ran.getNextDouble();
		return (int)r;
	}
	
	public float getSkillCostIncreaseRate(){
		switch(difficultyLevel){
			case 0: return 0.15f;
			case 1: return 0.3f;
			case 2: return 0.5f;
			case 3: return 0.8f;
		}
		throw new RuntimeException("difficulty level not registered! (" + difficultyLevel + ")");
	}
	
	public int getLevelCount(){
		return levelCount;
	}

	public void reduceScreenShake(float t){
		screenPosOffset.multiply((float)Math.pow(0.1, t/1000.0));
		screenShake = Math.max(0f, screenShake - t/1000f);
	}
	
	public void viewInventoryExchange(Inventory i1, Inventory i2){
		boolean wasDifferentState = currSubState != exchangeSubState;
		wasDifferentState |= i1 != exchangeSubState.getLeftInventory();
		wasDifferentState |= i2 != exchangeSubState.getRightInventory();
		currSubState = exchangeSubState;
		if(wasDifferentState) exchangeSubState.init(i1, i2);
	}
	
	public void viewInventory(Inventory i){
		boolean wasDifferentState = currSubState != inventorySubState;
		currSubState = inventorySubState;
		if(wasDifferentState || i != inventorySubState.getInventory()) inventorySubState.init(i);
	}
	
	public void viewSkills(SkillSet s){
		boolean wasDifferentState = currSubState != skillsSubState;
		currSubState = skillsSubState;
		if(wasDifferentState || s != skillsSubState.getSkillSet()) skillsSubState.init(s);
	}
	
	public void cheatOverview() {
		if (currSubState != overviewSubState) {
			currSubState = overviewSubState;
			overviewSubState.onActivate();
		}
	}

	public void cameraShake(float amount){
		screenShake = Math.max(amount, screenShake);
	}
	
	public void cameraRecoil(float angle, float amount){
		screenPosOffset.x += Math.cos(angle) * amount;
		screenPosOffset.y += Math.sin(angle) * amount;
	}
	
	public boolean shouldDrawMobOutsideVisionRange(Mob m){
		float d = player.totalStats.getBasicSenseRange();
		return m != player && player.pos.squaredDistanceTo(m.pos) <= d*d;
	}
	
	public boolean shouldDrawReloadBar(Mob m){
		float d =player.totalStats.getReloadSenseRange();
		return m == player || player.pos.squaredDistanceTo(m.pos) <= d*d;
	}
	
	public boolean shouldDrawHealthBar(Mob m){
		float d = player.totalStats.getHealthSenseRange();
		return m != player && player.pos.squaredDistanceTo(m.pos) <= d*d;
	}
	
	public InventoryItem getNearestItem(Mob m){
		InventoryItem i = null;
		float d = m.totalStats.getMaxPickupRange();
		for(InventoryItem i2 : items){
			float d2 = i2.pos.squaredDistanceTo(m.pos);
			if(d2 < d){
				i = i2;
				d = d2;
			}
		}
		return i;
	}
	
	public boolean moveMob(Mob m, float vx, float vy, int time, boolean collide){
		return moveThing(m.pos, vx, vy, m.size, time, collide);
	}
	
	public boolean moveThing(PointF pos, float vx, float vy, float size, int time, boolean collide){
		pos.x += vx * (time / 1000f);
		pos.y += vy * (time / 1000f);
		if(collide){
			PointF corr = level.doCollision(pos, size).corr;
			pos.add(corr);
			return !corr.isZero();
		}
		return false;
	}
	
	public void playerDied(){
		music.stop();
		playerIsAlive = false;
		player.hp = 0;
		currSubState = gameOverState;
		gameplaySubState.lootMode = false;
	}
	
	public void dropItem(InventoryItem i, Mob m, boolean useLookAngle){
		float a = (m == null || !useLookAngle)
				? (float)(2d * Math.PI * random.nextDouble())
				: m.getLookAngle();
		if(m != null) i.pos = m.pos.clone();
		float v = random.nextFloat() * 2f + 3f;
		i.vel.x = v * (float)Math.cos(a);
		i.vel.y = v * (float)Math.sin(a);
		i.angle = (float)(2d * Math.PI * random.nextDouble());
		i.vAngle = 0.4f * (random.nextFloat() - 0.5f);
		addItem(i);
	}
	
	public void addItem(InventoryItem i){
		i.parentInventory = null;
		i.initLabel();
		i.labelPos = i.pos.clone();
		items.add(i);
	}
	
	public LinkedList<Mob> getMobsInRange(Mob m){
		LinkedList<Mob> ans = new LinkedList<>();
		float d = m.totalStats.getSightRange();
		d *= d;
		for(Mob m2 : mobs) if(m2.pos.squaredDistanceTo(m.pos) <= d) ans.add(m2);
		return ans;
	}
	
	public boolean playerIsAlive(){
		return playerIsAlive;
	}
	
	public void addFallingText(String text, PointF pos, Color c, Mob parent){
		if(!level.isVisible(pos.round())) return;
		IngameText t = new IngameText(text, c, pos, 0.5f);
		t.vel.x = 1.9f*(random.nextFloat()-0.5f);
		t.vel.y = -2.8f;
		t.floorHeight = 0.3f*random.nextFloat();
		t.parent = parent;
		texts.add(t);
	}
	
	public void addFloatingText(String text, PointF pos, Color c, Mob parent){
		addFloatingText(text, 1f, pos, c, parent);
	}
	
	public void addFloatingText(String text, float scale, PointF pos, Color c, Mob parent){
		if(!level.isVisible(pos.round())) return;
		IngameText t = new IngameText(text, c, pos, scale);
		t.lifeTime = 800;
		t.vel.x = 0.2f*(random.nextFloat()-0.5f);
		t.vel.y = -0.2f;
		t.useGravity = false;
		t.parent = parent;
		texts.add(t);
	}
	
	public void addMob(Mob m){
		mobsToAdd.add(m);
	}
	
	public void addExplosion(PointF pos, Damage d, Shot shot, Sound sound){
		explosions.add(new Explosion(pos, d.getAreaRadius()));
		dealAreaDamage(pos, d, shot);
		cameraShake(EXPLODE_SHAKE_MULTIPLIER * d.getAreaRadius());
		if(sound != null) playSoundAt(sound, pos);
	}
	
	private int getAreaDamage(float r, float d, int dmg){
		// ratio : x = 1 - d/r
		// quadratic function: y = 1 - (x - 1)^2
		// -> y = 1 - (1 - d/r - 1)^2 = 1 - (d/r)^2
		return Math.round((1f - d*d/r/r) * dmg);
	}
	
	public void dealAreaDamage(PointF p, Damage damage, Shot s){
		// TODO: intelligently damage tiles (maybe reduce damage to mobs too when behind a wall?)
		float r = damage.getAreaRadius();
		if(r <= 0) throw new RuntimeException("deal area damage called without area damage object!");
		for(Mob m : mobs) if(!mobsToRemove.contains(m)){
			float dd = m.pos.squaredDistanceTo(p);
			if(dd < (r + m.size/2f) * (r + m.size/2f)){
				float d = Math.max(0f, (float)Math.sqrt(dd) - m.size/2f);
				int dmg = getAreaDamage(r, d, damage.calcFinalDamage(m.totalStats));
				damageMob(m, dmg, null, s);
			}
		}
		int normalDmg = damage.calcFinalDamage();
		PointI pc = p.round();
		int border = (int)Math.ceil(r);
		for(int x=-border; x<=border; x++){
			for(int y=-border; y<=border; y++){
				PointI pi = new PointI(x + pc.x, y + pc.y);
				float d = Math.max(0f, p.distanceTo(pi.toFloat()) - 0.5f);
				if(d >= 1) continue;
				int dmg = getAreaDamage(r, d, normalDmg);
				level.damageTile(pi, dmg, s == null ? p : s.pos);
			}
		}
		for(InventoryItem i : items){
			float dd = i.pos.squaredDistanceTo(p);
			if(dd < r*r){
				float d = Math.max(0f, (float)Math.sqrt(dd) - 0.5f);
				int dmg = getAreaDamage(r, d, normalDmg);
				if(dmg >= 4) itemsToRemove.add(i);
			}
		}
		float rm = 2.1f;
		for(Particle part : particles){
			float dd = part.pos.squaredDistanceTo(p);
			if(dd > 0f && dd < r*r*rm*rm){
				float force = 1f - dd/(r*r*rm*rm);
				PointF vel = part.pos.clone();
				vel.subtract(p);
				vel.multiply(2.6f * force / vel.length());
				part.vel.add(vel);
			}
		}
	}
	
	public void createDebris(PointF pos, Image img, int borderX, int borderY, float r){
		createDebris(pos, img, borderX, borderY, 3, 8, r);
	}
	
	public void createDebris(PointF pos, Image img, int borderX, int borderY, int min, int max, float r){
		int n = min;
		if(max > min) n += GameController.random.nextInt(max - min);
		for(int i=0; i<n; i++){
			Particle p = new Particle(img, borderX, borderY, pos, r);
			GameController.get().particles.add(p);
		}
	}
	
	public void createDebris(PointF pos, float dir, Image origImage, int borderX, int borderY, int min, int max, float r){
		int n = min;
		if(max > min) n += GameController.random.nextInt(max - min);
		for(int i=0; i<n; i++){
			Particle p = new Particle(origImage, borderX, borderY, pos, dir, r);
			GameController.get().particles.add(p);
		}
	}
	
	public void damageMob(Mob m, int dmg, PointF pos, Shot s){
		addFallingText(""+dmg, pos != null ? pos.clone() : m.pos.clone(), m == player ? Color.red : Color.green, null);
		dmg = Math.min(dmg, m.hp);
		m.totalDamageTaken += dmg;
		m.hp -= dmg;
		if(m.hp <= 0){
			if(m.onDeath(s)) mobsToRemove.add(m);
		} else {
			m.onHit(dmg, s);
		}
	}
	
	public void collisionStateChanged(PointF p1, PointF p2){
		for(Particle p : particles){
			if(p.pos.squaredDistanceToRect(p1, p2) < p.size*p.size/4f){
				PointF corr = level.doCollision(p.pos, p.size).corr;
				if(!corr.isZero()){
					p.pos.add(corr);
					p.vel.add(corr);
				}
			}
		}
		for(InventoryItem i : items){
			if(i.pos.squaredDistanceToRect(p1, p2) < i.size*i.size/4f){
				PointF corr = level.doCollision(i.pos, i.size).corr;
				if(!corr.isZero()){
					i.pos.add(corr);
					i.vel.add(corr);
				}
			}
		}
		for(Mob m : mobs){
			if(m.pos.squaredDistanceToRect(p1, p2) < m.size*m.size/4f){
				m.pos.add(level.doCollision(m.pos, m.size).corr);
			}
		}
	}
	
	public void requestDeleteMob(Mob m) {
		mobsToRemove.add(m);
	}
	
	public void requestDeleteShot(Shot s) {
		shotsToRemove.add(s);
	}
	
	public void requestDeleteExplosion(Explosion e) {
		explosionsToRemove.add(e);
	}
	
	public void requestDeleteItem(InventoryItem i) {
		itemsToRemove.add(i);
	}
	
	public boolean isMarkedForRemoval(Mob m){
		return mobsToRemove.contains(m);
	}
	
	public boolean isMarkedForRemoval(Shot s){
		return shotsToRemove.contains(s);
	}
	
	public boolean isMarkedForRemoval(Explosion e){
		return explosionsToRemove.contains(e);
	}
	
	public boolean isMarkedForRemoval(InventoryItem i){
		return itemsToRemove.contains(i);
	}
	
	public void update(GameContainer gc, StateBasedGame sbg, int time) throws SlickException{
		inputMap.update(time / 1000f);
		timeStamp += time;
		mousePosInPixels.x = gc.getInput().getMouseX();
		mousePosInPixels.y = gc.getInput().getMouseY();
		// update of states
		if(!isPaused()) omniSubState.update(this, gc, sbg, time);
		currSubState.update(this, gc, sbg, time);
		renderer.update(time);
		// remove marked stuff
		mobs.removeAll(mobsToRemove);
		for(Mob m : mobsToAdd) if(!mobs.contains(m)) mobs.add(m);
		shots.removeAll(shotsToRemove);
		explosions.removeAll(explosionsToRemove);
		items.removeAll(itemsToRemove);
		mobsToRemove.clear();
		mobsToAdd.clear();
		shotsToRemove.clear();
		explosionsToRemove.clear();
		itemsToRemove.clear();
	}
	
	public void render() throws SlickException{
		renderer.render(this);
	}
	
	private void renderVisionDebugInfo(){
		PointF p = mousePosInTiles.round().toFloat();
		PointF pps = transformTilesToScreen(player.pos);
		PointF ps = transformTilesToScreen(p);
		renderer.getGraphics().setColor(Color.yellow);
		renderer.getGraphics().drawLine(pps.x, pps.y, ps.x, ps.y);
		
		float r = transformTilesToScreen(player.totalStats.getSightRange());
		renderer.getGraphics().setColor(Color.yellow);
		renderer.getGraphics().drawOval(pps.x - r, pps.y - r, 2f * r, 2f * r);
		
		r = transformTilesToScreen(player.totalStats.getBasicSenseRange());
		renderer.getGraphics().setColor(Color.blue);
		renderer.getGraphics().drawOval(pps.x - r, pps.y - r, 2f * r, 2f * r);
		
		r = transformTilesToScreen(player.totalStats.getReloadSenseRange());
		renderer.getGraphics().setColor(Color.white);
		renderer.getGraphics().drawOval(pps.x - r, pps.y - r, 2f * r, 2f * r);
		
		r = transformTilesToScreen(player.totalStats.getHealthSenseRange());
		renderer.getGraphics().setColor(Color.red);
		renderer.getGraphics().drawOval(pps.x - r, pps.y - r, 2f * r, 2f * r);
		
		renderer.getGraphics().setColor(Color.yellow);
		LinkedList<PointI> l = level.getTilesOnLine(player.pos, p, 100);
		for(PointI pi : l){
			PointF p1 = pi.toFloat();
			p1.x -= 0.5;
			p1.y -= 0.5;
			p1 = transformTilesToScreen(p1);
			float s = transformTilesToScreen(1f);
			renderer.getGraphics().drawRect(p1.x, p1.y, s, s);
			if(level.isSightBlocking(pi.x, pi.y)) break;
		}
	}
	
	// ----- net code ----------------------------------------------------------
	
	public void netStateObjectCreated(NetStateObject o){
		objectMap.put(o.getID(), o);
	}
	
	public void netStateObjectDestroyed(NetStateObject o){
		objectMap.remove(o.getID());
	}
	
	public Collection<NetStateObject> getNetStateObjectList(){
		return objectMap.values();
	}
	
	public NetState createNetState(){
		NetState state = new NetState(timeStamp, objectMap.size());
		for(NetStateObject o : objectMap.values()) state.addObject(o);
		return state;
	}
	
	public boolean containsNetStateObject(long id){
		return objectMap.containsKey(id);
	}
	
	public NetStateObject getNetStateObject(long id){
		return objectMap.get(id);
	}
	
	public void setNetState(NetState state){
		// TODO: set state of every object, create new ones, remove old ones not contained in state
	}

}
