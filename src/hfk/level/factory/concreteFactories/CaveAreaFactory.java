/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.tiles.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import hfk.level.factory.generators.BorderGenerator;
import hfk.level.factory.generators.CaveGenerator;
import hfk.level.factory.generators.CrateGenerator;
import hfk.level.factory.generators.CryoChamberGenerator;
import hfk.level.factory.generators.MapSelectedGenerator;
import hfk.level.factory.generators.RandomTemplateGenerator;
import hfk.level.factory.generators.RoomsGenerator;
import hfk.level.tiles.TileLayer;
import hfk.level.tiles.TileTemplate;

/**
 *
 * @author LostMekka
 */
public class CaveAreaFactory extends LevelFactory{

	private final LevelGenerator tunnelFloor;
	private final LevelGenerator tunnelWalls;
	private final MapSelectedGenerator caveFloor;
	private final MapSelectedGenerator caveWalls;
	private final CaveGenerator cave;
	
	public CaveAreaFactory(int width, int height) {
		super(width, height);
		tunnelFloor = new RandomTemplateGenerator(this);
		tunnelFloor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 0, -1), 1f);
		tunnelFloor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 1, -1), 0.4f);
		tunnelWalls = new RandomTemplateGenerator(this);
		TileLayer tal = TileLayer.createPrimitiveLayer(false, 0, new int[]{0,1}, true, 200, true, false);
		TileLayer tbl = TileLayer.createPrimitiveLayer(false, 1, new int[]{0,1}, true, 200, true, false);
		tunnelWalls.addTileTemplate(new TileTemplate(tal), 1f);
		tunnelWalls.addTileTemplate(new TileTemplate(tbl), 1f);
		RandomTemplateGenerator caveFloorA = new RandomTemplateGenerator(this);
		RandomTemplateGenerator caveFloorB = new RandomTemplateGenerator(this);
		RandomTemplateGenerator caveWallsA = new RandomTemplateGenerator(this);
		RandomTemplateGenerator caveWallsB = new RandomTemplateGenerator(this);
		TileLayer fal = TileLayer.createPrimitiveLayer(true, 2, new int[]{2}, true, -1, false, true);
		TileLayer fbl = TileLayer.createPrimitiveLayer(true, 3, new int[]{3}, true, -1, false, true);
		TileLayer wal = TileLayer.createPrimitiveLayer(false, 2, new int[]{2}, true, 200, true, false);
		TileLayer wbl = TileLayer.createPrimitiveLayer(false, 2, new int[]{2}, true, 200, true, false);
		caveFloorA.addTileTemplate(new TileTemplate(fal), 1f);
		caveFloorB.addTileTemplate(new TileTemplate(fbl), 1f);
		caveWallsA.addTileTemplate(new TileTemplate(wal), 1f);
		caveWallsB.addTileTemplate(new TileTemplate(wbl), 1f);
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
		t.addLayersFromTemplate(TileTemplate.createSimplePrimitive(false, 0, -1));
		t.addLayersFromTemplate(TileTemplate.createSimplePrimitive(true, 0, -1));
		return t;
	}

	@Override
	public void generate(Level l, Shape s) {
		cave.generate(l, s);
	}
	
}
