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
import hfk.items.AmmoItem;
import hfk.items.EmptyItem;
import hfk.items.HealthPack;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.InventoryListener;
import hfk.items.weapons.Weapon;
import hfk.menu.MenuItemList;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import hfk.stats.Damage;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class ExchangeSubState extends GameSubState implements InventoryListener {

	private Inventory invLeft = null, invRight = null;
	private InventoryItem selectedItem = null;
	private SplitMenuBox mb;
	private SimpleMenuBox mbLeft, mbRight, mbDescr;
	private MenuItemList<InventoryItem> listLeft, listRight;
	
	public ExchangeSubState(InputMap inputMap) {
		super(inputMap);
	}

	public Inventory getLeftInventory() {
		return invLeft;
	}

	public Inventory getRightInventory() {
		return invRight;
	}

	public void init(Inventory il, Inventory ir){
		if(invLeft != null) invLeft.removeInventoryListener(this);
		if(invRight != null) invRight.removeInventoryListener(this);
		invLeft = il;
		invRight = ir;
		invLeft.addInventoryListener(this);
		invRight.addInventoryListener(this);
		listLeft.unselect();
		listRight.unselect();
		populateInventoryLists();
	}

	@Override
	public void inventoryChanged(Inventory inventory) {
		populateInventoryLists();
	}

	@Override
	public void inventoryGearChanged(Inventory inventory) {}

	@Override
	public void inventoryQuickslotChanged(Inventory inventory) {}
	
	private void populateInventoryLists(){
		// left
		int sel = listLeft.getSelectedIndex();
		listLeft.clearList();
		for(InventoryItem i : invLeft.getList()){
			listLeft.addListItem(i, i.getDisplayName(), i.getDisplayColor());
		}
		listLeft.selectIndex(sel);
		// right
		sel = listRight.getSelectedIndex();
		listRight.clearList();
		for(InventoryItem i : invRight.getList()){
			listRight.addListItem(i, i.getDisplayName(), i.getDisplayColor());
		}
		listRight.selectIndex(sel);
	}
	
	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		int descLineCount = 1 + Math.max(Damage.DAMAGE_TYPE_COUNT, Weapon.AMMO_TYPE_COUNT);
		mb = new SplitMenuBox(gc, 1f, 0.5f);
		mbRight = new SimpleMenuBox(mb, SplitMenuBox.Location.topRight);
		listRight = new MenuItemList<>(mbRight, true);
		float s = SplitMenuBox.getSplitRatioFromSecondSize(gc.getHeight(), descLineCount*GameRenderer.MIN_TEXT_HEIGHT);
		SplitMenuBox smb2 = new SplitMenuBox(mb, SplitMenuBox.Location.topLeft, s, 1f);
		mbLeft = new SimpleMenuBox(smb2, SplitMenuBox.Location.topLeft);
		listLeft = new MenuItemList<>(mbLeft, true);
		mbDescr = new SimpleMenuBox(smb2, SplitMenuBox.Location.bottomLeft);
	}
	
	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		InputMap in = getInputMap();
		if(in.isPressed(Action.A_INVENTORY_OPEN)){
			ctrl.viewInventory(ctrl.player.inventory);
			return;
		}
		if(in.isPressed(Action.A_SKILLS_OPEN)){
			ctrl.viewSkills(ctrl.player.skills);
			return;
		}
		if(in.isPressed(Action.A_EXCHANGE_CLOSE)){
			ctrl.setCurrSubState(ctrl.gameplaySubState);
			return;
		}
		int mx = gc.getInput().getMouseX();
		int my = gc.getInput().getMouseY();
		// handle scrolling
		if(mbLeft.isMouseInsideBox(mx, my)){
			if(in.isPressed(Action.A_INVENTORY_UP) || in.getMouseWheelMove() > 0) listLeft.scroll(-1);
			if(in.isPressed(Action.A_INVENTORY_DOWN) || in.getMouseWheelMove() < 0) listLeft.scroll(1);
		}
		if(mbRight.isMouseInsideBox(mx, my)){
			if(in.isPressed(Action.A_INVENTORY_UP) || in.getMouseWheelMove() > 0) listRight.scroll(-1);
			if(in.isPressed(Action.A_INVENTORY_DOWN) || in.getMouseWheelMove() < 0) listRight.scroll(1);
		}
		// handle selection
		listLeft.updateSelection(mx, my);
		listRight.updateSelection(mx, my);
		selectedItem = listLeft.getSelectedObject();
		boolean selectedIsLeft = true;
		if(selectedItem == null){
			selectedItem = listRight.getSelectedObject();
			selectedIsLeft = false;
		}
		if(selectedItem instanceof EmptyItem){
			(selectedIsLeft ? listLeft : listRight).unselect();
			selectedItem = null;
		}
		// handle click input
		if(selectedItem != null){
			Inventory source = selectedIsLeft ? invLeft : invRight;
			Inventory target = selectedIsLeft ? invRight : invLeft;
			if(in.isPressed(Action.A_EXCHANGE_MOVE)){
				// move item. ammo items are handled differently
				if(selectedItem instanceof AmmoItem){
					// TODO: make ammo item a stackable item and check for stackable instead here
					AmmoItem ai = (AmmoItem)selectedItem;
					Weapon.AmmoType t = ai.getAmmoType();
					int tTotal = target.getAmmoCount(t);
					int tStack = target.getMaxAmmoStackSize(t);
					int tLast = tTotal % tStack;
					int amount = in.isDown(Action.A_EXCHANGE_ALTERNATIVE)
							? Math.min(tTotal, tStack*target.getFreeSlots() + tStack - tLast)
							: Math.min(ai.getAmmoCount(), tStack - tLast);
					if(amount > 0){
						source.removeAmmo(t, amount);
						int back = target.addAmmo(t, amount);
						source.addAmmo(t, back);
						populateInventoryLists();
					}
				} else{
					InventoryItem removed = source.removeItem(selectedItem);
					if(removed != null){
						InventoryItem leftover = target.addItem(removed);
						// add remaining item back to source inventory
						if(leftover != null){
							InventoryItem i = source.addItem(leftover);
							if(i != null) exchangeError("exchange", i);
						}
						populateInventoryLists();
					}
				}
			} else if(in.isPressed(Action.A_EXCHANGE_USE)){
				// use item if possible
				source.useItemFromInventory(selectedItem, invLeft.getParent());
				populateInventoryLists();
			} else if(in.isPressed(Action.A_EXCHANGE_DROP)){
				// drop item
				InventoryItem removed = source.removeItem(selectedItem);
				if(removed != null) ctrl.dropItem(selectedItem, invLeft.getParent(), false);
				populateInventoryLists();
			} else if(in.isPressed(Action.A_EXCHANGE_UNLOAD)){
				// unload weapon. put ammo in left inventory
				if(selectedItem instanceof Weapon && invLeft.getParent() != null){
					invLeft.getParent().unloadWeapon((Weapon)selectedItem);
				}
			}
		}
	}
	
	private void exchangeError(String action, InventoryItem i){
		// this should never happen if inventories work
		// as intended. just in case they dot't: drop
		// leftover item instead of just feeding it to
		// the garbage collector.
		System.out.format("WARNING: %s has gone wrong! Neither inventory wants to accept leftover item! (%s) Dropping it instead...\n", action, i.getClass().getName());
		GameController.get().dropItem(i, invLeft.getParent(), false);
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();
		renderDescription(r);
	}
	
	private void renderDescription(GameRenderer r){
		int x = mbDescr.getUsableX();
		int y = mbDescr.getUsableY();
		if(selectedItem == null){
			r.drawStringOnScreen("hover over an item to see a description here", 
					x, y, GameRenderer.COLOR_TEXT_INACTIVE, 1f);
			return;
		}
		if(selectedItem instanceof Weapon){
			((Weapon)selectedItem).renderInformation(x, y, true);
			return;
		}
		if(selectedItem instanceof HealthPack){
			HealthPack h = (HealthPack)selectedItem;
			r.drawStringOnScreen("health pack", x, y, 
					selectedItem.getDisplayColor(), 1f);
			y += GameRenderer.MIN_TEXT_HEIGHT;
			x += 25;
			r.drawStringOnScreen("heals for " + h.hp + " hp", x, y, 
					GameRenderer.COLOR_TEXT_NORMAL, 1f);
			return;
		}
		// no special description handler available. just print the item description string
		r.drawStringOnScreen(selectedItem.getDisplayName(), x, y, 
				selectedItem.getDisplayColor(), 1f);
	}
	
}
