/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import hfk.level.factory.generators.BorderGenerator;
import hfk.level.factory.generators.CaveGenerator;
import hfk.level.factory.generators.CrateGenerator;
import hfk.level.factory.generators.CryoChamberGenerator;
import hfk.level.factory.generators.MapSelectedGenerator;
import hfk.level.factory.generators.RandomPrimitiveGenerator;
import hfk.level.factory.generators.RoomsGenerator;

/**
 *
 * @author LostMekka
 */
public class CaveAreaFactory extends LevelFactory{

	private final RandomPrimitiveGenerator tunnelFloor;
	private final RandomPrimitiveGenerator tunnelWalls;
	private final MapSelectedGenerator caveFloor;
	private final MapSelectedGenerator caveWalls;
	private final CaveGenerator cave;
	
	public CaveAreaFactory(int width, int height) {
		super(width, height);
		tunnelFloor = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.floor, this);
		tunnelFloor.addPrimitiveTileType(0, 0, 0, 1f);
		tunnelFloor.addPrimitiveTileType(0, 0, 1, 0.1f);
		tunnelWalls = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.wall, this);
		tunnelWalls.addPrimitiveTileType(0, 0, 0, 1f);
		tunnelWalls.addPrimitiveTileType(0, 0, 1, 0.1f);
		RandomPrimitiveGenerator caveFloorA = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.floor, this);
		caveFloorA.addPrimitiveTileType(1, 0, 0, 1f);
		caveFloorA.addPrimitiveTileType(1, 0, 1, 0.1f);
		RandomPrimitiveGenerator caveFloorB = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.floor, this);
		caveFloorB.addPrimitiveTileType(1, 1, 0, 1f);
		caveFloorB.addPrimitiveTileType(1, 1, 1, 0.1f);
		RandomPrimitiveGenerator caveWallsA = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.wall, this);
		caveWallsA.addPrimitiveTileType(1, 0, 0, 1f);
		caveWallsA.addPrimitiveTileType(1, 0, 1, 0.1f);
		RandomPrimitiveGenerator caveWallsB = new RandomPrimitiveGenerator(RandomPrimitiveGenerator.Type.wall, this);
		caveWallsB.addPrimitiveTileType(1, 1, 0, 1f);
		caveWallsB.addPrimitiveTileType(1, 1, 1, 0.1f);
		caveFloor = new MapSelectedGenerator(new LevelGenerator[]{caveFloorA,caveFloorB}, new float[]{0.5f}, this);
		caveWalls = new MapSelectedGenerator(new LevelGenerator[]{caveWallsA,caveWallsB}, new float[]{0.5f}, this);
		caveFloor.map = PropertyMap.createRandom(width, height, 6, 2, 0f, 1f);
		caveWalls.map = caveFloor.map;
		cave = new CaveGenerator(this);
		cave.generateDefaultPropertyMaps(width, height);
		cave.floor = caveFloor;
		cave.walls = caveWalls;
		cave.tunnelFloor = tunnelFloor;
		cave.tunnelWalls = tunnelWalls;
	}

	@Override
	public Shape getLegalSpawnArea() {
		return cave.lastSpawnArea;
	}

	@Override
	public Tile getDefaultTile() {
		Tile t = new Tile();
		t.addWall(1, 0, 0);
		t.addFloor(1, 0, 0);
		return t;
	}

	@Override
	public void generate(Level l, Shape s) {
		cave.generate(l, s);
	}
	
}
