/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.PointI;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.HFKGame;
import hfk.game.InputMap;
import hfk.game.InputMap.Action;
import hfk.menu.MenuBox;
import hfk.menu.SimpleMenuBox;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class PauseSubState extends GameSubState{

	private MenuBox mb = null;
	
	public PauseSubState(InputMap inputMap) {
		super(inputMap);
	}

	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		mb = new SimpleMenuBox(gc);
	}

	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		if(getInputMap().isPressed(Action.A_RESTART_GAME)) ctrl.newGame();
		if(getInputMap().isPressed(Action.A_QUIT)) sbg.enterState(HFKGame.STATEID_MENU);
		if(getInputMap().isPressed(Action.A_RESUME_GAME)){
			GameController.get().setCurrSubState(GameController.get().gameplaySubState);
		}
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();
		int w = gc.getWidth(), h = gc.getHeight();
		int y = h/2 - 150;
		y = drawString(r, y, "pause", Color.yellow, 4, w);
		y = drawString(r, y, "------------------", Color.yellow, 2, w);
		y = drawString(r, y, "esc : resume", Color.yellow, 2, w);
		y = drawString(r, y, "r : restart", Color.yellow, 2, w);
		y = drawString(r, y, "q : quit to main menu", Color.yellow, 2, w);
	}
	
	private int drawString(GameRenderer r, int y, String s, Color c, int scale, int w){
		PointI size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, c, scale);
		return y + size.y * scale + 8;
	}
	
}
