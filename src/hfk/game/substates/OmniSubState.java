/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.IngameText;
import hfk.PointF;
import hfk.PointI;
import hfk.Shot;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.items.InventoryItem;
import hfk.mobs.Mob;
import java.util.LinkedList;
import java.util.ListIterator;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class OmniSubState extends GameSubState {

	// this state is always present. of all other states, only one is present at a given time
	
	public OmniSubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_M, InputMap.A_TOGGLEMUSIC);
	}

	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		ctrl.reduceScreenShake(time);
		if(getInputMap().isKeyPressed(InputMap.A_TOGGLEMUSIC)) ctrl.toggleMusic();
		// updates
		for(InventoryItem i1 : ctrl.items){
			i1.update(time);
			if(!i1.vel.isZero()) i1.pos.add(ctrl.level.doCollision(i1.pos, i1.size));
			boolean blockX = false;
			boolean blockY = false;
			PointF corr = new PointF();
			PointF i1Pos = i1.getTopLeftLabelPos();
			for(InventoryItem i2 : ctrl.items){
				if(i1 == i2) continue;
				PointF i2Pos = i2.getTopLeftLabelPos();
				PointF c = ctrl.level.doRectCollision(
						i1Pos.x, i1Pos.y, i1.labelSize.x, i1.labelSize.y, 
						i2Pos.x, i2Pos.y, i2.labelSize.x, i2.labelSize.y);
				if(corr.x * c.x < 0){
					blockX = true;
					corr.x = 0f;
				}
				if(!blockX && Math.abs(c.x) > Math.abs(corr.x)) corr.x = c.x;
				if(corr.y * c.y < 0f){
					blockY = true;
					corr.y = 0f;
				}
				if(!blockY && Math.abs(c.y) > Math.abs(corr.y)) corr.y = c.y;
			}
			i1.labelPos.add(corr);
		}
		ListIterator<IngameText> textIter = ctrl.texts.listIterator();
		while(textIter.hasNext()) if(textIter.next().update(time)) textIter.remove();
		LinkedList<Mob> mobsToDelete = new LinkedList<>();
		for(Mob m : ctrl.mobs) m.update(time);
		
		ListIterator<Shot> shotIter = ctrl.shots.listIterator();
		while(shotIter.hasNext()){
			Shot s = shotIter.next();
			s.update(time);
			PointI coll = ctrl.level.testCollision(s.pos, s.size);
			if(coll != null){
				shotIter.remove();
				ctrl.playSoundAt(s.hit, s.pos);
				ctrl.level.damageTile(coll, s.dmg.calcFinalDamage());
				continue;
			}
			for(Mob m : ctrl.mobs){
				if(m.pos.squaredDistanceTo(s.pos) <= (m.size + s.size) / 2f){
					// collision with mob!
					if(s.team == Shot.Team.friendly && m == ctrl.player) continue;
					if(s.team == Shot.Team.hostile && m != ctrl.player) continue;
					// valid collision! deal damage and remove shot!
					shotIter.remove();
					ctrl.playSoundAt(s.hit, s.pos);
					float r = s.dmg.getAreaRadius();
					if(r > 0){
						for(Mob m2 : ctrl.mobs){
							if(s.team == Shot.Team.friendly && m2 == ctrl.player) continue;
							if(s.team == Shot.Team.hostile && m2 != ctrl.player) continue;
							float d = m2.pos.squaredDistanceTo(s.pos) - m2.size / 2f;
							if(d < 0f) d = 0f;
							if(d < r){
								int dmg = Math.round((1f - d/r) * s.dmg.calcFinalDamage(m2.totalStats));
								ctrl.addFallingText(""+dmg, s.pos.clone(), m2 == ctrl.player ? Color.red : Color.green, null);
								m2.hp -= dmg;
								if(m2.hp <= 0){
									m2.onDeath(s);
									mobsToDelete.add(m2);
								} else {
									m2.onHit(dmg, s);
								}
							}
						}
					} else {
						int dmg = s.dmg.calcFinalDamage(m.totalStats);
						ctrl.addFallingText(""+dmg, s.pos.clone(), m == ctrl.player ? Color.red : Color.green, null);
						m.hp -= dmg;
						if(m.hp <= 0){
							m.onDeath(s);
							mobsToDelete.add(m);
						} else {
							m.onHit(dmg, s);
						}
					}
					break;
				}
			}
			ctrl.mobs.removeAll(mobsToDelete);
			mobsToDelete.clear();
		}
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		PointF screenPos = ctrl.getScreenPos();
		ctrl.level.draw(
				(int)Math.floor(screenPos.x - 0.5f),
				(int)Math.floor(screenPos.y - 0.5f),
				(int)Math.ceil(ctrl.transformScreenToTiles(gc.getWidth()) + screenPos.x - 0.5f),
				(int)Math.ceil(ctrl.transformScreenToTiles(gc.getHeight()) + screenPos.y - 0.5f));
		for(InventoryItem i : ctrl.items) i.render();
		for(Mob m : ctrl.mobs) m.draw();
		for(Shot s : ctrl.shots) s.draw();
		for(IngameText t : ctrl.texts) t.draw();
	}
	
}
