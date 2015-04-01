/*
 */
package hfk.menu;

import hfk.game.GameController;
import hfk.game.GameRenderer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import org.newdawn.slick.Color;

/**
 *
 * @author LostMekka
 */
public class MenuItemList<T> extends MenuBox {

	public static final int SELECTABLE_LINE_HEIGHT = 30;
	public static final int UNSELECTABLE_LINE_HEIGHT = 25;
	
	private class Item<T>{
		public T object;
		public String name;
		public Color color;
		public boolean[] flags;
		public Item(T object, String name, Color color, boolean[] flags) {
			this.object = object;
			this.name = name;
			this.color = color;
			this.flags = flags == null ? new boolean[0] : flags;
		}
	}
	
	private LinkedList<Item<T>> items = new LinkedList<>();
	private int scrollOffset = 0, selected = -1, lineHeight, flagCount;
	private char[] flags;
	boolean selectable;

	public MenuItemList(SimpleMenuBox parent, boolean selectable, char... flags) {
		super(parent);
		init(selectable, flags);
	}

	public MenuItemList(int x, int y, int w, int h, boolean selectable, char... flags) {
		super(x, y, w, h);
		init(selectable, flags);
	}
	
	private final void init(boolean selectable, char... flags){
		this.flags = flags == null ? new char[0] : flags;
		flagCount = this.flags.length;
		lineHeight = selectable ? SELECTABLE_LINE_HEIGHT : UNSELECTABLE_LINE_HEIGHT;
		this.selectable = selectable;
	}
	
	public void clearList(){
		items.clear();
	}
	
	public void addListItem(T object, String name, Color color, boolean... flags){
		items.add(new Item<T>(object, name, color, flags));
	}
	
	public void insertListItem(int pos, T object, String name, Color color, boolean... flags){
		items.add(pos, new Item<T>(object, name, color, flags));
		if(selected >= pos) selected++;
	}
	
	public void replaceListItem(T original, T object, String name, Color color, boolean... flags){
		ListIterator<Item<T>> iter = items.listIterator();
		while(iter.hasNext()) if(iter.next().object == object){
			iter.set(new Item<T>(object, name, color, flags));
			return;
		}
	}
	
	public void removeListItem(T object){
		ListIterator<Item<T>> iter = items.listIterator();
		int i = 0;
		while(iter.hasNext()){
			if(iter.next().object == object){
				iter.remove();
				if((selected == i && !iter.hasNext()) || selected > i) selected--;
				return;
			}
			i++;
		}
	}
	
	public void setItemName(T object, String name){
		getItem(object).name = name;
	}
	
	public void setItemColor(T object, Color color){
		getItem(object).color = color;
	}
	
	public void setItemFlag(T object, int index, boolean value){
		getItem(object).flags[index] = value;
	}
	
	public int getItemPosition(T object){
		int ans = 0;
		for(Item<T> i : items){
			if(i.object == object) return ans;
			ans++;
		}
		return -1;
	}
	
	private Item<T> getItem(T object){
		for(Item<T> i : items) if(i.object == object) return i;
		return null;
	}
	
	@Override
	public void render() {
		GameRenderer r = GameController.get().renderer;
		// render selection box if necessary
		if(selected != -1){
			r.drawMenuBox(
					getX(), 
					getY() + lineHeight * (selected - scrollOffset), 
					getWidth() - 6, 
					lineHeight - 1, 
					GameRenderer.COLOR_MENUITEM_BG, GameRenderer.COLOR_MENUITEM_LINE);
		}
		// render item names
		Iterator<Item<T>> iter = items.iterator();
		int n = 0, max = getMaxVisibleRowCount();
		for(int i=0; i<scrollOffset; i++) iter.next();
		while(iter.hasNext() && n < max){
			Item<T> i = iter.next();
			r.drawStringOnScreen(i.name, 
					8 + getX(), 
					7 + getY() + n*lineHeight, 
					i.color, 1f);
			n++;
		}
		// render scroll bar if necessary
		float size = items.size();
		if(max < size){
			float start = scrollOffset / size * getHeight();
			float ratio = max / size * getHeight();
			r.getGraphics().setColor(GameRenderer.COLOR_MENU_LINE);
			r.getGraphics().fillRect(getX() + getWidth() - 1, getY() + start, 5, ratio);
		}
	}

	public void scroll(int n){
		int max = getMaxVisibleRowCount();
		if(max > items.size()) return;
		scrollOffset += n;
		scrollOffset = Math.max(0, scrollOffset);
		scrollOffset = Math.min(items.size() - max, scrollOffset);
	}

	public void updateSelection(int mouseX, int mouseY){
		if(!isMouseInside(mouseX, mouseY)){
			selected = -1;
			return;
		}
		int y = getRelativeMouseY(mouseY);
		selected = y / lineHeight + scrollOffset;
		if(selected >= items.size()) selected = -1;
	}
	
	public boolean hasSelection(){
		return selected != -1;
	}
	
	public int getSelectedIndex(){
		return selected;
	}
	
	public T getSelectedObject(){
		if(selected == -1) return null;
		return items.get(selected).object;
	}
	
	public void selectIndex(int index){
		selected = (index >= items.size() || index < 0) ? -1 : index;
	}
	
	public void selectObject(T object){
		selected = 0;
		for(Item<T> i : items){
			if(i.object == object) return;
			selected++;
		}
		selected = -1;
	}
	
	public void moveSelection(int amount){
		if(selected == -1) return;
		selected += amount;
		if(selected < 0) selected += (1-selected/items.size()) * items.size();
		selected %= items.size();
		scrollToSelected();
	}
	
	public void selectNext(){
		moveSelection(1);
	}
	
	public void selectPrevious(){
		moveSelection(-1);
	}
	
	private void scrollToSelected(){
		if(selected == -1) return;
		if(selected < scrollOffset) scroll(selected - scrollOffset);
		int lastVisible = scrollOffset + getMaxVisibleRowCount() - 1;
		if(selected > lastVisible) scroll(selected - lastVisible);
	}
	
	public void unselect(){
		selected = -1;
	}
	
	public int getMaxVisibleRowCount(){
		return getHeight() / lineHeight;
	}

}
