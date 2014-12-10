package hfk.level.factory.generators;

import hfk.PointI;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.LevelGenerator;

/**
 *
 * @author LostMekka
 */
public class BorderGenerator extends LevelGenerator {

	public BorderGenerator(LevelGenerator parent) {
		super(parent);
	}

	@Override
	public void generate(Level l, Shape s) {
		PointI p = new PointI();
		for(p.x=0; p.x<l.getWidth(); p.x++){
			for(p.y=0; p.y<l.getHeight(); p.y++){
				if(!s.isInside(p)){
					PrimitiveTileType t = getRandomPrimitiveTileType();
					l.getTile(p).addWall(t.type, t.subtype, t.variant);
				}
			}
		}
	}

}
