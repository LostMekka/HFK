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

	public enum LayerType{
		floor, debris, items, mob1, mob2, mob3, 
		particles, projectiles, walls, flying1, flying2, flying3
	}
	
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
	public static final Color RENDER_FOG = new Color(0.5f, 0.5f, 0.5f);
	public static final int MIN_TEXT_HEIGHT = 20;

	private static final float BIG_FONT_SCALE = 6f;
	
	private final GameContainer container;
	private Font font = null, font_big = null;
	private RenderBatch[] layers = new RenderBatch[LayerType.values().length];

	public GameRenderer(GameContainer container) {
		this.container = container;
		for(int i=0; i<layers.length; i++) layers[i] = new RenderBatch(this);
	}
	
	public Graphics getGraphics(){
		return container.getGraphics();
	}
	
	private static final Color MM_BACK = new Color(0f, 0f, 0f);
	private static final Color MM_WALL_INSIGHT = new Color(1f, 1f, 1f);
	private static final Color MM_WALL_SCOUTED = new Color(0.7f, 0.7f, 0.7f);
	private static final Color MM_FLOOR_INSIGHT = new Color(1f, 1f, 1f, 0.55f);
	private static final Color MM_FLOOR_SCOUTED = new Color(0.7f, 0.7f, 0.7f, 0.55f);
	private static final Color MM_STAIRS = new Color(0f, 1f, 0f);
	private static final Color MM_STAIRS_SQUARES = new Color(0f, 1f, 0f);
	private static final Color MM_DOOR_O = new Color(0.5f, 0.5f, 0f);
	private static final Color MM_DOOR_C = new Color(1f, 1f, 0f);
	private static final Color MM_PLAYER = new Color(0f, 1f, 0f);
	private static final Color MM_ENEMY = new Color(1f, 0f, 0f);
	private static final Color MM_LOOT = new Color(0f, 0f, 1f);
	private static final Color MM_FINAL = new Color(1f, 1f, 1f, 0.5f);
	private static final int MM_MAX_TIMER = 1000;
	private HashMap<PointF, Image> mmImages = new HashMap<>();
	private int mmStairsTimer = 0;
	
	public void drawMiniMap(PointF pos, PointF size, PointI min, PointI max, float alpha){
		PointI s = new PointI(max.x - min.x + 1, max.y - min.y + 1);
		float zoom = Math.min(
				(float)Math.floor(size.x / s.x), 
				(float)Math.floor(size.y / s.y));
		PointF mid = new PointF((min.x + max.x)/2f, (min.y + max.y)/2f);
		drawMiniMap(pos, size, zoom, mid, alpha);
	}
	
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
			throw new RuntimeException(ex);
		}
		// clear area
		g.clear();
		g.setColor(MM_BACK);
		g.fillRect(pos.x, pos.y, size.x, size.y);
		// draw tiles and level items first
		for(int x=ctrl.level.getScoutedMinX(); x<=ctrl.level.getScoutedMaxX(); x++){
			for(int y=ctrl.level.getScoutedMinY(); y<=ctrl.level.getScoutedMaxY(); y++){
				PointI pi = new PointI(x, y);
			PointF pf = pi.toFloat();
				if(ctrl.level.isScouted(pi)){
					boolean inSight = ctrl.level.isVisible(pi);
					// draw tiles
					if(ctrl.level.isWallTile(pi.x, pi.y)){
						fillMMRect(g, inSight ? MM_WALL_INSIGHT : MM_WALL_SCOUTED, pf, 1f, mid, size, zoom);
					} else {
						fillMMRect(g, inSight ? MM_FLOOR_INSIGHT : MM_FLOOR_SCOUTED, pf, 1f, mid, size, zoom);
					}
				}
			}
		}
		// draw level item on top of tiles
		for(UsableLevelItem i : ctrl.level.items) if(ctrl.level.isScouted(i.pos)){
				Color c;
				if(i instanceof Stairs){
					c = MM_STAIRS;
				} else if(i instanceof Door){
					Door d = (Door)i;
					c = d.isOpen() ? MM_DOOR_O : MM_DOOR_C;
				} else {
					c = ctrl.level.isVisible(i.pos) ? MM_WALL_INSIGHT : MM_WALL_SCOUTED;
				}
				fillMMRect(g, c, i.pos.toFloat(), i.size.x, i.size.y, mid, size, zoom);
			}
		// draw other things on top
		for(InventoryItem i : ctrl.items){
			if(!ctrl.level.isScouted(i.pos.round())) continue;
			fillMMRect(g, MM_LOOT, i.pos, i.size, mid, size, zoom);
		}
		for(Mob m : ctrl.mobs){
			if(m instanceof Player || !ctrl.level.isVisible(m.pos.round())) continue;
			fillMMRect(g, MM_ENEMY, m.pos, m.size, mid, size, zoom);
		}
		fillMMRect(g, MM_PLAYER, ctrl.player.pos, ctrl.player.size, mid, size, zoom);
		// draw tactical overlay stuff
		for(UsableLevelItem i : ctrl.level.items) if(i instanceof Stairs) if(ctrl.level.isScouted(i.pos)){
			float s = mmStairsTimer/(float)MM_MAX_TIMER;
			MM_STAIRS_SQUARES.a = (1f + s)/2f;
			drawMMRect(g, MM_STAIRS_SQUARES, i.pos.toFloat(), 3f - 2f*s, 5, mid, size, zoom);
			MM_STAIRS_SQUARES.a = s/2f;
			drawMMRect(g, MM_STAIRS_SQUARES, i.pos.toFloat(), 5f - 2f*s, 5, mid, size, zoom);
		}
		// draw final image on screen
		MM_FINAL.a = alpha;
		getGraphics().drawImage(mmImg, pos.x, pos.y, MM_FINAL);
	}
	private void fillMMRect(Graphics g, Color c, PointF pos, float size, PointF mmMid, PointF mmSize, float zoom){
		fillMMRect(g, c, pos, size, size, mmMid, mmSize, zoom);
	}
	private void drawMMRect(Graphics g, Color c, PointF pos, float size, int border, PointF mmMid, PointF mmSize, float zoom){
		g.setColor(c);
		for(int i=0; i<border; i++) g.drawRect(
				mmSize.x/2f + (pos.x - size/2f - mmMid.x)*zoom + i, 
				mmSize.y/2f + (pos.y - size/2f - mmMid.y)*zoom + i, 
				size * zoom - 2*i, size * zoom - 2*i);
	}
	private void fillMMRect(Graphics g, Color c, PointF pos, float sizeX, float sizeY, PointF mmMid, PointF mmSize, float zoom){
		g.setColor(c);
		g.fillRect(
				mmSize.x/2f + (pos.x - sizeX/2f - mmMid.x)*zoom, 
				mmSize.y/2f + (pos.y - sizeY/2f - mmMid.y)*zoom, 
				sizeX * zoom, sizeY * zoom);
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
	
	public PointI getStringSize(String s){
		return new PointI(getStringWidth(s), getStringHeight(s));
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
	
	public void drawImage(Image i, PointF pos, boolean ignoreVisionRange, LayerType l){
		drawImage(i, pos, 1f, 0f, ignoreVisionRange, l);
	}

	public void drawImage(Image i, PointF pos, float scale, boolean ignoreVisionRange, LayerType l){
		drawImage(i, pos, scale, 0f, ignoreVisionRange, l);
	}

	public void drawImage(Image i, PointF pos, float scale, float angle, boolean ignoreVisionRange, LayerType l){
		layers[l.ordinal()].drawImage(i, pos, scale, angle, ignoreVisionRange);
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

	public void update(int time){
		mmStairsTimer = (mmStairsTimer + time) % MM_MAX_TIMER;
	}

	public void executeLayerBatches(){
		for(RenderBatch layer : layers) layer.execute();
	}
	
	public void render(GameController ctrl) throws SlickException{
		ctrl.omniSubState.render(ctrl, this, container);
		ctrl.getCurrSubState().render(ctrl, this, container);
	}
	
}
