/*
 */
package hfk.level.tiles;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author LostMekka
 */
public class TileTemplate implements Serializable, Iterable<TileLayer> {
	
	public static TileTemplate createSimplePrimitive(boolean isFloor, int type, int hp){
		if(!isFloor) type += 64;
		TileLayer l = TileLayer.createPrimitiveLayer(isFloor, type, new int[]{type}, isFloor, hp, !isFloor, isFloor);
		return new TileTemplate(l);
	}
	
	private final LinkedList<TileLayer> layers = new LinkedList<>();

	public TileTemplate() {}

	public TileTemplate(TileLayer... layers) {
		this.layers.addAll(Arrays.asList(layers));
	}

	public boolean addLayer(TileLayer e) {
		return layers.add(e);
	}

	@Override
	public Iterator<TileLayer> iterator() {
		return layers.iterator();
	}
	
}
