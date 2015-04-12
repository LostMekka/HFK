/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public abstract class GameSubState {
	
	private InputMap inputMap;

	public GameSubState(InputMap inputMap) {
		this.inputMap = inputMap;
	}

	public InputMap getInputMap() {
		return inputMap;
	}

	public void setInputMap(InputMap inputMap) {
		this.inputMap = inputMap;
	}
	
	public void initAfterLoading(GameController ctrl, GameContainer gc){}
	
	public abstract void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException;
	public abstract void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException;
	
	public void onNextLevel() {}
}
