/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level.factory.generators;

import hfk.PointCloud;
import hfk.PointI;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;
import hfk.level.factory.PropertyMap;
import java.util.ArrayList;

/**
 *
 * @author LostMekka
 */
public class MapSelectedGenerator extends LevelGenerator{

	public PropertyMap map = null;
	
	private LevelGenerator[] generators;
	private float[] borders;

	public MapSelectedGenerator(LevelGenerator[] generators, float[] borders, LevelGenerator parent) {
		super(parent);
		if(generators.length != borders.length + 1) throw new IllegalArgumentException("array sizes do not match!");
		this.generators = generators;
		this.borders = borders;
	}
	
	@Override
	public void generateDefaultPropertyMaps(int width, int height) {
		map = PropertyMap.createRandom(width, height, 6, 2, 0f, 1f);
	}

	@Override
	public void generate(Level l, Shape s) {
		int len = generators.length;
		PointCloud[] areas = new PointCloud[len];
		// populate areas
		for(int i=0; i<len; i++) areas[i] = new PointCloud();
		for(PointI p : s){
			float f = map.getFloatAt(p);
			boolean done = false;
			for(int i=0; i<len-1; i++){
				if(f <= borders[i]){
					areas[i].addPoint(p);
					done = true;
					break;
				}
			}
			if(!done) areas[len-1].addPoint(p);
		}
		// generate areas
		for(int i=0; i<len; i++) generators[i].generate(l, areas[i]);
	}
	
}
