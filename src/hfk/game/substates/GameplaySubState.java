/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.level.UsableLevelItem;
import hfk.mobs.Player;
import hfk.skills.Skill;
import hfk.stats.Damage;
import java.util.Iterator;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameplaySubState extends GameSubState{

	public boolean lootMode = false, drawMap = false;
	public InventoryItem selectedLoot = null;
	public UsableLevelItem selectedLevelItem = null;
	
	public GameplaySubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_F1, InputMap.A_CHEAT_OVERVIEW);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_PAUSE);
		inputMap.addKey(Input.KEY_I, InputMap.A_OPEN_INVENTORY);
		inputMap.addKey(Input.KEY_K, InputMap.A_OPEN_SKILLS);
		inputMap.addKey(Input.KEY_LCONTROL, InputMap.A_LOOT);
		inputMap.addKey(Input.KEY_W, InputMap.A_MOVE_UP);
		inputMap.addKey(Input.KEY_S, InputMap.A_MOVE_DOWN);
		inputMap.addKey(Input.KEY_A, InputMap.A_MOVE_LEFT);
		inputMap.addKey(Input.KEY_D, InputMap.A_MOVE_RIGHT);
		inputMap.addKey(Input.KEY_E, InputMap.A_USE_LEVITEM);
		inputMap.addKey(Input.KEY_E, InputMap.A_LOOT_USE);
		inputMap.addKey(Input.KEY_G, InputMap.A_GRAB);
		inputMap.addKey(Input.KEY_R, InputMap.A_RELOAD);
		inputMap.addKey(Input.KEY_R, InputMap.A_LOOT_UNLOAD);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_SHOOT);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_LOOT_GRAB);
		inputMap.addMouseButton(Input.MOUSE_RIGHT_BUTTON, InputMap.A_SHOOT_ALTERNATIVE);
		for(int i=0; i<10; i++) inputMap.addKey(Input.KEY_1 + i, InputMap.A_QUICKSLOTS[i]);
	}

	private static final int MAX_HEALTH_TIMER = 300;
	private static final Color HEALTH_COLOR_1 = new Color(1f, 0f, 0f);
	private static final Color HEALTH_COLOR_2 = new Color(1f, 0.9f, 0.9f);
	private static final Color HEALTH_COLOR_3 = new Color(0.4f, 0f, 0f);
	private int healthTimer = 0;
	private Color healthColor = new Color(0.3f, 1f, 0.3f);
	private static final int MAX_SKILLS_TIMER = 1000;
	private static final Color SKILLS_COLOR_1 = new Color(0.2f, 0.65f, 0.2f);
	private static final Color SKILLS_COLOR_2 = new Color(0.1f, 0.4f, 0.1f);
	private int skillsTimer = 0;
	private Color skillsColor = new Color(0.3f, 1f, 0.3f);

	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		Player player = ctrl.player;
		healthTimer = (healthTimer + time) % MAX_HEALTH_TIMER;
		healthColor = (player.hp < 20 && healthTimer < MAX_HEALTH_TIMER / 2) ?
				HEALTH_COLOR_2 : HEALTH_COLOR_1;
		skillsTimer = (skillsTimer + time) % MAX_SKILLS_TIMER;
		float sc = Math.abs((float)skillsTimer / MAX_SKILLS_TIMER - 0.5f);
		skillsColor.r = 0.2f + 0.4f * sc;
		skillsColor.g = 0.6f + 0.2f * sc;
		skillsColor.b = 0.2f + 0.4f * sc;
		
		InputMap in = getInputMap();
		Input input = gc.getInput();
		drawMap = input.isKeyDown(Input.KEY_TAB);
		if(in.isActionPressed(InputMap.A_PAUSE)){
			ctrl.setCurrSubState(ctrl.pauseSubState);
			return;
		}
		if(in.isActionPressed(InputMap.A_OPEN_INVENTORY)){
			ctrl.viewInventory(ctrl.player.inventory);
			lootMode = false;
			return;
		}
		if(in.isActionPressed(InputMap.A_OPEN_SKILLS)){
			ctrl.viewSkills(ctrl.player.skills);
			lootMode = false;
			return;
		}
		if(in.isActionPressed(InputMap.A_CHEAT_OVERVIEW)){
			ctrl.cheatOverview();
			return;
		}
		lootMode = in.isActionDown(InputMap.A_LOOT);
		for(int i=0; i<10; i++) if(in.isActionPressed(InputMap.A_QUICKSLOTS[i])) ctrl.player.inventory.setActiveQuickSlot(i);
		int vx = 0, vy = 0;
		if(in.getMouseWheelMove() > 0) ctrl.player.inventory.previousQuickSlot();
		if(in.getMouseWheelMove() < 0) ctrl.player.inventory.nextQuickSlot();
		if(in.isActionDown(InputMap.A_MOVE_RIGHT)) vx++;
		if(in.isActionDown(InputMap.A_MOVE_LEFT)) vx--;
		if(in.isActionDown(InputMap.A_MOVE_DOWN)) vy++;
		if(in.isActionDown(InputMap.A_MOVE_UP)) vy--;
		float s = player.totalStats.getMaxSpeed();
		if(vx != 0 && vy != 0) s /= GameController.SQRT2;
		PointF oldPlayerPos = ctrl.player.pos.clone();
		ctrl.moveMob(player, vx * s, vy * s, time, true);
		if(ctrl.recalcVisibleTiles || !oldPlayerPos.equals(ctrl.player.pos) || !ctrl.level.hasScoutedTiles()){
			ctrl.recalcVisibleTiles = false;
			// update visible tiles
			ctrl.level.clearVisible();
			float r = ctrl.player.totalStats.getSightRange();
			float r2 = r + 0.5f;
			float rs = ctrl.player.totalStats.getBasicSenseRange();
			int ir = (int)Math.ceil(Math.max(r, rs)) + 1;
			PointI ppi = ctrl.player.pos.round();
			for(int x=ppi.x-ir; x<=ppi.x+ir; x++){
				for(int y=ppi.y-ir; y<=ppi.y+ir; y++){
					PointF pf = new PointF(x, y);
					PointI pi = new PointI(x, y);
					float dd = pf.squaredDistanceTo(ctrl.player.pos);
					if(dd <= rs*rs) ctrl.level.setScouted(pi);
					if(dd <= r2*r2){
						float angle = ctrl.player.pos.angleTo(pf);
						angle = GameController.getAngleDiff(angle, ctrl.player.getLookAngle());
						if(Math.abs(angle) < ctrl.player.totalStats.getVisionAngle() / 2f){
							LinkedList<PointI> tiles = ctrl.level.getTilesOnLine(ctrl.player.pos, pf, r2);
							for(PointI pi2 : tiles) if(ctrl.player.pos.squaredDistanceTo(pi2.toFloat()) < r*r){
								ctrl.level.setVisible(pi2);
								if(ctrl.level.isSightBlocking(pi2.x, pi2.y)) break;
							}
						}
					}
				}
			}
		}
		// camera
		ctrl.screenPosOriginal.x = player.pos.x - ctrl.transformScreenToTiles(gc.getWidth()) / 2f;
		ctrl.screenPosOriginal.y = player.pos.y - ctrl.transformScreenToTiles(gc.getHeight()) / 2f;
		if(ctrl.player.getActiveWeapon() != null){
			float z = player.getActiveWeapon().totalStats.weaponZoom;
			ctrl.screenPosOriginal.x += z * (ctrl.mousePosInPixels.x * 2f / gc.getWidth() - 1f);
			ctrl.screenPosOriginal.y += z * (ctrl.mousePosInPixels.y * 2f / gc.getHeight() - 1f);
		}
		ctrl.screenPos.set(ctrl.screenPosOriginal);
		ctrl.screenPos.add(ctrl.screenPosOffset);
		float angle = (float)Math.PI * GameController.random.nextFloat()* 2f;
		ctrl.screenPos.x += (float)Math.cos(angle) * ctrl.screenShake;
		ctrl.screenPos.y += (float)Math.sin(angle) * ctrl.screenShake;
		ctrl.mousePosInTiles = ctrl.transformScreenToTiles(ctrl.mousePosInPixels.toFloat());
		player.lookAt(ctrl.mousePosInTiles);
		// shoot and reload or looting
		if(in.isActionPressed(InputMap.A_GRAB)){
			// grab nearest item
			InventoryItem i = ctrl.getNearestItem(player);
			if(i != null && player.inventory.addItem(i) == null) ctrl.items.remove(i);
		}
		if(lootMode){
			selectedLevelItem = null;
			Iterator<InventoryItem> iter = ctrl.items.iterator();
			float r = player.totalStats.getMaxPickupRange();
			r *= r;
			selectedLoot = null;
			while(iter.hasNext()){
				InventoryItem i = iter.next();
				if(i.isOnLabel(ctrl.mousePosInTiles) && i.pos.squaredDistanceTo(player.pos) <= r){
					selectedLoot = i;
					break;
				}
			}
			if(selectedLoot != null){
				if(in.isActionPressed(InputMap.A_LOOT_GRAB)){
					// take item into inventory
					if(player.inventory.addItem(selectedLoot) == null){
						ctrl.items.remove(selectedLoot);
						selectedLoot = null;
					}
				}
				if(in.isActionPressed(InputMap.A_LOOT_USE)){
					// use item directly
					InventoryItem newItem = selectedLoot.use(player);
					if(newItem != selectedLoot){
						ctrl.items.remove(selectedLoot);
						if(newItem != null){
							newItem.pos = selectedLoot.pos.clone();
							newItem.vel = selectedLoot.vel.clone();
							ctrl.addItem(newItem);
						}
						selectedLoot = newItem;
					}
				}
				if(in.isActionPressed(InputMap.A_LOOT_UNLOAD)){
					// unload weapon on ground
					if(selectedLoot instanceof Weapon){
						player.unloadWeapon((Weapon)selectedLoot);
					}
				}
			}
		} else {
			selectedLevelItem = ctrl.level.getUsableItemOnLine(
					ctrl.player.pos, ctrl.mousePosInTiles, 
					ctrl.player.totalStats.getMaxPickupRange(), lootMode);
			if(selectedLevelItem != null && !selectedLevelItem.isInRangeToUse(player)) selectedLevelItem = null;
			if(selectedLevelItem != null && in.isActionPressed(InputMap.A_USE_LEVITEM)) selectedLevelItem.use(player);
			selectedLoot = null;
			Weapon w = player.getActiveWeapon();
			if(w != null){
				boolean pr = in.isActionPressed(InputMap.A_SHOOT);
				boolean dn = in.isActionDown(InputMap.A_SHOOT);
				boolean apr = in.isActionPressed(InputMap.A_SHOOT_ALTERNATIVE);
				boolean adn = in.isActionDown(InputMap.A_SHOOT_ALTERNATIVE);
				boolean a = w.totalStats.isAutomatic;
				if((a && dn) || (!a && pr)){
					w.pullTrigger();
				}
				if((a && adn) || (!a && apr)){
					w.pullAlternativeTrigger();
				}
				if(in.isActionPressed(InputMap.A_RELOAD)){
					if(w.reload()){
						PointF p = player.pos.clone();
						p.y -= 0.8f;
						ctrl.addFloatingText("reloading...", 0.5f, p, Color.white, null);
					}
				}
			}
		}
	}
	
	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		// draw text boxes for loot and level items
		if(lootMode){
			PointF pp = ctrl.transformTilesToScreen(ctrl.player.pos);
			float rad = ctrl.transformTilesToScreen(ctrl.player.totalStats.getMaxPickupRange());
			r.getGraphics().setColor(new Color(1f, 1f, 1f, 0.4f));
			r.getGraphics().drawOval(pp.x-rad, pp.y-rad, 2*rad, 2*rad);
			for(InventoryItem i : ctrl.items){
				if(!ctrl.level.isScouted(i.pos.round())) continue;
				int hl = 0;
				if(i.pos.squaredDistanceTo(ctrl.player.pos) <= ctrl.player.totalStats.getMaxPickupRange()) hl++;
				if(i == selectedLoot) hl++;
				i.drawItemTextBox(hl);
			}
		} else {
			if(selectedLevelItem != null) selectedLevelItem.drawNameBox();
		}
		
		// draw HUD
		int hudBorder = 10;
		Graphics g = r.getGraphics();
		// health bar and level
		int healthBarLen = 250;
		int barHeight = 21;
		g.setColor(HEALTH_COLOR_3);
		g.drawRect(hudBorder, hudBorder, healthBarLen + 9, barHeight + 9);
		g.setColor(healthColor);
		g.fillRect(hudBorder + 1, hudBorder + 1, healthBarLen + 8, barHeight + 8);
		g.setColor(Color.black);
		g.fillRect(hudBorder + 3, hudBorder + 3, healthBarLen + 4, barHeight + 4);
		int hp = healthBarLen * ctrl.player.hp / ctrl.player.totalStats.getMaxHP();
		if(hp > 0){
			g.setColor(healthColor);
			g.fillRect(hudBorder + 5, hudBorder + 5, hp, barHeight);
		}
		if(hp < healthBarLen){
			g.setColor(HEALTH_COLOR_3);
			g.fillRect(hudBorder + 5 + hp, hudBorder + 5, healthBarLen - hp, barHeight);
		}
		String hpText = String.format("%d / %d", ctrl.player.hp, ctrl.player.totalStats.getMaxHP());
		int hpTextLen = r.getStringWidth(hpText);
		r.drawStringOnScreen(hpText, 
				hudBorder + 5 + (healthBarLen - hpTextLen) / 2, 
				hudBorder + 8, Color.white, 1f);
		Color levCol = ctrl.mobs.size() <= 1
				? GameRenderer.COLOR_TEXT_NORMAL
				: Color.white;
		String levStr = ctrl.mobs.size() <= 1
				? String.format("level : %d %s", ctrl.getLevelCount(), "(clear)")
				: String.format("level : %d", ctrl.getLevelCount());
		r.drawStringOnScreen(levStr, hudBorder + 4, hudBorder + 35, levCol, 1f);
		
		// xp and skills
		LinkedList<Skill> sl = ctrl.player.getTrackedSkillsList();
		int skCount = sl.size();
		String[] strings = null;
		int[] xpNeeded = null;
		int skWidth = 0;
		if(skCount > 0){
			strings = new String[skCount];
			xpNeeded = new int[skCount];
			int i = 0;
			for(Skill s : sl){
				strings[i] = s.name + " : " + s.getCost();
				xpNeeded[i] = s.getCost();
				int w = r.getStringWidth(strings[i]);
				if(w > skWidth) skWidth = w;
				i++;
			}
		}
		skWidth += 6;
		if(skWidth < 200) skWidth = 200;
		int skX = gc.getWidth() - skWidth - 9 - hudBorder;
		int skY = hudBorder + 6;
		int skHeight = 0;
		if(skCount > 0) skHeight += 2 + (barHeight + 2) * skCount;
		g.setColor(SKILLS_COLOR_2);
		g.drawRect(skX - 5, skY - 3, skWidth + 9, skHeight + 24);
		g.setColor(SKILLS_COLOR_1);
		g.fillRect(skX - 4, skY - 2, skWidth + 8, skHeight + 23);
		String xpText = String.format("xp : %d", ctrl.player.xp);
		int xpTextLen = r.getStringWidth(xpText);
		r.drawStringOnScreen(xpText, 
				skX + (skWidth - xpTextLen) / 2, skY + 1, Color.white, 1f);
		if(skCount > 0){
			g.setColor(Color.black);
			g.fillRect(skX - 2, skY + 19, skWidth + 4, skHeight);
			for(int i=0; i<skCount; i++){
				int xp = Math.min(skWidth, skWidth * ctrl.player.xp / xpNeeded[i]);
				if(xp > 0){
					g.setColor(xp == skWidth ? skillsColor : SKILLS_COLOR_1);
					g.fillRect(skX, skY + 21 + i * 23, xp, barHeight);
				}
				if(xp < skWidth){
					g.setColor(SKILLS_COLOR_2);
					g.fillRect(skX + xp, skY + 21 + i * 23, skWidth - xp, barHeight);
				}
				xpTextLen = r.getStringWidth(strings[i]);
				r.drawStringOnScreen(strings[i], 
						skX + (skWidth - xpTextLen) / 2, 
						skY + 24 + i * 23, Color.white, 1f);
			}
		}
		
		// mini map
		if(drawMap){
			r.drawMiniMap(new PointF(10, 10), 
				new PointF(gc.getWidth()-20, gc.getHeight()-20), 
				ctrl.level.getScoutedMin(), ctrl.level.getScoutedMax(), 0.5f);
		} else {
			int w = 300;
			int h = 300;
			PointF mmPos = new PointF(gc.getWidth()-w-1, gc.getHeight()-h-1);
			r.drawMiniMap(mmPos, new PointF(w, h), 10, ctrl.player.pos, 0.3f);
			r.getGraphics().setColor(new Color(0f, 0.8f, 1f, 0.1f));
			r.getGraphics().drawRect(mmPos.x, mmPos.y, w, h);
		}
		
		// rest of the HUD
		Weapon wpn = ctrl.player.getActiveWeapon();
		if(wpn != null){
			int h = gc.getHeight();
			int sh = r.getStringHeight("a") + 4;
			int y = h - 20 - (1 + Math.max(Damage.DAMAGE_TYPE_COUNT, Weapon.AMMO_TYPE_COUNT)) * sh;
			wpn.renderInformation(20, y, true);
		}
	}
	
}
