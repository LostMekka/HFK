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
public class SplitMenuBox extends MenuBox {
	
	public static enum Location { topLeft, topRight, bottomLeft, bottomRight;
		public boolean isBottom(){ return this == bottomLeft || this == bottomRight; }
		public boolean isRight(){ return this == topRight || this == bottomRight; }
	}
	
	public static float getSplitRatioFromFirstSize(int totalSize, int firstSize){
		float s = totalSize - 7 * MenuBox.BORDER;
		if(s < firstSize) System.err.println("WARNING: (getSplitRatio) not enough space for required field!");
		return firstSize / s;
	}

	public static float getSplitRatioFromSecondSize(int totalSize, int secondSize){
		return 1f - getSplitRatioFromFirstSize(totalSize, secondSize);
	}
	
	private MenuBox[] boxes = new MenuBox[Location.values().length];
	private boolean sh = false, sv = false;
	private int x1 = 0, y1 = 0, w1, h1, x2, y2, w2, h2;

	public SplitMenuBox(SplitMenuBox b, Location l, float splitHoriz, float splitVert) {
		super(b, l);
		init(splitHoriz, splitVert);
	}

	public SplitMenuBox(GameContainer gc, float splitHoriz, float splitVert) {
		super(gc);
		init(splitHoriz, splitVert);
	}

	public SplitMenuBox(SimpleMenuBox parent, float splitHoriz, float splitVert) {
		super(parent);
		init(splitHoriz, splitVert);
	}

	public SplitMenuBox(int x, int y, int w, int h, float splitHoriz, float splitVert) {
		super(x, y, w, h);
		init(splitHoriz, splitVert);
	}

	private void init(float splitHoriz, float splitVert){
		sh = splitHoriz > 0f && splitHoriz < 1f;
		sv = splitVert > 0f && splitVert < 1f;
		if(!sh && !sv) throw new IllegalArgumentException("split menu box is not splitted at all");
		w1 = sv ? Math.round((getWidth()-7*MenuBox.BORDER) * splitVert) + 4*MenuBox.BORDER : getWidth();
		x2 = w1 - MenuBox.BORDER;
		w2 = getWidth() - x2;
		h1 = sh ? Math.round((getHeight()-7*MenuBox.BORDER) * splitHoriz) + 4*MenuBox.BORDER : getHeight();
		y2 = h1 - MenuBox.BORDER;
		h2 = getHeight() - y2;
	}
	
	public int getChildX(Location l) {
		switch(l){
			case topLeft: case bottomLeft: return x1;
			case topRight: case bottomRight: return x2;
			default: throw new RuntimeException("this should never happen!");
		}
	}

	public int getChildY(Location l) {
		switch(l){
			case topLeft: case topRight: return y1;
			case bottomLeft: case bottomRight: return y2;
			default: throw new RuntimeException("this should never happen!");
		}
	}

	public int getChildWidth(Location l) {
		switch(l){
			case topLeft: case bottomLeft: return w1;
			case topRight: case bottomRight: return w2;
			default: throw new RuntimeException("this should never happen!");
		}
	}

	public int getChildHeigth(Location l) {
		switch(l){
			case topLeft: case topRight: return h1;
			case bottomLeft: case bottomRight: return h2;
			default: throw new RuntimeException("this should never happen!");
		}
	}

	public MenuBox getChild(Location l) {
		return boxes[l.ordinal()];
	}

	public void setChild(Location l, MenuBox child) {
		if(!sh && l.isBottom()) throw new RuntimeException("split box does not split horizontally");
		if(!sv && l.isRight()) throw new RuntimeException("split box does not split vertically");
		boxes[l.ordinal()] = child;
	}

	@Override
	public void render() {
		for(MenuBox b : boxes) if(b != null) b.render();
	}
}
