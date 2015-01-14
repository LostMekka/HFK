/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.menu;

import org.newdawn.slick.GameContainer;

/**
 *
 * @author LostMekka
 */
public abstract class MenuBox {
	
	public static final int BORDER = 10;
	
	private int x, y, w, h;

	public MenuBox(SplitMenuBox b, SplitMenuBox.Location l) {
		x = b.getChildX(l);
		y = b.getChildY(l);
		w = b.getChildWidth(l);
		h = b.getChildHeigth(l);
		b.setChild(l, this);
	}

	public MenuBox(GameContainer gc) {
		x = 0;
		y = 0;
		w = gc.getWidth();
		h = gc.getHeight();
	}

	public MenuBox(SimpleMenuBox parent) {
		x = parent.getBoxX();
		y = parent.getBoxY();
		w = parent.getBoxWidth();
		h = parent.getBoxHeight();
		parent.setChild(this);
	}

	public MenuBox(int x, int y, int w, int h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public abstract void render();

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return w;
	}

	public int getHeight() {
		return h;
	}
	
	public int getBoxX() {
		return x + BORDER;
	}

	public int getBoxY() {
		return y + BORDER;
	}

	public int getBoxWidth() {
		return w - 2 * BORDER;
	}

	public int getBoxHeight() {
		return h - 2 * BORDER;
	}
	
	public int getInsideX(){
		return x + 2 * BORDER;
	}
	
	public int getInsideY(){
		return y + 2 * BORDER;
	}
	
	public int getInsideWidth(){
		return w - 4 * BORDER;
	}
	
	public int getInsideHeight(){
		return h - 4 * BORDER;
	}
	
	public boolean isMouseInside(int mouseX, int mouseY){
		mouseX -= 2 * BORDER + x;
		mouseY -= 2 * BORDER + y;
		return mouseX >= 0 && mouseX < getInsideWidth() && 
				mouseY >= 0 && mouseY < getInsideHeight();
	}
	
	public boolean isMouseInsideBox(int mouseX, int mouseY){
		mouseX -= BORDER + x;
		mouseY -= BORDER + y;
		return mouseX >= 0 && mouseX < getBoxWidth() && 
				mouseY >= 0 && mouseY < getBoxHeight();
	}
	
	public int getInsideMouseX(int mouseX){
		mouseX -= 2 * BORDER + x;
		if(mouseX < 0 || mouseX >= getInsideWidth()) return -1;
		return mouseX;
	}
	
	public int getInsideMouseY(int mouseY){
		mouseY -= 2 * BORDER + y;
		if(mouseY < 0 || mouseY >= getInsideHeight()) return -1;
		return mouseY;
	}
	
}
