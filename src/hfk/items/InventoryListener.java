/*
 */
package hfk.items;

/**
 *
 * @author LostMekka
 */
public interface InventoryListener {
	
	public void inventoryChanged(Inventory inventory);
	public void inventoryGearChanged(Inventory inventory);
	public void inventoryQuickslotChanged(Inventory inventory);
	
}
