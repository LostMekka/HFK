/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import hfk.PointF;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

/**
 *
 * @author LostMekka
 */
public class GameRenderer {

	public static final Color COLOR_MENU_BG = new Color(1f, 1f, 1f, 0.5f);
	public static final Color COLOR_MENU_LINE = new Color(0f, 1f, 0f, 1f);
	public static final Color COLOR_LOOT_NORMAL_BG = new Color(1f, 1f, 1f, 0.4f);
	public static final Color COLOR_LOOT_NORMAL_LINE = new Color(0f, 0f, 1f, 0.5f);
	public static final Color COLOR_LOOT_INRANGE_BG = new Color(1f, 1f, 1f, 0.6f);
	public static final Color COLOR_LOOT_INRANGE_LINE = new Color(0f, 0f, 1f, 0.8f);
	public static final Color COLOR_LOOT_SELECTED_BG = new Color(1f, 1f, 1f, 0.8f);
	public static final Color COLOR_LOOT_SELECTED_LINE = new Color(0f, 0f, 1f, 1f);
	public static final int MIN_TEXT_HEIGHT = 20;

	private static final float BIG_FONT_SCALE = 6f;
	
	private final GameContainer container;
	private Font font = null, font_big = null;

	public GameRenderer(GameContainer container) {
		this.container = container;
	}
	
	public Graphics getGraphics(){
		return container.getGraphics();
	}
	
	public void drawMenuBox(float x, float y, float w, float h, Color backgroundColor, Color lineColor){
		Graphics g = container.getGraphics();
		g.setColor(backgroundColor);
		g.fillRect(x, y, w, h);
		g.setColor(lineColor);
		g.drawRect(x, y, w, h);
	}
	
	public void drawTextBoxCentered(String s, PointF center, int border, Color backgroundColor, Color lineColor, Color textColor){
		int w = font.getWidth(s) + 2 * border;
		int h = font.getHeight(s) + 2 * border;
		PointF p = GameController.get().transformTilesToScreen(center);
		int x = Math.round(p.x) - w / 2;
		int y = Math.round(p.y) - h / 2;
		drawMenuBox(x, y, w, h, backgroundColor, lineColor);
		drawStringOnScreen(s, x + border, y + border, textColor, 1f);
	}
	
	public void drawStringOnScreen(String s, float x, float y, Color col, float scale){
		Font f = font;
		if(scale > 1f){
			f = font_big;
			scale /= BIG_FONT_SCALE;
		}
		Graphics g = container.getGraphics();
		g.pushTransform();
		g.scale(scale, scale);
		f.drawString(Math.round(x / scale), Math.round(y / scale), s, col);
		g.popTransform();
	}
	
	public void drawString(String s, PointF pos, Color col){
		drawString(s, pos, col, 1f, false);
	}
	
	public void drawString(String s, PointF pos, Color col, float scale, boolean useViewZoom){
		GameController ctrl = GameController.get();
		PointF screenPos = ctrl.getScreenPos();
		if(useViewZoom) scale *= ctrl.getZoom();
		Font f = font;
		if(scale > 1f){
			f = font_big;
			scale /= BIG_FONT_SCALE;
		}
		Graphics g = container.getGraphics();
		g.pushTransform();
		g.scale(scale, scale);
		f.drawString(
				ctrl.transformTilesToScreen(pos.x - screenPos.x) / scale, 
				ctrl.transformTilesToScreen(pos.y - screenPos.y) / scale,
				s, col);
		g.popTransform();
	}

	public int getStringWidth(String string) {
		return font.getWidth(string)-1;
	}

	public int getStringHeight(String string) {
		return font.getHeight(string)-1;
	}
	
	public void drawImage(Image i, PointF pos){
		drawImage(i, pos, 1f);
	}
	
	public void drawImage(Image i, PointF pos, float scale){
		GameController gc = GameController.get();
		float zoom = gc.getZoom();
		PointF screenPos = gc.getScreenPos();
		i.draw( gc.transformTilesToScreen(pos.x - screenPos.x - scale * 0.5f), 
				gc.transformTilesToScreen(pos.y - screenPos.y - scale * 0.5f), 
				zoom * scale);
	}
	
	public void drawImage(Image i, PointF pos, float scale, float angle){
		GameController gc = GameController.get();
		float zoom = gc.getZoom();
		i.setCenterOfRotation(scale * zoom * gc.SPRITE_SIZE / 2f, scale * zoom * gc.SPRITE_SIZE / 2f);
		i.setRotation(angle / (float)Math.PI * 180f);
		drawImage(i, pos, scale);
	}
	
	public void drawDebugPath(PointF start, LinkedList<PointF> path){
		GameController gc = GameController.get();
		Graphics g = container.getGraphics();
		g.setColor(Color.green);
		PointF p1 = gc.transformTilesToScreen(start);
		for(PointF p2 : path){
			p2 = gc.transformTilesToScreen(p2);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			p1 = p2;
		}
	}
	
	public void initAfterLoading(){
		font = Resources.getFont("font");
		font_big = Resources.getFont("font_big");
	}

	public void render(GameController ctrl) throws SlickException{
		ctrl.omniSubState.render(ctrl, this, container);
		ctrl.getCurrSubState().render(ctrl, this, container);
	}
	
}
