/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory.generators;

import hfk.PointI;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;

/**
 *
 * @author LostMekka
 */
public class RandomPrimitiveGenerator extends LevelGenerator {

	public static enum Type{ floor, wall, crate }
	
	private final Type type;

	public RandomPrimitiveGenerator(Type type, LevelGenerator parent) {
		super(parent);
		this.type = type;
	}
	
	@Override
	public void generate(Level l, Shape s) {
		switch(type){
			case floor:
				for(PointI p : s) if(l.isInLevel(p)){
					PrimitiveTileType t = getRandomPrimitiveTileType();
					l.getTile(p).addFloor(t.type, t.subtype, t.variant);
				}
				break;
			case wall:
				for(PointI p : s) if(l.isInLevel(p)){
					PrimitiveTileType t = getRandomPrimitiveTileType();
					l.getTile(p).addWall(t.type, t.subtype, t.variant);
				}
				break;
			case crate:
				for(PointI p : s) if(l.isInLevel(p)){
					PrimitiveTileType t = getRandomPrimitiveTileType();
					l.getTile(p).addCrate(t.type, t.subtype, t.variant);
				}
				break;
		}
	}
	
}
