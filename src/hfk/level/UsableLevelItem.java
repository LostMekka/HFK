/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level;

import hfk.PointF;
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
	public PointF size = new PointF(1f, 1f);
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
		float r = m.totalStats.getMaxPickupRange();
		return m.pos.squaredDistanceToRect(getTopLeftPoint(), getBottomRightPoint()) <= r*r;
	}
	
	public PointF getTopLeftPoint(){
		return new PointF(pos.x - size.x/2f, pos.y - size.y/2f);
	}
	
	public PointF getBottomRightPoint(){
		return new PointF(pos.x + size.x/2f, pos.y + size.y/2f);
	}
	
	public void draw(){
		if(img != null) GameController.get().renderer.drawImage(img, pos.toFloat(), true, GameRenderer.LayerType.items);
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
	public int getIntCount() {
		return 3;
	}

	@Override
	public int getLongCount() {
		return 0;
	}

	@Override
	public int getFloatCount() {
		return 2;
	}

	@Override
	public int getBoolCount() {
		return 0;
	}

	@Override
	public void fillStateFields(int[] ints, int intOffset, long[] longs, int longOffset, float[] floats, int floatOffset, boolean[] bools, int boolOffset) {
		ints[intOffset+0] = pos.x;
		ints[intOffset+1] = pos.y;
		ints[intOffset+2] = hp;
		floats[longOffset+0] = size.x;
		floats[longOffset+0] = size.y;
	}

	@Override
	public void applyFromStateFields(NetState state, int[] ints, int intOffset, long[] longs, int longOffset, float[] floats, int floatOffset, boolean[] bools, int boolOffset) {
		pos.x = ints[intOffset+0];
		pos.y = ints[intOffset+1];
		hp = ints[intOffset+2];
		size.x = floats[longOffset+0];
		size.y = floats[longOffset+0];
	}
	
}
