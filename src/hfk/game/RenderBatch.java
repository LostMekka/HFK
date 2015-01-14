/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hfk.game;

import hfk.Box;
import hfk.PointF;
import hfk.PointI;
import java.util.HashMap;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author LostMekka
 */
public class RenderBatch {
	
	private class RenderBatchItem{
		public Image i;
		public PointF pos;
		public Box clip = null;
		public Color color = null;
		public float angle = 0f, scale;
		public RenderBatchItem(Image i, PointF pos, float scale) {
			this.i = i;
			this.pos = pos;
			this.scale = scale;
		}
	}
	
	private GameRenderer r;
	private HashMap<Texture, LinkedList<RenderBatchItem>> batch = new HashMap<>();

	public RenderBatch(GameRenderer r) {
		this.r = r;
	}

	public void drawImage(Image i, PointF pos, boolean ignoreVisionRange){
		drawImage(i, pos, 1f, 0f, ignoreVisionRange);
	}

	public void drawImage(Image i, PointF pos, float scale, boolean ignoreVisionRange){
		drawImage(i, pos, scale, 0f, ignoreVisionRange);
	}

	public void drawImage(Image i, PointF pos, float scale, float angle, boolean ignoreVisionRange){
		float sx = i.getWidth() / GameController.SPRITE_SIZE;
		float sy = i.getHeight() / GameController.SPRITE_SIZE;
		GameController ctrl = GameController.get();
		PointI offset = new PointI(
				Math.round(pos.x - scale * sx * 0.499f), 
				Math.round(pos.y - scale * sy * 0.499f));
		PointI size = new PointI(
				1 + Math.round(pos.x + scale * sx * 0.499f) - Math.round(pos.x - scale * sx * 0.499f), 
				1 + Math.round(pos.y + scale * sy * 0.499f) - Math.round(pos.y - scale * sy * 0.499f));
		LinkedList<Box> fullClips = new LinkedList<>();
		LinkedList<Box> darkClips = new LinkedList<>();
		for(int x=0; x<size.x; x++){
			for(int y=0; y<size.y; y++){
				PointI p = new PointI(x + offset.x, y + offset.y);
				if(!ctrl.level.isScouted(p)) continue;
				if(ctrl.level.isVisible(p)){
					fullClips.add(new Box(p.x, p.y, 1, 1));
				} else if(ignoreVisionRange) {
					darkClips.add(new Box(p.x, p.y, 1, 1));
				}
			}
		}
		if(fullClips.isEmpty() && darkClips.isEmpty()) return; // nothing to render
		PointF screenPos = ctrl.getScreenPos();
		PointF drawPos = new PointF(
				ctrl.transformTilesToScreen(pos.x - screenPos.x - scale * sx * 0.5f), 
				ctrl.transformTilesToScreen(pos.y - screenPos.y - scale * sy * 0.5f));
		scale *= ctrl.getZoom();
		if(fullClips.size() == size.x * size.y){
			// image to draw is on visible tiles only. draw normally
			addBatchItem(i, null, drawPos, null, scale, angle);
			return;
		}
		if(darkClips.size() == size.x * size.y){
			// image to draw is on fog tiles only. draw darker
			addBatchItem(i, GameRenderer.RENDER_FOG, drawPos, null, scale, angle);
			return;
		}
		// image is only partly visible. calculate clip rects
		processBoxList(fullClips, i, drawPos, scale, angle, null, screenPos);
		processBoxList(darkClips, i, drawPos, scale, angle, GameRenderer.RENDER_FOG, screenPos);
	}

	private void processBoxList(LinkedList<Box> l, Image i, PointF imgPos, float scale, float angle, Color c, PointF screenPos){
		// try to merge boxes
			// TODO: merge here!
		// add batch items for each box
		GameController ctrl = GameController.get();
		int one = Math.round(ctrl.transformTilesToScreen(1));
		for(Box b : l){
			b.x = Math.round(ctrl.transformTilesToScreen(b.x - screenPos.x - 0.5f));
			b.y = Math.round(ctrl.transformTilesToScreen(b.y - screenPos.y - 0.5f));
			b.w *= one;
			b.h *= one;
			addBatchItem(i, c, imgPos, b, scale, angle);
		}
	}

	private void addBatchItem(Image i, Color c, PointF p, Box clip, float scale, float angle){
		RenderBatchItem bi = new RenderBatchItem(i, p, scale);
		Texture tex = i.getTexture();
		if(batch.containsKey(tex)){
			batch.get(tex).add(bi);
		} else {
			LinkedList<RenderBatchItem> l = new LinkedList<>();
			l.add(bi);
			batch.put(tex, l);
		}
		bi.clip = clip;
		bi.color = c;
		bi.angle = angle;
	}

	public void execute(){
		// draw items
		for(Texture tex : batch.keySet()){
			tex.bind();
			for(RenderBatchItem item : batch.get(tex)){
				item.i.setCenterOfRotation(item.scale * item.i.getWidth() / 2f, item.scale * item.i.getHeight() / 2f);
				item.i.setRotation(item.angle / (float)Math.PI * 180f);
				if(item.clip != null) r.getGraphics().setClip(item.clip.x, item.clip.y, item.clip.w, item.clip.h);
				if(item.color == null){
					item.i.draw(item.pos.x, item.pos.y, item.scale);
				} else {
					item.i.draw(item.pos.x, item.pos.y, item.scale, item.color);
				}
				r.getGraphics().clearClip();
			}
		}
		batch.clear();
	}

}
