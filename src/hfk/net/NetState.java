/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.net;

import hfk.game.GameController;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author LostMekka
 */
public class NetState implements Serializable {

	private final long timeStamp, parentTimeStamp;
	private final ArrayList<NetStatePart> parts;

	public NetState(long timeStamp, int objectCount) {
		this(timeStamp, -1, objectCount);
	}

	public NetState(long timeStamp, long parentTimeStamp, int objectCount) {
		this.timeStamp = timeStamp;
		this.parentTimeStamp = parentTimeStamp;
		parts = new ArrayList<>(objectCount);
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public NetStateObject getObject(long id){
		// see if game controller already has the object
		NetStateObject o = GameController.get().getNetStateObject(id);
		if(o != null) return o;
		// object is not there. create a new one!
		for(NetStatePart part : parts){
			if(part.getID() == id) return part.createObject();
		}
		throw new RuntimeException("no information found about this object!");
	}

	public boolean addObject(NetStateObject o) {
		long id = o.getID();
		for(NetStatePart p : parts) if(p.getID() == id) return false;
		parts.add(NetStatePart.create(o));
		return true;
	}
	
	public NetState createDiffTo(NetState parent) {
		NetState ans = new NetState(timeStamp, parent.timeStamp, parts.size());
		// TODO: implement diff
		return ans;
	}

	public NetState createFromDiffTo(NetState parent) {
		NetState ans = new NetState(timeStamp, parts.size());
		// TODO: implement diff
		return ans;
	}

	private static ByteArrayOutputStream arrOut = null;
	private static ObjectOutputStream objOut = null;
	public byte[] toBytes() {
		try {
			if(arrOut == null){
				arrOut = new ByteArrayOutputStream();
				objOut = new ObjectOutputStream(arrOut);
			} else {
				arrOut.reset();
			}
			objOut.writeObject(this);
			byte[] ans = arrOut.toByteArray();
			return ans;
		} catch(IOException e){
			throw new RuntimeException("could not write net state to byte array!", e);
		}
	}
	
	public static NetState readFromBytes(byte[] bytes, int length){
		try {
			ByteArrayInputStream arrIn = new ByteArrayInputStream(bytes);
			GZIPInputStream zipIn = new GZIPInputStream(arrIn);
			ObjectInputStream objIn = new ObjectInputStream(arrIn);
			NetState ans = (NetState)objIn.readObject();
			objIn.close();
			return ans;
		} catch(IOException | ClassNotFoundException e){
			throw new RuntimeException("could not read net state from byte array!", e);
		}
	}
	
}
