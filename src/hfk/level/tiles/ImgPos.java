/*
 */
package hfk.level.tiles;

/**
 *
 * @author LostMekka
 */
public class ImgPos {
	
	public int sheet, x, y;
	
	public ImgPos() {}
	
	public ImgPos(ImgPos p) {
		sheet = p.sheet;
		x = p.x;
		y = p.y;
	}
	
	public ImgPos(int sheet, int x, int y) {
		this.sheet = sheet;
		this.x = x;
		this.y = y;
	}
	
	public void set(int sheet, int x, int y) {
		this.sheet = sheet;
		this.x = x;
		this.y = y;
	}
}
