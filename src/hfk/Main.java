/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk;

import hfk.game.GameController;
import hfk.game.HFKGame;
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
		//printFontStuff();
		for(String arg : args) {
			if(arg.equalsIgnoreCase("-nomusic")) System.setProperty("nomusic", "true");
		}
		try {
			AppGameContainer c = new AppGameContainer(new HFKGame(), 1024, 768, false);
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
