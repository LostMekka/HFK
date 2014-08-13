/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author LostMekka
 */
public class NetState implements Serializable {

	private long timeStamp, parentTimeStamp = -1;
	public HashMap<Long, NetStatePart> parts = new HashMap<>();

	public NetState(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public boolean addObject(NetStateObject o) {
		long id = o.getID();
		if (parts.containsKey(id)) {
			return false;
		}
		parts.put(id, NetStatePart.create(o, this));
		return true;
	}

	public NetState createDiffTo(NetState parent) {
		NetState ans = new NetState(timeStamp);
		boolean changed = false;
		for (NetStatePart ownSP : parts.values()) {
			long id = ownSP.getObjectID();
			NetStatePart diff = ownSP.createDiff(parent.parts.get(id));
			if (diff != null) {
				ans.parts.put(id, diff);
				changed = true;
			}
		}
		return changed ? ans : null;
	}

	public byte[] toBytes() {
		try {
			ByteArrayOutputStream arrOut = new ByteArrayOutputStream();
			//GZIPOutputStream zipOut = new GZIPOutputStream(arrOut);
			ObjectOutputStream objOut = new ObjectOutputStream(arrOut);
			objOut.writeObject(this);
			objOut.close();
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
