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
public class ButtonsMenuBox extends SimpleMenuBox {

	private Button[] buttons = null;
	
	public ButtonsMenuBox(SplitMenuBox b, SplitMenuBox.Location l) {
		super(b, l);
	}

	public ButtonsMenuBox(GameContainer gc) {
		super(gc);
	}

	public ButtonsMenuBox(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public void setButtons(String... names){
		int mx = getUsableX() + getUsableWidth() / 2;
		int my = getUsableY() + getUsableHeight() / 2;
		int bh = Button.getDefaultHeight() + 4;
		int n = names.length;
		buttons = new Button[n];
		for(int i=0; i<n; i++){
			buttons[i] = new Button(mx, my + i*bh - n*bh/2, names[i]);
		}
	}
	
	public String getButtonText(int i){
		return buttons[i].getText();
	}
	
	public int getButtonIndexUnderMouse(int mouseX, int mouseY){
		for(int i=0; i<buttons.length; i++){
			if(buttons[i].isMouseInside(mouseX, mouseY)) return i;
		}
		return -1;
	}

	public String getButtonTextUnderMouse(int mouseX, int mouseY){
		for(Button b : buttons){
			if(b.isMouseInside(mouseX, mouseY)) return b.getText();
		}
		return null;
	}

	@Override
	public void render() {
		super.render();
		GameRenderer r = GameController.get().renderer;
		for(Button b : buttons) b.draw(r);
	}

}
