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
		if(slot < 0 || slot >= quickSlots.length) return false;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponUnSelected();
		activeQuickSlot = slot;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponSelected();
		return true;
	}
	
	public void nextQuickSlot(){
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponUnSelected();
		activeQuickSlot = (activeQuickSlot + 1) % quickSlots.length;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponSelected();
	}
	
	public void previousQuickSlot(){
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponUnSelected();
		activeQuickSlot = (activeQuickSlot - 1 + quickSlots.length) % quickSlots.length;
		if(quickSlots[activeQuickSlot] != null) quickSlots[activeQuickSlot].weaponSelected();
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
		if(rn > 0) generateList();
		return n - rn;
	}
	
	public boolean removeAmmo(Weapon.AmmoType t, int n){
		if(ammo[t.ordinal()] >= n){
			ammo[t.ordinal()] -= n;
			generateList();
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
	
	public boolean removeItem(InventoryItem i){
		if(i instanceof AmmoItem){
			AmmoItem ai = (AmmoItem)i;
			return removeAmmo(ai.getAmmoType(), ai.getAmmoCount());
		}
		boolean b = getListFor(i).remove(i);
		if(b){
			generateList();
			i.parentInventory = null;
		}
		return b;
	}
	
	public LinkedList<InventoryItem> removeAll(){
		LinkedList<InventoryItem> l = new LinkedList<>();
		for(int i=0; i<quickSlots.length; i++){
			Weapon w = quickSlots[i];
			if(w != null){
				w.weaponUnSelected();
				l.add(w);
				w.parentInventory = null;
				quickSlots[i] = null;
			}
		}
		for(InventoryItem i : list){
			if(i instanceof EmptyItem) continue;
			l.add(i);
			removeItem(i);
			i.parentInventory = null;
		}
		if(parent != null) parent.recalculateCards();
		return l;
	}
	
	public boolean useItem(InventoryItem i){
		LinkedList<InventoryItem> l = getListFor(i);
		if(!l.contains(i)) return false;
		if(!i.use(parent, true)) return false;
		if(i.destroyWhenUsed) removeItem(i);
		return true;
	}
	
	public boolean equipWeaponFromInventory(Weapon w){
		Weapon tmp = quickSlots[activeQuickSlot];
		if(tmp != null && !tmp.isReady()) return false;
		if(tmp != null) tmp.weaponUnSelected();
		w.weaponSelected();
		int i = weapons.indexOf(w);
		if(i == -1) throw new IllegalArgumentException("weapon is not in inventory!");
		quickSlots[activeQuickSlot] = w;
		w.parentInventory = this;
		weapons.remove(w);
		if(tmp != null) insertSorted(tmp, weapons);
		w.angle = parent.getLookAngle();
		w.shotTeam = Shot.Team.hostile;
		if(parent == GameController.get().player) w.shotTeam = Shot.Team.friendly;
		generateList();
		parent.recalculateCards();
		return true;
	}
	
	public boolean equipWeaponFromGround(Weapon w){
		if(quickSlots[activeQuickSlot] != null) return false;
		quickSlots[activeQuickSlot] = w;
		w.weaponSelected();
		w.parentInventory = this;
		w.angle = parent.getLookAngle();
		w.shotTeam = Shot.Team.hostile;
		if(parent == GameController.get().player) w.shotTeam = Shot.Team.friendly;
		parent.recalculateCards();
		return true;
	}
	
	public boolean unequipCurrentWeapon(){
		if(freeSlots <= 0) return false;
		Weapon w = quickSlots[activeQuickSlot];
		if(!w.isReady()) return false;
		w.weaponUnSelected();
		quickSlots[activeQuickSlot] = null;
		insertSorted(w, weapons);
		generateList();
		parent.recalculateCards();
		return true;
	}
	
	public boolean unequipWeapon(int index){
		if(freeSlots <= 0 || index < 0 || index >= quickSlots.length) return false;
		Weapon w = quickSlots[index];
		if(!w.isReady()) return false;
		w.weaponUnSelected();
		quickSlots[index] = null;
		insertSorted(w, weapons);
		generateList();
		parent.recalculateCards();
		return true;
	}
	
	public Weapon dropCurrentWeapon(){
		Weapon w = quickSlots[activeQuickSlot];
		if(!w.isReady()) return null;
		quickSlots[activeQuickSlot] = null;
		w.parentInventory = null;
		w.weaponUnSelected();
		parent.recalculateCards();
		return w;
	}
	
	public Weapon dropWeapon(int index){
		if(index < 0 || index >= quickSlots.length) return null;
		Weapon w = quickSlots[index];
		w.weaponUnSelected();
		quickSlots[index] = null;
		w.parentInventory = null;
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
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++){
			Weapon.AmmoType t = Weapon.AmmoType.values()[i];
			int a = ammo[i];
			int ss = msc.getAmmoSlotSize(i);
			if(ss == 0) continue;
			int ns = a / ss;
			int rest = a - ns * ss;
			for(int n=0; n<ns; n++) list.add(new AmmoItem(new PointF(), t, ss));
			AmmoItem ai = new AmmoItem(new PointF(), t, rest);
			ai.parentInventory = this;
			if(rest != 0) list.add(ai);
		}
		freeSlots = msc.getInventorySize() - list.size();
		for(int i=0; i<freeSlots; i++) list.add(new EmptyItem(new PointF()));
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
