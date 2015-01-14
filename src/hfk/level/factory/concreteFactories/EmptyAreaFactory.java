/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.tiles.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.generators.RandomTemplateGenerator;
import hfk.level.tiles.TileTemplate;

/**
 *
 * @author LostMekka
 */
public class EmptyAreaFactory extends LevelFactory{

	private RandomTemplateGenerator floor;
	
	public EmptyAreaFactory(int width, int height) {
		super(width, height);
		floor = new RandomTemplateGenerator(this);
		floor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 0, -1), 1f);
		floor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 1, -1), 0.1f);
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
	}
	
}
