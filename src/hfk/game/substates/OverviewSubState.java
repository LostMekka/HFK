package hfk.game.substates;

import hfk.PointF;
import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;

import hfk.game.InputMap.Action;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

public class OverviewSubState extends GameSubState {
	
	public OverviewSubState(InputMap inputMap) {
		super(inputMap);
	}

	private PointF cameraPos;
	private float cameraSpeed = 10f;
	
	private float originalZoom;

	public float getCameraSpeed() {
		return cameraSpeed;
	}

	public void setCameraSpeed(float cameraSpeed) {
		this.cameraSpeed = cameraSpeed;
	}

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
		if(in.isPressed(Action.A_CHEAT_OVERVIEW_CLOSE)){
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
		if(in.isDown(Action.A_MOVE_RIGHT)) vx++;
		if(in.isDown(Action.A_MOVE_LEFT)) vx--;
		if(in.isDown(Action.A_MOVE_DOWN)) vy++;
		if(in.isDown(Action.A_MOVE_UP)) vy--;
		cameraPos.x += vx * cameraSpeed * time / 1000f;
		cameraPos.y += vy * cameraSpeed * time / 1000f;
	}
}
