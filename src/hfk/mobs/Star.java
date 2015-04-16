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
import hfk.items.weapons.Weapon;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.WeaponStatsCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Star extends Mob {
	
	private final SpriteSheet sheet;
	private final Sound shootSound, shotHitSound;
	private final Image shotImage;
	private final DamageCard damageCard;

	public Star(PointF pos) {
		super(pos);
		sheet = Resources.getSpriteSheet("e_star.png");
		animation = new Animation(sheet, new int[]{1,0,0,0,1,1,0,0}, new int[]{300,300,300,300});
		animation.update(GameController.random.nextInt(5000));
		shootSound = Resources.getSound("star_shot.wav");
		shotHitSound = Resources.getSound("hit1.wav");
		hitSound = Resources.getSound("e_star_hit.wav");
		alertSound = Resources.getSound("e_star_alert.wav");
		deathSound = Resources.getSound("e_star_die.wav");
		shotImage = Resources.getImage("shot.png");
		damageCard = DamageCard.createNormal();
		int physical = Damage.DamageType.physical.ordinal();
		damageCard.setDieCount(physical, 1);
		damageCard.setEyeCount(physical, 5);
		autoUseWeapon = true;
		barrageTimeOnLost = 500;
		inventory.addAmmo(Weapon.AmmoType.bullet, 2 + GameController.random.nextInt(9));
		Weapon w = new Weapon(getLookAngle(), pos) {
			{shotTeam = Shot.Team.hostile;}
			@Override
			public Shot initShot(Shot s) {
				s.img = shotImage;
				s.hitSound = shotHitSound;
				s.size = 0.08f;
				return s;
			}
			@Override
			public WeaponStatsCard getDefaultWeaponStats() {
				WeaponStatsCard ans = WeaponStatsCard.createNormal();
				ans.clipSize[0] = 1;
				ans.ammoPerBurst[0] = 1;
				ans.reloadCount[0] = 1;
				ans.reloadTimes[0] = 2000;
				ans.shotInterval = 200;
				ans.maxScatter = 10f;
				ans.minScatter = 10f;
				ans.shotsPerBurst = 5;
				ans.shotVel = 4.8f;
				return ans;
			}
			@Override
			public DamageCard getDefaultDamageCard() {
				return damageCard;
			}
			@Override
			public String getWeaponName() {
				return "STAR_BIONIC_WPN";
			}
			@Override
			public long getRarityScore() {
				return 1;
			}
		};
		w.shotSound = shootSound;
		setBionicWeapon(w);
	}

	@Override
	public String getDisplayName() {
		return "Star";
	}

	@Override
	public int getDifficultyScore() {
		return 1;
	}

	@Override
	public MobStatsCard getDefaultMobStatsCard() {
		MobStatsCard c = MobStatsCard.createNormal();
		c.setMaxHP(30);
		c.setMaxSpeed(1f);
		c.setSightRange(4f);
		c.setAmmoSlotSize(Weapon.AmmoType.bullet.ordinal(), 1000);
		c.setAmmoSlotSize(Weapon.AmmoType.plasmaRound.ordinal(), 1000);
		return c;
	}
	
}
