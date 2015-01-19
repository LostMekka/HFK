/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.tiles;

import hfk.PointF;
import hfk.PointI;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.Resources;
import hfk.level.Level;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.imageout.ImageIOWriter;

/**
 *
 * @author LostMekka
 */
public class Tile implements Serializable{
	
	private static class BorderConfig{
		public boolean[] edges = new boolean[4];     // order: counter clockwise starting right
		public boolean[] innerCorners = new boolean[4]; // order: same direction starting top right
		public BorderConfig() {}
		public BorderConfig(Tile t, Level l, PointI tilePos) {
			Tile[] sideTiles = new Tile[]{
				l.getTile(new PointI(tilePos.x+1, tilePos.y)),
				l.getTile(new PointI(tilePos.x, tilePos.y-1)),
				l.getTile(new PointI(tilePos.x-1, tilePos.y)),
				l.getTile(new PointI(tilePos.x, tilePos.y+1)),
			};
			Tile[] cornerTiles = new Tile[]{
				l.getTile(new PointI(tilePos.x+1, tilePos.y-1)),
				l.getTile(new PointI(tilePos.x-1, tilePos.y-1)),
				l.getTile(new PointI(tilePos.x-1, tilePos.y+1)),
				l.getTile(new PointI(tilePos.x+1, tilePos.y+1)),
			};
			for(int i=0; i<4; i++) edges[i] = 
					sideTiles[i] != null && 
					!t.connects(sideTiles[i].curr.primitiveType);
			for(int i=0; i<4; i++) innerCorners[i] = 
					cornerTiles[i] != null && 
					!edges[i] && 
					!edges[(i+1)%4] && 
					!t.connects(cornerTiles[i].curr.primitiveType);
		}
	}
	
	private static final SpriteSheet[] sheets = new SpriteSheet[7];
	private static Image sheetImages[] = new Image[6];
	private static Graphics[] tmpGraphics = new Graphics[6];
	private static final int[][] tileEdgePositions = {
		{16, 17, 18, 19, 32, 33, 38, 39, 40, 42, 44, 45, 46},  // tiles containing right edge
		{20, 21, 22, 23, 32, 33, 34, 35, 41, 42, 43, 45, 46},  // tiles containing upper edge
		{24, 25, 26, 27, 34, 35, 36, 37, 40, 42, 43, 44, 46},  // tiles containing left edge
		{28, 29, 30, 31, 36, 37, 38, 39, 41, 43, 44, 45, 46}}; // tiles containing lower edge
	private static final int[][] tileInnerCornerPositions = {
		{ 1,  3,  5,  7,  9, 11, 13, 15, 26, 27, 29, 31, 37},  // tiles containing upper right inner corner
		{ 2,  3,  6,  7, 10, 11, 14, 15, 17, 19, 30, 31, 39},  // tiles containing upper left inner corner
		{ 4,  5,  6,  7, 12, 13, 14, 15, 18, 19, 21, 23, 33},  // tiles containing lower left inner corner
		{ 8,  9, 10, 11, 12, 13, 14, 15, 22, 23, 25, 27, 35}}; // tiles containing lower right inner corner
	private static final int[][] tileOuterCornerPositions = {
		{32, 33, 42, 45, 46},  // tiles containing upper right outer corner
		{34, 35, 42, 43, 46},  // tiles containing upper left outer corner
		{36, 37, 43, 44, 46},  // tiles containing lower left outer corner
		{38, 39, 44, 45, 46}}; // tiles containing lower right outer corner
	private static final int[] defaultOffset = {0, 0};
	private static final int[][] edgeOffsets = {
		{16, 0}, {0, 0}, {0, 0}, {0, 16}};
	private static final int[][] cornerOffsets = {
		{16, 0}, {0, 0}, {0, 16}, {16, 16}};

	public static void createTileSheet(int i) throws SlickException{
		Image img = new Image(1024, 1024, Image.FILTER_NEAREST);
		sheetImages[i] = img;
		tmpGraphics[i] = img.getGraphics();
	}
	
