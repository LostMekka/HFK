/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.mobs;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.items.weapons.Weapon;
import hfk.game.Resources;
import hfk.skills.Skill;
import hfk.stats.MobStatsCard;
import java.util.LinkedList;
import org.newdawn.slick.Animation;

/**
 *
 * @author LostMekka
 */
public class Player extends Mob {

	private final LinkedList<Skill> trackedSkills = new LinkedList<>();
	private String name = null;
	
	public Player(PointF pos){
		super(pos);
		animation = new Animation(Resources.getSpriteSheet("player.png"), 350);
		hitSound = Resources.getSound("p_hit.wav");
		deathSound = Resources.getSound("p_die.wav");
		autoFollowPath = false;
		autoFollowPlayer = false;
		autoFollowPlayerOnHit = false;
		autoFollowPlayerOnNotify = false;
		autoLockOnPlayer = false;
		autoReloadWeapon = false;
		autoSetPath = false;
		autoUseWeapon = false;
	}

	public boolean trackSkill(Skill s){
		s = skills.getSkill(s.name);
		if(trackedSkills.contains(s)) return false;
		trackedSkills.add(s);
		return true;
	}
	
	public boolean untrackSkill(Skill s){
		s = skills.getSkill(s.name);
		return trackedSkills.remove(s);
	}
	
	public void toggleTrackSkill(Skill s){
		s = skills.getSkill(s.name);
		if(trackedSkills.remove(s)) return;
		trackedSkills.add(s);
	}
	
	public boolean isTrackedSkill(Skill s){
		s = skills.getSkill(s.name);
		return trackedSkills.contains(s);
	}
	
	public LinkedList<Skill> getTrackedSkillsList(){
		return trackedSkills;
	}
	
	@Override
	public int getDifficultyScore() {
		return -1;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDisplayName() {
		return name == null ? "Player" : name;
	}

	@Override
	public void lookInDirection(float angle) {
		float a = getLookAngle();
		super.lookInDirection(angle);
		if(a != angle && totalStats.getVisionAngle() < (float)Math.PI * 2f){
			GameController.get().recalcVisibleTiles = true;
		}
	}

	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setMaxHP(100);
		c.setSightRange(5.5f);
		c.setVisionAngle(360f);
		c.setMaxSpeed(4f);
		c.setAmmoSlotSize(Weapon.AmmoType.bullet.ordinal(), 100);
		c.setAmmoSlotSize(Weapon.AmmoType.shell.ordinal(), 50);
		c.setAmmoSlotSize(Weapon.AmmoType.sniperRound.ordinal(), 20);
		c.setAmmoSlotSize(Weapon.AmmoType.grenade.ordinal(), 20);
		c.setAmmoSlotSize(Weapon.AmmoType.rocket.ordinal(), 5);
		c.setAmmoSlotSize(Weapon.AmmoType.plasmaRound.ordinal(), 150);
		c.setInventorySize(15);
		c.setHearRange(10);
		c.setQuickSlotCount(2);
		return c;
	}

	@Override
	public boolean mobOnDeath(Shot s) {
		GameController.get().playerDied();
		return true;
	}
	
}
