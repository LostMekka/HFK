/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.menu;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import org.newdawn.slick.GameContainer;

/**
 *
 * @author LostMekka
 */
public class SimpleMenuBox extends MenuBox {
	
	private MenuBox child = null;

	public SimpleMenuBox(SplitMenuBox b, SplitMenuBox.Location l) {
		super(b, l);
	}
	
	public SimpleMenuBox(GameContainer gc) {
		super(gc);
	}

	public SimpleMenuBox(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public MenuBox getChild() {
		return child;
	}

	public void setChild(MenuBox child) {
		this.child = child;
	}

	@Override
	public void render() {
		GameRenderer r = GameController.get().renderer;
		r.drawMenuBox(getBoxX(), getBoxY(), getBoxWidth(), getBoxHeight(),
				GameRenderer.COLOR_MENU_BG, GameRenderer.COLOR_MENU_LINE);
		if(child != null) child.render();
	}
	
}
