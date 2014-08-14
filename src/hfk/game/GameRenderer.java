/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import hfk.PointF;
import hfk.PointI;
import hfk.items.InventoryItem;
import hfk.level.Door;
import hfk.level.Stairs;
import hfk.level.UsableLevelItem;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import java.util.ArrayList;
import java.util.HashMap;
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

	public static final Color COLOR_TEXT_NORMAL = new Color(0f, 0.35f, 0.65f, 1f);
	public static final Color COLOR_TEXT_INACTIVE = new Color(0.45f, 0.45f, 0.45f, 1f);
	public static final Color COLOR_MENU_BG = new Color(1f, 1f, 1f, 0.7f);
	public static final Color COLOR_MENU_LINE = new Color(0f, 1f, 0f, 1f);
	public static final Color COLOR_MENUITEM_BG = new Color(1f, 1f, 1f, 0.7f);
	public static final Color COLOR_MENUITEM_LINE = new Color(0f, 0.8f, 0f, 1f);
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
	
	private static final Color MM_BACK = new Color(0f, 0f, 0f);
	private static final Color MM_WALL = new Color(1f, 1f, 1f);
	private static final Color MM_FLOOR = new Color(1f, 1f, 1f, 0.2f);
	private static final Color MM_STAIRS = new Color(1f, 0.5f, 0f);
	private static final Color MM_DOOR_O = new Color(1f, 1f, 0f);
	private static final Color MM_DOOR_C = new Color(1f, 1f, 0f);
	private static final Color MM_PLAYER = new Color(0f, 1f, 0f);
	private static final Color MM_ENEMY = new Color(1f, 0f, 0f);
	private static final Color MM_LOOT = new Color(0f, 0f, 1f);
	private static final Color MM_FINAL = new Color(1f, 1f, 1f, 0.5f);
	private HashMap<PointF, Image> mmImages = new HashMap<>();
	public void drawMiniMap(PointF pos, PointF size, float zoom, PointF mid, float alpha){
		GameController ctrl = GameController.get();
		Graphics g = null;
		Image mmImg;
		try {
			int w = Math.round(size.x);
			int h = Math.round(size.y);
			mmImg = mmImages.get(size);
			if(mmImg == null){
				mmImg = new Image(w, h, Image.FILTER_NEAREST);
				mmImages.put(size, mmImg);
			}
			g = mmImg.getGraphics();
		} catch (SlickException ex) {
			throw new RuntimeException();
		}
		float fl = mid.x - size.x/2f/zoom;
		float fr = mid.x + size.x/2f/zoom;
		float ft = mid.y - size.y/2f/zoom;
		float fb = mid.y + size.y/2f/zoom;
		int il = Math.round(fl)-1;
		int ir = Math.round(fr)+1;
		int it = Math.round(ft)-1;
		int ib = Math.round(fb)+1;
		PointF spf;
		g.clear();
		g.setColor(MM_BACK);
		g.fillRect(pos.x, pos.y, size.x, size.y);
		g.setColor(MM_LOOT);
		for(InventoryItem i : ctrl.items){
			if(!ctrl.scoutedTiles.contains(i.pos.round())) continue;
			spf = getMMPos(mid, size, i.pos, zoom);
			g.fillRect(spf.x, spf.y, zoom, zoom);
		}
		g.setColor(MM_ENEMY);
		for(Mob m : ctrl.mobs){
			if(m instanceof Player || !ctrl.visibleTiles.contains(m.pos.round())) continue;
			spf = getMMPos(mid, size, m.pos, zoom);
			g.fillRect(spf.x, spf.y, zoom, zoom);
		}
		g.setColor(MM_PLAYER);
		spf = getMMPos(mid, size, ctrl.player.pos, zoom);
		g.fillRect(spf.x, spf.y, zoom, zoom);
		g.setColor(MM_WALL);
		for(PointI pi : ctrl.scoutedTiles){
			PointF pf = pi.toFloat();
			spf = getMMPos(mid, size, pf, zoom);
			UsableLevelItem i = ctrl.level.getUsableLevelItem(pf);
			if(i != null){
				if(i instanceof Stairs){
					g.setColor(MM_STAIRS);
				} else if(i instanceof Door){
					g.setColor(((Door)i).isOpen() ? MM_DOOR_O : MM_DOOR_C);
				}
				g.fillRect(spf.x, spf.y, zoom, zoom);
			} else if(ctrl.level.isWall(pi.x, pi.y)){
				g.setColor(MM_WALL);
				g.fillRect(spf.x, spf.y, zoom, zoom);
			} else {
				g.setColor(MM_FLOOR);
				g.fillRect(spf.x, spf.y, zoom, zoom);
			}
		}
		MM_FINAL.a = alpha;
		getGraphics().drawImage(mmImg, pos.x, pos.y, MM_FINAL);
	}
	private PointF getMMPos(PointF mmMid, PointF mmSize, PointF pos, float zoom){
		return new PointF(
				mmSize.x/2f + (pos.x - mmMid.x)*zoom,
				mmSize.y/2f + (pos.y - mmMid.y)*zoom);
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
	
	public String[] wordWrapString(String s, int maxWidth){
		if(s.contains("\n")){
			String[] parts = s.split("\n");
			int numLines = 0;
			ArrayList<String[]> all = new ArrayList<>(parts.length);
			for(String part : parts) {
				String[] partLines = wordWrapString(part, maxWidth);
				all.add(partLines);
				numLines += partLines.length;
			}
			int n = 0;
			String[] ans = new String[numLines];
			for(String[] part : all){
				System.arraycopy(part, 0, ans, n, part.length);
				n += part.length;
			}
			return ans;
		}
		String[] words = s.split(" ");
		LinkedList<String> lines = new LinkedList<>();
		String currLine = words[0];
		int c = 0;
		for(int i=0; i<words.length; i++){
			String line;
			if(c == 0){
				line = words[i];
			} else {
				line = currLine + " " + words[i];
			}
			int w = getStringWidth(line);
			if(w > maxWidth){
				if(c == 0) throw new RuntimeException("maxWidth is too small, the word \"" + line + "\" does not fit in!");
				lines.add(currLine);
				c = 0;
				i--;
			} else {
				currLine = line;
				c++;
			}
		}
		lines.add(currLine);
		return lines.toArray(new String[lines.size()]);
	}
	
	public void drawImage(Image i, PointF pos, boolean ignoreVisionRange){
		drawImage(i, pos, 1f, ignoreVisionRange);
	}
	
	private static final Color RENDER_FOG = new Color(0.3f, 0.3f, 0.3f);
	public void drawImage(Image i, PointF pos, float scale, boolean ignoreVisionRange){
		float sx = i.getWidth() / GameController.SPRITE_SIZE;
		float sy = i.getHeight() / GameController.SPRITE_SIZE;
		GameController ctrl = GameController.get();
		LinkedList<PointI> tiles = new LinkedList<>();
		for(int x=Math.round(pos.x - scale * sx * 0.499f); x<=Math.round(pos.x + scale * sx * 0.499f); x++){
			for(int y=Math.round(pos.y - scale * sy * 0.499f); y<=Math.round(pos.y + scale * sy * 0.499f); y++){
				tiles.add(new PointI(x, y));
			}
		}
		float zoom = ctrl.getZoom();
		PointF screenPos = ctrl.getScreenPos();
		int one = Math.round(ctrl.transformTilesToScreen(1));
		for(PointI p : tiles){
			boolean bright = ctrl.visibleTiles.contains(p);
			if(bright || (ignoreVisionRange && ctrl.scoutedTiles.contains(p))){
				getGraphics().setClip(
						Math.round(ctrl.transformTilesToScreen(p.x - screenPos.x - 0.5f)), 
						Math.round(ctrl.transformTilesToScreen(p.y - screenPos.y - 0.5f)), 
						one, one);
				if(bright){
					i.draw( ctrl.transformTilesToScreen(pos.x - screenPos.x - scale * sx * 0.5f), 
							ctrl.transformTilesToScreen(pos.y - screenPos.y - scale * sy * 0.5f), 
							zoom * scale);
				} else {
					i.draw( ctrl.transformTilesToScreen(pos.x - screenPos.x - scale * sx * 0.5f), 
							ctrl.transformTilesToScreen(pos.y - screenPos.y - scale * sy * 0.5f), 
							zoom * scale, RENDER_FOG);
				}
			}
		}
		getGraphics().clearClip();
	}
	
	public void drawImage(Image i, PointF pos, float scale, float angle, boolean ignoreVisionRange){
		GameController ctrl = GameController.get();
		float zoom = ctrl.getZoom();
		i.setCenterOfRotation(scale * zoom * i.getWidth() / 2f, scale * zoom * i.getHeight() / 2f);
		i.setRotation(angle / (float)Math.PI * 180f);
		drawImage(i, pos, scale, ignoreVisionRange);
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
