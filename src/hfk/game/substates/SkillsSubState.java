/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.menu.MenuBox;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class SkillsSubState extends GameSubState {

	private static final int DESC_LINE_HEIGHT = GameRenderer.MIN_TEXT_HEIGHT;
	private static final int DESC_LINE_COUNT = 4;
	
	private MenuBox mb, mbTree, mbDescr;
	
	public SkillsSubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_CLOSE_WINDOW);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_SELECTSKILL);
	}

	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		float s = SplitMenuBox.getSplitRatioFromFirstSize(gc.getHeight(), DESC_LINE_COUNT * DESC_LINE_HEIGHT);
		SplitMenuBox smb = new SplitMenuBox(gc, s, 1f);
		mb = smb;
		mbTree = new SimpleMenuBox(smb, SplitMenuBox.Location.topLeft);
		mbDescr = new SimpleMenuBox(smb, SplitMenuBox.Location.bottomLeft);
	}

	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		
	}
	
}
