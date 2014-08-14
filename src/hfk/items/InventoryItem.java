/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.items;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.items.weapons.EnergyPistol;
import hfk.items.weapons.GrenadeLauncher;
import hfk.items.weapons.Pistol;
import hfk.items.weapons.PlasmaMachinegun;
import hfk.items.weapons.RocketLauncher;
import hfk.items.weapons.DoubleBarrelShotgun;
import hfk.items.weapons.PumpActionShotgun;
import hfk.items.weapons.SniperRifle;
import hfk.items.weapons.Weapon;
import hfk.mobs.Mob;
import hfk.stats.DamageCard;
import hfk.stats.ItemEffect;
import hfk.stats.MobStatsCard;
import hfk.stats.StatsModifier;
import hfk.stats.WeaponStatsCard;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

/**
 *
 * @author LostMekka
 */
public abstract class InventoryItem implements StatsModifier {
	
	private static final double POS_FRICTION = 0.01d;
	private static final double ANGLE_FRICTION = 0.1d;
	private static final float LABEL_VEL = 3f;
	private static final PointF LABEL_OFFSET = new PointF(0f, -0.4f);
	private static final float LABEL_BORDER = 4f;
	
	public LinkedList<ItemEffect> effects = new LinkedList<>();
	public PointF pos, vel = new PointF(0f, 0f);
	public float angle = 0f, vAngle = 0f;
	public Image image = null;
	public Inventory parentInventory = null;
	public float size = 0.6f;
	public PointF labelPos = new PointF();
	public PointF labelSize = new PointF();
	public PointF labelSizeInPixels = new PointF();
	public String label = null;
	public boolean destroyWhenUsed = true;
	
	public abstract String getDisplayName();
	public abstract long getRarityScore();
	public abstract Color getDisplayColor();
	public abstract boolean use(Mob m, boolean fromInventory);

	public static InventoryItem create(PointF pos, int maxRarity){
		Random r = GameController.random;
		LinkedList<InventoryItem> l = new LinkedList<>();
		l.add(new AmmoItem(pos, Weapon.AmmoType.bullet, r.nextInt(91)+10));
		l.add(new AmmoItem(pos, Weapon.AmmoType.shell, r.nextInt(46)+5));
		l.add(new AmmoItem(pos, Weapon.AmmoType.sniperRound, r.nextInt(19)+2));
		l.add(new AmmoItem(pos, Weapon.AmmoType.grenade, r.nextInt(36)+5));
		l.add(new AmmoItem(pos, Weapon.AmmoType.rocket, r.nextInt(4)+2));
		l.add(new AmmoItem(pos, Weapon.AmmoType.plasmaRound, r.nextInt(181)+20));
		l.add(new HealthPack(pos, HealthPack.Type.small));
		l.add(new HealthPack(pos, HealthPack.Type.medium));
		l.add(new HealthPack(pos, HealthPack.Type.big));
		l.add(new Pistol(0, pos));
		l.add(new DoubleBarrelShotgun(0, pos));
		l.add(new PumpActionShotgun(0, pos));
		l.add(new GrenadeLauncher(0, pos));
		l.add(new RocketLauncher(0, pos));
		l.add(new SniperRifle(0, pos));
		l.add(new EnergyPistol(0, pos));
		l.add(new PlasmaMachinegun(0, pos));
		ListIterator<InventoryItem> iter = l.listIterator();
		while(iter.hasNext()) if(iter.next().getRarityScore() > maxRarity) iter.remove();
		if(l.isEmpty()) return null;
		return l.get(r.nextInt(l.size()));
	}

	public InventoryItem(PointF pos) {
		this.pos = pos;
	}
	
	public void initLabel(){
		label = getDisplayName();
		labelSizeInPixels.x = 2 * LABEL_BORDER + GameController.get().renderer.getStringWidth(label);
		labelSizeInPixels.y = 2 * LABEL_BORDER + GameController.get().renderer.getStringHeight(label);
		labelSize.x = GameController.get().transformScreenToTiles(labelSizeInPixels.x);
		labelSize.y = GameController.get().transformScreenToTiles(labelSizeInPixels.y);
		calcLabelPosOnScreen();
	}
	
	public PointF getTopLeftLabelPos(){
		PointF p = labelPos.clone();
		p.x -= labelSize.x / 2f;
		p.y -= labelSize.y / 2f;
		return p;
	}
	
	private void calcLabelPosOnScreen(){
	}
	
	public boolean isOnLabel(PointF p){
		float x = (p.x - labelPos.x) / labelSize.x + 0.5f;
		float y = (p.y - labelPos.y) / labelSize.y + 0.5f;
		return x >= 0f && x < 1f && y >= 0f && y < 1f;
	}
	
	public void render(){
		if(image != null){
			GameController.get().renderer.drawImage(image, pos, 1f, angle, true);
		}
	}
	
	public void drawItemTextBox(int highlightLevel){
		if(label == null) initLabel();
		PointF p = GameController.get().transformTilesToScreen(getTopLeftLabelPos());
		Color bg, lc;
		switch(highlightLevel){
			case 0:
				bg = GameRenderer.COLOR_LOOT_NORMAL_BG;
				lc = GameRenderer.COLOR_LOOT_NORMAL_LINE;
				break;
			case 1:
				bg = GameRenderer.COLOR_LOOT_INRANGE_BG;
				lc = GameRenderer.COLOR_LOOT_INRANGE_LINE;
				break;
			case 2:
				bg = GameRenderer.COLOR_LOOT_SELECTED_BG;
				lc = GameRenderer.COLOR_LOOT_SELECTED_LINE;
				break;
			default: throw new RuntimeException("highlight level unknown (" + highlightLevel + ")");
		}
		GameController.get().renderer.drawMenuBox(p.x, p.y, labelSizeInPixels.x, labelSizeInPixels.y, bg, lc);
		GameController.get().renderer.drawStringOnScreen(label, p.x + LABEL_BORDER + 1, p.y + LABEL_BORDER + 1, getDisplayColor(), 1f);
	}
	
	public void update(int time){
		if(label == null) initLabel();
		float t = time / 1000f;
		// own position
		float f = (float)Math.pow(POS_FRICTION, t);
		vel.x *= f;
		vel.y *= f;
		vAngle *= (float)Math.pow(ANGLE_FRICTION, t);
		if(Math.abs(vel.x) < 0.1) vel.x = 0f;
		if(Math.abs(vel.y) < 0.1) vel.y = 0f;
		if(Math.abs(vAngle) < 0.1) vAngle = 0f;
		pos.x += vel.x * t;
		pos.y += vel.y * t;
		angle += vAngle * t;
		// label position
		calcLabelPosOnScreen();
		labelPos.x += LABEL_VEL * t * (pos.x + LABEL_OFFSET.x - labelPos.x);
		labelPos.y += LABEL_VEL * t * (pos.y + LABEL_OFFSET.y - labelPos.y);
	}

	@Override
	public void addDamageCardEffects(DamageCard card, Weapon w, Mob m) {
		for(ItemEffect e : effects){
			if(e.dc != null && (e.weaponType == null || e.weaponType == w.type)) card.add(e.dc);
		}
	}

	@Override
	public void addWeaponStatsCardEffects(WeaponStatsCard card, Weapon w, Mob m) {
		for(ItemEffect e : effects){
			if(e.wsc != null && (e.weaponType == null || e.weaponType == w.type)) card.add(e.wsc);
		}
	}

	@Override
	public void addMobStatsCardEffects(MobStatsCard card, Mob m) {
		for(ItemEffect e : effects){
			if(e.msc != null) card.add(e.msc);
		}
	}

}
