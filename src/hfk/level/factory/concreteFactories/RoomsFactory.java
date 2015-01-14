/*
 */
package hfk.level.factory.concreteFactories;

import hfk.Box;
import hfk.PointF;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.tiles.Tile;
import hfk.level.factory.LevelFactory;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import hfk.level.factory.generators.BorderGenerator;
import hfk.level.factory.generators.CrateGenerator;
import hfk.level.factory.generators.CryoChamberGenerator;
import hfk.level.factory.generators.RandomTemplateGenerator;
import hfk.level.factory.generators.RoomsGenerator;
import hfk.level.tiles.TileLayer;
import hfk.level.tiles.TileTemplate;

/**
 *
 * @author LostMekka
 */
public class RoomsFactory extends LevelFactory{

	private final LevelGenerator floor, floorCryo;
	private final LevelGenerator walls;
	private final CrateGenerator crates;
	private final RoomsGenerator rooms;
	private final BorderGenerator border;
	private final CryoChamberGenerator cryo;
	
	public RoomsFactory(int width, int height) {
		super(width, height);
		floor = new RandomTemplateGenerator(this);
		floor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 0, -1), 1f);
		floor.addTileTemplate(TileTemplate.createSimplePrimitive(true, 1, -1), 0.1f);
		floorCryo = new RandomTemplateGenerator(this);
		floorCryo.addTileTemplate(TileTemplate.createSimplePrimitive(true, 4, -1), 1f);
		floorCryo.addTileTemplate(TileTemplate.createSimplePrimitive(true, 5, -1), 0.2f);
		walls = new RandomTemplateGenerator(this);
		TileLayer wal = TileLayer.createPrimitiveLayer(false, 0, new int[]{0,1}, true, 200, true, false);
		TileLayer wbl = TileLayer.createPrimitiveLayer(false, 1, new int[]{0,1}, true, 200, true, false);
		walls.addTileTemplate(new TileTemplate(wal), 1f);
		walls.addTileTemplate(new TileTemplate(wbl), 0.2f);
		crates = new CrateGenerator(this);
		crates.floor = floor;
		crates.generateDefaultPropertyMaps(width, height);
		for(int i=0; i<4; i++){
			TileLayer l = TileLayer.createCustomLayer(false, i, 0, true, false, new PointF(0.875f, 0.875f), 60+20*i);
			crates.addTileTemplate(new TileTemplate(l), 1f);
		}
		crates.mapMinX = PropertyMap.createRandom(width, height, 7, 1, 1.2f, 3f);
		crates.mapMaxX = PropertyMap.createRandom(width, height, 7, 1, 2f, 4.2f);
		crates.mapMinY = PropertyMap.createRandom(width, height, 7, 1, 1.2f, 3f);
		crates.mapMaxY = PropertyMap.createRandom(width, height, 7, 1, 2f, 4.2f);
		crates.boxProbability = PropertyMap.createRandom(width, height, 7, 1, -0.2f, 0.7f);
		cryo = new CryoChamberGenerator(this);
		cryo.generateDefaultPropertyMaps(width, height);
		cryo.floor1 = floor;
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
		barrelChance = PropertyMap.createRandom(width, width, 7, 1, -0.1f, 0.17f);
	}

	@Override
	public Shape getLegalSpawnArea() {
		return border.lastInsideShape;
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
		border.generate(l, s);
		rooms.generate(l, border.lastInsideShape);
	}
	
}
