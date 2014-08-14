/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hfk.game.substates;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import hfk.game.InputMap;
import hfk.menu.MenuBox;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import hfk.skills.Skill;
import hfk.skills.SkillSet;
import java.util.HashMap;
import java.util.Iterator;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

/**
 *
 * @author LostMekka
 */
public class SkillsSubState extends GameSubState {

	private static final int DESC_LINE_HEIGHT = GameRenderer.MIN_TEXT_HEIGHT;
	public static final int SKILLS_LINE_HEIGHT = 30;
	
	private MenuBox mb, mbSkills, mbDescr;
	private Skill selectedSkill = null;
	private int selectedIndex = -1, offset = 0;
	private SkillSet set = null;
	
	public SkillsSubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_CLOSE_SKILLS);
		inputMap.addKey(Input.KEY_K, InputMap.A_CLOSE_SKILLS);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_SELECTSKILL);
	}

	public SkillSet getSkillSet() {
		return set;
	}

	public void init(SkillSet s){
		set = s;
		selectedIndex = -1;
		selectedSkill = null;
		offset = 0;
	}
	
	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		SplitMenuBox smb = new SplitMenuBox(gc, 1f, 0.4f);
		mb = smb;
		mbSkills = new SimpleMenuBox(smb, SplitMenuBox.Location.topLeft);
		mbDescr = new SimpleMenuBox(smb, SplitMenuBox.Location.topRight);
	}

	@Override
	public void update(GameController ctrl, GameContainer gc, StateBasedGame sbg, int time) throws SlickException {
		InputMap in = getInputMap();
		Input input = gc.getInput();
		if(in.isKeyPressed(InputMap.A_OPEN_INVENTORY)){
			ctrl.viewInventory(ctrl.player.inventory);
			return;
		}
		if(in.isKeyPressed(InputMap.A_CLOSE_SKILLS)){
			ctrl.setCurrSubState(ctrl.gameplaySubState);
			return;
		}
		int mx = input.getMouseX();
		int my = input.getMouseY();
		
		// inventory sub window
		if(in.isKeyPressed(InputMap.A_INV_UP) || in.getMouseWheelMove() > 0) scroll(-1);
		if(in.isKeyPressed(InputMap.A_INV_DOWN) || in.getMouseWheelMove() < 0) scroll(1);
		if(mbSkills.isMouseInside(mx, my)){
			update(mbSkills.getInsideMouseY(my), in, ctrl);
		} else {
			deselect();
		}
	}

	private void scroll(int n){
		int max = getSkillSlotCount();
		if(max > set.size()) return;
		offset += n;
		offset = Math.max(0, offset);
		offset = Math.min(set.size() - max, offset);
	}

	private void deselect() {
		selectedIndex = -1;
		selectedSkill = null;
	}
	
	private void update(int my, InputMap in, GameController ctrl){
		// get selected item
		int i = my / SKILLS_LINE_HEIGHT;
		selectedIndex = i + offset;
		if(selectedIndex < set.size() && i < getSkillSlotCount()){
			selectedSkill = set.getSkillList().get(selectedIndex);
		} else {
			deselect();
		}
		// use or drop selected item
		if(in.isMousePressed(InputMap.A_SELECTSKILL) && selectedSkill != null){
			if(selectedSkill.canLevelUp()) selectedSkill.levelUp();
		}
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();
		int max = getSkillSlotCount();
		
		// render skill list
		if(selectedSkill != null){
			r.drawMenuBox(
					mbSkills.getInsideX(), 
					mbSkills.getInsideY() + SKILLS_LINE_HEIGHT * (selectedIndex - offset), 
					mbSkills.getInsideWidth(), 
					SKILLS_LINE_HEIGHT, 
					GameRenderer.COLOR_MENUITEM_BG, GameRenderer.COLOR_MENUITEM_LINE);
		}
		Iterator<Skill> iter = set.getSkillList().iterator();
		for(int i=0; i<offset; i++) iter.next();
		int n = 0;
		while(iter.hasNext() && n < max){
			Skill s = iter.next();
			Color c;
			boolean req = s.canLevelUp();
			if(s.getLevel() == 0){
				c = req ? Color.white : GameRenderer.COLOR_TEXT_INACTIVE;
			} else {
				c = req ? Color.green : new Color(0.8f, 0.8f, 0f);
			}
			r.drawStringOnScreen(s.name + " (" + s.getLevel() + ")", 
					8 + mbSkills.getInsideX(), 
					7 + mbSkills.getInsideY() + n * SKILLS_LINE_HEIGHT, 
					c, 1f);
			n++;
		}
		
		// render scroll bar if necessary
		float size = set.size();
		if(max < size){
			float start = offset / size * mbSkills.getInsideHeight();
			float ratio = max / size * mbSkills.getInsideHeight();
			r.getGraphics().setColor(GameRenderer.COLOR_MENU_LINE);
			r.getGraphics().fillRect(mbSkills.getBoxX() + 4, mbSkills.getInsideY() + start, 2, ratio);
		}
		
		// render description
		int x = mbDescr.getInsideX();
		int y = mbDescr.getInsideY();
		String str = "super skills: ";
		int w = r.getStringWidth(str);
		r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
		int ssc = set.getSuperSkillCount(), ssm = set.getSuperSkillMax();
		r.drawStringOnScreen("" + ssc + "/" + ssm, x+w, y, ssc<ssm ? Color.green : Color.red, 1f); y += DESC_LINE_HEIGHT;
		str = "xp available: ";
		w = r.getStringWidth(str);
		r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
		r.drawStringOnScreen("" + set.getParent().xp, x+w, y, Color.green, 1f); y += 2*DESC_LINE_HEIGHT;
		
		if(selectedSkill == null){
			String[] sa = r.wordWrapString("hover over a skill to see a detailed description.", mbDescr.getInsideWidth());
			for(String s : sa){
				r.drawStringOnScreen(s, x, y, GameRenderer.COLOR_TEXT_INACTIVE, 1f); y += DESC_LINE_HEIGHT;
			}
			return;
		}
		Skill s = selectedSkill;
		Skill.SkillAvailability av = s.getSkillAvailability();
		int cost = s.getCost();
		r.drawStringOnScreen(s.name, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
		str = "current level: " + s.getLevel();
		if(s.getLevel() == s.getMaxLevel()) str += " (maxed)";
		r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
		switch(av){
			case available: case cantAfford:
				boolean b = av == Skill.SkillAvailability.available;
				Color c = b ? Color.green : Color.red;
				str = "next level cost: ";
				w = r.getStringWidth(str);
				r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f);
				r.drawStringOnScreen("" + cost, x+w, y, c, 1f); y += DESC_LINE_HEIGHT;
				if(b){
					str = "click this skill to level up!";
				} else {
					str = "you cannot afford this skill!";
				}
				r.drawStringOnScreen(str, x, y, c, 1f); y += DESC_LINE_HEIGHT;
				break;
			case isBlocked:
				r.drawStringOnScreen("this skill is blocked by another skill!", x, y, Color.red, 1f); y += DESC_LINE_HEIGHT;
				break;
			case reqNeeded:
				r.drawStringOnScreen("required skills not active!", x, y, Color.red, 1f); y += DESC_LINE_HEIGHT;
				break;
			case superMaxed:
				r.drawStringOnScreen("cannot have more super skills!", x, y, Color.red, 1f); y += DESC_LINE_HEIGHT;
				break;
		}
		y += DESC_LINE_HEIGHT;
		String[] descr = r.wordWrapString(s.description, mbDescr.getInsideWidth());
		for(String line : descr){
			r.drawStringOnScreen(line, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
		}
		w = 0;
		HashMap<Skill, Integer> req = s.getRequrirements();
		if(!req.isEmpty()){
			int ty = y;
			r.drawStringOnScreen("requires:", x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
			for(Skill sr : req.keySet()){
				int lr = req.get(sr);
				str = sr.name + " (" + lr + ")";
				int wr = r.getStringWidth(str);
				w = Math.max(w, wr);
				r.drawStringOnScreen(str, x+20, y, sr.getLevel()>=lr ? Color.green : Color.red, 1f); y += DESC_LINE_HEIGHT;
			}
			y = ty;
			x += 60;
		}
		if(!s.blocks.isEmpty()){
			x += w;
			r.drawStringOnScreen("blocks:", x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
			for(Skill sb : s.blocks){
				r.drawStringOnScreen(sb.name + " (" + sb.getLevel() + ")", x+20, y, sb.getLevel()==0 ? Color.green : Color.red, 1f); y += DESC_LINE_HEIGHT;
			}
		}
	}
	
	private int getSkillSlotCount(){
		return mbSkills.getInsideHeight() / SKILLS_LINE_HEIGHT;
	}

}
