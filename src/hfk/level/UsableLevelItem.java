/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level;

import hfk.PointI;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.mobs.Mob;
import hfk.net.NetState;
import hfk.net.NetStateObject;
import hfk.net.NetStatePart;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public abstract class UsableLevelItem implements NetStateObject{
	
	public PointI pos;
	public Image img = null;
	public int hp;
	public float size = 1f;
	private long id;

	public UsableLevelItem() {
		pos = new PointI();
	}
	
	public UsableLevelItem(PointI pos) {
		this.pos = pos;
		id = GameController.get().createIdFor(this);
	}
	
	public void update(int time){};
	public abstract boolean isSquare();
	public abstract boolean blocksSight();
	public abstract boolean blocksMovement();
	public abstract boolean canUse(Mob m);
	public abstract boolean useInternal(Mob m);
	public abstract String getDisplayName();
	
	public final boolean use(Mob m){
		return canUse(m) && isInRangeToUse(m) && useInternal(m);
	}
	
	public final boolean isInRangeToUse(Mob m){
		return m.pos.distanceTo(pos.toFloat()) <= m.totalStats.getMaxPickupRange()+size/2f;
	}
	
	public void draw(){
		if(img != null) GameController.get().renderer.drawImage(img, pos.toFloat(), true);
	}
	
	public void drawNameBox(){
		GameController.get().renderer.drawTextBoxCentered(getDisplayName(), 
				pos.toFloat(), 4, GameRenderer.COLOR_LOOT_SELECTED_BG, 
				GameRenderer.COLOR_LOOT_SELECTED_LINE, Color.white);
	}
	
	public boolean damage(int dmg){
		if(hp >= 0){
			hp -= dmg;
			if(hp <= 0) return true;
		}
		return false;
	}
	
	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID(long id) {
		this.id = id;
	}

	@Override
	public NetStatePart fillStateParts(NetStatePart part, NetState state) {
		part.setInteger(0, pos.x);
		part.setInteger(1, pos.y);
		part.setInteger(2, hp);
		part.setFloat(0, size);
		return part;
	}

	@Override
	public void updateFromStatePart(NetStatePart part, NetState state) {
		pos.x = part.getInteger(0);
		pos.y = part.getInteger(1);
		hp = part.getInteger(2);
		size = part.getFloat(0);
	}
	
	
}
