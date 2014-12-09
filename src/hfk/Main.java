package hfk;

import hfk.game.HFKGame;
import hfk.level.factory.PropertyMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.SlickException;

/**
 *
 * @author LostMekka
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
//		Random r1 = new Random();
//		Random r2 = new Random();
//		long seed1 = r1.nextLong();
//		long seed2 = r1.nextLong();
//		for(int i=0; i<10; i++){
//			//PropertyMap m = PropertyMap.createRandom(200, 100, 4, 0, -2.5f, 1f, true);
//			r1.setSeed(seed1);
//			r2.setSeed(seed2);
//			float infl = i<5 ? i/5f : (10-i)/5f;
//			PropertyMap m = PropertyMap.createRandom(200, 100, 4, 0, -2.5f, 1f, r1, r2, 0);
//			//m.fadeOutEllypse(0.5f, 1f, 1f);
//			m.draw("" + i, 8);
//			//m.drawSliced("" + i, 8, 2);
//		}
//		if(true) return;
//		for(int i=0; i<50; i++){
//			//PropertyMap m = PropertyMap.createRandom(200, 100, 4, 0, -2.5f, 1f, true);
//			PropertyMap m = PropertyMap.createRandom(50, 50, 7, 0, -1.4f, 1f);
//			//m.fadeOutEllypse(0.5f, 1f, 1f);
//			m.draw("" + i, 8);
//			//m.drawSliced("" + i, 8, 7);
//			//m.drawSlicedCustomBorders("" + i, 8, 0f);
//		}
//		if(true) return;
		//printFontStuff();
		boolean fullscreen = true;
		System.setProperty("mode", "normal");
		for(String arg : args) {
			System.out.println(arg);
			if(arg.equalsIgnoreCase("-windowed")) fullscreen = false;
			if(arg.equalsIgnoreCase("-nomusic")) System.setProperty("nomusic", "true");
			if(arg.equalsIgnoreCase("-server")) System.setProperty("mode", "server");
			if(arg.equalsIgnoreCase("-client")) System.setProperty("mode", "client");
		}
		try {
			AppGameContainer c = new AppGameContainer(new HFKGame(), 1024, 768, false);
			int w = c.getScreenWidth();
			int h = c.getScreenHeight();
			if(fullscreen){
				c.setDisplayMode(w, h, true);
			} else {
				c.setDisplayMode(w-20, h-140, false);
			}
			c.setShowFPS(false);
			c.setAlwaysRender(true);
			c.start();
		} catch (UnsupportedClassVersionError ex) {
			Logger.getLogger(HFKGame.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(null, "Java version 7 is required. Please update Java.", "ERROR!", JOptionPane.ERROR_MESSAGE);
		} catch (SlickException ex) {
			Logger.getLogger(HFKGame.class.getName()).log(Level.SEVERE, null, ex);
			JOptionPane.showMessageDialog(null, ex, "ERROR!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public static void printFontStuff(){
		String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!?:\"+-%/() ";
		int charsPerLine = 12;
		int zoom = 6;
		int sx = 10;
		int sy = 16;
		
		System.out.println("chars count=" + chars.length());
		int x = 0, y = 0;
		for(char c : chars.toCharArray()){
			if(c == 'a'){ x=0; y=0; }
			int num = c;
			int adv = sx;
			switch(c){
				case '.':
				case ':':
				case '!':
				case 'I':
				case 'i': adv = 4; break;
				case ',': adv = 5; break;
				case '(':
				case ')': adv = 6; break;
				case ' ':
				case '"': adv = 8; break;
			}
			System.out.println(
					"char id=" + num +
					" x=" + x*sx*zoom +
					" y=" + y*sy*zoom +
					" width=" + sx*zoom +
					" height=" + sy*zoom +
					" xoffset=" + 0 +
					" yoffset=" + 0 +
					" xadvance=" + adv*zoom +
					" page=0 chnl=15");
			x++;
			if(x >= charsPerLine){ x=0; y++; }
		}
	}
	
}
