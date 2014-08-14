/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.items.EmptyItem;
import hfk.items.HealthPack;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.menu.MenuBox;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import hfk.stats.Damage;
import java.util.Iterator;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class InventorySubState extends GameSubState{

	public static final int INV_LINE_HEIGHT = 30;
	public static final int GEAR_HEADLINE_HEIGHT = 25;
	public static final int GEAR_TAB = 30;
	public static final int DESCR_LINE_HEIGHT = GameRenderer.MIN_TEXT_HEIGHT;

	private Inventory inventory = null;
	private InventoryItem selectedInvItem = null, selectedGear = null;
	private int inventoryOffset = 0, lastInventoryIndex = -1;
	private MenuBox mb, mbInv, mbDescr, mbGear;
	
	public InventorySubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_CLOSE_INVENTORY);
		inputMap.addKey(Input.KEY_I, InputMap.A_CLOSE_INVENTORY);
		inputMap.addKey(Input.KEY_UP, InputMap.A_INV_UP);
		inputMap.addKey(Input.KEY_DOWN, InputMap.A_INV_DOWN);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_INV_USE);
		inputMap.addMouseButton(Input.MOUSE_RIGHT_BUTTON, InputMap.A_INV_DROP);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void init(Inventory i){
		inventory = i;
		inventoryOffset = 0;
		deselectInventoryItem();
	}
	
	private int getVisibleItemCount(){
		return mbInv.getInsideHeight() / INV_LINE_HEIGHT;
	}

	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		int descLineCount = 1 + Math.max(Damage.DAMAGE_TYPE_COUNT, Weapon.AMMO_TYPE_COUNT);
		SplitMenuBox smb1 = new SplitMenuBox(gc, 1f, 0.5f);
		mb = smb1;
		mbGear = new SimpleMenuBox(smb1, SplitMenuBox.Location.topRight);
		float s = SplitMenuBox.getSplitRatioFromSecondSize(gc.getHeight(), descLineCount*DESCR_LINE_HEIGHT);
		SplitMenuBox smb2 = new SplitMenuBox(smb1, SplitMenuBox.Location.topLeft, s, 1f);
		mbInv = new SimpleMenuBox(smb2, SplitMenuBox.Location.topLeft);
		mbDescr = new SimpleMenuBox(smb2, SplitMenuBox.Location.bottomLeft);
	}
	
	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		InputMap in = getInputMap();
		if(in.isKeyPressed(InputMap.A_OPEN_SKILLS)){
			ctrl.viewSkills(ctrl.player.skills);
			return;
		}
		if(in.isKeyPressed(InputMap.A_CLOSE_INVENTORY)){
			ctrl.setCurrSubState(ctrl.gameplaySubState);
			return;
		}
		for(int i=0; i<10; i++) if(in.isKeyPressed(InputMap.A_QUICKSLOTS[i])) ctrl.player.inventory.setActiveQuickSlot(i);
		Input input = gc.getInput();
		int mx = input.getMouseX();
		int my = input.getMouseY();
		
		// inventory sub window
		if(mbInv.isMouseInsideBox(mx, my)){
			if(in.isKeyPressed(InputMap.A_INV_UP) || in.getMouseWheelMove() > 0) scrollInventory(-1);
			if(in.isKeyPressed(InputMap.A_INV_DOWN) || in.getMouseWheelMove() < 0) scrollInventory(1);
		}
		if(mbInv.isMouseInside(mx, my)){
			updateInventoryWindow(mbInv.getInsideMouseY(my), in, ctrl);
		} else {
			deselectInventoryItem();
		}
		// gear sub window
		if(mbGear.isMouseInsideBox(mx, my)){
			if(in.isKeyPressed(InputMap.A_INV_UP) || in.getMouseWheelMove() > 0) inventory.previousQuickSlot();
			if(in.isKeyPressed(InputMap.A_INV_DOWN) || in.getMouseWheelMove() < 0) inventory.nextQuickSlot();
		}
		if(mbGear.isMouseInside(mx, my)){
			updateGearWindow(mbGear.getInsideMouseX(mx), mbGear.getInsideMouseY(my), in, ctrl);
		} else {
			selectedGear = null;
		}
	}
	
	private void deselectInventoryItem(){
		lastInventoryIndex = -1;
		selectedInvItem = null;
	}
	
	private void updateInventoryWindow(int my, InputMap in, GameController ctrl){
		// get selected item
		int i = my / INV_LINE_HEIGHT;
		lastInventoryIndex = i + inventoryOffset;
		if(lastInventoryIndex < inventory.size() && i < getVisibleItemCount()){
			selectedInvItem = inventory.getList().get(lastInventoryIndex);
			if(selectedInvItem instanceof EmptyItem) deselectInventoryItem();
		} else {
			deselectInventoryItem();
		}
		// use or drop selected item
		if(in.isMousePressed(InputMap.A_INV_USE) && selectedInvItem != null){
			inventory.useItem(selectedInvItem);
		}
		if(in.isMousePressed(InputMap.A_INV_DROP) && selectedInvItem != null){
			boolean dropped = inventory.removeItem(selectedInvItem);
			if(dropped){
				ctrl.dropItem(selectedInvItem, inventory.getParent(), true);
			}
		}
	}

	private void updateGearWindow(int mx, int my, InputMap in, GameController ctrl){
		// test for weapon selection
		selectedGear = null;
		if(mx >= GEAR_TAB && my >= 2*GEAR_HEADLINE_HEIGHT){
			int i = (my - 2*GEAR_HEADLINE_HEIGHT) / INV_LINE_HEIGHT;
			if(i < inventory.getQuickSlotCount()) selectedGear = inventory.getQuickslot(i);
			if(selectedGear != null){
				if(in.isMouseDown(InputMap.A_INV_USE)) inventory.unequipWeapon(i);
				if(in.isMouseDown(InputMap.A_INV_DROP)) ctrl.dropItem(inventory.dropWeapon(i), inventory.getParent(), true);
			}
		}
	}
	
	private void scrollInventory(int n){
		int max = getVisibleItemCount();
		if(max > inventory.size()) return;
		inventoryOffset += n;
		inventoryOffset = Math.max(0, inventoryOffset);
		inventoryOffset = Math.min(inventory.size() - max, inventoryOffset);
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();
		renderInventorySlots(r);
		renderGear(r);
		renderDescription(r);
	}
	
	private void renderInventorySlots(GameRenderer r){
		// render selection box if necessary
		if(selectedInvItem != null){
			r.drawMenuBox(
					mbInv.getInsideX(), 
					mbInv.getInsideY() + INV_LINE_HEIGHT * (lastInventoryIndex - inventoryOffset), 
					mbInv.getInsideWidth(), 
					INV_LINE_HEIGHT, 
					GameRenderer.COLOR_MENUITEM_BG, GameRenderer.COLOR_MENUITEM_LINE);
		}
		// render item names
		Iterator<InventoryItem> iter = inventory.getList().iterator();
		int n = 0, max = getVisibleItemCount();
		for(int i=0; i<inventoryOffset; i++) iter.next();
		while(iter.hasNext() && n < max){
			InventoryItem i = iter.next();
			r.drawStringOnScreen(i.getDisplayName(), 
					8 + mbInv.getInsideX(), 
					7 + mbInv.getInsideY() + n*INV_LINE_HEIGHT, 
					i.getDisplayColor(), 1f);
			n++;
		}
		// render scroll bar if necessary
		float size = inventory.size();
		if(max < size){
			float start = inventoryOffset / size * mbInv.getInsideHeight();
			float ratio = max / size * mbInv.getInsideHeight();
			r.getGraphics().setColor(GameRenderer.COLOR_MENU_LINE);
			r.getGraphics().fillRect(mbInv.getBoxX() + 4, mbInv.getInsideY() + start, 2, ratio);
		}
	}
	
	private void renderGear(GameRenderer r){
		int x = mbGear.getInsideX();
		int y = mbGear.getInsideY();
		int x2 = x + GEAR_TAB;
		String str = "health: ";
		int strW = r.getStringWidth(str);
		r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
		r.drawStringOnScreen("" + inventory.getParent().hp, x+strW, y, Color.red, 1f); y += GEAR_HEADLINE_HEIGHT;
		r.drawStringOnScreen("weapons:", x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += GEAR_HEADLINE_HEIGHT;
		for(int i=0; i<inventory.getQuickSlotCount(); i++){
			Weapon w = inventory.getQuickslot(i);
			if(inventory.getActiveWeaponIndex() == i){
				r.drawStringOnScreen("(A)", x, 7 + y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
			}
			if(w == null){
				r.drawStringOnScreen("none", 8 + x2, 7 + y, GameRenderer.COLOR_TEXT_INACTIVE, 1f);
			} else {
				if(selectedGear == w){
					r.drawMenuBox(x2, y, mbGear.getInsideWidth() - GEAR_TAB, INV_LINE_HEIGHT, 
							GameRenderer.COLOR_MENUITEM_BG, GameRenderer.COLOR_MENUITEM_LINE);
				}
				r.drawStringOnScreen(w.getDisplayName(), 8 + x2, 7 + y, w.getDisplayColor(), 1f);
			}
			y += INV_LINE_HEIGHT;
		}
	}
	
	private void renderDescription(GameRenderer r){
		InventoryItem item = selectedInvItem != null ? selectedInvItem : selectedGear;
		int x = mbDescr.getInsideX();
		int y = mbDescr.getInsideY();
		if(item == null){
			r.drawStringOnScreen("hover over an item to see a description here", x, y, GameRenderer.COLOR_TEXT_INACTIVE, 1f);
			return;
		}
		if(item instanceof Weapon){
			((Weapon)item).renderInformation(x, y, true);
			return;
		}
		if(item instanceof HealthPack){
			HealthPack h = (HealthPack)item;
			r.drawStringOnScreen("health pack", x, y, item.getDisplayColor(), 1f);
			y += DESCR_LINE_HEIGHT;
			x += 25;
			r.drawStringOnScreen("heals for " + h.hp + " hp", x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
			return;
		}
		// no special description handler available. just print the item description string
		r.drawStringOnScreen(item.getDisplayName(), x, y, item.getDisplayColor(), 1f);
	}
	
}
