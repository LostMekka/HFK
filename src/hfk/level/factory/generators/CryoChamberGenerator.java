package hfk.level.factory.generators;

import hfk.PointCloud;
import hfk.PointF;
import hfk.PointI;
import hfk.Shape;
import hfk.game.GameController;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import hfk.level.tiles.TileLayer;
import hfk.level.tiles.TileTemplate;
import java.util.Random;

/**
 *
 * @author LostMekka
 */
public class CryoChamberGenerator extends LevelGenerator {

	public PropertyMap damageProbability = null;
	
	public LevelGenerator floor1 = null;
	public LevelGenerator floor2 = null;
	
	private TileTemplate normalTemplate, damagedTemplate;
	
	public CryoChamberGenerator(LevelGenerator parent) {
		super(parent);
		int[] x = new int[]{ 4, 5, 4, 5 };
		int[] y = new int[]{ 0, 0, 1, 1 };
		int[] t = new int[]{ 600, 600, 600, 600 };
		int[] xd = new int[]{ 2, 2, 2, 2 };
		int[] yd = new int[]{ 0, 1, 0, 1 };
		int[] td = new int[]{ 200, 800, 500, 200 };
		PointF size = new PointF(1f, 0.57f);
		int hp = 60;
		TileLayer normalLayer = TileLayer.createAnimatedCustomLayer(false, x, y, t, true, true, size, hp);
		TileLayer damagedLayer = TileLayer.createAnimatedCustomLayer(false, xd, yd, td, true, true, size, hp);
		normalTemplate = new TileTemplate();
		damagedTemplate = new TileTemplate();
		normalTemplate.addLayer(normalLayer);
		normalTemplate.addLayer(damagedLayer);
		damagedTemplate.addLayer(damagedLayer);
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
				if(r.nextFloat() <= damageProbability.getFloatAt(p)){
					// damaged
					l.getTile(p).addLayersFromTemplate(damagedTemplate);
				} else {
					// not damaged
					l.getTile(p).addLayersFromTemplate(normalTemplate);
				}
			}
		}
		floor1.generate(l, s1);
		floor2.generate(l, s2);
	}
	
}
