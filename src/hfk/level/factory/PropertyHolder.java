/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.level.factory;

import java.util.HashMap;

/**
 *
 * @author LostMekka
 */
public abstract class PropertyHolder {
	
	private PropertyHolder parent;
	private HashMap<String, Property> properties = new HashMap<>();

	public PropertyHolder() {
		parent = null;
	}
	
	public PropertyHolder(PropertyHolder parent) {
		this.parent = parent;
	}
	
}
