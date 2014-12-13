package hfk.level.factory.generators;

import hfk.Box;
import hfk.PointI;
import hfk.Shape;
import hfk.game.GameController;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import java.util.LinkedList;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class CrateGenerator extends LevelGenerator {

	public PropertyMap mapMinX = null;
	public PropertyMap mapMaxX = null;
	public PropertyMap mapMinY = null;
	public PropertyMap mapMaxY = null;
	public PropertyMap boxProbability = null;
	
	public LevelGenerator floor = null;
	
	public CrateGenerator(LevelGenerator parent) {
		super(parent);
	}

	@Override
	public void generateDefaultPropertyMaps(int width, int height) {
		mapMinX = PropertyMap.createRandom(width, height, 6, 0, 1.2f, 3f);
		mapMaxX = PropertyMap.createRandom(width, height, 6, 0, 2f, 4.8f);
		mapMinY = PropertyMap.createRandom(width, height, 6, 0, 1.2f, 3f);
		mapMaxY = PropertyMap.createRandom(width, height, 6, 0, 2f, 4.8f);
		boxProbability = PropertyMap.createRandom(width, height, 7, 0, -0.06f, 0.12f);
	}
	
	@Override
	public void generate(Level l, Shape s) {
		floor.generate(l, s);
		Random r = GameController.random;
		LinkedList<Box> bl = new LinkedList<>();
		for(PointI p : s) if(r.nextFloat() <= boxProbability.getFloatAt(p)){
			Box b = new Box();
			b.x = p.x;
			b.y = p.y;
			int minW = mapMinX.getIntAt(p);
			int maxW = Math.max(minW, mapMaxX.getIntAt(p));
			int minH = mapMinY.getIntAt(p);
			int maxH = Math.max(minH, mapMaxY.getIntAt(p));
			b.w = r.nextInt(maxW-minW+1)+minW;
			b.h = r.nextInt(maxH-minH+1)+minH;
			if(!s.contains(b)) continue;
			boolean touch = false;
			for(Box b2 : bl) if(b.touchesBox(b2)){
				touch = true;
				break;
			}
			if(!touch) bl.add(b);
		}
		// fill boxes
		for(Box b : bl){
			PrimitiveTileType t = getRandomPrimitiveTileType();
			for(PointI p : b) l.getTile(p).addCrate(t.type, t.subtype, t.variant);
		}
	}
	
}
