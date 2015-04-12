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
import hfk.items.ItemType;
import hfk.items.weapons.Weapon;
import hfk.skills.Skill;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.WeaponStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Grunt extends Mob {

	public Grunt(PointF pos) {
		super(pos);
		SpriteSheet sheet = Resources.getSpriteSheet("e_grunt.png");
		animation = new Animation(sheet, 300);
		randomizeAnimationState();
		hitSound = Resources.getSound("e_star_hit.wav");
		deathSound = Resources.getSound("e_star_die.wav");
		alertSound = Resources.getSound("e_star_alert.wav");
		barrageTimeOnHit = 300;
		barrageTimeOnLost = 300;
		Weapon w = new Weapon(0.5f, pos) {
			{shotTeam = Shot.Team.hostile; type = ItemType.wGrenadeLauncher;}
			@Override
			public Shot initShot(Shot s) {
				s.img = Resources.getImage("s_grenade.png");
				s.bounceSound = Resources.getSound("s_grenade_bounce.wav");
				s.hitSound = Resources.getSound("s_grenade_hit.wav");
				s.size = 0.08f;
				s.friction = 4f + 9f * GameController.random.nextFloat();
				s.lifetime = 1200 + GameController.random.nextInt(800);
				return s;
			}
			@Override
			public WeaponStatsCard getDefaultWeaponStats() {
				WeaponStatsCard ans = WeaponStatsCard.createNormal();
				int gren = Weapon.AmmoType.grenade.ordinal();
				ans.clipSize[gren] = 1;
				ans.ammoPerBurst[gren] = 1;
				ans.reloadCount[gren] = 1;
				ans.reloadTimes[gren] = 5000;
				ans.shotsPerBurst = 5;
				ans.projectilesPerShot = 2;
				ans.minScatter = 30f;
				ans.maxScatter = 90f;
				ans.scatterPerShot = 10f;
				ans.scatterCoolRate = 8f;
				ans.shotVel = 7f;
				return ans;
			}
			@Override
			public DamageCard getDefaultDamageCard() {
				DamageCard ans = DamageCard.createNormal();
				int phys = Damage.DamageType.physical.ordinal();
				int fire = Damage.DamageType.fire.ordinal();
				ans.setDieCount(phys, 5);
				ans.setEyeCount(phys, 6);
				ans.setDieCount(fire, 2);
				ans.setEyeCount(fire, 7);
				ans.setAreaRadius(1f);
				return ans;
			}
			@Override
			public String getWeaponName() {
				return "grunt bionic weapon";
			}
			@Override
			public long getRarityScore() {
				return 1;
			}
		};
		w.shotSound = Resources.getSound("w_sg_s.wav");
		setBionicWeapon(w);
		inventory.addAmmo(Weapon.AmmoType.grenade, GameController.random.nextInt(36) + 5);
		xp = 1000000;
		Skill spider = skills.getSkill("spider senses");
		spider.levelUp();
		spider.levelUp();
		Skill smart = skills.getSkill("smart grenades");
		smart.levelUp();
		smart.levelUp();
	}

	@Override
	public int getDifficultyScore() {
		return 20;
	}

	@Override
	public String getDisplayName() {
		return "Grunt";
	}
	
	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setAmmoSlotSize(Weapon.AmmoType.grenade.ordinal(), 20);
		c.setMaxHP(300);
		c.setSightRange(4.6f);
		c.setMaxSpeed(0.5f);
		c.setResistance(Damage.DamageType.physical.ordinal(), 3);
		c.setResistance(Damage.DamageType.fire.ordinal(), 3);
		return c;
	}
	
}
