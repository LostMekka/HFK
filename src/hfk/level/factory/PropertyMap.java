/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.level.factory;

import hfk.PointF;
import hfk.PointI;
import hfk.Shape;
import hfk.game.GameController;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 *
 * @author LostMekka
 */
public final class PropertyMap {

	public static PropertyMap createClone(PropertyMap m){
		PropertyMap p = new PropertyMap(m.width, m.height);
		for(int x=0; x<m.width; x++){
			System.arraycopy(m.field[x], 0, p.field[x], 0, m.height);
		}
		return p;
	}
	public static PropertyMap createRandom(int w, int h, int layerCount, int layerOffset, float min, float max){
		if(min > max) throw new IllegalArgumentException("min > max");
		PropertyMap m = new PropertyMap(w, h);
		float size = 2.4f;
		for(int i=0; i<layerCount+layerOffset; i++){
			if(i>0) size *= 1.5f;
			if(i < layerOffset) continue;
			PointF off = SimplexNoise.getRandomOffset();
			float rot = GameController.random.nextFloat();
			for(int x=0; x<w; x++){
				for(int y=0; y<h; y++){
					m.field[x][y] += size * (float)SimplexNoise.noise(x, y, 1f/size, off, rot);
				}
			}
		}
		m.normalize(min, max);
		return m;
	}
	private static void fillArray(float[] array, int len, float min, float max, float factor, Random r){
		if(r == null) r = GameController.random;
		for(int n=0; n<len; n++){
			array[n] = (min+max)/2f + (r.nextFloat()-0.5f)*factor;
		}
	}
	
	private int width, height;
	private float[][] field, tmpField = null;
	private float defaultValue = 0f;
	private boolean useDefaultValueForBlur = false;

	public PropertyMap(int width, int height) {
		this.width = width;
		this.height = height;
		field = new float[width][height];
	}
	
	public PropertyMap(int width, int height, float startingValue) {
		this(width, height);
		fillWith(startingValue);
	}

	public void setDefaultValue(float defaultValue) {
		this.defaultValue = defaultValue;
	}

	public void useDefaultValueForBlur(boolean useDefaultValueForBlur) {
		this.useDefaultValueForBlur = useDefaultValueForBlur;
	}
	
	public void fillWith(float value){
		for(int i=0; i<width; i++) Arrays.fill(field[i], value);
	}
	
	public void putValue(int x, int y, float value){
		if(x<0 || x>=width || y<0 || y>=height) return;
		field[x][y] = value;
	}
	
