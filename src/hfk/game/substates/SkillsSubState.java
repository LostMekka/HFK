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
import hfk.menu.MenuItemList;
import hfk.menu.SimpleMenuBox;
import hfk.menu.SplitMenuBox;
import hfk.mobs.Mob;
import hfk.mobs.Player;
import hfk.skills.Skill;
import hfk.skills.SkillSet;
import java.util.HashMap;
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
	
	private MenuBox mb, mbDescr;
	private SimpleMenuBox mbSkills;
	private MenuItemList<Skill> skillsList;
	private Skill selectedSkill = null;
	private SkillSet set = null;
	private Mob parent = null;
	private Player player = null;
	
	public SkillsSubState(InputMap inputMap) {
		super(inputMap);
		inputMap.addKey(Input.KEY_ESCAPE, InputMap.A_CLOSE_SKILLS);
		inputMap.addKey(Input.KEY_K, InputMap.A_CLOSE_SKILLS);
		inputMap.addMouseButton(Input.MOUSE_LEFT_BUTTON, InputMap.A_SELECTSKILL);
		inputMap.addMouseButton(Input.MOUSE_RIGHT_BUTTON, InputMap.A_TRACKSKILL);
	}

	public SkillSet getSkillSet() {
		return set;
	}

	public void init(SkillSet skillSet){
		set = skillSet;
		selectedSkill = null;
		parent = set.getParent();
		player = parent instanceof Player ? (Player)parent : null;
		populateList();
	}
	
	private void populateList(){
		skillsList.clearList();
		for(Skill s : set.getSkillList()){
			skillsList.addListItem(s, s.getDisplayName(), s.getDisplayColor(), 
					player != null && player.isTrackedSkill(s), s.isSuperSkill);
		}
	}
	
	@Override
	public void initAfterLoading(GameController ctrl, GameContainer gc) {
		SplitMenuBox smb = new SplitMenuBox(gc, 1f, 0.4f);
		mb = smb;
		mbSkills = new SimpleMenuBox(smb, SplitMenuBox.Location.topLeft);
		skillsList = new MenuItemList<>(mbSkills, true, 'X', 'S');
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
		
		if(in.isKeyPressed(InputMap.A_INV_UP) || in.getMouseWheelMove() > 0) skillsList.scroll(-1);
		if(in.isKeyPressed(InputMap.A_INV_DOWN) || in.getMouseWheelMove() < 0) skillsList.scroll(1);
		skillsList.updateSelection(mx, my);
		selectedSkill = skillsList.getSelectedObject();
		if(selectedSkill != null){
			if(in.isMousePressed(InputMap.A_SELECTSKILL) && selectedSkill.canLevelUp()){
				selectedSkill.levelUp();
				// colors of all skills may change -> repopulate the list
				populateList();
			}
			if(in.isMousePressed(InputMap.A_TRACKSKILL) && player != null
					&& !selectedSkill.isMaxed()){
				player.toggleTrackSkill(selectedSkill);
				skillsList.setItemFlag(selectedSkill, 0, player != null && player.isTrackedSkill(selectedSkill));
			}
		}
	}

	@Override
	public void render(GameController ctrl, GameRenderer r, GameContainer gc) throws SlickException {
		mb.render();

		// render description
		int x = mbDescr.getUsableX();
		int y = mbDescr.getUsableY();
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
			String[] sa = r.wordWrapString("hover over a skill to see a detailed description.", mbDescr.getUsableWidth());
			for(String s : sa){
				r.drawStringOnScreen(s, x, y, GameRenderer.COLOR_TEXT_INACTIVE, 1f); y += DESC_LINE_HEIGHT;
			}
			return;
		}
		Skill s = selectedSkill;
		Skill.SkillAvailability av = s.getSkillAvailability();
		int cost = s.getCost();
		r.drawStringOnScreen(s.name, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
		str = "current level: " + s.getLevel() + "/" + s.getMaxLevel();
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
		if(player != null && !selectedSkill.isMaxed()){
			if(player.isTrackedSkill(s)){
				str = "right click to track this skill";
			} else {
				str = "right click to untrack this skill";
			}
			r.drawStringOnScreen(str, x, y, GameRenderer.COLOR_TEXT_NORMAL, 1f); y += DESC_LINE_HEIGHT;
		}
		y += DESC_LINE_HEIGHT;
		String[] descr = r.wordWrapString(s.description, mbDescr.getUsableWidth());
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
	
}
