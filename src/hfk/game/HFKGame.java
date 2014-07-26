/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game;

import hfk.game.slickstates.LoadingState;
import hfk.game.slickstates.GameplayState;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class HFKGame extends StateBasedGame {

	public static final int STATEID_LOADING = 1;
	public static final int STATEID_GAMEPLAY = 2;
	
	public HFKGame() {
		super("Himmelfahrtskommando");
	}

	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
		addState(new LoadingState());
		addState(new GameplayState());
	}
	
}
