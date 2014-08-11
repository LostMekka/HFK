/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.net;

/**
 *
 * @author LostMekka
 */
public interface NetStateObject{
	
	public long getID();
	public void setID(long id);
	public NetStatePart fillStateParts(NetStatePart part, NetState state);
	public void updateFromStatePart(NetStatePart part, NetState state);
	
}
