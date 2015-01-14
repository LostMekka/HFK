package hfk.level.factory.generators;

import hfk.PointI;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;

/**
 *
 * @author LostMekka
 */
public class RandomTemplateGenerator extends LevelGenerator {

	public RandomTemplateGenerator(LevelGenerator parent) {
		super(parent);
	}
	
	@Override
	public void generate(Level l, Shape s) {
		for(PointI p : s) if(l.isInLevel(p)){
			l.getTile(p).addLayersFromTemplate(getRandomTileTemplate());
		}
	}
	
}
