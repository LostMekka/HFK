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
public class NetState {
	
	private long timeStamp, parentTimeStamp = -1;
	public HashMap<Long, NetStatePart> parts = new HashMap<>();

	public NetState(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public long getTimeStamp() {
		return timeStamp;
	}
	
	public NetState createDiffTo(NetState parent){
		NetState ans = new NetState(timeStamp);
		boolean changed = false;
		for(NetStatePart ownSP : parts.values()){
			long id = ownSP.getObjectID();
			NetStatePart diff = ownSP.createDiff(parent.parts.get(id));
			if(diff != null){
				ans.parts.put(id, diff);
				changed = true;
			}
		}
		return changed ? ans : null;
	}
	
}
