/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level;

import hfk.PointI;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.mobs.Mob;
import hfk.stats.Damage;
import hfk.stats.DamageCard;
import org.newdawn.slick.Animation;
import org.newdawn.slick.Sound;

/**
 *
 * @author LostMekka
 */
public class ExplosiveBarrel extends UsableLevelItem {

	public static DamageCard dc = null;
	public static Sound exSound = null;
	
	private Animation animation;
	private int timer = -1;
	
	public ExplosiveBarrel(PointI pos) {
		super(pos);
		if(dc == null){
			dc = DamageCard.createNormal();
			int fire = Damage.DamageType.fire.ordinal();
			int phys = Damage.DamageType.physical.ordinal();
			dc.setDieCount(fire, 20);
			dc.setEyeCount(fire, 3);
			dc.setDieCount(phys, 20);
			dc.setDieCount(phys, 3);
			dc.setAreaRadius(1.5f);
			exSound = Resources.getSound("s_grenade_hit.wav");
		}
		hp = 5 + GameController.random.nextInt(26);
		animation = new Animation(Resources.getSpriteSheet("barrel.png"), 300 + GameController.random.nextInt(100));
		animation.update(GameController.random.nextInt(5000));
		size = 0.6f;
	}

	@Override
	public void update(int time) {
		animation.update(time);
		img = animation.getCurrentFrame();
		if(timer > 0){
			timer -= time;
			if(timer <= 0){
				GameController.get().addExplosion(pos.toFloat(), dc.createDamage(), null, exSound);
				GameController.get().level.requestDeleteItem(this);
			}
		}
	}

	@Override
	public boolean damage(int dmg) {
		if(super.damage(dmg)) timer = GameController.random.nextInt(40) + 60;
		return false;
	}

	@Override
	public boolean canUse(Mob m) {
		return false;
	}

	@Override
	public boolean useInternal(Mob m) {
		return false;
	}

	@Override
	public String getDisplayName() {
		return " explosive barrel";
	}
	
}
