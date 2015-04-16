package hfk.game.substates;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class OverviewSubState extends GameSubState {
	
	public OverviewSubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_W, InputMap.A_MOVE_UP);
		inputMap.addKey(Input.KEY_S, InputMap.A_MOVE_DOWN);
		inputMap.addKey(Input.KEY_A, InputMap.A_MOVE_LEFT);
		inputMap.addKey(Input.KEY_D, InputMap.A_MOVE_RIGHT);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_CHEAT_CLOSE);
	}

	PointF cameraPos;
	float cameraSpeed = 10f;
	
	float originalZoom;
	
	@Override
	public void onNextLevel() {
		onActivate();
	}
	
	public void onActivate() {
		GameController ctrl = GameController.get();
		GameController.CHEAT_VISIBLE = true;
		cameraPos = ctrl.player.pos.clone();
		originalZoom = ctrl.getZoom();
		ctrl.setZoom(1f);
	}
	
	@Override
	public void update(GameController ctrl, GameContainer gc,
			StateBasedGame sbg, int time) throws SlickException {
		getInputMap().update(time);
		updateCamera(time);
		updateScreenPos(ctrl, gc);
		updateSubState(ctrl);
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc)
			throws SlickException {
		
	}
	
	private void updateSubState(GameController ctrl) {
		InputMap in = getInputMap();
		if(in.isActionPressed(InputMap.A_CHEAT_CLOSE)){
			GameController.CHEAT_VISIBLE = false;
			ctrl.setZoom(originalZoom);
			ctrl.setCurrSubState(ctrl.gameplaySubState);
		}
	}

	private void updateScreenPos(GameController ctrl, GameContainer gc) {
		float w2 = ctrl.transformScreenToTiles(gc.getWidth()) / 2f;
		float h2 = ctrl.transformScreenToTiles(gc.getHeight()) / 2f;
		ctrl.screenPosOriginal.x = cameraPos.x - w2;
		ctrl.screenPosOriginal.y = cameraPos.y - h2;
		ctrl.screenPos.set(ctrl.screenPosOriginal);
		ctrl.screenPos.add(ctrl.screenPosOffset);
	}

	private void updateCamera(int time) {
		float vx = 0, vy = 0;
		InputMap in = getInputMap();
		if(in.isActionDown(InputMap.A_MOVE_RIGHT)) vx++;
		if(in.isActionDown(InputMap.A_MOVE_LEFT)) vx--;
		if(in.isActionDown(InputMap.A_MOVE_DOWN)) vy++;
		if(in.isActionDown(InputMap.A_MOVE_UP)) vy--;
		cameraPos.x += vx * cameraSpeed * time / 1000f;
		cameraPos.y += vy * cameraSpeed * time / 1000f;
	}
}
