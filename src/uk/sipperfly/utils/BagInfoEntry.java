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
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

public class BagInfoEntry extends JPanel {

	private JLabel label;
	private JLabel value;
	private JTextField labelField;
	private JTextArea valueField;
	private JTextField id;
	private JButton minus;
	private BagInfoList parent;
	private JScrollPane scrollPane;
	private JSeparator separator;

	/**
	 * Create panel with label, jTextFields and button which is dynamically added to UI.
	 *
	 * @param label value of label field
	 * @param value input field value
	 * @param id    id if already existed in db
	 * @param list  list of all entries in parent panel
	 * @param size  size of list
	 */
	public BagInfoEntry(String label, String value, String id, BagInfoList list, int size) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));

		this.label = new JLabel("Label");
		this.label.setFont(new java.awt.Font("Verdana", 0, 13));
		this.label.setAlignmentX((float) 0.0);
		this.label.setAlignmentY((float) 0.5);
		this.label.setBounds(6, 6, 72, 28);

		this.value = new JLabel("Value");
		this.value.setFont(new java.awt.Font("Verdana", 0, 13));
		this.value.setAlignmentX((float) 0.0);
		this.value.setAlignmentY((float) 0.5);
		this.value.setBounds(6, 6, 72, 28);

		this.separator = new JSeparator(JSeparator.HORIZONTAL);
		this.separator.setPreferredSize(new Dimension(550, 2));

		this.minus = new JButton(new RemoveEntryAction());
		this.minus.setFont(new java.awt.Font("Verdana", 0, 13));
		this.minus.setPreferredSize(new Dimension(41, 35));

		this.parent = list;

		this.labelField = new JTextField();
		this.labelField.setPreferredSize(new Dimension(450, 35));
		this.labelField.setSize(203, 30);
		this.labelField.setText(label);
		this.labelField.setFont(new java.awt.Font("Verdana", 0, 13));
		this.labelField.setMargin(new Insets(2, 2, 2, 2));

		this.id = new JTextField(5);
		this.id.setText(id);
		this.id.setVisible(false);

		this.valueField = new JTextArea();
		this.valueField.setSize(203, 30);
		this.valueField.setText(value);
		this.valueField.setFont(new java.awt.Font("Verdana", 0, 13));
		this.valueField.setMargin(new Insets(2, 2, 2, 2));
		this.valueField.setLineWrap(true);
		this.valueField.setWrapStyleWord(true);
		this.scrollPane = new JScrollPane(this.valueField);
		this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scrollPane.setPreferredSize(new Dimension(450, 57));

		add(this.label);
		add(this.labelField);
		add(this.minus);
		add(this.value);
		add(this.scrollPane);
		add(this.separator);

		this.setVisible(true);
		this.setPreferredSize(new Dimension(571, 120));

		int y_axis;
		if (size == 0) {
			y_axis = 0;
		} else {
			y_axis = size * 120;
		}
		this.setBounds(0, y_axis, 571, 120);
		InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK), DefaultEditorKit.selectAllAction);
	}

	/**
	 *
	 * @return label
	 */
	public JLabel getLabelField() {
		return label;
	}

	/**
	 *
	 * @return value of value of input field
	 */
	public String getValue() {
		return valueField.getText();
	}

	/**
	 *
	 * @return the value of label input field
	 */
	public String getLabel() {
		return labelField.getText();
	}

	/**
	 *
	 * @return id
	 */
	public String getId() {
		return id.getText();
	}

	/**
	 * Set id
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
			parent.removeItem(BagInfoEntry.this);
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