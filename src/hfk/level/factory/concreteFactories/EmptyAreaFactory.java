/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.level.Level;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.generators.RandomFloorGenerator;

/**
 *
 * @author LostMekka
 */
public class EmptyAreaFactory extends LevelFactory{

	private RandomFloorGenerator floor;
	
	public EmptyAreaFactory(int width, int height) {
		super(width, height);
		floor = new RandomFloorGenerator(this);
		floor.addFloorType(0, 0, 0, 1f);
		floor.addFloorType(0, 0, 1, 0.1f);
	}

	@Override
	public Box getLegalSpawnArea() {
		return new Box(0, 0, getLevelSize().x, getLevelSize().y);
	}

	@Override
	public void generate(Level l, Box b) {
		floor.generate(l, b);
	}
	
}
