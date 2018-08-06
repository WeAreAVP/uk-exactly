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
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class Entry extends JPanel {

	private JLabel label;
	private JTextField textField;
	private JButton browse;
	private JButton minus;
	private EntryList parent;

	/**
	 * Create panel with label, jTextField and buttons which is dynamically added to UI.
	 * 
	 * @param list list of all entries in parent panel
	 * @param size size of list
	 * @param text input text of JTextField
	 */
	public Entry(EntryList list, int size, String text) {
		this.label = new JLabel("Source");
		this.label.setFont(new java.awt.Font("Verdana", 0, 13));
		this.label.setAlignmentX((float) 0.0);
		this.label.setAlignmentY((float) 0.5);
		this.label.setBounds(49, 104, 60, 17);
		this.label.setPreferredSize(new Dimension(96, 17));

		this.browse = new JButton(new AddEntryAction());
		this.minus = new JButton(new RemoveEntryAction());
		this.parent = list;

		this.browse.setFont(new java.awt.Font("Verdana", 0, 13));
		this.minus.setFont(new java.awt.Font("Verdana", 0, 13));

		this.textField = new JTextField();
		this.textField.setPreferredSize(new Dimension(303, 30));
		this.textField.setSize(303, 32);
		this.textField.setText(text);
		this.textField.setFont(new java.awt.Font("Verdana", 0, 13));
		this.textField.setMargin(new Insets(2, 2, 2, 2));
		this.textField.setEditable(false);


		add(this.label);
		add(this.textField);
		add(this.browse);
		add(this.minus);
		this.setVisible(true);
		this.setPreferredSize(new Dimension(571, 38));

		int y_axis;
		if (size == 0) {
			y_axis = 0;
		} else {
			y_axis = size * 38;
		}
		this.setBounds(0, y_axis, 571, 38);
	}

	/**
	 * 
	 * @return value of label
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * 
	 * @return value of input field
	 */
	public String getTextField() {
		return textField.getText();
	}

	/**
	 * File chooser action performed when browse button in the panel clicked
	 */
	public class AddEntryAction extends AbstractAction {

		private final JFileChooser fileChooser;

		public AddEntryAction() {
			super("Browse");
			fileChooser = new javax.swing.JFileChooser();
		}

		public void actionPerformed(ActionEvent e) {

			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			int returnVal = fileChooser.showOpenDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				String inputDirPath = file.getAbsolutePath();
				textField.setText(inputDirPath);
			}
		}
	}

	/**
	 * Remove entry from UI when - button is clicked
	 */
	public class RemoveEntryAction extends AbstractAction {

		public RemoveEntryAction() {
			super("-");
		}

		public void actionPerformed(ActionEvent e) {
			parent.removeItem(Entry.this);
		}
	}

	/**
	 * Enable browse button in UI.
	 * 
	 * @param enabled 
	 */
	public void enableAdd(boolean enabled) {
		this.browse.setEnabled(enabled);
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