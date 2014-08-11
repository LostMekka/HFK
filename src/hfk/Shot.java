/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk;

import hfk.game.GameController;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.stats.Damage;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;

/**
 *
 * @author LostMekka
 */
public class Shot {
	
	public enum Team { friendly, hostile, dontcare }
	
	public static final float EXPLODE_SHAKE_MULTIPLIER = 0.2f;
	
	public int lifetime = -1;
	public float friction = 0f;
	public PointF pos, vel, origin;
	public Image img;
	public Sound hitSound, bounceSound;
	public float angle, size;
	public Damage dmg;
	public Team team;
	public boolean isGrenade = false;
	public Weapon parent = null;
	// skill based extra stuff
	public int bounceCount = 0;
	public float overDamageSplashRadius = 0f;
	public int smartGrenadeLevel = 0; // 0 = explode on contact, 1 = bounce off walls, 2 = no friendly fire
	public int manualDetonateLevel = 0;
	
	public Shot(Weapon w, Image img, Sound hit, float size){
		this(w.pos, img, hit, w.getScatteredAngle(), w.totalStats.shotVel, size, w.lengthOffset + w.weaponLength);
	}

	public Shot(PointF pos, Image img, Sound hit, float angle, float vel, float size, float startDiff) {
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		this.pos = new PointF(pos.x + cos * startDiff, pos.y + sin * startDiff);
		this.hitSound = hit;
		this.img = img;
		this.angle = angle;
		this.size = size;
		this.vel = new PointF(cos * vel, sin * vel);
		origin = pos.clone();
	}
	
	public void draw(){
		GameController.get().renderer.drawImage(img, pos, 1f, angle);
	}
	
	public void update(int time){
		float t = time / 1000f;
		pos.x += vel.x * t;
		pos.y += vel.y * t;
		if(lifetime >= 0){
			lifetime -= time;
			if(lifetime <= 0 && manualDetonateLevel <= 0){
				GameController.get().shotsToRemove.add(this);
				if(isGrenade){
					GameController.get().dealAreaDamage(pos, dmg, this);
					GameController.get().playSoundAt(hitSound, pos);
					hit();
				}
			}
		}
		if(friction != 0f){
			float l = vel.length();
			float diff = friction * t;
			if(l >= diff){
				vel.multiply((l - diff) / l);
			} else {
				vel.x = 0f;
				vel.y = 0f;
			}
		}
	}
	
	public void hit(){
		GameController ctrl = GameController.get();
		ctrl.shotsToRemove.add(this);
		if(hitSound != null) ctrl.playSoundAt(hitSound, pos);
		if(dmg.getAreaRadius() > 0){
			ctrl.dealAreaDamage(pos, dmg, this);
			ctrl.explosions.add(new Explosion(pos, dmg.getAreaRadius()));
			ctrl.cameraShake(EXPLODE_SHAKE_MULTIPLIER * dmg.getAreaRadius());
		}
	}
	
	public void hitSingle(Mob m){
		GameController ctrl = GameController.get();
		ctrl.shotsToRemove.add(this);
		if(hitSound != null) ctrl.playSoundAt(hitSound, pos);
		ctrl.damageMob(m, dmg.calcFinalDamage(m.totalStats), pos, this);
	}
	
	public void hitSingle(PointI tilePos){
		GameController ctrl = GameController.get();
		ctrl.shotsToRemove.add(this);
		if(hitSound != null) ctrl.playSoundAt(hitSound, pos);
		ctrl.level.damageTile(tilePos, dmg.calcFinalDamage());
	}
	
	public boolean onCollideWithMob(Mob m){
		if(manualDetonateLevel > 0) return false;
		GameController ctrl = GameController.get();
		if(!isGrenade || smartGrenadeLevel >= 2){
			if(team == Shot.Team.friendly && m == ctrl.player) return false;
			if(team == Shot.Team.hostile && m != ctrl.player) return false;
		}
		// valid collision! deal damage and remove shot!
		if(dmg.getAreaRadius() > 0){
			hit();
		} else {
			hitSingle(m);
		}
		return true;
	}
	
	public void onCollideWithLevel(PointF corr){
		GameController ctrl = GameController.get();
		// get the position of the colliding tile
		PointF tmp = pos.clone();
		tmp.add(corr);						// get the corrected position
		corr.multiply(size / corr.length());	// set corr length to shot size
		tmp.subtract(corr);					// this should move the point right to the edge of the colliding tile
		corr.multiply(0.1f / size);			// set corr length to something small
		tmp.subtract(corr);					// move the point over the edge into the colliding tile
		PointI tilePos = tmp.round();
		// handle collision
		if(smartGrenadeLevel > 0 || manualDetonateLevel > 0 || bounceCount > 0){
			pos.add(corr);
			PointF bounceAns = vel.bounce(corr, 0.4f);
			angle += bounceAns.y;
			if(!isGrenade && manualDetonateLevel <= 0){
				ctrl.level.damageTile(tilePos, Math.round(bounceAns.x * dmg.calcFinalDamage()));
			}
			bounceCount--;
			if(bounceSound != null) ctrl.playSoundAt(bounceSound, pos);
		} else {
			if(dmg.getAreaRadius() > 0){
				hit();
			} else {
				hitSingle(tilePos);
			}
		}
	}
	
}
