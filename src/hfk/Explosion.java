/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.Resources;
import org.newdawn.slick.Animation;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Explosion {
	
	public PointF pos;
	
	private int lifeTime, maxLifeTime;
	private float radius, maxRadius;
	private Animation animation;

	public Explosion(PointF pos, float maxRadius) {
		this.pos = pos.clone();
		this.maxRadius = maxRadius;
		radius = 0f;
		lifeTime = 0;
		maxLifeTime = 250;
		Image image = Resources.getImage("ex_002.png");
		animation = new Animation(new SpriteSheet(image, 64, 64), maxLifeTime / 10);
	}
	
	public void update(int time){
		lifeTime += time;
		animation.update(time);
		if(lifeTime > maxLifeTime){
			radius = maxRadius;
			GameController.get().explosionsToRemove.add(this);
		} else {
			radius = maxRadius * (float)Math.sin(Math.PI / 2 * lifeTime / maxLifeTime);
		}
	}
	
	public void draw(GameRenderer r, GameContainer gc){
		GameController ctrl = GameController.get();
		PointF p = ctrl.transformTilesToScreen(pos);
		float rad = ctrl.transformTilesToScreen(radius);
		r.drawImage(animation.getCurrentFrame(), pos, maxRadius, false);
//		r.getGraphics().setColor(Color.white);
//		r.getGraphics().drawOval(p.x-rad, p.y-rad, 2*rad, 2*rad);
	}
	
}
