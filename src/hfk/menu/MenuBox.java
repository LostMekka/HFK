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
		x = parent.getUsableX();
		y = parent.getUsableY();
		w = parent.getUsableWidth();
		h = parent.getUsableHeight();
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
	
	public int getUsableX(){
		return x + 2 * BORDER;
	}
	
	public int getUsableY(){
		return y + 2 * BORDER;
	}
	
	public int getUsableWidth(){
		return w - 4 * BORDER;
	}
	
	public int getUsableHeight(){
		return h - 4 * BORDER;
	}
	
	public boolean isMouseInside(int mouseX, int mouseY){
		mouseX -= x;
		mouseY -= y;
		return mouseX >= 0 && mouseX < getUsableWidth() && 
				mouseY >= 0 && mouseY < getUsableHeight();
	}
	
	public boolean isMouseInsideBox(int mouseX, int mouseY){
		mouseX -= BORDER + x;
		mouseY -= BORDER + y;
		return mouseX >= 0 && mouseX < getBoxWidth() && 
				mouseY >= 0 && mouseY < getBoxHeight();
	}
	
	public boolean isMouseInsideUsable(int mouseX, int mouseY){
		mouseX -= 2 * BORDER + x;
		mouseY -= 2 * BORDER + y;
		return mouseX >= 0 && mouseX < getUsableWidth() && 
				mouseY >= 0 && mouseY < getUsableHeight();
	}
	
	public int getRelativeMouseX(int mouseX){
		mouseX -= x;
		if(mouseX < 0 || mouseX >= getUsableWidth()) return -1;
		return mouseX;
	}
	
	public int getRelativeMouseY(int mouseY){
		mouseY -= y;
		if(mouseY < 0 || mouseY >= getUsableHeight()) return -1;
		return mouseY;
	}
	
	public int getBoxRelativeMouseX(int mouseX){
		mouseX -= BORDER + x;
		if(mouseX < 0 || mouseX >= getUsableWidth()) return -1;
		return mouseX;
	}
	
	public int getBoxRelativeMouseY(int mouseY){
		mouseY -= BORDER + y;
		if(mouseY < 0 || mouseY >= getUsableHeight()) return -1;
		return mouseY;
	}
	
	public int getUsableRelativeMouseX(int mouseX){
		mouseX -= 2 * BORDER + x;
		if(mouseX < 0 || mouseX >= getUsableWidth()) return -1;
		return mouseX;
	}
	
	public int getUsableRelativeMouseY(int mouseY){
		mouseY -= 2 * BORDER + y;
		if(mouseY < 0 || mouseY >= getUsableHeight()) return -1;
		return mouseY;
	}
	
}
