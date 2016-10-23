/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.game.InputMap.Action;
import hfk.items.EmptyItem;
import hfk.items.HealthPack;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.InventoryListener;
import hfk.items.weapons.Weapon;
import hfk.menu.MenuBox;
import hfk.menu.MenuItemList;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import hfk.mobs.Mob;
import hfk.stats.Damage;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class InventorySubState extends GameSubState implements InventoryListener {

	private static final int INV_LINE_HEIGHT = 30;
	private static final int GEAR_HEADLINE_HEIGHT = 25;
	private static final int GEAR_TAB = 30;
	private static final int DESCR_LINE_HEIGHT = GameRenderer.MIN_TEXT_HEIGHT;

	private Inventory inventory = null;
	private InventoryItem selectedInvItem = null, selectedGear = null;
	private MenuBox mb, mbDescr, mbGear;
	private SimpleMenuBox mbInv;
	private MenuItemList<InventoryItem> invList;
	
	public InventorySubState(InputMap inputMap) {
		super(inputMap);
	}

	public Inventory getInventory() {
		return inventory;
	}

	public void init(Inventory i){
		if(inventory != null) inventory.removeInventoryListener(this);
		i.addInventoryListener(this);
		inventory = i;
		invList.unselect();
		populateInventoryList();
	}

	@Override
	public void inventoryChanged(Inventory inventory) {
		populateInventoryList();
	}

	@Override
	public void inventoryGearChanged(Inventory inventory) {}

	@Override
	public void inventoryQuickslotChanged(Inventory inventory) {}
	
	private void populateInventoryList(){
		if(inventory == null) return;
		int sel = invList.getSelectedIndex();
		invList.clearList();
		for(InventoryItem i : inventory.getList()){
			invList.addListItem(i, i.getDisplayName(), i.getDisplayColor());
		}
		invList.selectIndex(sel);
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
		invList = new MenuItemList<>(mbInv, true);
		mbDescr = new SimpleMenuBox(smb2, SplitMenuBox.Location.bottomLeft);
	}
	
	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		InputMap in = getInputMap();
		if(in.isPressed(Action.A_SKILLS_OPEN)){
			ctrl.viewSkills(ctrl.player.skills);
			return;
		}
		if(in.isPressed(Action.A_INVENTORY_CLOSE)){
			ctrl.setCurrSubState(ctrl.gameplaySubState);
			return;
		}
		if(in.isPressed(Action.A_QUICK_SLOT_0)) ctrl.player.inventory.setActiveQuickSlot(0);
		if(in.isPressed(Action.A_QUICK_SLOT_1)) ctrl.player.inventory.setActiveQuickSlot(1);
		if(in.isPressed(Action.A_QUICK_SLOT_2)) ctrl.player.inventory.setActiveQuickSlot(2);
		if(in.isPressed(Action.A_QUICK_SLOT_3)) ctrl.player.inventory.setActiveQuickSlot(3);
		if(in.isPressed(Action.A_QUICK_SLOT_4)) ctrl.player.inventory.setActiveQuickSlot(4);
		if(in.isPressed(Action.A_QUICK_SLOT_5)) ctrl.player.inventory.setActiveQuickSlot(5);
		if(in.isPressed(Action.A_QUICK_SLOT_6)) ctrl.player.inventory.setActiveQuickSlot(6);
		if(in.isPressed(Action.A_QUICK_SLOT_7)) ctrl.player.inventory.setActiveQuickSlot(7);
		if(in.isPressed(Action.A_QUICK_SLOT_8)) ctrl.player.inventory.setActiveQuickSlot(8);
		if(in.isPressed(Action.A_QUICK_SLOT_9)) ctrl.player.inventory.setActiveQuickSlot(9);
		Input input = gc.getInput();
		int mx = input.getMouseX();
		int my = input.getMouseY();
		
		updateInventoryWindow(mx, my, in, ctrl);
		// gear sub window
		if(mbGear.isMouseInsideBox(mx, my)){
			if(in.isPressed(Action.A_INVENTORY_UP) || in.getMouseWheelMove() > 0) inventory.previousQuickSlot();
			if(in.isPressed(Action.A_INVENTORY_DOWN) || in.getMouseWheelMove() < 0) inventory.nextQuickSlot();
		}
		if(mbGear.isMouseInsideUsable(mx, my)){
			updateGearWindow(mbGear.getUsableRelativeMouseX(mx), mbGear.getUsableRelativeMouseY(my), in, ctrl);
		} else {
			selectedGear = null;
		}
	}
	
	private void updateInventoryWindow(int mx, int my, InputMap in, GameController ctrl){
		if(mbInv.isMouseInsideBox(mx, my)){
			if(in.isPressed(Action.A_INVENTORY_UP) || in.getMouseWheelMove() > 0) invList.scroll(-1);
			if(in.isPressed(Action.A_INVENTORY_DOWN) || in.getMouseWheelMove() < 0) invList.scroll(1);
		}
		invList.updateSelection(mx, my);
		selectedInvItem = invList.getSelectedObject();
		if(selectedInvItem instanceof EmptyItem){
			invList.unselect();
			selectedInvItem = null;
		}
		if(selectedInvItem != null){
			// use or drop selected item
			if(in.isPressed(Action.A_INVENTORY_USE) && selectedInvItem != null){
				inventory.useItemFromInventory(selectedInvItem);
				populateInventoryList();
			}
			if(in.isPressed(Action.A_INVENTORY_DROP) && selectedInvItem != null){
				InventoryItem dropped = inventory.removeItem(selectedInvItem);
				if(dropped != null){
					ctrl.dropItem(dropped, inventory.getParent(), true);
					populateInventoryList();
				}
			}
			if(in.isPressed(Action.A_INVENTORY_UNLOAD) && selectedInvItem instanceof Weapon){
				Mob p = inventory.getParent();
				if(p == null){
					((Weapon)selectedInvItem).unloadToGround();
				} else {
					p.unloadWeapon((Weapon)selectedInvItem);
				}
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
				if(in.isPressed(Action.A_INVENTORY_USE)){
					inventory.unequipWeapon(i);
					populateInventoryList();
				}
				if(in.isPressed(Action.A_INVENTORY_DROP)){
					InventoryItem dropped = inventory.removeEquippedItem(selectedGear);
					if(dropped != null) ctrl.dropItem(dropped, inventory.getParent(), true);
				}
			}
		}
	}
	
	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();
		renderGear(r);
		renderDescription(r);
	}
	
	private void renderGear(GameRenderer r){
		int x = mbGear.getUsableX();
		int y = mbGear.getUsableY();
		int x2 = x + GEAR_TAB;
		String str = "health: ";
		int strW = r.getStringWidth(str);
		r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
		r.drawStringOnScreen("" + inventory.getParent().hp + " / " + inventory.getParent().totalStats.getMaxHP(), x+strW, y, Color.red, 1f); y += GEAR_HEADLINE_HEIGHT;
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
					r.drawMenuBox(x2, y, mbGear.getUsableWidth() - GEAR_TAB, INV_LINE_HEIGHT, 
							GameRenderer.COLOR_MENUITEM_BG, GameRenderer.COLOR_MENUITEM_LINE);
				}
				r.drawStringOnScreen(w.getDisplayName(), 8 + x2, 7 + y, w.getDisplayColor(), 1f);
			}
			y += INV_LINE_HEIGHT;
		}
	}
	
	private void renderDescription(GameRenderer r){
		InventoryItem item = selectedInvItem != null ? selectedInvItem : selectedGear;
		int x = mbDescr.getUsableX();
		int y = mbDescr.getUsableY();
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
