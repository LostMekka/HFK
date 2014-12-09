/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory.generators;

import hfk.Box;
import hfk.game.GameController;
import hfk.level.Level;
import hfk.level.Tile;
import hfk.level.factory.LevelGenerator;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class RandomFloorGenerator extends LevelGenerator {

	private class FloorType{
		public final int type, subtype, variant;
		public final float p;
		public FloorType(int type, int subtype, int variant, float p) {
			this.type = type;
			this.subtype = subtype;
			this.variant = variant;
			this.p = p;
		}
	}

	private final LinkedList<FloorType> types = new LinkedList<>();
	private float pSum = 0f;

	public RandomFloorGenerator(LevelGenerator parent) {
		super(parent);
	}

	public void addFloorType(int type, int subtype, int variant, float p){
		types.add(new FloorType(type, subtype, variant, p));
		pSum += p;
	}
	
	@Override
	public void generate(Level l, Box b) {
		for(int x=b.x; x<b.x+b.w; x++){
			for(int y=b.y; y<b.y+b.h; y++){
				float p = GameController.random.nextFloat()*pSum;
				float orig = p;
				for(FloorType t : types){
					p -= t.p;
					if(p <= 0f){
						l.setTile(x, y, Tile.createFloor(t.type, t.subtype, t.variant));
						break;
					}
					throw new RuntimeException("cannot get floor type!");
				}
			}
		}
	}
	
}
