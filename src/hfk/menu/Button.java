/*
 */
package hfk.menu;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

/**
 *
 * @author LostMekka
 */
public class Button {
	
	public static int getDefaultHeight(){
		return GameController.get().renderer.getStringHeight("hello, world!") + 2*PADDING_H;
	}
	
	private static final int PADDING_W = 10;
	private static final int PADDING_H = 5;
	
	private int x, y, w, h, sw, sh;
	private String text;

	public Button(int midX, int midY, String text) {
		this.text = text;
		GameRenderer r = GameController.get().renderer;
		sw = r.getStringWidth(text);
		sh = r.getStringHeight(text);
		w = sw + 2 * PADDING_W;
		h = sh + 2 * PADDING_H;
		x = midX - w / 2;
		y = midY - h / 2;
	}
	
	public Button(int midX, int midY, int w, String text) {
		this.w = w;
		this.text = text;
		GameRenderer r = GameController.get().renderer;
		sw = r.getStringWidth(text);
		sh = r.getStringHeight(text);
		h = sh + 2 * PADDING_H;
		x = midX - w / 2;
		y = midY - h / 2;
	}
	
	public Button(int x, int y, int w, int h, String text) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.text = text;
		GameRenderer r = GameController.get().renderer;
		sw = r.getStringWidth(text);
		sh = r.getStringHeight(text);
	}
	
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

	public String getText() {
		return text;
	}
	
	public void draw(GameRenderer r){
		Graphics g = r.getGraphics();
		g.setColor(GameRenderer.COLOR_MENUITEM_BG);
		g.fillRect(x, y, w, h);
		g.setColor(GameRenderer.COLOR_MENUITEM_LINE);
		g.drawRect(x, y, w, h);
		r.drawStringOnScreen(text, x+(w-sw)/2, y+(h-sh)/2, GameRenderer.COLOR_TEXT_NORMAL, 1f);
	}
	
	public boolean isMouseInside(int mouseX, int mouseY){
		mouseX -= x;
		mouseY -= y;
		return mouseX >= 0 && mouseX < w && mouseY >= 0 && mouseY < h;
	}
	
}
