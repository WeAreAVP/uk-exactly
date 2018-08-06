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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class EmailEntry extends JPanel {
	private JLabel label;
	private JTextField textField;
	private JTextField valueField;
	private JTextField id;
	private JButton minus;
	private EmailList parent;

	/**
	 * Create panel with label, jTextField and button which is dynamically added to UI.
	 * 
	 * @param value input field value
	 * @param id id if already existed in db
	 * @param list list of all entries in parent panel
	 * @param size size of list
	 */
	public EmailEntry(String value, String id, EmailList list, int size) {
		this.label = new JLabel("Email Address");
		this.label.setFont(new java.awt.Font("Verdana", 0, 13));
		this.label.setAlignmentX((float) 0.0);
		this.label.setAlignmentY((float) 0.5);
		this.label.setBounds(6, 6, 72, 28);
		
		
		this.textField = new JTextField();
		this.textField.setPreferredSize(new Dimension(303, 34));
		this.textField.setSize(303, 32);	
		this.textField.setText(value);
		this.textField.setFont(new java.awt.Font("Verdana", 0, 13));
		this.textField.setMargin(new Insets(2,2,2,2));
		

		this.minus = new JButton(new RemoveEntryAction());
		this.minus.setFont(new java.awt.Font("Verdana", 0, 13));
		
		this.parent = list;
		
		this.id = new JTextField(5);
		this.id.setText(id);
		this.id.setVisible(false);
		
		add(this.label);
		add(this.textField);	
		add(this.minus);	
		
		this.setVisible(true);
		this.setPreferredSize(new Dimension(515, 38));
		
		int y_axis;
		if (size == 0) {
			y_axis = 0;
		} else {
			y_axis = size * 38;
		}
		this.setBounds(0, y_axis, 515, 38);
	}

	/**
	 * 
	 * @return label
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * 
	 * @return value of input field
	 */
	public String getValue() {
		return textField.getText();
	}
	
	/**
	 * 
	 * @return id
	 */
	public String getId() {
		return id.getText();
	}
	
	/**
	 * Set db id of email
	 * 
	 * @param id 
	 */
	public void setId(String id) {
		this.id.setText(id);
	}

	/**
	 * Remove entry from UI when - button is clicked
	 */
	public class RemoveEntryAction extends AbstractAction {

		public RemoveEntryAction() {
			super("-");
		}

		public void actionPerformed(ActionEvent e) {
			parent.removeItem(EmailEntry.this);
		}
	}
	
	/**
	 * Enable - button in UI.
	 * 
	 * @param enabled 
	 */
	public void enableMinus(boolean enabled) {
		this.minus.setEnabled(enabled);
	}
}
