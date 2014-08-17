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
public class Scout extends Mob {

	private int playerTime = 0;
	private PointF playerPos = null;
	private boolean notifyMode = false;
	
	public Scout(PointF pos) {
		super(pos);
		SpriteSheet ss = Resources.getSpriteSheet("e_scout.png");
		animation = new Animation(ss, new int[]{0,0}, new int[]{1000});
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
	}

	@Override
	public float getSpawnProbabilityModifier() {
		return 0.1f;
	}

	@Override
	public String getDisplayName() {
		return "Scout Bot";
	}
	
	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setMaxHP(20);
		c.setMaxSpeed(3f);
		c.setSightRange(4f);
		return c;
	}

	@Override
	public int getDifficultyScore() {
		return 2;
	}
	
	public void flee(PointF p, boolean initial){
		notifyMode = true;
		playerPos = p;
		path = GameController.get().level.getPathAwayFrom(pos, p, 8, true);
	}

	@Override
	public void mobOnGetSightOfPlayer(PointF playerPos) {
		flee(playerPos, !notifyMode);
	}

	@Override
	public void mobOnHit(int dmg, Shot s) {
		if(s != null) flee(s.origin, !notifyMode);
	}

	@Override
	public void mobUpdate(int time, boolean playerVisible) {
		if(notifyMode){
			playerTime += time;
			// notify all near mobs
			for(Mob m : GameController.get().getMobsInRange(this)) m.notifyPlayerPosition(playerPos, playerTime);
		}
		if(path.isEmpty()){
			if(notifyMode){
				flee(playerPos, false);
			} else {
				createNewRandomPath();
			}
		}
	}

	@Override
	public void mobOnLostMemoryOfPlayer() {
		notifyMode = false;
		path.clear();
	}
	
	
}
