package hfk.level.factory;

import hfk.Box;
import hfk.PointI;
import hfk.game.GameController;
import hfk.level.Level;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author LostMekka
 */
public abstract class LevelGenerator {
	
	private class InnerGenerator{
		public LevelGenerator g;
		public float p;
		public InnerGenerator(LevelGenerator g, float p) {
			this.g = g;
			this.p = p;
		}
	}

	private LevelGenerator parent;
	private HashMap<String, PropertyMap> maps = null;
	private ArrayList<InnerGenerator> innerGenerators = null;

	public LevelGenerator(LevelGenerator parent) {
		this.parent = parent;
	}
	
	public PointI getLevelSize(){
		return parent.getLevelSize();
	}
	
	public void addInnerGenerator(LevelGenerator generator, float probabilityModifier){
		if(innerGenerators == null) innerGenerators = new ArrayList<>();
		innerGenerators.add(new InnerGenerator(generator, probabilityModifier));
	}
	
	public void putPropertyMap(String name, PropertyMap map){
		if(maps == null) maps = new HashMap<>();
		maps.put(name, map);
	}
	
	public PropertyMap getPropertyMap(String name, boolean forceGlobal, LevelGenerator sender){
		if(!forceGlobal && maps != null && maps.containsKey(name)){
			return maps.get(name);
		}
		if(parent == null) return sender.generatePropertyMap(name, getLevelSize());
		return parent.getPropertyMap(name, forceGlobal, sender);
	}
	
	/**
	 * this will be called if the a map is requested by getPropertyMap,
	 * but no map is found. override this to generate the needed maps!
	 * if return super(name, levelSize); is included at the end of the overriding method,
	 * the generation request will automatically be passed to the next parent generator.
	 * (this may cause problems if the parent generators don't know the name)
	 * @param name the name of the property map
	 * @param levelSize the size of the level.
	 * this should be the size for the map as well, in case other generators use the same map elsewhere.
	 * @return the generated property map
	 */
	public PropertyMap generatePropertyMap(String name, PointI levelSize){
		return parent.generatePropertyMap(name, levelSize);
	}
	
	public abstract void generate(Level l, Box b);
	
}
