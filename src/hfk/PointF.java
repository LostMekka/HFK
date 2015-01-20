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
public class PointF {
	
	public static PointF createRandom(){
		return new PointF(GameController.random.nextFloat(), GameController.random.nextFloat());
	}
	
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

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 67 * hash + Float.floatToIntBits(this.x);
		hash = 67 * hash + Float.floatToIntBits(this.y);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PointF other = (PointF) obj;
		if (Float.floatToIntBits(this.x) != Float.floatToIntBits(other.x)) {
			return false;
		}
		if (Float.floatToIntBits(this.y) != Float.floatToIntBits(other.y)) {
			return false;
		}
		return true;
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

	/**
	 * Computes the distance to another point.
	 * This method always calls sqrt, unlike distanceToRect(PointF, PointF, float),
	 * because the area, where no sqrt is required is infinitely thin.
	 * (dx == 0f || dy == 0f)
	 * If additional knowledge suggests that that this rare case is more likely,
	 * it should be tested before calling this method. Alternatively,
	 * approximateDistanceTo(PointF) can be used instead to avoid the sqrt call.
	 * @param p the other point
	 * @return the distance between the points
	 */
	public float distanceTo(PointF p){
		return (float)Math.sqrt((p.x-x)*(p.x-x) + (p.y-y)*(p.y-y));
	}

	/**
	 * Computes the approximate distance to another point.
	 * This method never calls sqrt but is not accurate like distanceTo(PointF).
	 * Use this if accuracy is not that important.
	 * @param p the other point
	 * @return the approximate distance between the points
	 */
	public float approximateDistanceTo(PointF p){
		float dx = Math.abs(x - p.x);
		float dy = Math.abs(y - p.y);
		return Math.min(Math.max(dx, dy), (dx + dy) / GameController.SQRT2);
	}

	/**
	 * Computes the distance of this point to the nearest point in the target rectangle.
	 * The computation only includes a sqrt if needed!
	 * @param p1 one corner of the target rectangle
	 * @param p2 the opposite corner of the target rectangle
	 * @return the distance to the closest point inside the target rectangle
	 */
	public float distanceToRect(PointF p1, PointF p2){
		float dx = Math.max(0f, Math.abs(x - (p1.x + p2.x) / 2f) - Math.abs(p1.x - p2.x) / 2f);
		float dy = Math.max(0f, Math.abs(y - (p1.y + p2.y) / 2f) - Math.abs(p1.y - p2.y) / 2f);
		// try to avoid the sqrt
		if(dx == 0f) return dy; // dy could be 0f too, we dont care :)
		if(dy == 0f) return dx;
		// sqrt is needed. what a shame :/
		return (float)Math.sqrt(dx*dx + dy*dy);
	}
	
	/**
	 * Computes the distance of this point to the nearest point in the target rectangle.
	 * The computation  only includes a sqrt if the distance is smaller than
	 * maxDistance and the computation definitely needs it.
	 * If the distance is greater than maxDistance, -1f is returned instead.
	 * The probability of a sqrt call to be needed is:
	 *                      PI*r^2
	 * p(sqrt) =  --------------------------
	 *            r*2*(w + h) + w*h + PI*r^2
	 * where:
	 *   - r is maxDistance,
	 *   - w is the width of the target rectangle,
	 *   - h is the height of the target rectangle,
	 *   - the actual point is within maxDistance of the rectangle and
	 *   - the position of the point is assumed to be uniformly distributed.
	 * With r = w = h: p(sqrt) = PI / (5 + PI) = 0.385869545...
	 * But since the part that doesn't need sqrt grows linearly with r and the
	 * part that will require sqrt grows quadratically with r, increasing r will
	 * increase p(sqrt), so maxDistance should be chosen as low as possible!
	 * @param p1 one corner of the target rectangle
	 * @param p2 the opposite corner of the target rectangle
	 * @param maxDistance the maximum distance
	 * @return the distance to the closest point inside the target rectangle
	 * if the distance is smaller than maxDistance, or else -1f.
	 */
	public float distanceToRect(PointF p1, PointF p2, float maxDistance){
		float dx = Math.max(0f, Math.abs(x - (p1.x + p2.x) / 2f) - Math.abs(p1.x - p2.x) / 2f);
		float dy = Math.max(0f, Math.abs(y - (p1.y + p2.y) / 2f) - Math.abs(p1.y - p2.y) / 2f);
		// try to avoid the sqrt
		if(dx*dx + dy*dy > maxDistance*maxDistance) return -1;
		// we are inside!
		if(dx == 0f) return dy;
		if(dy == 0f) return dx;
		// sqrt is needed. what a shame :/
		return (float)Math.sqrt(dx*dx + dy*dy);
	}

	/**
	 * Computes the squared distance of this point to the target rectangle.
	 * The computation includes no sqrt call.
	 * WARNING: do not use to check distances and then manually sqrt the distance!
	 * This can be done more efficiently with distanceToRect(PointF, PointF, float).
	 * @param p1 one corner of the target rectangle
	 * @param p2 the opposite corner of the target rectangle
	 * @return the squared distance to the closest point inside the target rectangle
	 */
	public float squaredDistanceToRect(PointF p1, PointF p2){
		float dx = Math.max(0f, Math.abs(x - (p1.x + p2.x) / 2f) - Math.abs(p1.x - p2.x) / 2f);
		float dy = Math.max(0f, Math.abs(y - (p1.y + p2.y) / 2f) - Math.abs(p1.y - p2.y) / 2f);
		// no sqrt here, so if statements are not needed
		return dx*dx + dy*dy;
	}

	public float squaredLength(){
		return x*x + y*y;
	}

	/**
	 * Computes the length of this vector.
	 * This method always calls sqrt, unlike distanceToRect(PointF, PointF, float),
	 * because the area, where no sqrt is required is infinitely thin.
	 * (x == 0f || y == 0f)
	 * If additional knowledge suggests that that this rare case is more likely,
	 * it should be tested before calling this method. Alternatively,
	 * approximateLength() can be used instead to avoid the sqrt call.
	 * @return the length of this vector.
	 */
	public float length(){
		return (float)Math.sqrt(x*x + y*y);
	}
	
	/**
	 * Computes the approximate length of this vector.
	 * This method never calls sqrt but is not accurate like length().
	 * Use this if accuracy is not that important.
	 * @return the approximate length of this vector.
	 */
	public float approximateLength(){
		return Math.min(Math.max(x, y), (x + y) / GameController.SQRT2);
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

	public boolean isNaN() {
		return Float.isNaN(x+y);
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
	
	public void set(float x, float y){
		this.x = x;
		this.y = y;
	}
	
}
