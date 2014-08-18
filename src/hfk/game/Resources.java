/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.game;

import java.util.HashMap;
import java.util.LinkedList;
import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Font;
import org.newdawn.slick.Image;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;

/**
 *
 * @author LostMekka
 */
public class Resources {
	
	private static final String[] fontNames = {
		"font",
		"font_big",
	};
	private static final String[] spriteSheetNames = {
		"tiles.png",
		"player.png",
		"e_star.png",
		"e_scout.png",
		"e_grunt.png",
		"door.png",
		"barrel.png",
		"ex_001.png",
	};
	private static final String[] imageNames = {
		"stairs.png",
		"healthpack.png",
		"ammo_bullet.png",
		"ammo_shell.png",
		"ammo_sniper.png",
		"ammo_grenade.png",
		"ammo_rocket.png",
		"ammo_plasmaround.png",
		"w_plasmamachinegun.png",
		"w_pistol.png",
		"w_shotgun.png",
		"w_pumpActionShotgun.png",
		"w_energypistol.png",
		"w_sniperrifle.png",
		"w_grenadelauncher.png",
		"w_rocketlauncher.png",
		"shot.png",
		"s_grenade.png",
		"s_rocket.png",
		"ex_002.png",
	};
	private static final String[] soundNames = {
		"s_grenade_hit.wav",
		"s_grenade_bounce.wav",
		"star_shot.wav",
		"w_p_s.wav",
		"w_sg_s.wav",
		"shot1.wav",
		"hit1.wav",
		"p_hit.wav",
		"p_die.wav",
		"e_star_hit.wav",
		"e_star_die.wav",
		"e_star_alert.wav",
		"e_scout_alert.wav",
		"reload_s_pr.wav",
		"reload_e_pr.wav",
		"door.wav",
	};
	private static final String[] musicNames = {
		"creepy_006.ogg",
	};
	
	private static final LinkedList<LoadingProgressListener> listeners = new LinkedList<>();
	private static String lastName = "NONE";
	private static float total = 0f;
	private static int state = 0, count = 0, count2 = 0;
	private Resources(){}
	public static void addLoadingProgressListener(LoadingProgressListener pl){
		listeners.add(pl);
	}
	public static void removeLoadingProgressListener(LoadingProgressListener pl){
		listeners.remove(pl);
	}
	private static final HashMap<String, SpriteSheet> spriteSheets = new HashMap<>();
	private static final HashMap<String, Image> images = new HashMap<>();
	private static final HashMap<String, Image> flippedImages = new HashMap<>();
	private static final HashMap<String, Sound> sounds = new HashMap<>();
	private static final HashMap<String, Music> music = new HashMap<>();
	private static final HashMap<String, Font> fonts = new HashMap<>();
	
	private static void notify(float p){
		for(LoadingProgressListener pl : listeners) pl.onProgress(p);
	}
	
	private static void notify(String m){
		for(LoadingProgressListener pl : listeners) pl.onLoadingMessage(m);
	}
	
	private static void notifyDone(){
		for(LoadingProgressListener pl : listeners) pl.onDone();
	}
	
	public static void loadBit(){
		try {
			switch(state){
				case 0:
					total = fontNames.length + spriteSheetNames.length + imageNames.length + soundNames.length + musicNames.length;
					state++;
					count = 0;
					notify(0f);
					notify("loading fonts");
					return;
				case 1:
					if(count < fontNames.length){
						lastName = fontNames[count];
						AngelCodeFont f = new AngelCodeFont("data/" + lastName + ".fnt", "data/" + lastName + ".png", true);
						fonts.put(lastName, f);
						count++;
						count2++;
						notify(count2/total);
					} else {
						count = 0;
						state++;
						notify("loading sprite sheets");
					}
					return;
				case 2:
					if(count < spriteSheetNames.length){
						lastName = spriteSheetNames[count];
						SpriteSheet ss = new SpriteSheet("data/" + lastName, 32, 32);
						ss.setFilter(Image.FILTER_NEAREST);
						spriteSheets.put(lastName, ss);
						count++;
						count2++;
						notify(count2/total);
					} else {
						count = 0;
						state++;
						notify("loading images");
					}
					return;
				case 3:
					if(count < imageNames.length){
						lastName = imageNames[count];
						Image i = new Image("data/" + lastName);
						i.setFilter(Image.FILTER_NEAREST);
						images.put(lastName, i);
						flippedImages.put(lastName, i.getFlippedCopy(false, true));
						count++;
						count2++;
						notify(count2/total);
					} else {
						count = 0;
						state++;
						notify("loading sounds");
					}
					return;
				case 4:
					if(count < soundNames.length){
						lastName = soundNames[count];
						Sound s = new Sound("data/" + lastName);
						sounds.put(lastName, s);
						count++;
						count2++;
						notify(count2/total);
					} else {
						count = 0;
						state++;
						notify("loading music");
					}
					return;
				case 5:
					if(count < musicNames.length){
						lastName = musicNames[count];
						Music m = new Music("data/" + lastName);
						music.put(lastName, m);
						count++;
						count2++;
						notify(count2/total);
					} else {
						count = 0;
						state++;
						notify("done.");
						notifyDone();
					}
					return;
			}
		} catch (SlickException ex) {
			throw new RuntimeException("error loading resource: " + lastName);
		}
	}
	
	public static Font getFont(String name){
		return fonts.get(name);
	}
	
	public static SpriteSheet getSpriteSheet(String name){
		return spriteSheets.get(name);
	}
	
	public static Image getImage(String name){
		return images.get(name);
	}
	
	public static Image getImage(String name, boolean flipped){
		if(flipped) return flippedImages.get(name);
		return images.get(name);
	}
	
	public static Sound getSound(String name){
		return sounds.get(name);
	}
	
	public static Music getMusic(String name){
		return music.get(name);
	}
	
}
