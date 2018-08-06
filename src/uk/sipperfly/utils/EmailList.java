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


public class EmailList extends JPanel {

	private List<EmailEntry> entries;
	private final Exactly parent;

	public EmailList(Exactly frame) {
		this.entries = new ArrayList<EmailEntry>();
		this.parent = frame;

	}

	/**
	 * Dynamically add email field on Email Notification screen
	 */
	public void addEntry() {
		int size = entries.size();
		EmailEntry initial = new EmailEntry("", "", this, size);
		addItem(initial);
	}

	/**
	 * Add entries during runtime
	 *
	 * @param value text for input filed
	 * @param id    database id for email
	 */
	public void editEntry(String value, String id) {
		int size = entries.size();
		EmailEntry theClone = new EmailEntry(value, id, this, size);
		addItem(theClone);
	}

	/**
	 * Add panel in list as well as in UI
	 *
	 * @param entry
	 */
	private void addItem(EmailEntry entry) {
		entries.add(entry);
		if (entry.getY() + 40 > this.parent.jPanel2.getPreferredSize().height) {
			int height = this.parent.jPanel2.getPreferredSize().height + 38;
			this.parent.jPanel2.setPreferredSize(new Dimension(515, height));
			int pheight = this.parent.jPanel7.getPreferredSize().height + 38;
			this.parent.jPanel7.setPreferredSize(new Dimension(515, pheight));
		}
		this.parent.jPanel2.add(entry);
		refresh();
	}

	/**
	 * Remove panel from UI
	 *
	 * @param entry
	 */
	public void removeItem(EmailEntry entry) {
		int index = entries.indexOf(entry) + 1;
		this.resetBounds(index);
		entries.remove(entry);
		this.parent.jPanel2.remove(entry);
		int height = this.parent.jPanel2.getPreferredSize().height - 38;
		this.parent.jPanel2.setPreferredSize(new Dimension(515, height));
		int pheight = this.parent.jPanel7.getPreferredSize().height - 38;
		this.parent.jPanel7.setPreferredSize(new Dimension(515, pheight));
		refresh();
	}

	/**
	 * Revalidate whole panel
	 */
	private void refresh() {
		this.parent.jPanel7.revalidate();
		this.parent.jPanel2.revalidate();
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
			entries.get(i).setBounds(0, newYAxis, 515, 38);
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
	 * Get email id from input field
	 *
	 * @param e object
	 * @return text of input field
	 */
	public String getJfieldTextValue(Object e) {
		EmailEntry entry = (EmailEntry) e;
		return entry.getValue();
	}

	/**
	 * Get id
	 *
	 * @param e object
	 * @return text id of email
	 */
	public String getJfieldTextId(Object e) {
		EmailEntry entry = (EmailEntry) e;
		return entry.getId();
	}

	/**
	 * Set db id for object
	 *
	 * @param e
	 * @param id
	 */
	public void setJFieldId(Object e, String id) {
		EmailEntry entry = (EmailEntry) e;
		entry.setId(id);
	}

	/**
	 * Reset UI and emailList
	 */
	public void resetEntryList() {
		for (EmailEntry e : entries) {
			this.parent.jPanel2.remove(e);
			int height = this.parent.jPanel2.getPreferredSize().height - 38;
			this.parent.jPanel2.setPreferredSize(new Dimension(515, height));
			int pheight = this.parent.jPanel7.getPreferredSize().height - 38;
			this.parent.jPanel7.setPreferredSize(new Dimension(515, pheight));
		}
		entries = new ArrayList<EmailEntry>();
		refresh();
	}
}
