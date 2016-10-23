/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.stream.Stream;
import org.newdawn.slick.Input;
import org.newdawn.slick.MouseListener;

/**
 *
 * @author LostMekka
 */
public class InputMap implements MouseListener {

	public static final class InputSource {
		private static final HashMap<String, InputSource> nameToObject = new HashMap<>();
		private static final HashMap<InputSource, String> objectToName = new HashMap<>();
		static {
			try {
				final int targetModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;
				for (Field field : Input.class.getDeclaredFields()) {
					if (field.getModifiers() != targetModifiers || !field.getType().equals(int.class)) continue;
					final String name = field.getName();
					if (name.startsWith("KEY_")) {
						put(name, key(field.getInt(null)));
					} else if (name.startsWith("MOUSE_")) {
						put(name, mouse(field.getInt(null)));
					}
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Could not reflect upon Slick engine's Input class to get member names.", e);
			}
		}
		private static void put(String name, InputSource object) {
			nameToObject.put(name, object);
			objectToName.put(object, name);
		}

		public static Stream<String> getAvailableSlickInputFieldNames() {
			return nameToObject.keySet().stream();
		}

		public static Stream<String> getAvailableActionNames() {
			return Stream.of(Action.values()).map(Action::name);
		}

		public static InputSource forFieldName(String fieldName) {
			InputSource ans = nameToObject.get(fieldName);
			if (ans == null) throw new IllegalArgumentException(String.format(
					"Input source for name \"%s\" not found!",
					fieldName
			));
			return ans;
		}

		public final boolean isMouse;
		public final int code;

		private InputSource(boolean isMouse, int code) {
			this.isMouse = isMouse;
			this.code = code;
		}

		public String getSlickInputFieldName() {
			return objectToName.get(this);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			InputSource that = (InputSource) o;

			return isMouse == that.isMouse && code == that.code;
		}

		@Override
		public int hashCode() {
			int result = (isMouse ? 1 : 0);
			result = 31 * result + code;
			return result;
		}
	}

	private static InputSource mouse(int code) {
		return new InputSource(true, code);
	}

	private static InputSource key(int code) {
		return new InputSource(false, code);
	}

