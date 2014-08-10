/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.net;

import java.util.HashMap;

/**
 *
 * @author LostMekka
 */
public final class NetStatePart<T extends NetStateObject>{
	
	private final Class<? extends NetStateObject> type;
	private final long objectID;
	private final HashMap<Integer, Boolean> bools = new HashMap<>();
	private final HashMap<Integer, Integer> ints = new HashMap<>();
	private final HashMap<Integer, Float> floats = new HashMap<>();
	private final HashMap<Integer, Object> objects = new HashMap<>();

	public static NetStatePart create(NetStateObject o){
		NetStatePart s = new NetStatePart(o);
		return o.fillStatePart(s);
	}
	
	private NetStatePart(T o) {
		objectID = o.getID();
		type = o.getClass();
	}
	
	private NetStatePart(NetStatePart sp) {
		objectID = sp.objectID;
		type = sp.type;
	}
	
	public NetStatePart createDiff(NetStatePart parent){
		if(parent == null) return this; // diff to nothing is everything
		if(objectID != parent.getObjectID()) throw new RuntimeException("trying to create net diff with wrong object!");
		NetStatePart ans = new NetStatePart(this);
		boolean changed = false;
		for(int i : bools.keySet()){
			boolean val = bools.get(i);
			if(val != parent.getBoolean(i)){
				ans.bools.put(i, val);
				changed = true;
			}
		}
		for(int i : ints.keySet()){
			int val = ints.get(i);
			if(val != parent.getInteger(i)){
				ans.ints.put(i, val);
				changed = true;
			}
		}
		for(int i : floats.keySet()){
			float val = floats.get(i);
			if(val != parent.getFloat(i)){
				ans.floats.put(i, val);
				changed = true;
			}
		}
		for(int i : objects.keySet()){
			Object val = objects.get(i);
			if(val != parent.getObject(i)){
				ans.objects.put(i, val);
				changed = true;
			}
		}
		return changed ? ans : null;
	}
	
	public T createObject(){
		try {
			NetStateObject o = type.newInstance();
			o.setID(objectID);
			o.updateFromStatePart(this);
			return (T)o;
		} catch (IllegalAccessException | InstantiationException ex) {
			throw new RuntimeException("exception while instantiating new object from net state part!", ex);
		}
	}

	public long getObjectID() {
		return objectID;
	}
	
	public void setBoolean(int id, boolean b){
		bools.put(id, b);
	}
	
	public void setInteger(int id, int i){
		ints.put(id, i);
	}
	
	public void setFloat(int id, float f){
		floats.put(id, f);
	}
	
	public void setObject(int id, Object o){
		objects.put(id, o);
	}
	
	public boolean getBoolean(int id){
		return bools.get(id);
	}
	
	public int getInteger(int id){
		return ints.get(id);
	}
	
	public float getFloat(int id){
		return floats.get(id);
	}
	
	public Object getObject(int id){
		return objects.get(id);
	}
	
}