	public static void initSheets() throws SlickException{
		// get the source sprite sheets
		SpriteSheet src = new SpriteSheet("data/tiles1.png", 32, 32);
		sheets[6] = Resources.getSpriteSheet("tiles2.png");
		// construct the big tile set out of the parts in the source
		PointI srcPos = new PointI();
		for(int type=0; type<2; type++){
			for(int tile=0; tile<64; tile++){
				// calculate positions of source and target sprites
				int primitiveType = tile + 64*type;
				int mainY = primitiveType + (primitiveType+1) / 2;
				int tailDir = 1 - 2 * (primitiveType % 2);
				ImgPos mainPos = new ImgPos(mainY / 32, mainY % 32 * 32, 0);
				ImgPos tailPos = new ImgPos((mainY + tailDir) / 32, (mainY + tailDir) % 32 * 32, (1 - tailDir) * 8 * 32);
				srcPos.set(512 * type + tile % 16 * 32, tile / 16 * 32);
				// create subimages for all different tile parts
				Image defaultImage = src.getSubImage(srcPos.x, srcPos.y, 32, 32);
				Image[] edges = new Image[]{
					src.getSubImage(16 + srcPos.x, 64 + srcPos.y, 16, 32),
					src.getSubImage(     srcPos.x, 32 + srcPos.y, 32, 16),
					src.getSubImage(     srcPos.x, 64 + srcPos.y, 16, 32),
					src.getSubImage(     srcPos.x, 48 + srcPos.y, 32, 16)};
				Image[] innerCorners = new Image[]{
					src.getSubImage(16 + srcPos.x, 128 + srcPos.y, 16, 16),
					src.getSubImage(     srcPos.x, 128 + srcPos.y, 16, 16),
					src.getSubImage(     srcPos.x, 144 + srcPos.y, 16, 16),
					src.getSubImage(16 + srcPos.x, 144 + srcPos.y, 16, 16)};
				Image[] outerCorners = new Image[]{
					src.getSubImage(16 + srcPos.x,  96 + srcPos.y, 16, 16),
					src.getSubImage(     srcPos.x,  96 + srcPos.y, 16, 16),
					src.getSubImage(     srcPos.x, 112 + srcPos.y, 16, 16),
					src.getSubImage(16 + srcPos.x, 112 + srcPos.y, 16, 16)};
				// draw every part on the different target sprites where it is needed
				for(int i=0; i<47; i++) drawOnTile(defaultImage, i, mainPos, tailPos, defaultOffset);
				for(int i=0; i<4; i++) for(int t : tileEdgePositions[i])
					drawOnTile(edges[i], t, mainPos, tailPos, edgeOffsets[i]);
				for(int i=0; i<4; i++) for(int t : tileInnerCornerPositions[i])
					drawOnTile(innerCorners[i], t, mainPos, tailPos, cornerOffsets[i]);
				for(int i=0; i<4; i++) for(int t : tileOuterCornerPositions[i])
					drawOnTile(outerCorners[i], t, mainPos, tailPos, cornerOffsets[i]);
			}
		}
		// release the off screen render buffers
		for(int i=0; i<6; i++){
			tmpGraphics[i].destroy();
			sheets[i] = new SpriteSheet(sheetImages[i], 32, 32);
		}
		tmpGraphics = null;
		//renderDebugTiles();
	}
	
	private static void drawOnTile(Image i, int target, ImgPos mainPos, ImgPos tailPos, int[] offset) throws SlickException{
		ImgPos pos;
		if(target < 32){
			pos = mainPos;
		} else {
			pos = tailPos;
			target -= 32;
		}
		Graphics g = tmpGraphics[pos.sheet];
		g.drawImage(i, pos.x + offset[0], pos.y + offset[1] + target * 32);
	}
	
	public static void getPosOfPrimitive(int primitiveType, int tileNumber, ImgPos ansPosObj){
		int mainY = primitiveType + (primitiveType+1) / 2;
		if(tileNumber < 32){
			ansPosObj.set(mainY / 32, mainY % 32, tileNumber);
		} else {
			int tailDir = 1 - 2 * (primitiveType % 2);
			ansPosObj.set((mainY + tailDir) / 32, (mainY + tailDir) % 32, 
					(1 - tailDir) * 8 + tileNumber - 32);
		}
	}
	
	private static int getImageNumber(BorderConfig bc){
		// this looks messy... but it is necessary because the room of possible
		// combinations of edges and inner corners is shrinked from 2^8 == 265
		// to 47 to include only valid combinations.
		int edgeCount = 0;
		for(boolean b : bc.edges) if(b) edgeCount++;
		switch(edgeCount){
			case 0:
				int ans = 0;
				if(bc.innerCorners[0]) ans += 1;
				if(bc.innerCorners[1]) ans += 2;
				if(bc.innerCorners[2]) ans += 4;
				if(bc.innerCorners[3]) ans += 8;
				return ans;
			case 1:
				int edgePos = -1;
				for(int i=0; i<4; i++) if(bc.edges[i]){
					edgePos = i;
					break;
				}
				ans = 16 + edgePos * 4;
				if(bc.innerCorners[(edgePos+1)%4]) ans += 1;
				if(bc.innerCorners[(edgePos+2)%4]) ans += 2;
				return ans;
			case 2:
				if(bc.edges[0] == bc.edges[2]) return bc.edges[0] ? 40 : 41;
				edgePos = -1;
				for(int i=0; i<4; i++) if(bc.edges[i]){
					edgePos = i;
					break;
				}
				if(!bc.edges[(edgePos+1)%4]) edgePos = 3;
				ans = 32 + edgePos * 2;
				if(bc.innerCorners[(edgePos+2)%4]) ans++;
				return ans;
			case 3:
				if(!bc.edges[0]) return 43;
				if(!bc.edges[1]) return 44;
				if(!bc.edges[2]) return 45;
				if(!bc.edges[3]) return 42;
		}
		return 46;
	}
	