	public enum Action {
		// main menu
		A_MAIN_MENU_QUIT(key(Input.KEY_ESCAPE)),
		A_MAIN_MENU_CLICK(mouse(Input.MOUSE_LEFT_BUTTON)), // TODO: add option to constructor to always use defaults and never write to config file
		// controls for omni state
		A_TOGGLE_MUSIC(key(Input.KEY_M)),
		// standard
		A_MOVE_UP(key(Input.KEY_W)),
		A_MOVE_DOWN(key(Input.KEY_S)),
		A_MOVE_LEFT(key(Input.KEY_A)),
		A_MOVE_RIGHT(key(Input.KEY_D)),
		A_SHOOT(mouse(Input.MOUSE_LEFT_BUTTON)),
		A_SHOOT_ALTERNATIVE(mouse(Input.MOUSE_RIGHT_BUTTON)),
		A_RELOAD(key(Input.KEY_R)),
		A_GRAB(key(Input.KEY_G)),
		A_INTERACT(key(Input.KEY_E)),
		A_QUICK_SLOT_1(key(Input.KEY_1)),
		A_QUICK_SLOT_2(key(Input.KEY_2)),
		A_QUICK_SLOT_3(key(Input.KEY_3)),
		A_QUICK_SLOT_4(key(Input.KEY_4)),
		A_QUICK_SLOT_5(key(Input.KEY_5)),
		A_QUICK_SLOT_6(key(Input.KEY_6)),
		A_QUICK_SLOT_7(key(Input.KEY_7)),
		A_QUICK_SLOT_8(key(Input.KEY_8)),
		A_QUICK_SLOT_9(key(Input.KEY_9)),
		A_QUICK_SLOT_0(key(Input.KEY_0)),
		A_SHOW_MAP(key(Input.KEY_TAB)),
		A_PAUSE_MENU(key(Input.KEY_ESCAPE)),
		// loot mode
		A_LOOT_MODE(key(Input.KEY_LCONTROL)),
		A_LOOT_USE(key(Input.KEY_E)),
		A_LOOT_GRAB(mouse(Input.MOUSE_LEFT_BUTTON)),
		A_LOOT_UNLOAD(key(Input.KEY_R)),
		// inventory screen
		A_INVENTORY_UP(key(Input.KEY_UP)),
		A_INVENTORY_DOWN(key(Input.KEY_DOWN)),
		A_INVENTORY_USE(key(Input.KEY_E), mouse(Input.MOUSE_LEFT_BUTTON)),
		A_INVENTORY_DROP(key(Input.KEY_Q)),
		A_INVENTORY_UNLOAD(key(Input.KEY_R)),
		A_INVENTORY_OPEN(key(Input.KEY_I)),
		A_INVENTORY_CLOSE(key(Input.KEY_ESCAPE), key(Input.KEY_I)),
		// skills screen
		A_SKILLS_LEARN(mouse(Input.MOUSE_LEFT_BUTTON)),
		A_SKILLS_TRACK(mouse(Input.MOUSE_RIGHT_BUTTON)),
		A_SKILLS_OPEN(key(Input.KEY_K)),
		A_SKILLS_CLOSE(key(Input.KEY_ESCAPE), key(Input.KEY_K)),
		// exchange screen
		A_EXCHANGE_UP(key(Input.KEY_UP)),
		A_EXCHANGE_DOWN(key(Input.KEY_DOWN)),
		A_EXCHANGE_USE(key(Input.KEY_E)),
		A_EXCHANGE_MOVE(mouse(Input.MOUSE_LEFT_BUTTON)),
		A_EXCHANGE_DROP(key(Input.KEY_Q)),
		A_EXCHANGE_UNLOAD(key(Input.KEY_R)),
		A_EXCHANGE_ALTERNATIVE(key(Input.KEY_LSHIFT)),
		A_EXCHANGE_CLOSE(key(Input.KEY_ESCAPE)),
		// pause screen
		A_RESUME_GAME(key(Input.KEY_ESCAPE)),
		A_RESTART_GAME(key(Input.KEY_R)),
		// game over screen
		A_NEW_GAME(key(Input.KEY_SPACE)),
		A_MAIN_MENU(key(Input.KEY_ESCAPE)),
		// both pause and game over screen
		A_QUIT(key(Input.KEY_Q)),
		// cheats
		A_CHEAT_OVERVIEW_OPEN(key(Input.KEY_F1)),
		A_CHEAT_OVERVIEW_CLOSE(key(Input.KEY_ESCAPE)),
		;
		private final InputSource[] defaults;

		public Stream<InputSource> defaultsStream() {
			return Stream.of(defaults);
		}

		Action(InputSource... defaults) {
			this.defaults = defaults;
		}
	}

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
		boolean isPressed = false, isDown = false;
		float downTime = 0f;
		LinkedList<String> actions = new LinkedList<>();
		Data(String action){
			actions.add(action);
		}
	}
	
	private Input in;
	private final HashMap<Integer, Data> keys = new HashMap<>();
	private final HashMap<Integer, Data> mouse = new HashMap<>();
	private int lastMW = 0, mouseWheel = 0;

	public InputMap(Input in) {
		this.in = in;
		in.addMouseListener(this);
	}

	public void setIn(Input in) {
		this.in = in;
	}
	
	private Stream<Data> getDataStream(Action action){
		return Stream.concat(keys.values().stream(), mouse.values().stream())
				.filter(d -> d.actions.contains(action.name()));
	}
	
	public int getMouseWheelMove(){
		return mouseWheel;
	}
	
	public void update(float time){
		mouseWheel = lastMW;
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
	
	public void addInput(String inputName, Action action){
		addInput(InputSource.forFieldName(inputName), action);
	}

	public void addInput(InputSource input, Action action){
		addInput(input.isMouse ? mouse : keys, input.code, action);
	}

	private void addInput(HashMap<Integer, Data> map, int key, Action action){
		if(map.containsKey(key)){
			Data d = map.get(key);
			if(!d.actions.contains(action.name())) d.actions.add(action.name());
		} else {
			map.put(key, new Data(action.name()));
		}
	}

	public boolean isDown(Action action){
		return getDataStream(action).anyMatch(data -> data.isDown);
	}
	
	public float getDownTime(Action action){
		return (float) getDataStream(action)
				.filter(data -> data.isDown)
				.mapToDouble(data -> data.downTime)
				.max()
				.orElse(0);
	}
	
	public boolean isPressed(Action action){
		return getDataStream(action).anyMatch(data -> data.isPressed);
	}
	
}
