package hfk.level.factory;

import hfk.PointI;
import hfk.RandomSelector;
import hfk.Shape;
import hfk.level.Level;
import java.util.ArrayList;

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
	
	private class InnerGenerator{
		public LevelGenerator g;
		public float p;
		public InnerGenerator(LevelGenerator g, float p) {
			this.g = g;
			this.p = p;
		}
	}

	private final LevelGenerator parent;
	private ArrayList<InnerGenerator> innerGenerators = null;
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
		if(innerGenerators == null) innerGenerators = new ArrayList<>();
		innerGenerators.add(new InnerGenerator(generator, probabilityModifier));
	}
	
	public void generateDefaultPropertyMaps(int width, int height){}
	
	public abstract void generate(Level l, Shape s);
	
}
