/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.game.Resources;
import hfk.net.NetState;
import hfk.net.NetStateObject;
import hfk.net.NetStatePart;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Tile implements NetStateObject{

	public enum TileType{ blueFloor, blueFloorB, blueWall, blueWallB, boxA, boxB, boxC, boxD }
	
	private static SpriteSheet sheet = null;
	
	private final PointI pos, imgPos;
	private boolean isWall;
	private int hp = 0, armor = 0;
	private Tile replacement = null;
	private long id;

	public Tile() {
		// empty constructor for net state
		pos = new PointI();
		imgPos = new PointI();
		init();
	}
	
	public Tile(PointI pos, TileType t) {
		this.pos = pos.clone();
		switch(t){
			case blueFloor:
				imgPos = new PointI(0, 0);
				isWall = false;
				break;
			case blueFloorB:
				imgPos = new PointI(0, 1);
				isWall = false;
				break;
			case blueWall:
				imgPos = new PointI(0, 2);
				isWall = true;
				replacement = new Tile(pos, TileType.blueFloor);
				hp = 200;
				armor = 3;
				break;
			case blueWallB:
				imgPos = new PointI(0, 3);
				isWall = true;
				replacement = new Tile(pos, TileType.blueFloor);
				hp = 200;
				armor = 3;
				break;
			case boxA:
				imgPos = new PointI(1, 0);
				isWall = true;
				replacement = new Tile(pos, TileType.blueFloor);
				hp = 200;
				armor = 2;
				break;
			case boxB:
				imgPos = new PointI(1, 1);
				isWall = true;
				replacement = new Tile(pos, TileType.blueFloor);
				hp = 160;
				armor = 1;
				break;
			case boxC:
				imgPos = new PointI(1, 2);
				isWall = true;
				replacement = new Tile(pos, TileType.blueFloor);
				hp = 120;
				break;
			case boxD:
				imgPos = new PointI(1, 3);
				isWall = true;
				replacement = new Tile(pos, TileType.blueFloor);
				hp = 70;
				break;
			default:
				throw new RuntimeException("tile type not recognized!");
		}
		init();
		id = GameController.get().createIdFor(this);
	}

	private void init(){
		if(sheet == null) sheet = Resources.getSpriteSheet("tiles.png");
	}
	
	public Tile damage(int dmg){
		if(replacement == null) return this;
		dmg = Math.max(dmg-armor, 0);
		hp -= dmg;
		if(hp <= 0) return replacement;
		return this;
	}
	
	public boolean isWall() {
		return isWall;
	}
	
	public Image getImage(){
		return sheet.getSprite(imgPos.x, imgPos.y);
	}
	
	public void draw(){
		GameController.get().renderer.drawImage(getImage(), pos.toFloat(), true);
	}
	
	public void moveTo(int x, int y){
		pos.x = x;
		pos.y = y;
	}
	
	@Override
	public long getID() {
		return id;
	}

	@Override
	public void setID(long id) {
		this.id = id;
	}

	@Override
	public NetStatePart fillStateParts(NetStatePart part, NetState state) {
		if(replacement != null){
			state.addObject(replacement);
			part.setID(0, replacement.id);
		}
		part.setInteger(0, hp);
		part.setInteger(1, armor);
		part.setInteger(2, pos.x);
		part.setInteger(3, pos.y);
		part.setInteger(4, imgPos.x);
		part.setInteger(5, imgPos.y);
		part.setBoolean(0, isWall);
		return part;
	}

	@Override
	public void updateFromStatePart(NetStatePart part, NetState state) {
		long repID = part.getID(0);
		if(repID >= 0){
			NetStateObject o = GameController.get().getNetStateObject(repID);
			if(o == null){
				replacement = new Tile();
			} else {
				replacement = (Tile)o;
			}
			replacement.updateFromStatePart(state.parts.get(repID), state);
		} else {
			replacement = null;
		}
		hp = part.getInteger(0);
		armor = part.getInteger(1);
		pos.x = part.getInteger(2);
		pos.y = part.getInteger(3);
		imgPos.x = part.getInteger(4);
		imgPos.y = part.getInteger(5);
		isWall = part.getBoolean(0);
	}
	
}
