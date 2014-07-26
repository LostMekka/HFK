/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk;

/**
 *
 * @author LostMekka
 */
public class PointF {
	
	public float x, y;

	public PointF() {
		this.x = 0f;
		this.y = 0f;
	}
	
	public PointF(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public PointF(float angle) {
		this.x = (float)Math.cos(angle);
		this.y = (float)Math.sin(angle);
	}
	
	public PointI round(){
		return new PointI(Math.round(x), Math.round(y));
	}
	
	public float angleTo(PointF p){
		float dx = (Float)(p.x) - (Float)x;
		float dy = (Float)(p.y) - (Float)y;
		return (float)Math.atan2(dy, dx);
	}
	
	public float squaredDistanceTo(PointF p){
		return (p.x-x)*(p.x-x) + (p.y-y)*(p.y-y);
	}

	@Override
	public String toString() {
		return "Point{" + "x=" + x + ", y=" + y + '}';
	}

	@Override
	public PointF clone(){
		return new PointF(x, y);
	}

	public boolean isZero() {
		return x == 0f && y == 0f;
	}

	public void add(PointF p) {
		x += p.x;
		y += p.y;
	}
	
	public void subtract(PointF p) {
		x -= p.x;
		y -= p.y;
	}
	
	public void multiply(float f) {
		x *= f;
		y *= f;
	}
	
	public void multiply(PointF p) {
		x *= p.x;
		y *= p.y;
	}
	
	public void divide(float f) {
		x /= f;
		y /= f;
	}
	
	public void divide(PointF p) {
		x /= p.x;
		y /= p.y;
	}
	
	public void set(PointF p){
		x = p.x;
		y = p.y;
	}
	
}
