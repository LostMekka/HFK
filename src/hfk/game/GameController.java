/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import hfk.game.substates.GameSubState;
import hfk.IngameText;
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
import hfk.items.weapons.Pistol;
import hfk.items.weapons.Weapon;
import hfk.level.Level;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import java.util.LinkedList;
import java.util.Random;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameController {
	
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
	
	public final LinkedList<Shot> shots = new LinkedList<>();
	public final LinkedList<Shot> shotsToRemove = new LinkedList<>();
	public final LinkedList<Mob> mobs = new LinkedList<>();
	public final LinkedList<IngameText> texts = new LinkedList<>();
	public final LinkedList<InventoryItem> items = new LinkedList<>();

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
	
	private final InputMap inputMap;
	
	private GameSubState currSubState;
	private Music music = null;
	private float zoom = 3f;
	private boolean playerIsAlive = true, musicIsOn = true;
	private int levelCount = 0;

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
		float hr = player.stats.getHearRange();
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
		Weapon w = new Pistol(0, pp);
		player = new Player(pp);
		player.inventory.equipWeaponFromGround(w);
		player.inventory.addAmmo(Weapon.AmmoType.bullet, 30);
		//for(int i=0; i<20; i++) player.inventory.addItem(InventoryItem.create(pp, 99999999));
		if(musicIsOn) startMusic();
		currSubState = gameplaySubState;
		playerIsAlive = true;
		nextLevel();
	}
	
	public void nextLevel(){
		mobs.clear();
		mobs.add(player);
		items.clear();
		shots.clear();
		texts.clear();
		levelCount++;
		int s = 20 + levelCount * 2;
		int d = (int)Math.pow(levelCount+5, 1.5);
		int r = (int)(Math.pow((levelCount)*100, 1.5) * (1f + 5f*random.nextFloat()*random.nextFloat()*random.nextFloat()));
		level = Level.Factory.createTestArena(s, s, d, r);
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
	
	public void cameraShake(float amount){
		screenShake = Math.max(amount, screenShake);
	}
	
	public void cameraRecoil(float angle, float amount){
		screenPosOffset.x += Math.cos(angle) * amount;
		screenPosOffset.y += Math.sin(angle) * amount;
	}
	
	public boolean shouldDrawReloadBar(Mob m){
		// TODO: base answer on skills
		return true;
	}
	
	public InventoryItem getNearestItem(Mob m){
		InventoryItem i = null;
		float d = m.stats.getMaxPickupRange();
		for(InventoryItem i2 : items){
			float d2 = i2.pos.squaredDistanceTo(m.pos);
			if(d2 < d){
				i = i2;
				d = d2;
			}
		}
		return i;
	}
	
	public void moveMob(Mob m, float vx, float vy, int time){
		m.pos.x += vx * (time / 1000f);
		m.pos.y += vy * (time / 1000f);
		PointF corr = level.doCollision(m.pos, m.size);
		m.pos.add(corr);
	}
	
	public void playerDied(){
		music.stop();
		playerIsAlive = false;
		player.hp = 0;
		currSubState = gameOverState;
		((GameplaySubState)gameplaySubState).lootMode = false;
		// TODO: handle player death properly
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
		float d = m.stats.getSightRange();
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
	public void update(GameContainer gc, StateBasedGame sbg, int time) throws SlickException{
		mousePosInPixels.x = gc.getInput().getMouseX();
		mousePosInPixels.y = gc.getInput().getMouseY();
		if(!isPaused()) omniSubState.update(this, gc, sbg, time);
		currSubState.update(this, gc, sbg, time);
		shots.removeAll(shotsToRemove);
		shotsToRemove.clear();
		inputMap.update(time / 1000f); // must be last
	}
	
	public void render() throws SlickException{
		renderer.render(this);
	}
	
}
