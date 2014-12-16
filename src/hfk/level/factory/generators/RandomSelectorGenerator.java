/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level.factory.generators;

import hfk.RandomSelector;
import hfk.Shape;
import hfk.level.Level;
import hfk.level.factory.LevelGenerator;

/**
 *
 * @author LostMekka
 */
public class RandomSelectorGenerator extends LevelGenerator{
	
	private RandomSelector<LevelGenerator> sel = new RandomSelector<>();

	public RandomSelectorGenerator(LevelGenerator parent) {
		super(parent);
	}

	public void addItem(LevelGenerator item, float p) {
		sel.addItem(item, p);
	}

	@Override
	public void generate(Level l, Shape s) {
		sel.getRandomItem().generate(l, s);
	}

}
