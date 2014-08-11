/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import hfk.Explosion;
import hfk.game.substates.GameSubState;
import hfk.IngameText;
import hfk.Particle;
import hfk.PointF;
import hfk.PointI;
import hfk.Shot;
import hfk.game.substates.GameOverSubState;
import hfk.game.substates.GameplaySubState;
import hfk.game.substates.InventorySubState;
import hfk.game.substates.OmniSubState;
import hfk.game.substates.PauseSubState;
import hfk.game.substates.SkillsSubState;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.weapons.CheatRifle;
import hfk.items.weapons.DoubleBarrelShotgun;
import hfk.items.weapons.EnergyPistol;
import hfk.items.weapons.GrenadeLauncher;
import hfk.items.weapons.Pistol;
import hfk.items.weapons.PumpActionShotgun;
import hfk.items.weapons.RocketLauncher;
import hfk.items.weapons.SniperRifle;
import hfk.items.weapons.Weapon;
import hfk.level.Level;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import hfk.net.NetState;
import hfk.net.NetStateObject;
import hfk.skills.SkillSet;
import hfk.stats.Damage;
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
	
	public static final String VERSION = "0.0.13";
	
	public static final float SQRT2 = (float)Math.sqrt(2);
	public static final Random random = new Random();
	public static final float SPRITE_SIZE = 32f;
	
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
	
	public final LinkedList<Explosion> explosions = new LinkedList<>(), explosionsToRemove = new LinkedList<>();
	public final LinkedList<Shot> shots = new LinkedList<>(), shotsToRemove = new LinkedList<>();
	public final LinkedList<Mob> mobs = new LinkedList<>(), mobsToRemove = new LinkedList<>();
	public final LinkedList<InventoryItem> items = new LinkedList<>(), itemsToRemove = new LinkedList<>();
	public final LinkedList<IngameText> texts = new LinkedList<>();
	public final LinkedList<Particle> particles = new LinkedList<Particle>();

	public GameOverSubState gameOverState;
	public GameplaySubState gameplaySubState;
	public InventorySubState inventorySubState;
	public OmniSubState omniSubState;
	public PauseSubState pauseSubState;
	public SkillsSubState skillsSubState;

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
	public boolean musicIsOn = true;
	
	private final InputMap inputMap;
	
	private GameSubState currSubState;
	private Music music = null;
	private float zoom = 3f;
	private boolean playerIsAlive = true;
	private int levelCount = 0;
	private long nextID = 0, timeStamp = 0;
	private HashMap<Long, NetStateObject> objectMap = new HashMap<>();

	public GameController(GameContainer gc, GameSettings s) {
		settings = s;
		inputMap = new InputMap(gc.getInput());
		gameOverState = new GameOverSubState(inputMap);
		gameplaySubState = new GameplaySubState(inputMap);
		inventorySubState = new InventorySubState(inputMap);
		omniSubState = new OmniSubState(inputMap);
		pauseSubState = new PauseSubState(inputMap);
		skillsSubState = new SkillsSubState(inputMap);
		currSubState = gameplaySubState;
		renderer = new GameRenderer(gc);
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
		music = Resources.getMusic("creepy_006.ogg");
		renderer.initAfterLoading();
		gameOverState.initAfterLoading(this, gc);
		gameplaySubState.initAfterLoading(this, gc);
		inventorySubState.initAfterLoading(this, gc);
		omniSubState.initAfterLoading(this, gc);
		pauseSubState.initAfterLoading(this, gc);
		skillsSubState.initAfterLoading(this, gc);
		newGame();
	}
	
	public void newGame(){
		if(musicIsOn) startMusic();
		levelCount = 0;
		PointF pp = new PointF();
		player = new Player(pp);
		player.inventory.equipWeaponFromGround(new Pistol(0, pp));
		player.inventory.addAmmo(Weapon.AmmoType.bullet, 50);
		//for(int i=0; i<20; i++) player.inventory.addItem(InventoryItem.create(pp, 99999999));
		if(musicIsOn) startMusic();
		currSubState = gameplaySubState;
		playerIsAlive = true;
		nextLevel();
//		for(int i=1; i<30; i++){
//			System.out.println("l: " + i + " d: " + 
//					getLevelDifficultyLimit(i) + " r: " + 
//					getLevelRarityLimit(i));
//		}
	}
	
	public void nextLevel(){
		particles.clear();
		mobs.clear();
		mobs.add(player);
		items.clear();
		shots.clear();
		texts.clear();
		levelCount++;
		int s = 20 + levelCount;
		int d = getLevelDifficultyLimit(levelCount);
		int r = getLevelRarityLimit(levelCount);
		level = Level.Factory.createTestArena(s, s, d, r);
	}
	
	public int getLevelDifficultyLimit(int level){
		// TODO: use difficultyLevel
		return (int)(Math.pow(level+3, 1.95) * (0.85f + 0.3f * random.nextFloat()));
	}
	
	public int getLevelRarityLimit(int level){
		// TODO: use difficultyLevel
		return 100*(int)(Math.pow(level+5, 2.1) * (1f + 5f*random.nextFloat()*random.nextFloat()*random.nextFloat()*random.nextFloat()));
	}
	
	public float getSkillCostIncreaseRate(){
		// TODO: use difficultyLevel
		return 0.5f;
	}
	
	public int getLevelCount(){
		return levelCount;
	}

	public void reduceScreenShake(float t){
		screenPosOffset.multiply((float)Math.pow(0.1, t/1000.0));
		screenShake = Math.max(0f, screenShake - t/1000f);
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
	
	public void cameraShake(float amount){
		screenShake = Math.max(amount, screenShake);
	}
	
	public void cameraRecoil(float angle, float amount){
		screenPosOffset.x += Math.cos(angle) * amount;
		screenPosOffset.y += Math.sin(angle) * amount;
	}
	
	public boolean shouldDrawReloadBar(Mob m){
		return m == player || player.skills.shouldRenderReloadBar(m);
	}
	
	public boolean shouldDrawHealthBar(Mob m){
		return m != player && player.skills.shouldRenderHealthBar(m);
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
	
	public void moveMob(Mob m, float vx, float vy, int time, boolean collide){
		moveThing(m.pos, vx, vy, m.size, time, collide);
	}
	
	public void moveThing(PointF pos, float vx, float vy, float size, int time, boolean collide){
		pos.x += vx * (time / 1000f);
		pos.y += vy * (time / 1000f);
		if(collide){
			PointF corr = level.doCollision(pos, size);
			pos.add(corr);
		}
	}
	
	public void playerDied(){
		music.stop();
		playerIsAlive = false;
		player.hp = 0;
		currSubState = gameOverState;
		((GameplaySubState)gameplaySubState).lootMode = false;
	}
	
	public void dropItem(InventoryItem i, Mob m, boolean useLookAngle){
		float a = m.getLookAngle();
		if(!useLookAngle) a = (float)(2d * Math.PI * random.nextDouble());
		i.pos = m.pos.clone();
		float v = random.nextFloat() * 2f + 3f;
		i.vel.x = v * (float)Math.cos(a);
		i.vel.y = v * (float)Math.sin(a);
		i.angle = (float)(2d * Math.PI * random.nextDouble());
		i.vAngle = 0.4f * (random.nextFloat() - 0.5f);
		addItem(i);
	}
	
	public void addItem(InventoryItem i){
		i.parentInventory = null;
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
		IngameText t = new IngameText(text, c, pos, scale);
		t.lifeTime = 800;
		t.vel.x = 0.2f*(random.nextFloat()-0.5f);
		t.vel.y = -0.2f;
		t.useGravity = false;
		t.parent = parent;
		texts.add(t);
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
		if(r <= 0) throw new RuntimeException("deeal area damage called without area damage object!");
		for(Mob m : mobs){
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
				float d = Math.max(0f, p.DistanceTo(pi.toFloat()) - 0.5f);
				if(d >= 1) continue;
				int dmg = getAreaDamage(r, d, normalDmg);
				level.damageTile(pi, dmg);
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
			if(dd < r*r*rm*rm){
				float force = 1f - dd/(r*r*rm*rm);
				PointF vel = part.pos.clone();
				vel.subtract(p);
				vel.multiply(2.6f * force / vel.length());
				part.vel.add(vel);
			}
		}
	}
	
	public void createDebris(PointF pos, Image i, int border){
		createDebris(pos, i, border, 3, 8);
	}
	
	public void createDebris(PointF pos, Image origImage, int border, int min, int max){
		int n = GameController.random.nextInt(5) + 4;
		for(int i=0; i<n; i++){
			Particle p = new Particle(origImage, border, pos, 0.4f);
			GameController.get().particles.add(p);
		}
	}
	
	public void damageMob(Mob m, int dmg, PointF pos, Shot s){
		addFallingText(""+dmg, pos != null ? pos.clone() : m.pos.clone(), m == player ? Color.red : Color.green, null);
		m.hp -= dmg;
		if(m.hp <= 0){
			m.onDeath(s);
			mobsToRemove.add(m);
		} else {
			m.onHit(dmg, s);
		}
	}
	
	public void update(GameContainer gc, StateBasedGame sbg, int time) throws SlickException{
//		if(inputMap.isKeyPressed(InputMap.A_CLOSE_INVENTORY)) nextLevel();
		timeStamp += time;
		mousePosInPixels.x = gc.getInput().getMouseX();
		mousePosInPixels.y = gc.getInput().getMouseY();
		// update of states
		if(!isPaused()) omniSubState.update(this, gc, sbg, time);
		currSubState.update(this, gc, sbg, time);
		// remove marked stuff
		mobs.removeAll(mobsToRemove);
		shots.removeAll(shotsToRemove);
		explosions.removeAll(explosionsToRemove);
		items.removeAll(itemsToRemove);
		mobsToRemove.clear();
		shotsToRemove.clear();
		explosionsToRemove.clear();
		itemsToRemove.clear();
		// update input
		inputMap.update(time / 1000f); // must be last
	}
	
	public void render() throws SlickException{
		renderer.render(this);
	}
	
	// ----- net code ----------------------------------------------------------
	
	public void netStateObjectCreated(NetStateObject o){
		objectMap.put(o.getID(), o);
	}
	
	public NetState createNetState(){
		NetState state = new NetState(timeStamp);
		// TODO: create net state parts for each directly known game object
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
