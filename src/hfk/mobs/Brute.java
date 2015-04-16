/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.mobs;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.items.weapons.PlasmaStorm;
import hfk.items.weapons.Weapon;
import hfk.skills.Skill;
import hfk.stats.Damage;
import hfk.stats.MobStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Brute extends Mob {

	public Brute(PointF pos) {
		super(pos);
		SpriteSheet sheet = Resources.getSpriteSheet("e_brute.png");
		animation = new Animation(sheet, 300);
		randomizeAnimationState();
		hitSound = Resources.getSound("e_star_hit.wav");
		deathSound = Resources.getSound("e_star_die.wav");
		alertSound = Resources.getSound("e_star_alert.wav");
		barrageTimeOnHit = 2000;
		barrageTimeOnLost = 1500;
		Weapon w = new PlasmaStorm(0f, pos.clone());
		setBionicWeapon(w);
		inventory.addAmmo(Weapon.AmmoType.plasmaRound, GameController.random.nextInt(31) + 10);
		xp = 1000000;
		Skill spider = skills.getSkill("spider senses");
		spider.levelUp();
		spider.levelUp();
		Skill rapid = skills.getSkill("rapid fire");
		rapid.levelUp();
		rapid.levelUp();
	}

	@Override
	public int getDifficultyScore() {
		return 35;
	}

	@Override
	public String getDisplayName() {
		return "Brute";
	}
	
	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setAmmoSlotSize(Weapon.AmmoType.plasmaRound.ordinal(), 150);
		c.setMaxHP(100);
		c.setSightRange(4.6f);
		c.setMaxSpeed(0.8f);
		c.setResistance(Damage.DamageType.plasma.ordinal(), 6);
		c.setResistance(Damage.DamageType.shock.ordinal(), 5);
		return c;
	}
	
}
