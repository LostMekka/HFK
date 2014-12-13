package hfk.level.factory;

import hfk.PointI;
import hfk.RandomSelector;
import hfk.Shape;
import hfk.level.Level;

/**
 *
 * @author LostMekka
 */
public abstract class LevelGenerator {
	
	public static class PrimitiveTileType{
		public final int type, subtype, variant;
		public PrimitiveTileType(int type, int subtype, int variant) {
			this.type = type;
			this.subtype = subtype;
			this.variant = variant;
		}
	}
	
	private final LevelGenerator parent;
	private final RandomSelector<LevelGenerator> innerGenerators = new RandomSelector<>();
	private final RandomSelector<PrimitiveTileType> ran = new RandomSelector<>();

	public LevelGenerator(LevelGenerator parent) {
		this.parent = parent;
	}
	
	public void addPrimitiveTileType(int type, int subtype, int variant, float p){
		ran.addItem(new PrimitiveTileType(type, subtype, variant), p);
	}
	
	public PrimitiveTileType getRandomPrimitiveTileType(){
		return ran.getRandomItem();
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
