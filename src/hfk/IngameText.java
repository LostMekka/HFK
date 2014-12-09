package hfk;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.mobs.Mob;
import org.newdawn.slick.Color;

/**
 *
 * @author LostMekka
 */
public class IngameText {
	
	private static final boolean DRAW_DEBUG_BOX = false;
	private static final int INITIAL_LIFETIME = 2000;
	private static final int BLINK_TIME = 500;
	private static final int BLINK_INTERVAL = 90;
	private static final float BOUNCE_FRICTION = 0.48f;
	private static final float GRAVITY = 15f;
	
	public int lifeTime = INITIAL_LIFETIME;
	public PointF pos, vel = new PointF(0f, 0f);
	public float floorHeight = 0.5f, scale;
	public String text;
	public Color color = Color.white;
	public boolean useGravity = true;
	public Mob parent = null;
	
	private boolean isFirstUpdate = true;
	private float floorY = 0f;

	public IngameText(String text, Color color, PointF pos, float scale) {
		GameController c = GameController.get();
		this.pos = pos.clone();
		this.scale = scale;
		this.text = "_" + text + "_";
		this.color = color;
		lifeTime = Math.round((GameController.random.nextFloat()*0.5f+1f) * INITIAL_LIFETIME);
		this.pos.x -= scale * c.getZoom() * c.transformScreenToTiles(c.renderer.getStringWidth(this.text)) / 2f;
		this.pos.y -= scale * c.getZoom() * c.transformScreenToTiles(c.renderer.getStringHeight(this.text)) / 2f;
	}
	
	public boolean update(int time){
		if(isFirstUpdate){
			isFirstUpdate = false;
			floorY = pos.y + floorHeight;
			if(parent != null){
				this.pos.x -= parent.pos.x;
				this.pos.y -= parent.pos.y;
			}
		}
		lifeTime -= time;
		if(lifeTime <= 0) return true;
		float t = (float)time / 1000f;
		pos.x += vel.x * t;
		pos.y += vel.y * t;
		if(useGravity) vel.y += GRAVITY * t;
		if(pos.y >= floorY){
			if(vel.y * vel.y / 2f / GRAVITY < 0.05f){
				vel.x = 0;
				vel.y = 0;
				pos.y = floorY;
			} else {
				vel.y *= -BOUNCE_FRICTION;
				vel.x *= BOUNCE_FRICTION;
				pos.y = 2*floorY - pos.y;
			}
		}
		return false;
	}
	
	public void draw(){
		if(DRAW_DEBUG_BOX){
			GameController c = GameController.get();
			GameRenderer r = c.renderer;
			PointF p1 = pos.clone();
			if(parent != null) p1.add(parent.pos);
			PointF p2 = new PointF();
			p2.x = r.getStringWidth(text) * scale * c.getZoom();
			p2.y = r.getStringHeight(text) * scale * c.getZoom();
			p1 = c.transformTilesToScreen(p1);
			r.getGraphics().setColor(color);
			r.getGraphics().drawRect(p1.x, p1.y, p2.x, p2.y);
		}
		if(lifeTime < BLINK_TIME && lifeTime % BLINK_INTERVAL >= BLINK_INTERVAL/2) return;
		PointF p = pos;
		if(parent != null){
			p = pos.clone();
			p.x += parent.pos.x;
			p.y += parent.pos.y;
		}
		GameController.get().renderer.drawString(text, p, color, scale, true);
	}
	
}
