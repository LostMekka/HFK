/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items;

import hfk.PointF;
import hfk.Shot;
import hfk.game.GameController;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import hfk.stats.DamageCard;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author LostMekka
 */
public final class Inventory implements StatsModifier {
	
	private final int[] ammo = new int[Weapon.AMMO_TYPE_COUNT];
	private final LinkedList<InventoryItem> weapons = new LinkedList<>();
	private final LinkedList<InventoryItem> misc = new LinkedList<>();
	private int activeQuickSlot = 0;
	private int freeSlots = -1;
	private Weapon[] quickSlots;
	private MobStatsCard msc = null;
	private Mob parent = null;
	private LinkedList<InventoryItem> list = new LinkedList<>();
	private LinkedList<InventoryListener> listeners = new LinkedList<>();
	private boolean invChanged = false, gearChanged = false, slotsChanged = false;

	public Inventory(Mob m) {
		parent = m;
		updateMSC(m.totalStats);
	}
	
	/***
	 * creates an inventory without parent.
	 * @param statsCard the stats to use for inventory size and ammo stack size
	 */
	public Inventory(MobStatsCard statsCard) {
		updateMSC(statsCard);
	}
	
	public void addInventoryListener(InventoryListener l){
		if(!listeners.contains(l)) listeners.add(l);
	}
	
	public void removeInventoryListener(InventoryListener l){
		listeners.remove(l);
	}
	
	public void triggerInventoryChanged(){
		invChanged = true;
	}
	
	public void triggerInventoryGearChanged(){
		gearChanged = true;
	}
	
	public void triggerInventoryQuickslotChanged(){
		slotsChanged = true;
	}
	
	public int size(){
		return msc.getInventorySize();
	}

	public Mob getParent() {
		return parent;
	}
	
	public Weapon getActiveWeapon(){
		return quickSlots[activeQuickSlot];
	}
	
	public int getActiveWeaponIndex(){
		return activeQuickSlot;
	}
	
	public boolean setActiveQuickSlot(int slot){
		if(slot < 0 || slot >= quickSlots.length || slot == activeQuickSlot) return false;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponUnSelected();
		activeQuickSlot = slot;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponSelected();
		triggerInventoryQuickslotChanged();
		return true;
	}
	
	public void nextQuickSlot(){
		if(quickSlots.length <= 1) return;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponUnSelected();
		activeQuickSlot = (activeQuickSlot + 1) % quickSlots.length;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponSelected();
		triggerInventoryQuickslotChanged();
	}
	
	public void previousQuickSlot(){
		if(quickSlots.length <= 1) return;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponUnSelected();
		activeQuickSlot = (activeQuickSlot - 1 + quickSlots.length) % quickSlots.length;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponSelected();
		triggerInventoryQuickslotChanged();
	}
	
	public int getQuickSlotCount(){
		return quickSlots.length;
	}
	
	public Weapon getQuickslot(int i){
		return quickSlots[i];
	}
	
