/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk;

import hfk.game.GameController;
import hfk.game.Resources;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Tile {
	
	public enum TileType{ blueFloor, blueFloorB, blueWall, blueWallB, boxA, boxB, boxC, boxD }
	
	private static SpriteSheet sheet = null;
	public static class Factory{
		private Factory(){}
		public static Tile createTile(PointI pos, TileType type){
			Tile t = new Tile(pos.clone());
			switch(type){
				case blueFloor:
					t.img = sheet.getSprite(0, 0); t.isWall = false;
					break;
				case blueFloorB:
					t.img = sheet.getSprite(0, 1); t.isWall = false;
					break;
				case blueWall:
					t.img = sheet.getSprite(0, 2); t.isWall = true;
					t.replacement = createTile(pos, TileType.blueFloor);
					t.hp = 200;
					t.armor = 3;
					break;
				case blueWallB:
					t.img = sheet.getSprite(0, 3); t.isWall = true; 
					t.replacement = createTile(pos, TileType.blueFloor);
					t.hp = 200;
					t.armor = 3;
					break;
				case boxA:
					t.img = sheet.getSprite(1, 0); t.isWall = true;
					t.replacement = createTile(pos, TileType.blueFloor);
					t.hp = 200;
					t.armor = 2;
					break;
				case boxB:
					t.img = sheet.getSprite(1, 1); t.isWall = true;
					t.replacement = createTile(pos, TileType.blueFloor);
					t.hp = 160;
					t.armor = 1;
					break;
				case boxC:
					t.img = sheet.getSprite(1, 2); t.isWall = true;
					t.replacement = createTile(pos, TileType.blueFloor);
					t.hp = 120;
					t.armor = 0;
					break;
				case boxD:
					t.img = sheet.getSprite(1, 3); t.isWall = true;
					t.replacement = createTile(pos, TileType.blueFloor);
					t.hp = 70;
					t.armor = 0;
					break;
			}
			return t;
		}
	}
	
	private final PointI pos;
	private Image img;
	private boolean isWall;
	private int hp = 0, armor = 0;
	private Tile replacement = null;

	private Tile(PointI pos) {
		if(sheet == null){
			// first time, init sprite sheet!
			sheet = Resources.getSpriteSheet("tiles.png");
			sheet.setFilter(Image.FILTER_NEAREST);
		}
		this.pos = pos;
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
	
	public void draw(){
		GameController.get().renderer.drawImage(img, new PointF(pos.x, pos.y));
	}
	
	public void move(int x, int y){
		pos.x = x;
		pos.y = y;
	}
	
}
