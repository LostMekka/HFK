package hfk.level.factory.generators;

import hfk.Box;
import hfk.PointCloud;
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
public class CryoChamberGenerator extends LevelGenerator {

	public PropertyMap damageProbability = null;
	
	public LevelGenerator floor1 = null;
	public LevelGenerator floor2 = null;
	
	public CryoChamberGenerator(LevelGenerator parent) {
		super(parent);
	}

	@Override
	public void generateDefaultPropertyMaps(int width, int height) {
		damageProbability = PropertyMap.createRandom(width, height, 5, 0, 0f, 0.5f);
	}
	
	@Override
	public void generate(Level l, Shape s) {
		Random r = GameController.random;
		PointI offset = new PointI(r.nextInt(5), r.nextInt(5));
		PointCloud s1 = new PointCloud(), s2 = new PointCloud();
		for(PointI p : s){
			int mx = (p.x + offset.x) % 5;
			int my = (p.y + offset.y) % 5;
			(mx < 3 && my < 3 ? s1 : s2).addPoint(p);
			if((mx == 0 || mx == 2) && (my == 0 || my == 2)){
				l.getTile(p).addSleepChamber(r.nextFloat() <= damageProbability.getFloatAt(p));
			}
		}
		floor1.generate(l, s1);
		floor2.generate(l, s2);
	}
	
}
