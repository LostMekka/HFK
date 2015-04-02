package hfk.level;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.items.AmmoItem;
import hfk.items.Inventory;
import hfk.items.InventoryItem;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import hfk.stats.MobStatsCard;

/**
 *
 * @author LostMekka
 */
public class Chest extends UsableLevelItem {

	private Inventory inv;

	public Chest(PointI pos) {
		super(pos);
		img = Resources.getImage("chest.png");
		hp = 70;
		size.set(28f/32f, 20f/32f);
		MobStatsCard card = MobStatsCard.createNormal();
		card.setInventorySize(20);
		for(int i=0; i<Weapon.AMMO_TYPE_COUNT; i++) card.setAmmoSlotSize(i, 1000);
		inv = new Inventory(card);
		inv.addAmmo(Weapon.AmmoType.bullet, 333);
		for(int i=0; i<10; i++) inv.addItem(InventoryItem.create(new PointF(), 99999999));
	}

	@Override
	public boolean damage(int dmg) {
		if(!super.damage(dmg)) return false;
		for(InventoryItem i : inv.removeAll()){
			i.pos = pos.toFloat();
			GameController.get().dropItem(i, null, false);
		}
		return true;
	}
	
	@Override
	public boolean isSquare() {
		return true;
	}

	@Override
	public boolean blocksSight() {
		return false;
	}

	@Override
	public boolean blocksMovement() {
		return true;
	}

	@Override
	public boolean canUse(Mob m) {
		// usable only by players.
		// this may change to players only in the future, but useInternal needs
		// to do something different when m is not a player...
		return m instanceof Player;
	}

	@Override
	public boolean useInternal(Mob m) {
		GameController.get().viewInventoryExchange(m.inventory, inv);
		return true;
	}

	@Override
	public String getDisplayName() {
		return "chest";
	}
	
}
