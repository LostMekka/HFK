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
	
	public int getIntCount();
	public int getLongCount();
	public int getFloatCount();
	public int getBoolCount();
	public long getID();
	public void setID(long id);
	public void fillStateFields(
			int[] ints, int intOffset,
			long[] longs, int longOffset,
			float[] floats, int floatOffset,
			boolean[] bools, int boolOffset);
	public void applyFromStateFields(NetState state,
			int[] ints, int intOffset,
			long[] longs, int longOffset,
			float[] floats, int floatOffset,
			boolean[] bools, int boolOffset);
	
}