	public void updateMSC(MobStatsCard msc){
		if(this.msc != null){
			for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
				if(msc.getAmmoSlotSize(i) < this.msc.getAmmoSlotSize(i)){
					throw new IllegalArgumentException("ammo stack for type " + Weapon.AmmoType.values()[i] + " got smaller!");
				}
			}
			if(msc.getInventorySize() < this.msc.getInventorySize()){
				throw new IllegalArgumentException("inventory got smaller!");
			}
			if(msc.getQuickSlotCount() < this.msc.getQuickSlotCount()){
				throw new IllegalArgumentException("inventory quick slot count got smaller!");
			}
		}
		this.msc = msc;
		Weapon[] nqs = new Weapon[msc.getQuickSlotCount()];
		if(quickSlots != null) System.arraycopy(quickSlots, 0, nqs, 0, quickSlots.length);
		quickSlots = nqs;
		generateList();
	}
	
	public LinkedList<InventoryItem> getEquippedItems(){
		LinkedList<InventoryItem> ans = new LinkedList<>();
		for(Weapon w : quickSlots) if(w != null) ans.add(w);
		// TODO: add armor as well once it is implemented
		return ans;
	}
	
	public LinkedList<InventoryItem> getList(){
		return list;
	}
	
	public int getAmmoRoomLeft(Weapon.AmmoType type){
		int stack = getMaxAmmoStackSize(type);
		if(stack <= 0) return 0;
		int last = ammo[type.ordinal()] % stack;
		last = last == 0 ? 0 : stack - last;
		return last + freeSlots * stack;
	}
	
	public int getAmmoCount(Weapon.AmmoType t){
		return ammo[t.ordinal()];
	}
	
	public int getMaxAmmoStackSize(Weapon.AmmoType t){
		return msc.getAmmoSlotSize(t.ordinal());
	}
	
	public boolean hasAmmo(Weapon.AmmoType t, int n){
		return ammo[t.ordinal()] >= n;
	}
	
	public boolean hasAmmo(Weapon.AmmoType t){
		return ammo[t.ordinal()] > 0;
	}
	
	private LinkedList<InventoryItem> getListFor(InventoryItem i){
		LinkedList<InventoryItem> l = misc;
		if(i instanceof Weapon) l = weapons;
		return l;
	}

	public int getFreeSlots() {
		return freeSlots;
	}
	
	/**
	 * Adds ammo to this inventory.
	 * @param t the ammo type
	 * @param n the ammo count
	 * @return the amount rejected in case there was not enough space.
	 */
	public int addAmmo(Weapon.AmmoType t, int n){
		return addAmmo(t.ordinal(), n);
	}

	public int addAmmo(int ammoType, int n){
		int ss = msc.getAmmoSlotSize(ammoType);
		if(ss == 0) return 0;
		int max = ss * (int)Math.ceil((float)ammo[ammoType] / (float)ss + (float)freeSlots) - ammo[ammoType];
		int rn = Math.min(n, max);
		ammo[ammoType] += rn;
		if(rn > 0){
			generateList();
			triggerInventoryChanged();
		}
		return n - rn;
	}
	
	public boolean removeAmmo(Weapon.AmmoType t, int n){
		if(ammo[t.ordinal()] >= n){
			ammo[t.ordinal()] -= n;
			generateList();
			triggerInventoryChanged();
			return true;
		}
		return false;
	}
	
	public InventoryItem addItem(InventoryItem i){
		if(i instanceof EmptyItem){
			System.out.println("WARNING! inventory got an empty item! it consumed it.");
			return null;
		}
		// addAmmo already generates the inventory list. for all others: re-generate the item list!!!
		if(i instanceof AmmoItem){
			AmmoItem ai = (AmmoItem)i;
			int s = ai.getAmmoCount();
			int n = addAmmo(ai.getAmmoType(), s);
			if(n == 0) return null;
			ai.setAmmoCount(n);
			return ai;
		}
		if(freeSlots <= 0) return i;
		LinkedList<InventoryItem> l = getListFor(i);
		if(l.contains(i)) return i;
		insertSorted(i, l);
		generateList();
		i.parentInventory = this;
		return null;
	}
	
	/**
	 * Removes the {@code InventoryItem} from the inventory.
	 * @param i The item to remove.
	 * @return the item actually removed or {@code null} if the item
	 * was not found or it is not removable.
	 */
	public InventoryItem removeItem(InventoryItem i){
		InventoryItem ans = null;
		LinkedList<InventoryItem> l = getListFor(i);
		if(i instanceof AmmoItem){
			// ammo in backpack
			AmmoItem ai = (AmmoItem)i;
			if(removeAmmo(ai.getAmmoType(), ai.getAmmoCount())) ans = ai;
		} else if(l.remove(i)){
			// other items in backpack
			ans = i;
		} else {
			// item is not in backpack. try to remove frop equipped items
			ans = removeEquippedItem(i);
		}
		if(ans != null) generateList();
		return ans;
	}
	
	/**
	 * Removes the equipped {@code InventoryItem} from the inventory.
	 * @param i The item to remove. (must be equipped)
	 * @return the item actually removed or {@code null} if the item
	 * was not found or it is not removable.
	 */
	public InventoryItem removeEquippedItem(InventoryItem i){
		if(i instanceof Weapon) return removeEquippedWeapon((Weapon)i);
		// TODO: remove armor
		return null;
	}
	
	/**
	 * Removes all {@code InventoryItem}s from this inventory. This also
	 * includes equipped items like weapons and armor. This is used to drop
	 * everything when a {@code Mob} dies.
	 * @return a list of all removed items.
	 */
	public LinkedList<InventoryItem> removeAll(){
		LinkedList<InventoryItem> removed = new LinkedList<>();
		// remove equipped weapons
		for(int i=0; i<quickSlots.length; i++){
			Weapon w = quickSlots[i];
			if(w != null){
				w.weaponUnSelected();
				removed.add(w);
				w.shotTeam = Shot.Team.dontcare;
				quickSlots[i] = null;
			}
		}
		// remove items in backpack
		for(InventoryItem i : list){
			if(i instanceof EmptyItem) continue;
			removed.add(i);
			removeItem(i);
			i.parentInventory = null;
		}
		// keep state consistent
		if(parent != null) parent.recalculateCards();
		generateList();
		triggerInventoryGearChanged();
		return removed;
	}
	
	/**
	 * Convenience method for using an {@code InventoryItem} that is stored in
	 * this Inventory. It uses {@code InventoryItem.use(Mob)} internally,
	 * removes the item and adds the resulting item if such item does exist.
	 * @param i the {@code InventoryItem} to use. (must be in this inventory)
	 * @return {@code true} if the use process was successful and {@code false}
	 * if unsuccessful or the item is not in this inventory.
	 */
	public boolean useItemFromInventory(InventoryItem i){
		return useItemFromInventory(i, parent);
	}
	
	/**
	 * Convenience method for using an {@code InventoryItem} that is stored in
	 * this Inventory. Use this method if the mob using the item is a different
	 * one than the owner of this inventory. The method uses
	 * {@code InventoryItem.use(Mob)} internally, removes the item and adds the
	 * resulting item if such item does exist.
	 * @param i the {@code InventoryItem} to use. (must be in this inventory)
	 * @param mobToUseOn the mob that uses the item.
	 * @return {@code true} if the use process was successful and {@code false}
	 * if unsuccessful or the item is not in this inventory.
	 */
	public boolean useItemFromInventory(InventoryItem i, Mob mobToUseOn){
		if(!getListFor(i).contains(i)) return false;
		InventoryItem iNew = i.use(mobToUseOn);
		if(iNew == i) return false; // TODO: return true when item changed state?
		removeItem(i);
		if(iNew != null) addItem(iNew);
		return true;
	}
	
	/**
	 * Convenience method for equipping an {@code InventoryItem} that is stored
	 * in this Inventory. It uses the appropriate equip method for the item type.
	 * @param i the {@code InventoryItem} to equip. (must be in this inventory)
	 * @return {@code true} if the item was successfully equipped and
	 * {@code false} if the parent of the inventory could not equip the item or
	 * the item is not in this inventory.
	 */
	public boolean equipItemFromInventory(InventoryItem i){
		if(i instanceof Weapon) return equipWeaponFromInventory((Weapon)i);
		// TODO: equip armor
		return false;
	}
	
	/**
	 * Equips the {@code InventoryItem} if possible. If there already was an
	 * item equipped at the target location it will be exchanged and the old
	 * item is returned.
	 * @param i The item to equip.
	 * @return {@code null} if all went well, the previously equipped
	 * {code InventoryItem} if there was one and the item {@code i} itself if
	 * it was not possible to equip the item.
	 */
	public InventoryItem equipItem(InventoryItem i){
		if(i instanceof Weapon) return equipWeapon((Weapon)i);
		// TODO: equip armor
		return i;
	}
	
	/**
	 * Convenience method for unequipping an {@code InventoryItem}. The item
	 * will automatically stored in the inventory.
	 * @param i the {@code InventoryItem} to unequip. (must be equipped)
	 * @return {@code true} if the item was successfully unequipped and
	 * {@code false} if the item was not equipped, the item cannot be unequipped
	 * or the inventory has no space to store the item.
	 */
	public boolean unequipItem(InventoryItem i){
		if(i instanceof Weapon) return unequipWeapon((Weapon)i);
		// TODO: unequip armor
		return false;
	}
	
	/**
	 * Convenience method for equipping a {@code Weapon} that is stored
	 * in this Inventory. It uses the appropriate equip method for the item type.
	 * @param w the {@code Weapon} to equip. (must be in this inventory)
	 * @return {@code true} if the weapon was successfully equipped and
	 * {@code false} if the parent of the inventory could not equip the weapon
	 * or the weapon is not in this inventory.
	 */
	public boolean equipWeaponFromInventory(Weapon w){
		Weapon active = quickSlots[activeQuickSlot];
		// only put active weapon in backpack if it is not shooting (i.e. burst in progress)
		if(active != null && !active.isShooting()) return false;
		int i = weapons.indexOf(w);
		if(i == -1) return false;
		// first unselect previous weapon
		if(active != null) active.weaponUnSelected();
		// swap weapons
		quickSlots[activeQuickSlot] = w;
		weapons.remove(w);
		if(active != null) insertSorted(active, weapons);
		// select new weapon
		w.angle = parent.getLookAngle();
		w.shotTeam = parent instanceof Player ? Shot.Team.friendly : Shot.Team.hostile;
		w.weaponSelected();
		// keep state consistent
		generateList();
		triggerInventoryGearChanged();
		parent.recalculateCards();
		return true;
	}
	
	/**
	 * Equips the {@code Weapon} if possible. If there already was a weapon
	 * equipped at the target location it will be exchanged and the old weapon
	 * is returned.
	 * @param w The weapon to equip.
	 * @return {@code null} if all went well, the previously equipped
	 * {code Weapon} if there was one and the weapon {@code w} itself if it was
	 * not possible to equip the weapon.
	 */
	public Weapon equipWeapon(Weapon w){
		return equipWeapon(w, false, true);
	}
	
	/**
	 * Equips the {@code Weapon} if possible. If there already was a weapon
	 * equipped at the current quick slot, then there are many possible outcomes
	 * depending on the other parameters: If {@code useOtherQuickslots} is
	 * {@code true} the method will try to fill another empty quick slot if
	 * available. If there is no free slot found, the method will exchange
	 * the currently held weapon for the new one if {@code replaceIfNecessary}
	 * is {@code true} and ignore the equip request otherwise.
	 * @param w The weapon to equip.
	 * @param useOtherQuickslots determines whether other empty quick slots
	 * should be used, if the currently active one is already full.
	 * @param replaceIfNecessary determines whether to replace the active
	 * weapon if there was no empty quick slot to equip to or just ignore the
	 * request.
	 * @return {@code null} if all went well, the previously equipped
	 * {code Weapon} if there was one and the weapon {@code w} itself if it was
	 * not possible to equip the weapon.
	 */
	public Weapon equipWeapon(Weapon w, boolean useOtherQuickslots, boolean replaceIfNecessary){
		// find suitable slot
		int index = activeQuickSlot;
		if(useOtherQuickslots && quickSlots[activeQuickSlot] != null){
			for(int i=0; i<quickSlots.length; i++){
				if(quickSlots[i] == null){
					index = i;
					break;
				}
			}
		}
		Weapon previous = quickSlots[index];
		// don't remove active weapon if it is shooting (i.e. burst in progress)
		// also don't replace weapons if parameter forbids it!
		if(previous != null && (!replaceIfNecessary || previous.isShooting())) return w;
		//first unselect current weapon
		if(previous != null){
			previous.weaponUnSelected();
			previous.parentInventory = null;
		}
		// exchange weapons
		quickSlots[index] = w;
		// select new weapon
		w.parentInventory = this;
		w.angle = parent.getLookAngle();
		w.shotTeam = parent instanceof Player ? Shot.Team.friendly : Shot.Team.hostile;
		w.weaponSelected();
		// keep state consistent
		parent.recalculateCards();
		triggerInventoryGearChanged();
		return previous;
	}
	
	/**
	 * Convenience method for unequipping the currently active weapon. This
	 * just calls {@code this.unequipWeapon(this.getActiveWeaponIndex())}.
	 * @return the value of {@code this.unequipWeapon(this.getActiveWeaponIndex())}
	 */
	public boolean unequipCurrentWeapon(){
		return unequipWeapon(activeQuickSlot);
	}
	
	/**
	 * Convenience method for unequipping the weapon specified. The weapon will
	 * automatically stored in the inventory.
	 * @param w the weapon to unequip. (must be equipped)
	 * @return {@code true} if the weapon was unequipped successfully and
	 * {@code false} if the weapon was not equipped or cannot be unequipped, the
	 * inventory has no room to store the weapon or the quick slot was empty.
	 */
	public boolean unequipWeapon(Weapon w){
		for(int i=0; i<quickSlots.length; i++){
			if(quickSlots[i] == w){
				return unequipWeapon(i);
			}
		}
		return false;
	}
	
	/**
	 * Convenience method for unequipping the {@code Weapon} in the specified
	 * quick slot. The weapon will automatically stored in the inventory.
	 * @param index the number of the weapon slot.
	 * @return {@code true} if the weapon was unequipped successfully and
	 * {@code false} if the weapon cannot be unequipped, the inventory has no
	 * room to store the weapon or the quick slot was empty.
	 */
	public boolean unequipWeapon(int index){
		if(freeSlots <= 0 || index < 0 || index >= quickSlots.length) return false;
		Weapon w = quickSlots[index];
		if(w == null || !w.isReady()) return false;
		w.weaponUnSelected();
		quickSlots[index] = null;
		insertSorted(w, weapons);
		generateList();
		triggerInventoryGearChanged();
		parent.recalculateCards();
		return true;
	}
	
	/**
	 * Convenience method for removing the currently active Weapon.
	 * @return the weapon removed or {@code null} if there is no current weapon
	 * or the current weapon is not unequippable.
	 */
	public Weapon removeCurrentWeapon(){
		return removeEquippedWeapon(activeQuickSlot);
	}
	
	/**
	 * Convenience method for removing an equipped Weapon.
	 * @param w the weapon to remove. (must be equipped)
	 * @return the weapon removed or {@code null} if there is no current weapon
	 * or the current weapon is not unequippable.
	 */
	public Weapon removeEquippedWeapon(Weapon w){
		for(int i=0; i<quickSlots.length; i++){
			if(quickSlots[i] == w){
				return removeEquippedWeapon(i);
			}
		}
		return null;
	}
	
	/**
	 * Convenience method for removing an equipped Weapon.
	 * @param index the index of the quick slot to remove the weapon from.
	 * @return the weapon removed or {@code null} if there is no weapon in this 
	 * quick slot or the weapon is not unequippable.
	 */
	public Weapon removeEquippedWeapon(int index){
		if(index < 0 || index >= quickSlots.length) return null;
		Weapon w = quickSlots[index];
		if(w == null) return null;
		w.weaponUnSelected();
		quickSlots[index] = null;
		w.parentInventory = null;
		w.shotTeam = Shot.Team.dontcare;
		w.initLabel();
		triggerInventoryGearChanged();
		parent.recalculateCards();
		return w;
	}
	
	public boolean shootWithActiveWeapon(){
		Weapon w = quickSlots[activeQuickSlot];
		if(w == null) return false;
		return w.pullTrigger();
	}
	
	public void update(int time){
		for(InventoryItem i : getEquippedItems()){
			i.update(time, true, i == getActiveWeapon());
		}
		for(InventoryItem i : list) i.update(time, false, false);
		// notify listeners once per update
		if(invChanged) for(InventoryListener l : listeners) l.inventoryChanged(this);
		if(gearChanged) for(InventoryListener l : listeners) l.inventoryGearChanged(this);
		if(slotsChanged) for(InventoryListener l : listeners) l.inventoryQuickslotChanged(this);
		invChanged = false;
		gearChanged = false;
		slotsChanged = false;
	}
	
	public void recalculateStats(){
		if(parent == null) return;
		updateMSC(parent.totalStats);
		LinkedList<InventoryItem> equipped = getEquippedItems();
		for(InventoryItem i : equipped){
			if(i instanceof Weapon){
				Weapon w = (Weapon)i;
				w.recalculateStats();
			}
		}
		for(InventoryItem i : list){
			if(!equipped.contains(i) && i instanceof Weapon){
				((Weapon)i).resetStats();
			}
		}
	}
	
	@Override
	public void addDamageCardEffects(DamageCard card, InventoryItem i ,Mob m) {
		for(InventoryItem equipped : getEquippedItems()){
			equipped.addDamageCardEffects(card, i, m);
		}
	}

	@Override
	public void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m) {
		for(InventoryItem i : getEquippedItems()){
			i.addWeaponStatsCardEffects(card, w, m);
		}
	}

	@Override
	public void addMobStatsCardEffects(MobStatsCard card, Mob m) {
		for(InventoryItem i : getEquippedItems()){
			i.addMobStatsCardEffects(card, m);
		}
	}

	private void generateList(){
		list = new LinkedList<>();
		list.addAll(weapons);
		list.addAll(misc);
		for(Weapon.AmmoType t : Weapon.AmmoType.values()){
			if(t == Weapon.AmmoType.energy) continue;
			int i = t.ordinal();
			int a = ammo[i];
			int ss = msc.getAmmoSlotSize(i);
			if(ss == 0) continue;
			int ns = a / ss;
			int rest = a - ns * ss;
			for(int n=0; n<ns; n++) list.add(new AmmoItem(new PointF(), t, ss));
			if(rest > 0) list.add(new AmmoItem(new PointF(), t, rest));
		}
		freeSlots = msc.getInventorySize() - list.size();
		for(int i=0; i<freeSlots; i++) list.add(new EmptyItem(new PointF()));
		triggerInventoryChanged();
	}

	private void insertSorted(InventoryItem i, LinkedList<InventoryItem> l){
		ListIterator<InventoryItem> iter = l.listIterator();
		long r = i.getRarityScore();
		while(iter.hasNext()){
			InventoryItem li = iter.next();
			if(li.getRarityScore() > r){
				iter.previous();
				iter.add(i);
				return;
			}
		}
		l.add(i);
	}

	public void addAmmoClips(Weapon weapon, int n) {
		int energyType = Weapon.AmmoType.energy.ordinal();
		for (int ammoType = 0; ammoType < Weapon.AMMO_TYPE_COUNT; ammoType++) {
			if (ammoType == energyType) {
				continue;
			}
			int clipSize = weapon.totalStats.clipSize[ammoType];
			if (clipSize > 0) {
				addAmmo(ammoType, clipSize * n);
			}
		}
	}

	public void addActiveWeaponAmmoClips(int n) {
		addAmmoClips(getActiveWeapon(), n);
	}
}