	public void fadeOutEllypse(float fadeBegin, float fadeEnd, float fadeValue){
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				float dx = (float)x*height/width - height/2f;
				float dy = y - height/2f;
				float f = (float)Math.sqrt(dx*dx + dy*dy) / (height/2f);
				if(f > fadeBegin){
					if(f >= fadeEnd){
						field[x][y] = fadeValue;
					} else {
						f = (f - fadeBegin) / (fadeEnd - fadeBegin);
						field[x][y] = f*fadeValue + (1f-f)*field[x][y];
					}
				}
			}
		}
	}
	
	public void blur(float sigma){
		// incremental computation of gauss curve
		float[] gauss = new float[1+(int)(sigma * 4f)];
		float v1 = 1f / (2.506628274631f * sigma); // the number is sqrt(2*PI)
		float v2 = (float)Math.exp(-0.5 / (sigma * sigma));
		float v3 = v2 * v2;
		gauss[0] = v1;
		float sum = v1;
		for(int i=1; i<gauss.length; i++){
			v1 *= v2;
			v2 *= v3;
			gauss[i] = v1;
			sum += 2f * v1;
		}
		// norm gauss curve
		for(int i=0; i<gauss.length; i++) gauss[i] /= sum;
		// apply horizontally
		if(tmpField == null) tmpField = new float[width][height];
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				tmpField[x][y] = gauss[0] * getFloatForBlur(x, y, false);
				for(int i=1; i<gauss.length; i++){
					tmpField[x][y] += gauss[i] * getFloatForBlur(x - i, y, false);
					tmpField[x][y] += gauss[i] * getFloatForBlur(x + i, y, false);
				}
			}
		}
		// apply vertically
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				field[x][y] = gauss[0] * getFloatForBlur(x, y, true);
				for(int i=1; i<gauss.length; i++){
					field[x][y] += gauss[i] * getFloatForBlur(x, y - i, true);
					field[x][y] += gauss[i] * getFloatForBlur(x, y + i, true);
				}
			}
		}
	}
	
	private float getFloatForBlur(int x, int y, boolean tmp){
		if(x<0 || x>=width || y<0 || y>=height) return useDefaultValueForBlur ? defaultValue : 0f;
		return (tmp ? tmpField : field)[x][y];
	}
	
	public void normalize(float minValue, float maxValue){
		defaultValue = (minValue + maxValue) / 2f;
		useDefaultValueForBlur = true;
		float currMin = Float.MAX_VALUE;
		float currMax = Float.MIN_VALUE;
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				if(field[x][y] < currMin) currMin = field[x][y];
				if(field[x][y] > currMax) currMax = field[x][y];
			}
		}
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				field[x][y] = (field[x][y]-currMin) / (currMax-currMin) * (maxValue-minValue) + minValue;
			}
		}
	}
	
	public float getFloatAt(PointI p){
		if(p.x<0 || p.x>=width || p.y<0 || p.y>=height) return defaultValue;
		return field[p.x][p.y];
	}
	
	public int getIntAt(PointI p){
		return Math.round(getFloatAt(p));
	}
	
	public float getAverageFloat(Shape s){
		float sum = 0f;
		for(PointI p : s){
			sum += getFloatAt(p);
		}
		return sum / s.getPointCount();
	}
	
	public int getAverageInt(Shape s){
		return Math.round(getAverageFloat(s));
	}
	
	public float getMinFloat(Shape s){
		float ans = Float.MAX_VALUE;
		for(PointI p : s){
			float v = getFloatAt(p);
			if(v < ans) ans = v;
		}
		return ans;
	}
	
	public int getMinInt(Shape s){
		return Math.round(getMinFloat(s));
	}
	
	public float getMaxFloat(Shape s){
		float ans = Float.MIN_VALUE;
		for(PointI p : s){
			float v = getFloatAt(p);
			if(v > ans) ans = v;
		}
		return ans;
	}
	
	public int getMaxInt(Shape s){
		return Math.round(getMinFloat(s));
	}
	
	public void draw(String filename, int pixelSize){
		BufferedImage img = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = img.createGraphics();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		PointI p = new PointI();
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				float f = getFloatAt(p);
				if(f < min) min = f;
				if(f > max) max = f;
			}
		}
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				float f = (getFloatAt(p) - min) / (max - min);
				g.setColor(new Color(f, f, f));
				g.fillRect(p.x*pixelSize, p.y*pixelSize, pixelSize, pixelSize);
			}
		}
		g.dispose();
		try{
			ImageIO.write(img, "png", new File("propertymap_" + filename + ".png"));
		} catch(IOException e){}
	}
	
	private static Color[] DRAWSLICEDCOLORS = new Color[] {Color.BLACK, Color.GREEN, Color.BLUE, Color.WHITE, Color.RED};
	public void drawSliced(String filename, int pixelSize, int sliceCount){
		BufferedImage img = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = img.createGraphics();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		PointI p = new PointI();
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				float f = getFloatAt(p);
				if(f < min) min = f;
				if(f > max) max = f;
			}
		}
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				float f = (getFloatAt(p) - min) / (max - min);
				int i = (int)(f * sliceCount) % DRAWSLICEDCOLORS.length;
				if(i >= sliceCount) i = sliceCount - 1;
				g.setColor(DRAWSLICEDCOLORS[i]);
				g.fillRect(p.x*pixelSize, p.y*pixelSize, pixelSize, pixelSize);
			}
		}
		g.dispose();
		try{
			ImageIO.write(img, "png", new File("propertymap_" + filename + ".png"));
		} catch(IOException e){}
	}
	
	public void drawSlicedCustomBorders(String filename, int pixelSize, float border){
		BufferedImage img = new BufferedImage(width*pixelSize, height*pixelSize, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = img.createGraphics();
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		PointI p = new PointI();
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				float f = getFloatAt(p);
				if(f < min) min = f;
				if(f > max) max = f;
			}
		}
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				g.setColor(getFloatAt(p)>border ? Color.white : Color.black);
				g.fillRect(p.x*pixelSize, p.y*pixelSize, pixelSize, pixelSize);
			}
		}
		g.dispose();
		try{
			ImageIO.write(img, "png", new File("propertymap_" + filename + ".png"));
		} catch(IOException e){}
	}
	
	public void print(){
		System.out.println("----- property map -----------------------------------------------");
		PointI p = new PointI();
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				System.out.format("%6.2f ", getFloatAt(p));
			}
			System.out.println();
		}
	}
	
	public void printSign(){
		System.out.println("----- property map -----------------------------------------------");
		PointI p = new PointI();
		for(p.x=0; p.x<width; p.x++){
			for(p.y=0; p.y<height; p.y++){
				float f = getFloatAt(p);
				if(f > 0) System.out.print("O ");
				if(f < 0) System.out.print("X ");
				if(f == 0) System.out.print("= ");
			}
			System.out.println();
		}
	}
	
}
