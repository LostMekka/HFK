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
public class RandomFloorGenerator extends LevelGenerator {

	public RandomFloorGenerator(LevelGenerator parent) {
		super(parent);
	}

	@Override
	public void generate(Level l, Shape s) {
		for(PointI p : s){
			PrimitiveTileType t = getRandomPrimitiveTileType();
			l.getTile(p).addFloor(t.type, t.subtype, t.variant);
		}
	}
	
}
