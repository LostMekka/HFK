/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import java.util.HashMap;
import java.util.LinkedList;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;

/**
 *
 * @author LostMekka
 */
public class InputMap implements MouseListener {

	public static final String A_MOVE_UP = "move_up";
	public static final String A_MOVE_DOWN = "move_down";
	public static final String A_MOVE_LEFT = "move_left";
	public static final String A_MOVE_RIGHT = "move_right";
	public static final String A_LOOT = "loot";
	public static final String A_LOOT_GRAB = "loot_grab";
	public static final String A_LOOT_USE = "loot";
	public static final String A_SHOOT = "shoot";
	public static final String A_RELOAD = "reload";
	public static final String A_GRAB = "grab";
	public static final String A_USE_LEVITEM = "use_levitem";
	
	public static final String A_INV_UP = "inventory_up";
	public static final String A_INV_DOWN = "inventory_down";
	public static final String A_INV_USE = "use_invitem";
	public static final String A_INV_DROP = "inventory_drop";
	
	public static final String A_INVENTORY = "inventory";
	public static final String A_SKILLS = "skills";
	public static final String A_CLOSE_WINDOW = "close_window";
	
	public static final String A_NEWGAME = "newgame";
	public static final String A_QUIT = "quit";
	public static final String A_MAINMENU = "mainmenu";
	public static final String A_TOGGLEMUSIC = "togglemusic";
	public static final String A_PAUSE = "pausegame";
	public static final String A_RESUMEGAME = "resumegame";
	
	public static final String A_SELECTSKILL = "selectskill";
	
	public static final String[] A_QUICKSLOTS = new String[10];
	static { for(int i=0; i<10; i++) A_QUICKSLOTS[i] = "quickslot" + i; }
	
	@Override
	public void mouseWheelMoved(int i) {
		lastMW = i;
	}
	@Override
	public void mouseClicked(int i, int i1, int i2, int i3) {}
	@Override
	public void mousePressed(int i, int i1, int i2) {}
	@Override
	public void mouseReleased(int i, int i1, int i2) {}
	@Override
	public void mouseMoved(int i, int i1, int i2, int i3) {}
	@Override
	public void mouseDragged(int i, int i1, int i2, int i3) {}
	@Override
	public void setInput(Input input) {}
	@Override
	public boolean isAcceptingInput() {return true;}
	@Override
	public void inputEnded() {}
	@Override
	public void inputStarted() {}
	
	private class Data{
		public boolean isPressed = false, isDown = false;
		public float downTime = 0f;
		public LinkedList<String> actions = new LinkedList<>();
		public Data(){}
		public Data(String action){
			actions.add(action);
		}
	}
	
	private Input in;
	private final HashMap<Integer, Data> keys = new HashMap<>();
	private final HashMap<Integer, Data> mouse = new HashMap<>();
	private int lastMW = 0;

	public InputMap(Input in) {
		this.in = in;
		in.addMouseListener(this);
	}

	public void setIn(Input in) {
		this.in = in;
	}
	
	private LinkedList<Data> getData(String action, HashMap<Integer, Data> map){
		LinkedList<Data> ans = new LinkedList<>();
		for(Data d : map.values()) if(d.actions.contains(action)) ans.add(d);
		return ans;
	}
	
	public int getMouseWheelMove(){
		return lastMW;
	}
	
	public void update(float time){
		lastMW = 0;
		for(Integer key : keys.keySet()){
			Data d = keys.get(key);
			boolean isDown = in.isKeyDown(key);
			if(isDown && d.isDown) d.downTime += time;
			d.isDown = isDown;
			d.isPressed = in.isKeyPressed(key);
		}
		for(Integer key : mouse.keySet()){
			Data d = mouse.get(key);
			boolean isDown = in.isMouseButtonDown(key);
			if(isDown && d.isDown) d.downTime += time;
			d.isDown = isDown;
			d.isPressed = in.isMousePressed(key);
		}
	}
	
	public void addKey(int key, String action){
		if(keys.containsKey(key)){
			Data d = keys.get(key);
			if(!d.actions.contains(action)) d.actions.add(action);
		} else {
			keys.put(key, new Data(action));
		}
	}
	
	public void addMouseButton(int key, String action){
		if(mouse.containsKey(key)){
			Data d = mouse.get(key);
			if(!d.actions.contains(action)) d.actions.add(action);
		} else {
			mouse.put(key, new Data(action));
		}
	}
	
	public boolean isKeyDown(String action){
		for(Data d : getData(action, keys)){
			if(d.isDown) return true;
		}
		return false;
	}
	
	public float getKeyDownTime(String action){
		float ans = 0f;
		for(Data d : getData(action, keys)){
			if(d.isDown && d.downTime > ans) ans = d.downTime;
		}
		return ans;
	}
	
	public boolean isKeyPressed(String action){
		for(Data d : getData(action, keys)){
			if(d.isPressed) return true;
		}
		return false;
	}
	
	public boolean isMouseDown(String action){
		for(Data d : getData(action, mouse)){
			if(d.isDown) return true;
		}
		return false;
	}
	
	public float getMouseDownTime(String action){
		float ans = 0f;
		for(Data d : getData(action, mouse)){
			if(d.isDown && d.downTime > ans) ans = d.downTime;
		}
		return ans;
	}
	
	public boolean isMousePressed(String action){
		for(Data d : getData(action, mouse)){
			if(d.isPressed) return true;
		}
		return false;
	}
	
}
