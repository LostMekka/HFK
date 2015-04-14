/*
 */
package hfk.items;

import hfk.PointF;
import hfk.items.weapons.AutoShotgun;
import hfk.items.weapons.CheatRifle;
import hfk.items.weapons.DamagedHuntingGun;
import hfk.items.weapons.DoubleBarrelShotgun;
import hfk.items.weapons.EnergyPistol;
import hfk.items.weapons.GrenadeLauncher;
import hfk.items.weapons.Pistol;
import hfk.items.weapons.PlasmaMachinegun;
import hfk.items.weapons.PlasmaStorm;
import hfk.items.weapons.PumpActionShotgun;
import hfk.items.weapons.RocketLauncher;
import hfk.items.weapons.SniperRifle;

/**
 *
 * @author LostMekka
 */
public class ItemType {

	public static final ItemType none = new ItemType("!!no type!!");
	public static final ItemType equippable = new ItemType("equippable");
	
//----- WEAPONS ----------------------------------------------------------------
	// generic types
	public static final ItemType weapon = new ItemType("weapon");
	public static final ItemType wCheatWeapon = new ItemType("cheat weapon");
	public static final ItemType wExplosiveWeapon = new ItemType("explosive weapon");
	public static final ItemType wEnergyWeapon = new ItemType("energy weapon");
	public static final ItemType wPlasmaWeapon = new ItemType("plasma weapon");
	public static final ItemType wZoomable = new ItemType("zoomable");
	public static final ItemType wShotgun = new ItemType("shotgun");
	public static final ItemType wMachinegun = new ItemType("machinegun");
	public static final ItemType wAutomatic = new ItemType("automatic weapon");
	public static final ItemType wSingleReload = new ItemType("single reload");
	// concrete types
	public static final ItemType wAutoShotgun = new ItemType("auto shotgun"){
		@Override InventoryItem create(PointF pos) { return new AutoShotgun(0, pos); }};
	public static final ItemType wCheatRifle = new ItemType("cheat rifle"){
		@Override InventoryItem create(PointF pos) { return new CheatRifle(0, pos); }};
	public static final ItemType wDamagedHuntingGun = new ItemType("damaged hunting gun"){
		@Override InventoryItem create(PointF pos) { return new DamagedHuntingGun(0, pos); }};
	public static final ItemType wDoubleBarrelShotgun = new ItemType("double barrel shotgun"){
		@Override InventoryItem create(PointF pos) { return new DoubleBarrelShotgun(0, pos); }};
	public static final ItemType wEnergyPistol = new ItemType("energy pistol"){
		@Override InventoryItem create(PointF pos) { return new EnergyPistol(0, pos); }};
	public static final ItemType wGrenadeLauncher = new ItemType("grenade launcher"){
		@Override InventoryItem create(PointF pos) { return new GrenadeLauncher(0, pos); }};
	public static final ItemType wPistol = new ItemType("pistol"){
		@Override InventoryItem create(PointF pos) { return new Pistol(0, pos); }};
	public static final ItemType wPlasmaMachinegun = new ItemType("plasma machinegun"){
		@Override InventoryItem create(PointF pos) { return new PlasmaMachinegun(0, pos); }};
	public static final ItemType wPlasmaStorm = new ItemType("plasma storm"){
		@Override InventoryItem create(PointF pos) { return new PlasmaStorm(0, pos); }};
	public static final ItemType wPumpActionShotgun = new ItemType("pump action shotgun"){
		@Override InventoryItem create(PointF pos) { return new PumpActionShotgun(0, pos); }};
	public static final ItemType wRocketLauncher = new ItemType("rocket launcher"){
		@Override InventoryItem create(PointF pos) { return new RocketLauncher(0, pos); }};
	public static final ItemType wSniperRifle = new ItemType("sniper rifle"){
		@Override InventoryItem create(PointF pos) { return new SniperRifle(0, pos); }};
	// init parents
	static { 
		// generic types
		weapon.setParents(new ItemType[]{equippable});
		wCheatWeapon.setParents(new ItemType[]{weapon});
		wExplosiveWeapon.setParents(new ItemType[]{weapon});
		wEnergyWeapon.setParents(new ItemType[]{weapon});
		wPlasmaWeapon.setParents(new ItemType[]{weapon});
		wZoomable.setParents(new ItemType[]{weapon});
		wShotgun.setParents(new ItemType[]{weapon});
		wMachinegun.setParents(new ItemType[]{weapon});
		wAutomatic.setParents(new ItemType[]{weapon});
		wSingleReload.setParents(new ItemType[]{weapon});
		wMachinegun.setParents(new ItemType[]{wAutomatic});
		wPistol.setParents(new ItemType[]{weapon});
		// concrete types
		wCheatRifle.setParents(new ItemType[]{wCheatWeapon, wGrenadeLauncher});
		wAutoShotgun.setParents(new ItemType[]{wShotgun, wAutomatic, wSingleReload});
		wDoubleBarrelShotgun.setParents(new ItemType[]{wShotgun});
		wEnergyPistol.setParents(new ItemType[]{wPistol, wEnergyWeapon});
		wGrenadeLauncher.setParents(new ItemType[]{wExplosiveWeapon});
		wPlasmaMachinegun.setParents(new ItemType[]{wMachinegun, wPlasmaWeapon});
		wPlasmaStorm.setParents(new ItemType[]{wMachinegun, wPlasmaWeapon});
		wPumpActionShotgun.setParents(new ItemType[]{wShotgun, wSingleReload});
		wRocketLauncher.setParents(new ItemType[]{wExplosiveWeapon});
		wSniperRifle.setParents(new ItemType[]{wZoomable, wSingleReload});
	}

	private final String name;
	private ItemType[] parents = null;

	private ItemType(String name){ 
		this.name = name;
	}
	
	public final InventoryItem createInstance(int maxRarity, PointF pos){
		return createInstance(maxRarity, pos, false);
	}

	public final InventoryItem createInstance(int maxRarity, PointF pos, boolean vanilla){
		InventoryItem i = create(pos);
		if(i.getRarityScore() > maxRarity) return null;
		// TODO: add item effects to equipment if rarity permits it
//		if(!vanilla && isSubTypeOf(equipment)) while(i.getRarityScore() < maxRarity){
//			// add item effect
//		}
//		// remove effect last added
		return i;
	}
	
	InventoryItem create(PointF pos){
		throw new UnsupportedOperationException("called createInstance on ItemType without instantiation code: " + name);
	}

	private void setParents(ItemType[] parents) {
		this.parents = parents;
		// test for cycles in parent relations
		for(ItemType parent : parents){
			ItemType[] path = parent.detectCycles(this, new ItemType[0]);
			if(path != null){
				String s = "cycle in weapon type parent relation detected: ";
				s += toString() + " -> ";
				for(ItemType t : path) s += t.toString() + " -> ";
				s += toString();
				throw new RuntimeException(s);
			}
		}
	}

	private ItemType[] detectCycles(ItemType t, ItemType[] path){
		if(this == t) return path;
		if(parents != null) for(ItemType parent : parents){
			ItemType[] newPath = new ItemType[path.length+1];
			System.arraycopy(path, 0, newPath, 0, path.length);
			newPath[path.length] = this;
			ItemType[] ans = parent.detectCycles(t, newPath);
			if(ans != null) return ans;
		}
		return null;
	}

	@Override
	public final String toString(){
		return name;
	}

	public final String getName() {
	return name;
	}

	public final boolean isSubTypeOf(ItemType t){
		if(this == t) return true;
		if(parents != null) for(ItemType parent : parents) if(parent.isSubTypeOf(t)) return true;
		return false;
	}
}