	private static void renderDebugImage(Image i, String name){
		try{
			ImageIOWriter w = new ImageIOWriter();
			FileOutputStream fos = new FileOutputStream("debug_image_" + name + ".png");
			w.saveImage(i, "png", fos, true);
			fos.close();
		} catch(IOException e){
			System.err.println("failed to write debug image \"" + name + "\" to disk!");
		}
	}

	private static void renderDebugTiles(){
		try{
			ImageIOWriter w = new ImageIOWriter();
			for(int i=0; i<7; i++){
				FileOutputStream fos = new FileOutputStream("debug_texture_" + i + ".png");
				w.saveImage(sheets[i], "png", fos, true);
				fos.close();
			}
		} catch(IOException e){
			System.err.println("failed to write debug textures to disk!");
		}
	}
	
	private LinkedList<TileLayer> wallLayers = new LinkedList<>();
	private LinkedList<TileLayer> floorLayers = new LinkedList<>();
	private TileLayer curr = null, currFloor = null;

	public void calcBorders(boolean initial, Level l, PointI p){
		if(curr == null || curr.primitiveType < 0 || !(initial || curr.updatesBorders)) return;
		BorderConfig bc = new BorderConfig(this, l, p);
		curr.primitiveNumber = getImageNumber(bc);
		getPosOfPrimitive(curr.primitiveType, curr.primitiveNumber, curr.imgPos);
	}
	
	private boolean connects(int t){
		if(curr.connectsTo == null) return false;
		for(int i=0; i<curr.connectsTo.length; i++) if(curr.connectsTo[i] == t) return true;
		return false;
	}
	
	public void addLayersFromTemplate(TileTemplate t){
		for(TileLayer l : t){
			(l.isFloor ? floorLayers : wallLayers).addLast(new TileLayer(l));
		}
		updateData();
	}
	
	public PointF getSize(){
		return curr.size;
	}
	
	public float getSizeX(){
		return curr.size.x;
	}
	
	public float getSizeY(){
		return curr.size.y;
	}
	
	public boolean damage(int dmg){
		if(curr.hp < 0) return false;
		dmg = Math.max(dmg-curr.armor, 0);
		curr.hp -= dmg;
		if(curr.hp <= 0){
			(wallLayers.isEmpty() ? floorLayers : wallLayers).removeFirst();
			updateData();
			return true;
		}
		return false;
	}
	
	public boolean isCollidable() {
		return curr.isCollidable;
	}
	
	public boolean isTransparent() {
		return curr.isTransparent;
	}
	
	public Image getImage(){
		return sheets[curr.imgPos.sheet].getSprite(curr.imgPos.x, curr.imgPos.y);
	}
	
	public Image getFloorImage(){
		return sheets[currFloor.imgPos.sheet].getSprite(currFloor.imgPos.x, currFloor.imgPos.y);
	}
	
	public void draw(PointF pos){
		// draw nothing until the tile has a valid layer configuration!
		// if there is no floor, currFloor is null.
		// if there is only no wall, curr is currFloor.
		if(currFloor == null) return;
		GameRenderer r = GameController.get().renderer;
		if(curr == currFloor){
			r.drawImage(getImage(), pos, true, GameRenderer.LayerType.floor1);
		} else {
			r.drawImage(getImage(), pos, true, GameRenderer.LayerType.walls1);
		}
		if(curr != currFloor && (curr.size.x < 1f || curr.size.y < 1f)){
			r.drawImage(getFloorImage(), pos, true, GameRenderer.LayerType.floor1);
		}
	}
	
	public void update(int time){
		update(wallLayers, time);
		update(floorLayers, time);
	}
	
	private void update(LinkedList<TileLayer> list, int time){
		for(TileLayer d : list) if(d.aniLen != null){
			d.currTimer -= time;
			if(d.currTimer <= 0){
				d.currFrame++;
				d.currFrame %= d.aniLen.length;
				d.currTimer += d.aniLen[d.currFrame];
				if(d.primitiveType >= 0){
					getPosOfPrimitive(d.primitiveType, d.primitiveNumber, d.imgPos);
				} else {
					d.imgPos = d.aniPos[d.currFrame];
				}
			}
		}
	}
	
	private void updateData(){
		currFloor = floorLayers.isEmpty() ? null : floorLayers.getFirst();
		curr = wallLayers.isEmpty() ? currFloor : wallLayers.getFirst();
	}
	
}
