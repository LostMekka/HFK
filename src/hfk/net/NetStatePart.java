/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.net;

import hfk.game.GameController;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author LostMekka
 */
public final class NetStatePart<T extends NetStateObject> implements Serializable{
	
	private final Class<? extends NetStateObject> type;
	private final long objectID;
	private int[] ints;
	private long[] longs;
	private float[] floats;
	private boolean[] bools;

	public static NetStatePart create(NetStateObject o){
		NetStatePart part = new NetStatePart(o);
		part.ints = new int[o.getIntCount()];
		part.longs = new long[o.getLongCount()];
		part.floats = new float[o.getFloatCount()];
		part.bools = new boolean[o.getBoolCount()];
		o.fillStateFields(part.ints, 0, part.longs, 0, part.floats, 0, part.bools, 0);
		return part;
	}
	
	private NetStatePart(T o) {
		objectID = o.getID();
		type = o.getClass();
	}
	
	private NetStatePart(NetStatePart sp) {
		objectID = sp.objectID;
		type = sp.type;
	}
	
	public NetStatePart createDiffTo(NetStatePart parent){
		if(parent == null) return this; // diff to nothing is everything
		if(objectID != parent.getID()) throw new RuntimeException("trying to create net diff with wrong object!");
		NetStatePart ans = new NetStatePart(this);
		for(int i=0; i<ints.length; i++) ans.ints[i] = ints[i] - parent.ints[i];
		for(int i=0; i<longs.length; i++) ans.longs[i] = longs[i] - parent.longs[i];
		for(int i=0; i<floats.length; i++) ans.floats[i] = floats[i] - parent.floats[i];
		for(int i=0; i<bools.length; i++) ans.bools[i] = bools[i] ^ parent.bools[i];
		return ans;
	}
	
	public NetStatePart createFromDiffTo(NetStatePart parent){
		if(parent == null) throw new RuntimeException("trying to create net state part from diff without parent!");
		if(objectID != parent.getID()) throw new RuntimeException("trying to create net state part from diff with wrong object!");
		NetStatePart ans = new NetStatePart(this);
		for(int i=0; i<ints.length; i++) ans.ints[i] = ints[i] + parent.ints[i];
		for(int i=0; i<longs.length; i++) ans.longs[i] = longs[i] + parent.longs[i];
		for(int i=0; i<floats.length; i++) ans.floats[i] = floats[i] + parent.floats[i];
		for(int i=0; i<bools.length; i++) ans.bools[i] = bools[i] ^ parent.bools[i];
		return ans;
	}
	
	public T createObject(){
		try {
			NetStateObject o = type.newInstance();
			o.setID(objectID);
			GameController.get().netStateObjectCreated(o);
			return (T)o;
		} catch (IllegalAccessException | InstantiationException ex) {
			throw new RuntimeException("exception while instantiating new object from net state part!", ex);
		}
	}

	public T getObject(){
		NetStateObject o = GameController.get().getNetStateObject(objectID);
		if(o == null) return createObject();
		return (T)o;
	}
	
	public void updateObject(NetStateObject o, NetState state){
		if(o.getID() != objectID) throw new RuntimeException("tried to update wron object!");
		o.applyFromStateFields(state, ints, 0, longs, 0, floats, 0, bools, 0);
	}
	
	public long getID() {
		return objectID;
	}
	
}
