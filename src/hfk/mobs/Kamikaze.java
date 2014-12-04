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
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Kamikaze extends Mob {
	
	private static final float EXPLODE_DISTANCE = 0.9f;
	
	private final Animation walkAnimation, explodeAnimation;
	private final DamageCard damageCard;
	private final Sound explodeSound;

	public Kamikaze(PointF pos) {
		super(pos);
		SpriteSheet sheet = Resources.getSpriteSheet("e_kamikaze.png");
		walkAnimation = new Animation(sheet, new int[]{0,0,1,0,2,0,3,0}, new int[]{190,190,190,190});
		walkAnimation.update(GameController.random.nextInt(5000));
		explodeAnimation = new Animation(sheet, new int[]{0,1,1,1,2,1,3,1}, new int[]{75,75,75,75});
		explodeAnimation.setLooping(false);
		animation = walkAnimation;
		hitSound = Resources.getSound("e_star_hit.wav");
		alertSound = Resources.getSound("e_star_alert.wav");
		explodeSound = Resources.getSound("s_grenade_hit.wav");
		damageCard = DamageCard.createNormal();
		int fire = Damage.DamageType.fire.ordinal();
		damageCard.setDieCount(fire, 6);
		damageCard.setEyeCount(fire, 5);
		damageCard.setAreaRadius(1.8f);
		autoUseWeapon = false;
		autoReloadWeapon = false;
		moveWhilePlayerVisible = true;
		moveWhileShooting = true;
	}

	@Override
	public String getDisplayName() {
		return "KamiKaze";
	}

	@Override
	public int getDifficultyScore() {
		return 4;
	}

	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setMaxHP(15);
		c.setMaxSpeed(2f);
		c.setSightRange(4f);
		return c;
	}
	
	private void startExplosion(){
		animation = explodeAnimation;
		hp = 0;
	}
	
	private void explode(){
		GameController.get().requestDeleteMob(this);
		if(givesXP) GameController.get().player.xp += getDifficultyScore();
		GameController.get().addExplosion(pos, damageCard.createDamage(), null, explodeSound);
	}

	@Override
	public void mobUpdate(int time, boolean playerVisible) {
		if(playerVisible && animation != explodeAnimation){
			float dd = pos.squaredDistanceTo(lastPlayerPos);
			if(dd <= EXPLODE_DISTANCE*EXPLODE_DISTANCE) startExplosion();
		}
		if(explodeAnimation.isStopped()) explode();
	}

	@Override
	public boolean mobOnDeath(Shot s) {
		startExplosion();
		return false;
	}
	
}
