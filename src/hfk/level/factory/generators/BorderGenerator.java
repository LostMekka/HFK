package hfk.level.factory.generators;

import hfk.Box;
import hfk.PointCloud;
import hfk.PointI;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;

/**
 *
 * @author LostMekka
 */
public class BorderGenerator extends LevelGenerator {

	public LevelGenerator walls;
	public LevelGenerator floor;
	public int border;
	public Shape lastInsideShape = null;

	public BorderGenerator(int border, LevelGenerator parent) {
		super(parent);
		this.border = border;
	}
	
	@Override
	public void generate(Level l, Shape s) {
		lastInsideShape = s.clone();
		Shape outside = lastInsideShape.subtractBorder(border);
		floor.generate(l, outside);
		walls.generate(l, outside);
	}

}
