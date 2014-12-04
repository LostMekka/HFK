/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory;

import hfk.level.Level;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class ChainFactory extends LevelFactory{

	public LinkedList<LevelFactory> chain = new LinkedList<>();
	
	public ChainFactory(int width, int height) {
		super(width, height);
	}

	@Override
	public void generate(Level l, Box b) {
		for(LevelFactory f : chain) f.generate(l, b);
	}
	
}
