/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.6
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@weareavp.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.utils;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import uk.sipperfly.ui.Exactly;


public class EntryList extends JPanel {

	private List<Entry> entries;
	private final Exactly parent;

	public EntryList(Exactly frame) {
		this.entries = new ArrayList<Entry>();
		this.parent = frame;

	}

	/**
	 * Dynamically add source field on deliver tab
	 * 
	 * @param text text of input field
	 */
	public void addEntry(String text) {
		int size = entries.size();
		Entry initial = new Entry(this, size, text);
		addItem(initial);
	}

	public void cloneEntry(Entry entry) {
		int size = entries.size();
		Entry theClone = new Entry(this, size, "");
		addItem(theClone);
	}

	/**
	 * Add panel in list as well as in UI
	 * 
	 * @param entry 
	 */
	private void addItem(Entry entry) {
		entries.add(entry);
		if (entry.getY() + 38 > this.parent.jPanel8.getPreferredSize().height) {
			int height = this.parent.jPanel8.getPreferredSize().height + 38;
			this.parent.jPanel8.setPreferredSize(new Dimension(571, height));
		}
		this.parent.jPanel8.add(entry);
		refresh();
	}

	/**
	 * Remove panel with source field from UI
	 * 
	 * @param entry 
	 */
	public void removeItem(Entry entry) {
		int index = entries.indexOf(entry) + 1;
		this.resetBounds(index);
		entries.remove(entry);
		this.parent.jPanel8.remove(entry);
		int height = this.parent.jPanel8.getPreferredSize().height - 38;
		this.parent.jPanel8.setPreferredSize(new Dimension(571, height));
		refresh();
	}

	/**
	 * Revalidate whole panel
	 */
	private void refresh() {
		this.parent.jPanel1.revalidate();
		this.parent.jPanel8.revalidate();
	}

	/**
	 * Reset Bounds of panel 
	 * 
	 * @param index 
	 */
	private void resetBounds(int index) {
		for (int i = index; i < entries.size(); i++) {
			int y = entries.get(i).getY();
			int newYAxis = y - 38;
			entries.get(i).setBounds(0, newYAxis, 571, 38);
		}
	}

	/**
	 * 
	 * @return list of entries dynamically added to UI
	 */
	public List getList() {
		return entries;
	}

	/**
	 * Get input Source value
	 * 
	 * @param e object
	 * @return text of input field
	 */
	public String getJfieldText(Object e) {
		Entry entry = (Entry) e;
		return entry.getTextField();
	}

	/**
	 * Reset UI and entryList
	 */
	public void resetEntryList() {
		for (Entry e : entries) {
			this.parent.jPanel8.remove(e);
			int height =this.parent.jPanel8.getPreferredSize().height - 38;
			this.parent.jPanel8.setPreferredSize(new Dimension(571, height));
		}		
		this.parent.jPanel1.revalidate();
		this.parent.jPanel8.revalidate();
		entries = new ArrayList<Entry>();
	}
}
