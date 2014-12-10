package hfk.game.slickstates;

import hfk.game.GameController;
import hfk.game.HFKGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameplayState extends BasicGameState{

	@Override
	public int getID() {
		return HFKGame.STATEID_GAMEPLAY;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		GameController.get().render();
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		GameController.get().update(gc, sbg, time);
	}
	
}
