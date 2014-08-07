/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.level.UsableLevelItem;
import hfk.mobs.Player;
import hfk.stats.Damage;
import java.util.Iterator;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameplaySubState extends GameSubState{

	public boolean lootMode = false;
	public InventoryItem selectedLoot = null;
	public UsableLevelItem selectedLevelItem = null;
	
	public GameplaySubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_PAUSE);
		inputMap.addKey(Input.KEY_I, InputMap.A_OPEN_INVENTORY);
		inputMap.addKey(Input.KEY_K, InputMap.A_OPEN_SKILLS);
		inputMap.addKey(Input.KEY_LCONTROL, InputMap.A_LOOT);
		inputMap.addKey(Input.KEY_W, InputMap.A_MOVE_UP);
		inputMap.addKey(Input.KEY_S, InputMap.A_MOVE_DOWN);
		inputMap.addKey(Input.KEY_A, InputMap.A_MOVE_LEFT);
		inputMap.addKey(Input.KEY_D, InputMap.A_MOVE_RIGHT);
		inputMap.addKey(Input.KEY_E, InputMap.A_USE_LEVITEM);
		inputMap.addKey(Input.KEY_G, InputMap.A_GRAB);
		inputMap.addKey(Input.KEY_R, InputMap.A_RELOAD);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_SHOOT);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_LOOT_GRAB);
		inputMap.addMouseButton(Input.MOUSE_RIGHT_BUTTON, InputMap.A_LOOT_USE);
		inputMap.addMouseButton(Input.MOUSE_RIGHT_BUTTON, InputMap.A_SHOOT_ALTERNATIVE);
		for(int i=0; i<10; i++) inputMap.addKey(Input.KEY_1 + i, InputMap.A_QUICKSLOTS[i]);
	}

	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		InputMap in = getInputMap();
		Input input = gc.getInput();
		Player player = ctrl.player;
		if(in.isKeyPressed(InputMap.A_PAUSE)){
			ctrl.setCurrSubState(ctrl.pauseSubState);
			return;
		}
		if(in.isKeyPressed(InputMap.A_OPEN_INVENTORY)){
			ctrl.viewInventory(ctrl.player.inventory);
			lootMode = false;
			return;
		}
		if(in.isKeyPressed(InputMap.A_OPEN_SKILLS)){
			ctrl.viewSkills(ctrl.player.skills);
			lootMode = false;
			return;
		}
		lootMode = in.isKeyDown(InputMap.A_LOOT);
		for(int i=0; i<10; i++) if(in.isKeyPressed(InputMap.A_QUICKSLOTS[i])) ctrl.player.inventory.setActiveQuickSlot(i);
		int vx = 0, vy = 0;
		if(in.getMouseWheelMove() > 0) ctrl.player.inventory.previousQuickSlot();
		if(in.getMouseWheelMove() < 0) ctrl.player.inventory.nextQuickSlot();
		if(in.isKeyDown(InputMap.A_MOVE_RIGHT)) vx++;
		if(in.isKeyDown(InputMap.A_MOVE_LEFT)) vx--;
		if(in.isKeyDown(InputMap.A_MOVE_DOWN)) vy++;
		if(in.isKeyDown(InputMap.A_MOVE_UP)) vy--;
		float s = player.totalStats.getMaxSpeed();
		if(vx != 0 && vy != 0) s /= GameController.SQRT2;
		ctrl.moveMob(player, vx * s, vy * s, time);
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
		if(in.isKeyPressed(InputMap.A_GRAB)){
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
				if(in.isMousePressed(InputMap.A_LOOT_GRAB)){
					// take item into inventory
					if(player.inventory.addItem(selectedLoot) == null){
						ctrl.items.remove(selectedLoot);
						selectedLoot = null;
					}
				}
				if(in.isMousePressed(InputMap.A_LOOT_USE)){
					// use item directly
					if(selectedLoot.use(player, false)){
						ctrl.items.remove(selectedLoot);
						selectedLoot = null;
					}
				}
			}
		} else {
			selectedLevelItem = ctrl.level.getUsableItemOnLine(
					ctrl.player.pos, ctrl.mousePosInTiles, 
					ctrl.player.totalStats.getMaxPickupRange(), lootMode);
			if(selectedLevelItem != null){
				float dd = selectedLevelItem.pos.toFloat().squaredDistanceTo(player.pos) - selectedLevelItem.size;
				if(dd > player.totalStats.getMaxPickupRange()) selectedLevelItem = null;
			}
			if(selectedLevelItem != null && in.isKeyPressed(InputMap.A_USE_LEVITEM)) selectedLevelItem.use(player);
			selectedLoot = null;
			Weapon w = player.getActiveWeapon();
			if(w != null){
				boolean pr = in.isMousePressed(InputMap.A_SHOOT);
				boolean dn = input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON);
				boolean a = w.totalStats.isAutomatic;
				if((a && dn) || (!a && pr)){
					w.pullTrigger();
				}
				if(in.isMousePressed(InputMap.A_SHOOT_ALTERNATIVE)){
					w.pullAlternativeTrigger();
				}
				if(in.isKeyPressed(InputMap.A_RELOAD)){
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
				int hl = 0;
				if(i.pos.squaredDistanceTo(ctrl.player.pos) <= ctrl.player.totalStats.getMaxPickupRange()) hl++;
				if(i == selectedLoot) hl++;
				i.drawItemTextBox(hl);
			}
		} else {
			if(selectedLevelItem != null) selectedLevelItem.drawNameBox();
		}
		
		// draw hud text
		int h = gc.getHeight();
		int sh = r.getStringHeight("a") + 4;
		int y = 20;
		int x = 20;
		r.drawStringOnScreen("hp : " + ctrl.player.hp, x, y, Color.white, 1f); y += sh;
		r.drawStringOnScreen("xp : " + ctrl.player.xp, x, y, Color.white, 1f); y += sh;
		r.drawStringOnScreen("level : " + ctrl.getLevelCount(), x, y, Color.white, 1f); y += sh;
		r.drawStringOnScreen("enemies left : " + (ctrl.mobs.size()-1), x, y, Color.white, 1f); y += sh;
		Weapon wpn = ctrl.player.getActiveWeapon();
		if(wpn != null){
			y = h - 20 - (1 + Math.max(Damage.DAMAGE_TYPE_COUNT, Weapon.AMMO_TYPE_COUNT)) * sh;
			wpn.renderInformation(x, y, true);
		}
	}
	
}
