/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk;

import hfk.game.GameController;

/**
 *
 * @author LostMekka
 */
public class PointI {
	
	public int x, y;

	public static PointI random(int rx, int ry){
		return new PointI(GameController.random.nextInt(rx), GameController.random.nextInt(ry));
	}
	
	public PointI(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public PointI(PointI p) {
		x = p.x;
		y = p.y;
	}

	public PointI() {
		x = 0;
		y = 0;
	}
	
	public void set(PointI p){
		x = p.x;
		y = p.y;
	}
	
	public void set(int x, int y){
		this.x = x;
		this.y = y;
	}

	@Override
	public PointI clone(){
		return new PointI(x, y);
	}
	
	public PointF toFloat(){
		return new PointF(x, y);
	}
	
	public int hammingDistanceTo(PointI p){
		return (int)Math.abs(x-p.x) + (int)Math.abs(y-p.y);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) return false;
		final PointI p = (PointI) obj;
		return x == p.x && y == p.y;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 97 * hash + this.x;
		hash = 97 * hash + this.y;
		return hash;
	}

	@Override
	public String toString() {
		return "PointI{" + x + "," + y + '}';
	}
	
}
