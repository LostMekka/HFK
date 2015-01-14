/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.game.slickstates;

import hfk.game.GameController;
import hfk.game.GameSettings;
import hfk.game.HFKGame;
import hfk.game.LoadingProgressListener;
import hfk.game.Resources;
import hfk.game.ServerGameController;
import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class LoadingState extends BasicGameState implements LoadingProgressListener {

	private String lastMessage = "";
	private float lastProgress = 0f;
	private boolean firstUpdate = true;
	private StateBasedGame sbg = null;
	
	@Override
	public int getID() {
		return HFKGame.STATEID_LOADING;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
		this.sbg = sbg;
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
		int left = Math.round(gc.getWidth() / 10f);
		int right = gc.getWidth() - left;
		int width = right - left;
		int hh = gc.getHeight() / 2;
		Graphics g = gc.getGraphics();
		g.setColor(new Color(0f, 0f, 0.3f));
		g.fillRect(0, 0, gc.getWidth(), gc.getHeight());
		g.setColor(Color.white);
		g.drawRect(left - 3, hh - 13, width + 5, 25);
		g.setColor(Color.green);
		g.fillRect(left, hh-10, Math.round(width * lastProgress), 20);
		Font f = Resources.getFont("font");
		if(f != null){
			f.drawString(left, hh + 30, lastMessage, Color.green);
		} else {
			g.drawString(lastMessage, left, hh + 30);
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
		if(firstUpdate){
			firstUpdate = false;
			Resources.addLoadingProgressListener(this);
		}
		Resources.loadBit();
	}

	@Override
	public void onProgress(float progress) {
		lastProgress = progress;
	}

	@Override
	public void onLoadingMessage(String message) {
		lastMessage = message;
	}

	@Override
	public void onDone() {
		GameSettings settings = new GameSettings();
		String noMusic = System.getProperty("nomusic");
		String ctrlMode = System.getProperty("mode");
		GameController ctrl;
		if(ctrlMode != null && ctrlMode.equalsIgnoreCase("server")){
			ctrl = new ServerGameController(7766, sbg.getContainer(), settings);
		} else {
			ctrl = new GameController(sbg.getContainer(), settings);
		}
		GameController.set(ctrl);
		if(noMusic != null && noMusic.equals("true")) GameController.get().musicIsOn = false;
		GameController.get().initAfterLoading(sbg.getContainer());
		sbg.enterState(HFKGame.STATEID_GAMEPLAY);
	}
	
}
