/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.generators.RandomPrimitiveGenerator;

/**
 *
 * @author LostMekka
 */
public class EmptyAreaFactory extends LevelFactory{

	private RandomPrimitiveGenerator floor;
	
	public EmptyAreaFactory(int width, int height) {
		super(width, height);
		floor = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.floor, this);
		floor.addPrimitiveTileType(0, 0, 0, 1f);
		floor.addPrimitiveTileType(0, 0, 1, 0.1f);
	}

	@Override
	public Box getLegalSpawnArea() {
		return new Box(0, 0, getLevelSize().x, getLevelSize().y);
	}

	@Override
	public Tile getDefaultTile() {
		Tile t = new Tile();
		t.addWall(0, 0, 0);
		t.addFloor(0, 0, 0);
		return t;
	}

	@Override
	public void generate(Level l, Shape s) {
		floor.generate(l, s);
	}
	
}
