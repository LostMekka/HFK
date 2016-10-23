package hfk.game.slickstates;

import hfk.game.GameController;
import hfk.game.HFKGame;
import hfk.game.InputMap;
import hfk.game.InputMap.Action;
import hfk.game.Resources;
import hfk.menu.ButtonsMenuBox;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class MenuState extends BasicGameState{

	private static final int logoSize = 5;

	private SimpleMenuBox logoBox;
	private SplitMenuBox split;
	private ButtonsMenuBox buttons;
	private Image logo;
	
	@Override
	public int getID() {
		return HFKGame.STATEID_MENU;
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {}
	
	public void initAfterLoading(GameContainer gc){
		logo = Resources.getImage("logo.png");
		float f = SplitMenuBox.getSplitRatioFromFirstSize(gc.getHeight(), (logo.getHeight()+4)*logoSize);
		split = new SplitMenuBox(gc, f, 1f);
		logoBox = new SimpleMenuBox(split, SplitMenuBox.Location.topLeft);
		buttons = new ButtonsMenuBox(split, SplitMenuBox.Location.bottomLeft);
		buttons.setButtons(2, 800, "new game: easy", "new game: medium", "new game: hard", "new game: insane", "exit");
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
		int w = gc.getWidth();
		int h = gc.getHeight();
		g.setColor(Color.blue);
		g.fillRect(0, 0, w, h);
		split.render();
		int lw = logo.getWidth()*logoSize;
		int lh = logo.getHeight()*logoSize;
		logo.draw((w-lw)/2, logoBox.getUsableY() + (logoBox.getUsableHeight()-lh)/2, logoSize);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		GameController ctrl = GameController.get();
		Input in = gc.getInput();
		InputMap map = ctrl.getInputMap();
		map.update(time);
		if(map.isPressed(Action.A_MAIN_MENU_QUIT)) gc.exit();
		if(map.isPressed(Action.A_MAIN_MENU_CLICK)){
			int b = buttons.getButtonIndexUnderMouse(in.getMouseX(), in.getMouseY());
			switch(b){
				case 0: case 1: case 2: case 3:
					ctrl.difficultyLevel = b;
					ctrl.newGame();
					sbg.enterState(HFKGame.STATEID_GAMEPLAY);
					break;
				case 4:
					gc.exit();
					break;
			}
		}
	}
	
}
