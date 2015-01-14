/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.mobs;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.stats.MobStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class KamikazeMaster extends Mob {

	public static final int SPAWN_TIME_LENGTH = 10000;
	public static final int SPAWN_TIME = 2000;
	
	private int spawnTimer = 0, timer = 0;
	
	public KamikazeMaster(PointF pos) {
		super(pos);
		SpriteSheet ss = Resources.getSpriteSheet("e_kamikazeSpawner.png");
		animation = new Animation(ss, 150);
		randomizeAnimationState();
		hitSound = Resources.getSound("e_star_hit.wav");
		deathSound = Resources.getSound("e_star_die.wav");
		alertSound = Resources.getSound("e_scout_alert.wav");
		allowPlayerPosNotify = false;
		autoFollowPlayer = false;
		autoFollowPlayerOnHit = false;
		autoFollowPlayerOnNotify = false;
		autoLockOnPlayer = false;
		autoReloadWeapon = false;
		autoUseWeapon = false;
		autoSetPath = false;
		canOpenDoors = true;
	}

	@Override
	public String getDisplayName() {
		return "KamiKaze Master";
	}
	
	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setMaxHP(20);
		c.setMaxSpeed(1.7f);
		c.setSightRange(4f);
		return c;
	}

	@Override
	public int getDifficultyScore() {
		return 12;
	}
	
	public void flee(PointF p){
		path = GameController.get().level.getPathAwayFrom(pos, p, 8, true, canOpenDoors);
	}

	@Override
	public void mobOnGetSightOfPlayer(PointF playerPos) {
		flee(pos);
		timer = SPAWN_TIME_LENGTH;
		lastPlayerPos = playerPos;
		lastPlayerTime = 0;
	}

	@Override
	public void mobOnHit(int dmg, Shot s) {
		flee(pos);
		timer = SPAWN_TIME_LENGTH;
		if(s != null){
			lastPlayerPos = s.origin;
			lastPlayerTime = 0;
		}
	}

	@Override
	public void mobUpdate(int time, boolean playerVisible) {
		// spawn minions if player position is known
		if(lastPlayerPos != null && timer > 0){
			timer -= time;
			spawnTimer -= time;
			while(spawnTimer <= 0){
				spawnTimer += SPAWN_TIME;
				Mob minion = new Kamikaze(pos.clone());
				minion.givesXP = false;
				minion.notifyPlayerPosition(lastPlayerPos, lastPlayerTime);
				minion.createPathToPlayer();
				GameController.get().addMob(minion);
			}
		} else {
			spawnTimer = 0;
		}
		if(path.isEmpty()){
			if(lastPlayerPos == null){
				createNewRandomPath();
			} else {
				flee(lastPlayerPos);
			}
		}
	}

}
