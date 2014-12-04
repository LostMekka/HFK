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
import org.newdawn.slick.Animation;
import org.newdawn.slick.Image;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Tile{

	private static SpriteSheet sheet = null;
	
	public final PointI imgPos;
	public final PointF size;
	public int hp = -1, armor = 0;
	private int tileType;
	private Animation animation = null;
	private Image floorImage = null;

	public Tile(int tileType, int tileSet) {
		this.tileType = tileType;
		imgPos = new PointI();
		size = new PointF();
		init(tileType, tileSet, true);
	}

	public int getTileType() {
		return tileType;
	}

	public void setTileType(int tileType, int tileSet, boolean tileSetChanged) {
		if(!tileSetChanged && tileType == this.tileType) return;
		this.tileType = tileType;
		init(tileType, tileSet, false);
	}

	public final void init(int tileType, int tileSet, boolean initFields){
		if(sheet == null) sheet = Resources.getSpriteSheet("tiles.png");
		int tsx = 4 * (tileSet % 4);
		int tsy = 4 * (tileSet / 4);
		if(tileType < 16){
			// standard tileset tiles
			if(tileType < 4){
				// standard wall
				imgPos.set(tsx, tsy+tileType);
				size.set(1f, 1f);
				if(initFields){
					hp = 200;
					armor = 3;
				}
			} else if(tileType < 8){
				// standard floor
				imgPos.set(tsx+1, tsy+tileType-4);
			} else if(tileType < 12){
				// standard crates
				int t = tileType-8;
				imgPos.set(tsx+2, tsy+t);
				size.set(0.875f, 0.875f);
				if(initFields){
					hp = 60*(1+t);
					armor = Math.max(0, t-1);
				}
			} else {
				// standard other stuff
				int t = tileType-12;
				imgPos.set(tsx+3, tsy+t);
				size.set(0.875f, 0.875f);
				if(initFields){
					hp = 60*(1+t);
					armor = Math.max(0, t-1);
				}
			}
		} else {
			// special tiles
			switch(tileType){
				case 16:
					// cold sleep chamber, functioning
					animation = new Animation(sheet, new int[]{0,4,1,4,0,5,1,5}, new int[]{600,600,600,600});
					size.set(1f, 0.57f);
					if(initFields) hp = 60;
					break;
				case 17:
					// cold sleep chamber, broken
					animation = new Animation(sheet, new int[]{2,5,2,4,2,5,2,4}, new int[]{200,200,800,500});
					size.set(1f, 0.57f);
					if(initFields) hp = 60;
					break;
			}
		}
		floorImage = null;
		if(size.x > 0f && size.y > 0f && (size.x < 1f || size.y < 1f)){
			floorImage = sheet.getSprite(tsx+1, tsy);
		}
	}
	
	public Tile damage(int dmg){
		if(hp < 0) return this;
		dmg = Math.max(dmg-armor, 0);
		hp -= dmg;
		if(hp <= 0){
			int tileSet = GameController.get().level.getTileSet();
			switch(tileType){
				case 16: return new Tile(17, tileSet);
				default: return new Tile(4, tileSet);
			}
		}
		return this;
	}
	
	public boolean isWall() {
		return size.x > 0f && size.y > 0f;
	}
	
	public Image getImage(){
		if(animation != null) return animation.getCurrentFrame();
		return sheet.getSprite(imgPos.x, imgPos.y);
	}
	
	public void draw(PointF pos){
		if(floorImage != null) GameController.get().renderer.drawImage(floorImage, pos, true);
		GameController.get().renderer.drawImage(getImage(), pos, true);
	}
	
	public void update(int time){
		if(animation != null) animation.update(time);
	}
	
}
