/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.PropertyMap;
import hfk.level.factory.generators.BorderGenerator;
import hfk.level.factory.generators.CrateGenerator;
import hfk.level.factory.generators.CryoChamberGenerator;
import hfk.level.factory.generators.RandomPrimitiveGenerator;
import hfk.level.factory.generators.RoomsGenerator;

/**
 *
 * @author LostMekka
 */
public class RoomsFactory extends LevelFactory{

	private final RandomPrimitiveGenerator floor, floorCryo;
	private final RandomPrimitiveGenerator walls;
	private final CrateGenerator crates;
	private final RoomsGenerator rooms;
	private final BorderGenerator border;
	private final CryoChamberGenerator cryo;
	
	public RoomsFactory(int width, int height) {
		super(width, height);
		floor = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.floor, this);
		floor.addPrimitiveTileType(0, 0, 0, 1f);
		floor.addPrimitiveTileType(0, 0, 1, 0.1f);
		floorCryo = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.floor, this);
		floorCryo.addPrimitiveTileType(0, 1, 0, 1f);
		floorCryo.addPrimitiveTileType(0, 1, 1, 0.1f);
		walls = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.wall, this);
		walls.addPrimitiveTileType(0, 0, 0, 1f);
		walls.addPrimitiveTileType(0, 0, 1, 0.1f);
		crates = new CrateGenerator(this);
		crates.floor = floor;
		crates.generateDefaultPropertyMaps(width, height);
		crates.addPrimitiveTileType(0, 0, 0, 1f);
		crates.addPrimitiveTileType(0, 0, 1, 1f);
		crates.addPrimitiveTileType(0, 1, 0, 1f);
		crates.addPrimitiveTileType(0, 1, 1, 1f);
		crates.mapMinX = PropertyMap.createRandom(width, height, 7, 1, 1.2f, 3f);
		crates.mapMaxX = PropertyMap.createRandom(width, height, 7, 1, 2f, 4.2f);
		crates.mapMinY = PropertyMap.createRandom(width, height, 7, 1, 1.2f, 3f);
		crates.mapMaxY = PropertyMap.createRandom(width, height, 7, 1, 2f, 4.2f);
		crates.boxProbability = PropertyMap.createRandom(width, height, 7, 1, -0.2f, 0.7f);
		cryo = new CryoChamberGenerator(this);
		cryo.generateDefaultPropertyMaps(width, height);
		cryo.floor1 = floorCryo;
		cryo.floor2 = floor;
		rooms = new RoomsGenerator(this);
		rooms.generateDefaultPropertyMaps(width, height);
		rooms.empty = floor;
		rooms.floors = floor;
		rooms.walls = walls;
		rooms.addInnerGenerator(floor, 1f);
		rooms.addInnerGenerator(crates, 40f);
		//rooms.addInnerGenerator(cryo, 3f);
		border = new BorderGenerator(LEVEL_BORDER, this);
		border.floor = floor;
		border.walls = walls;
		barrelChance = PropertyMap.createRandom(width, width, 8, 0, 0f, 0.5f);
	}

	@Override
	public Shape getLegalSpawnArea() {
		return border.lastInsideShape;
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
		border.generate(l, s);
		rooms.generate(l, border.lastInsideShape);
	}
	
}
