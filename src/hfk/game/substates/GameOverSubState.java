/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.PointI;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.menu.MenuBox;
import hfk.menu.SimpleMenuBox;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class GameOverSubState extends GameSubState {

	private MenuBox mb = null;
	
	public GameOverSubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_SPACE, InputMap.A_NEWGAME);
		inputMap.addKey(Input.KEY_Q, InputMap.A_QUIT);
		// TODO: add menu key when there is a menu
	}

	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		mb = new SimpleMenuBox(gc);
	}
	
	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		if(getInputMap().isKeyPressed(InputMap.A_NEWGAME)) GameController.get().newGame();
		if(getInputMap().isKeyPressed(InputMap.A_QUIT)) gc.exit();
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();
		int w = gc.getWidth(), h = gc.getHeight();
		int y = h/2 - 100;
		String s = "game over";
		int scale = 4;
		PointI size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		scale = 2;

		s = "-------------------------------";
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		s = "level reached: " + ctrl.getLevelCount();
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		s = "xp gathered: " + (ctrl.player.xp + ctrl.player.skills.getTotalXpSpent());
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		s = "skills learned: " + ctrl.player.skills.getSkillLearnedCount();
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		s = "-------------------------------";
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		
		s = "space : retry";
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
		s = "q : quit";
		size = r.getStringSize(s);
		r.drawStringOnScreen(s, (w-size.x*scale)/2, y, Color.yellow, scale); y += size.y * scale + 4;
	}

}
