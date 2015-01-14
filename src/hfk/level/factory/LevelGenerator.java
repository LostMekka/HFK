package hfk.level.factory;

import hfk.PointI;
import hfk.RandomSelector;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.tiles.TileTemplate;

/**
 *
 * @author LostMekka
 */
public abstract class LevelGenerator {
	
	private final LevelGenerator parent;
	private final RandomSelector<LevelGenerator> innerGenerators = new RandomSelector<>();
	private final RandomSelector<TileTemplate> templates = new RandomSelector<>();

	public LevelGenerator(LevelGenerator parent) {
		this.parent = parent;
	}
	
	public void addTileTemplate(TileTemplate t, float p){
		templates.addItem(t, p);
	}
	
	public TileTemplate getRandomTileTemplate(){
		return templates.getRandomItem();
	}

	public PointI getLevelSize(){
		return parent.getLevelSize();
	}
	
	public void addInnerGenerator(LevelGenerator generator, float probabilityModifier){
		innerGenerators.addItem(generator, probabilityModifier);
	}
	
	public LevelGenerator getRandomInnerGenerator(){
		return innerGenerators.getRandomItem();
	}
	
	public void generateDefaultPropertyMaps(int width, int height){}
	
	public abstract void generate(Level l, Shape s);
	
}
