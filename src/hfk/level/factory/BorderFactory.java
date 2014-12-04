package hfk.level.factory;

import hfk.level.Level;

/**
 *
 * @author LostMekka
 */
public class BorderFactory extends LevelFactory {

	public BorderFactory(int width, int height) {
		super(width, height);
	}

	@Override
	public void generate(Level l, Box b) {
		for(int x=0; x<getWidth(); x++){
			for(int y=0; y<getHeight(); y++){
				if(!b.isInside(x, y)) l.setTile(x, y, 0+getTileVariation(x, y));
			}
		}
		LevelFactory inner = getRandomInnerFactory();
		if(inner != null) inner.generate(l, b);
	}
	
}
