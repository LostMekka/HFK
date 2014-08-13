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
	private final HashMap<Integer, Boolean> bools = new HashMap<>();
	private final HashMap<Integer, Integer> ints = new HashMap<>();
	private final HashMap<Integer, Long> references = new HashMap<>();
	private final HashMap<Integer, Float> floats = new HashMap<>();
	private final HashMap<Integer, Object> objects = new HashMap<>();

	public static NetStatePart create(NetStateObject o, NetState state){
		NetStatePart s = new NetStatePart(o);
		return o.fillStateParts(s, state);
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
		for(int i : references.keySet()){
			long val = references.get(i);
			if(val != parent.getID(i)){
				ans.references.put(i, val);
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
	
	public T createObject(NetState state){
		try {
			NetStateObject o = type.newInstance();
			o.setID(objectID);
			o.updateFromStatePart(this, state);
			GameController.get().netStateObjectCreated(o);
			return (T)o;
		} catch (IllegalAccessException | InstantiationException ex) {
			throw new RuntimeException("exception while instantiating new object from net state part!", ex);
		}
	}

	public T getAndUpdateObject(NetState state){
		T ans = (T)GameController.get().getNetStateObject(objectID);
		if(ans == null){
			ans = createObject(state);
		} else {
			ans.updateFromStatePart(this, state);
		}
		return ans;
	}
	
	public long getObjectID() {
		return objectID;
	}
	
	public void setBoolean(int index, boolean b){
		bools.put(index, b);
	}
	
	public void setID(int index, long id){
		references.put(index, id);
	}
	
	public void setInteger(int index, int i){
		ints.put(index, i);
	}
	
	public void setFloat(int index, float f){
		floats.put(index, f);
	}
	
	public void setObject(int index, Object o){
		objects.put(index, o);
	}
	
	public boolean getBoolean(int index){
		return bools.get(index);
	}
	
	public int getInteger(int index){
		return ints.get(index);
	}
	
	public long getID(int index){
		return references.get(index);
	}
	
	public float getFloat(int index){
		return floats.get(index);
	}
	
	public Object getObject(int index){
		return objects.get(index);
	}
	
}
