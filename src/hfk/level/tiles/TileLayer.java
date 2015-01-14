/*
 */
package hfk.level.tiles;

import hfk.PointF;
import java.io.Serializable;

/**
 *
 * @author LostMekka
 */
public class TileLayer implements Serializable{
	
	public static TileLayer createPrimitiveLayer(boolean isFloor, int type, int[] connectsTo, boolean updatesBorders, 
			int hp, boolean collidable, boolean transparent){
		TileLayer l = new TileLayer();
		l.connectsTo = connectsTo.clone();
		if(isFloor){
			l.size = new PointF();
		} else {
			type += 64;
			for(int i=0; i<connectsTo.length; i++) l.connectsTo[i] += 64;
			l.size = new PointF(1f, 1f);
		}
		l.imgPos = new ImgPos();
		Tile.getPosOfPrimitive(type, 0, l.imgPos);
		l.isFloor = isFloor;
		l.updatesBorders = updatesBorders;
		l.isCollidable = collidable;
		l.isTransparent = transparent;
		l.hp = hp;
		l.primitiveType = type;
		Tile.getPosOfPrimitive(l.primitiveType, l.primitiveNumber, l.imgPos);
		return l;
	}
	
	public static TileLayer createAnimatedPrimitiveLayer(boolean isFloor, int[] types, int[] animationTimes, 
			int[] connectsTo, boolean updatesBorders, int hp, boolean collidable, boolean transparent){
		TileLayer l = new TileLayer();
		l.aniTypes = types.clone();
		l.connectsTo = connectsTo.clone();
		if(isFloor){
			l.size = new PointF();
		} else {
			l.size = new PointF(1f, 1f);
			for(int i=0; i<types.length; i++) l.aniTypes[i] += 64;
			for(int i=0; i<connectsTo.length; i++) l.connectsTo[i] += 64;
		}
		l.imgPos = new ImgPos();
		Tile.getPosOfPrimitive(l.aniTypes[0], 0, l.imgPos);
		l.aniLen = animationTimes;
		l.updatesBorders = updatesBorders;
		l.isCollidable = collidable;
		l.isTransparent = transparent;
		l.hp = hp;
		l.primitiveType = types[0];
		return l;
	}
	
	public static TileLayer createCustomLayer(boolean isFloor, int x, int y, boolean collidable, boolean transparent,
			PointF size, int hp){
		TileLayer l = new TileLayer();
		l.imgPos = new ImgPos(6, x, y);
		l.size = size;
		l.hp = hp;
		l.isCollidable = collidable;
		l.isTransparent = transparent;
		return l;
	}
	
	public static TileLayer createAnimatedCustomLayer(boolean isFloor, int[] x, int[] y, int[] animationTimes, 
			boolean collidable, boolean transparent, PointF size, int hp){
		TileLayer l = new TileLayer();
		l.aniPos = new ImgPos[x.length];
		for(int i=0; i<x.length; i++) l.aniPos[i] = new ImgPos(6, x[i], y[i]);
		l.imgPos = l.aniPos[0];
		l.size = size;
		l.hp = hp;
		l.isCollidable = collidable;
		l.isTransparent = transparent;
		return l;
	}
	
	
	public boolean isFloor;
	public boolean isCollidable = false, isTransparent = true;
	public boolean updatesBorders;
	public int hp = -1, armor;
	public int primitiveType = -1, primitiveNumber;
	public ImgPos imgPos;
	public int[] aniLen;
	public int[] aniTypes;
	public float[] dps;
	public int[] connectsTo;
	public ImgPos[] aniPos;
	public PointF size;
	
	public int currFrame, currTimer;

	public TileLayer() {
	}
	
	public TileLayer(TileLayer l) {
		isFloor = l.isFloor;
		isCollidable = l.isCollidable;
		isTransparent = l.isTransparent;
		updatesBorders = l.updatesBorders;
		hp = l.hp;
		armor = l.armor;
		primitiveType = l.primitiveType;
		primitiveNumber = l.primitiveNumber;
		if(l.aniLen != null){
			aniLen = l.aniLen.clone();
			imgPos = new ImgPos(l.aniPos[0]);
		} else {
			imgPos = new ImgPos(l.imgPos);
		}
		if(l.aniTypes != null) aniTypes = l.aniTypes.clone();
		if(l.dps != null) dps = l.dps.clone();
		if(l.connectsTo != null) connectsTo = l.connectsTo.clone();
		size = l.size.clone();
	}
	
}
