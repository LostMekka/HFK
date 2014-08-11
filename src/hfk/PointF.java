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
	/**
	 * bounces the vector off of a line with a specified normal vector
	 * @param normal normal vector of the line to bounce off from
	 * @param bounceFriction the maximum energy that can be lost by the bounce (0f..1f)
	 * @return a point where x is the actual energy lost based on the bounce angle and y is the angle that was needed to rotate the vector
	 */
	public PointF bounce(PointF normal, float bounceFriction){
		float pi = (float)Math.PI;
		float aDiff = (normal.angle() - angle() + 2*pi) % (2*pi);
		float aRot = 2*(aDiff) - pi;
		rotate(aRot);
		float force = bounceFriction * (1f - Math.abs(aDiff/pi*2 - 2f));
		multiply(1f - force);
		return new PointF(force, aRot);
	}
	
	public PointI round(){
		return new PointI(Math.round(x), Math.round(y));
	}
	
	public float angleTo(PointF p){
		float dx = (Float)(p.x) - (Float)x;
		float dy = (Float)(p.y) - (Float)y;
		return (float)Math.atan2(dy, dx);
	}
	
	public float angle(){
		return (float)Math.atan2(y, x);
	}
	
	public void rotate(float angle){
		float cos = (float)Math.cos(angle);
		float sin = (float)Math.sin(angle);
		float tx = cos * x - sin * y;
		y = sin * x + cos * y;
		x = tx;
	}
	
	public float squaredDistanceTo(PointF p){
		return (p.x-x)*(p.x-x) + (p.y-y)*(p.y-y);
	}

	public float DistanceTo(PointF p){
		return (float)Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
	}

	public float squaredLength(){
		return x*x + y*y;
	}

	public float length(){
		return (float)Math.sqrt(x*x + y*y);
	}
	
	public void normalize(){
		divide(length());
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
