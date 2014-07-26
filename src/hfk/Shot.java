/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk;

import hfk.game.GameController;
import hfk.game.slickstates.GameplayState;
import hfk.items.weapons.Weapon;
import hfk.stats.Damage;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;

/**
 *
 * @author LostMekka
 */
public class Shot {
	
	public enum Team { friendly, hostile, dontcare }
	
	public int lifetime = -1;
	public float friction = 1f;
	public PointF pos, vel, origin;
	public Image img;
	public Sound hit;
	public float angle, size;
	public Damage dmg;
	public Team team;
	
	public Shot(Weapon w, Image img, Sound hit, float size){
		this(w.pos, img, hit, w.getScatteredAngle(), w.stats.shotVel, size, w.lengthOffset + w.weaponLength);
	}

	public Shot(PointF pos, Image img, Sound hit, float angle, float vel, float size, float startDiff) {
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		this.pos = new PointF(pos.x + cos * startDiff, pos.y + sin * startDiff);
		this.hit = hit;
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
			if(lifetime <= 0) GameController.get().shotsToRemove.add(this);
		}
		if(friction != 1) vel.multiply((float)Math.pow(friction, t));
	}
	
}
