/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory.generators;

import hfk.Box;
import hfk.PointI;
import hfk.game.GameController;
import hfk.level.Level;
import hfk.level.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import static hfk.level.factory.RoomsFactory.PROP_MAXROOMHEIGHT;
import static hfk.level.factory.RoomsFactory.PROP_MAXROOMWIDTH;
import static hfk.level.factory.RoomsFactory.PROP_MINROOMHEIGHT;
import static hfk.level.factory.RoomsFactory.PROP_MINROOMWIDTH;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class CrateGenerator extends LevelGenerator {

	public static final String PROP_MINBOXWIDTH = "minBoxWidth";
	public static final String PROP_MAXBOXWIDTH = "maxBoxWidth";
	public static final String PROP_MINBOXHEIGHT = "minBoxHeight";
	public static final String PROP_MAXBOXHEIGHT = "maxBoxHeight";

	public CrateGenerator(LevelGenerator parent) {
		super(parent);
	}
	
	@Override
	public PropertyMap generatePropertyMap(String name, PointI levelSize) {
		if(name.equals(PROP_MINBOXWIDTH)) return PropertyMap.createRandom(levelSize.x, levelSize.y, 6, 0, 1f, 3f);
		if(name.equals(PROP_MAXBOXWIDTH)) return PropertyMap.createRandom(levelSize.x, levelSize.y, 6, 0, 2f, 5f);
		if(name.equals(PROP_MINBOXHEIGHT)) return PropertyMap.createRandom(levelSize.x, levelSize.y, 6, 0, 1f, 3f);
		if(name.equals(PROP_MAXBOXHEIGHT)) return PropertyMap.createRandom(levelSize.x, levelSize.y, 6, 0, 2f, 5f);
		return super.generatePropertyMap(name, levelSize);
	}

	@Override
	public void generate(Level l, Box b) {
		Random ran = GameController.random;
		int minW = getPropertyMap(PROP_MINBOXWIDTH, false, this).getAverageInt(b);
		int maxW = getPropertyMap(PROP_MAXBOXWIDTH, false, this).getAverageInt(b);
		int minH = getPropertyMap(PROP_MINBOXHEIGHT, false, this).getAverageInt(b);
		int maxH = getPropertyMap(PROP_MAXBOXHEIGHT, false, this).getAverageInt(b);
		// do the boxes fit?
		if(minW <= b.w-2 || minH <= b.h-2){
			// create boxes
			LinkedList<Box> bl = new LinkedList<>();
			for(int i=0; i<b.w*b.h*0.03f; i++){
				Box b1 = new Box();
				b1.w = Math.min(ran.nextInt(maxW-minW+1)+minW, b.w-2);
				b1.h = Math.min(ran.nextInt(maxH-minH+1)+minH, b.h-2);
				b1.x = ran.nextInt(b.w-b1.w-1)+1+b.x;
				b1.y = ran.nextInt(b.h-b1.h-1)+1+b.y;
				boolean touch = false;
				for(Box b2 : bl) if(b1.touchesBox(b2)){
					touch = true;
					break;
				}
				if(!touch) bl.add(b1);
			}
			// fill boxes
			for(Box b1 : bl){
				int t = ran.nextInt(4) + 8;
				for(int x=b1.x; x<b1.x+b1.w; x++){
					for(int y=b1.y; y<b1.y+b1.h; y++){
						l.setTile(x, y, t);
					}
				}
			}
		}
		// clear all untouced tiles
		for(int x=b.x; x<b.x+b.w; x++){
			for(int y=b.y; y<b.y+b.h; y++){
				if(!l.hasTile(x, y)){
					l.setTile(x, y, 4+getTileVariation(x, y));
				}
			}
		}
	}
	
}
