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
