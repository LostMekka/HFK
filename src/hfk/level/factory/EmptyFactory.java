/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory;

import hfk.level.Level;
import hfk.level.Tile;

/**
 *
 * @author LostMekka
 */
public class EmptyFactory extends LevelFactory {

	public EmptyFactory(int width, int height) {
		super(width, height);
	}

	@Override
	public void generate(Level l, Box b) {
		for(int x=b.x; x<b.x+b.w; x++){
			for(int y=b.y; y<b.y+b.h; y++){
				l.setTile(x, y, 4+getTileVariation(x, y));
			}
		}
	}
	
}
