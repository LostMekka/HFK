/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.PointF;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.tiles.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.generators.CrateGenerator;
import hfk.level.factory.generators.RandomTemplateGenerator;
import hfk.level.tiles.TileLayer;
import hfk.level.tiles.TileTemplate;

/**
 *
 * @author LostMekka
 */
public class CrateAreaFactory extends LevelFactory{

	private RandomTemplateGenerator floor;
	private CrateGenerator crates;
	
	public CrateAreaFactory(int width, int height) {
		super(width, height);
		floor = new RandomTemplateGenerator(this);
		floor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 0, -1), 1f);
		floor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 1, -1), 0.1f);
		crates = new CrateGenerator(this);
		crates.generateDefaultPropertyMaps(width, height);
		for(int i=0; i<4; i++){
			TileLayer l = TileLayer.createCustomLayer(false, i, 0, true, false, new PointF(28f/32f, 28f/32f), 60);
			crates.addTileTemplate(new TileTemplate(l), 1f);
		}
	}

	@Override
	public Box getLegalSpawnArea() {
		return new Box(0, 0, getLevelSize().x, getLevelSize().y);
	}

	@Override
	public Tile getDefaultTile() {
		Tile t = new Tile();
		t.addLayersFromTemplate(TileTemplate.createSimplePrimitive(false, 0, -1));
		t.addLayersFromTemplate(TileTemplate.createSimplePrimitive(true, 0, -1));
		return t;
	}

	@Override
	public void generate(Level l, Shape s) {
		floor.generate(l, s);
		crates.generate(l, s);
	}
	
}
