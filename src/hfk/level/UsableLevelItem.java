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
import hfk.game.slickstates.GameplayState;
import hfk.mobs.Player;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public abstract class UsableLevelItem {
	
	public PointI pos;
	public Image img = null;
	public int hp;
	public float size = 0.4f;

	public UsableLevelItem(PointI pos) {
		this.pos = pos;
	}
	
	public abstract boolean use(Player p);
	public abstract String getDisplayName();
	
	public void draw(){
		if(img != null) GameController.get().renderer.drawImage(img, pos.toFloat());
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
	
	public void update(GameplayState gs, int time){}
}
