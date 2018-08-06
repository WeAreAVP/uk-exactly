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
 */
package uk.sipperfly.ui;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import uk.sipperfly.persistent.BagInfo;
import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.persistent.DefaultTemplate;
import uk.sipperfly.persistent.FTP;
import uk.sipperfly.persistent.Recipients;
import uk.sipperfly.persistent.SFTP;
import uk.sipperfly.repository.BagInfoRepo;
import uk.sipperfly.repository.ConfigurationsRepo;
import uk.sipperfly.repository.DefaultTemplateRepo;
import uk.sipperfly.repository.RecipientsRepo;
import uk.sipperfly.repository.FTPRepo;
import uk.sipperfly.repository.SFTPRepo;
import uk.sipperfly.utils.CommonUtil;
import uk.sipperfly.utils.SFTPUtil;

/**
 *
 * @author noumantayyab
 */
public class UIManager {

	ConfigurationsRepo configurationsRepo;
	RecipientsRepo recipientsRepo;
	BagInfoRepo bagInfoRepo;
	Exactly mainFrame;
	FTPRepo FTPRepo;
	CommonUtil commonUtil;
	SFTPRepo sftpRepo;
	DefaultTemplateRepo defaultTemplateRepo;
	SFTPUtil sftp;

	/**
	 * Constructor for UIManager.
	 *
	 * @param mainFrame
	 */
	public UIManager(Exactly mainFrame) {
		this.FTPRepo = new FTPRepo();
		this.configurationsRepo = new ConfigurationsRepo();
		this.recipientsRepo = new RecipientsRepo();
		this.bagInfoRepo = new BagInfoRepo();
		this.mainFrame = mainFrame;
		this.commonUtil = new CommonUtil();
		this.defaultTemplateRepo = new DefaultTemplateRepo();
		this.sftpRepo = new SFTPRepo();
//		this.bag_size = new int[1];
	}

	/**
	 * Set email settings to UI.
	 *
	 */
	public void setTemplate() {
		DefaultTemplate template = this.defaultTemplateRepo.getOneOrCreateOne();
		if (!template.getCurrentTemplate().equals("")) {
			mainFrame.currentTemplate.setText("Current Template: ".concat(template.getCurrentTemplate()));
			mainFrame.clearTempButton.setVisible(true);
		}
	}

	/**
	 * Set email settings to UI.
	 *
	 */
	public void setConfigurationFields() {
		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
		mainFrame.mailServerField.setText(configurations.getServerName());
		mainFrame.userNameField.setText(configurations.getUsername());
		mainFrame.passwordField.setText(configurations.getPassword());
		mainFrame.serverPort.setText(configurations.getServerPort());
		if (configurations.getServerProtocol().equalsIgnoreCase("TLS")) {
			mainFrame.tlsProtocol.setSelected(true);
		} else if (configurations.getServerProtocol().equalsIgnoreCase("SSL")) {
			mainFrame.sslProtocol.setSelected(true);
		} else {
			mainFrame.noneProtocol.setSelected(true);
		}

		mainFrame.emailNotifications.setSelected(configurations.getEmailNotifications());
		mainFrame.editInputDir1.setText(configurations.getDropLocation());
//		mainFrame.bagSize.setText(String.valueOf(configurations.getSize()));
	}

	/**
	 * Set FTP settings to UI.
	 *
	 */
	public void setFtpFields() {
		FTP configurations = this.FTPRepo.getOneOrCreateOne();
		mainFrame.ftpHost.setText(configurations.getHostName());
		mainFrame.ftpUser.setText(configurations.getUsername());
		mainFrame.ftpPass.setText(configurations.getPassword());
		mainFrame.ftpPort.setText(String.valueOf(configurations.getPort()));
		if (configurations.getMode().equalsIgnoreCase("active")) {
			mainFrame.activeMode.setSelected(true);
		} else if (configurations.getMode().equalsIgnoreCase("passive")) {
			mainFrame.passiveMode.setSelected(true);
		}
		mainFrame.ftpDestination.setText(configurations.getDestination());
	}

	public void setSftpFields() {
		SFTP configurations = this.sftpRepo.getOneOrCreateOne();
		mainFrame.sftp_host.setText(configurations.getHost());
		mainFrame.sftp_user.setText(configurations.getUsername());
		mainFrame.sftp_pass.setText(configurations.getPassword());
		mainFrame.sftp_port.setText(String.valueOf(configurations.getPort()));
		mainFrame.sftp_dest.setText(configurations.getDestination());
		mainFrame.sftp_type.setSelectedIndex(configurations.getType());
		mainFrame.sftp_privateKey.setText(configurations.getPrivateKey());
		mainFrame.sftp_passPhrase.setText(configurations.getPassPhrase());
		if (configurations.getType() == 1) {
			mainFrame.sftp_connect.setVisible(true);
			this.mainFrame.jLabel22.setVisible(false);
			this.mainFrame.sftp_pass.setVisible(false);
		} else {
			mainFrame.sftp_connect.setVisible(false);
			this.mainFrame.jLabel22.setVisible(true);
			this.mainFrame.sftp_pass.setVisible(true);
		}
	}

	/**
	 * Restore the transfer path if user input invalid path.
	 */
	public void restoreTransferPath() {
		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
		mainFrame.editInputDir1.setText(configurations.getDropLocation());
//		mainFrame.bagSize.setText(String.valueOf(configurations.getSize()));
	}

	/**
	 * Set bag info to UI.
	 *
	 */
	public void setBagInfoFields(boolean condition) {
		int counter = 0;
		List<BagInfo> bagInfo = this.bagInfoRepo.getOneOrCreateOne();
		int size = bagInfo.size();
		this.mainFrame.idList = new int[size];
		for (BagInfo b : bagInfo) {
			try {
				this.mainFrame.bagInfo.editEntry(b.getLabel(), b.getValue(), String.valueOf(b.getId()));
				int x = b.getId().intValue();
				this.mainFrame.idList[counter] = x;
			} catch (NullPointerException p) {
				Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, p);
				System.out.println(p);
			}
			counter++;
		}
		if (condition) {
			this.mainFrame.bag_size = new int[size];
			System.arraycopy(this.mainFrame.idList, 0, this.mainFrame.bag_size, 0, size);
		}
	}

	/**
	 * Set email settings to UI.
	 *
	 */
	public void setEmailFields() {
		int counter = 0;
		List<Recipients> recipients = this.recipientsRepo.getAll();
		int size = recipients.size();
		this.mainFrame.emailIdList = new int[size];
		for (Recipients b : recipients) {
			try {
				this.mainFrame.email.editEntry(b.getEmail(), String.valueOf(b.getId()));
				int x = b.getId().intValue();
				this.mainFrame.emailIdList[counter] = x;
			} catch (NullPointerException p) {
				Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, p);
				System.out.println(p);
			}
			counter++;
		}
	}

	/**
	 * Save email settings.
	 */
	public boolean saveEmailSettings() {
		if (this.validateEmailSettings()) {
			Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
			configurations.setServerName(mainFrame.mailServerField.getText());
			configurations.setUsername(mainFrame.userNameField.getText());
			configurations.setPassword(new String(mainFrame.passwordField.getPassword()));
			configurations.setServerPort(mainFrame.serverPort.getText());
			configurations.setEmailNotifications(mainFrame.emailNotifications.isSelected());
			if (mainFrame.sslProtocol.isSelected()) {
				configurations.setServerProtocol("SSL");
			} else if (mainFrame.tlsProtocol.isSelected()) {
				configurations.setServerProtocol("TLS");
			} else if (mainFrame.noneProtocol.isSelected()) {
				configurations.setServerProtocol("None");
			}
			this.configurationsRepo.save(configurations);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Save location and filter to database.
	 *
	 * @param location String
	 * @param filter   String
	 */
	public void saveLocationAndFilter(String location, String filter) {

		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
		configurations.setFilters(filter);
		configurations.setDropLocation(location);

		this.configurationsRepo.save(configurations);
	}
	
	public void saveBagSize(){
		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
//		configurations.setSize(Integer.valueOf(mainFrame.bagSize.getText()));
		this.configurationsRepo.save(configurations);
	}

	/**
	 * Validate the email settings.
	 *
	 * @return boolean
	 */
	public Boolean validateEmailSettings() {
		String userName = mainFrame.userNameField.getText();
		String host = mainFrame.mailServerField.getText();
		String password = new String(mainFrame.passwordField.getPassword());
		String port = mainFrame.serverPort.getText();
		String protocol = "TLS";
		if (mainFrame.sslProtocol.isSelected()) {
			protocol = "SSL";
		} else if (mainFrame.tlsProtocol.isSelected()) {
			protocol = "TLS";
		} else if (mainFrame.noneProtocol.isSelected()) {
			protocol = "None";
		}
		if (userName.isEmpty() || host.isEmpty() || password.isEmpty()) {
			return false;
		}
		MailSender ms = new MailSender(host, userName, password, false, port, protocol);
		return ms.Validate();

	}

	/**
	 * Get the values from UI and save into baginfo entity.
	 *
	 * @return
	 */
	public boolean saveBagInfo() {
		this.checkDeletedInfo();
		BagInfo bagInfo;
		List l = this.mainFrame.bagInfo.getList();
		for (Object e : l) {
			if (this.mainFrame.bagInfo.getJfieldTextId(e) == "" || this.mainFrame.bagInfo.getJfieldTextId(e) == null || this.mainFrame.bagInfo.getJfieldTextId(e).isEmpty()) {
				bagInfo = new BagInfo();
			} else {
				bagInfo = this.bagInfoRepo.getOneRecord(Integer.parseInt(this.mainFrame.bagInfo.getJfieldTextId(e)));
			}
			bagInfo.setLabel(this.mainFrame.bagInfo.getJfieldTextLabel(e));
			bagInfo.setValue(this.mainFrame.bagInfo.getJfieldTextValue(e));
			this.bagInfoRepo.save(bagInfo);
			this.mainFrame.bagInfo.setJFieldId(e, String.valueOf(bagInfo.getId()));
		}
		int counter = 0;
		List<BagInfo> totalBagInfo = this.bagInfoRepo.getOneOrCreateOne();
		int size = totalBagInfo.size();
		this.mainFrame.idList = new int[size];
		for (BagInfo b : totalBagInfo) {
			try {
				int x = b.getId().intValue();
				this.mainFrame.idList[counter] = x;
			} catch (NullPointerException p) {
				Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, p);
				System.out.println(p);
			}
			counter++;
		}
		return true;
	}

	/**
	 * delete all metadata fields from db which have been removed from UI.
	 */
	private void checkDeletedInfo() {
		int chk;
		int[] ids = this.mainFrame.idList;
		List l = this.mainFrame.bagInfo.getList();
		for (int i = 0; i < this.mainFrame.idList.length; i++) {
			chk = 0;
			for (Object e : l) {
				if (this.mainFrame.bagInfo.getJfieldTextId(e) != "" && this.mainFrame.bagInfo.getJfieldTextId(e) != null && !this.mainFrame.bagInfo.getJfieldTextId(e).isEmpty()) {
					if (ids[i] == Integer.parseInt(this.mainFrame.bagInfo.getJfieldTextId(e))) {
						chk = 1;
					}
				}
			}
			if (chk == 0) {
				this.bagInfoRepo.deleteRecord(ids[i]);
			}
		}
	}

	/**
	 * delete all recipients from db which have been removed from UI.
	 */
	private void checkDeletedEmails() {
		if (this.mainFrame.emailIdList.length > 0) {
			int chk;
			int[] ids = this.mainFrame.emailIdList;
			List l = this.mainFrame.email.getList();
			for (int i = 0; i < this.mainFrame.emailIdList.length; i++) {
				if (ids[i] == 0) {
					break;
				}
				chk = 0;
				for (Object e : l) {
					if (this.mainFrame.email.getJfieldTextId(e) != "" && this.mainFrame.email.getJfieldTextId(e) != null && !this.mainFrame.email.getJfieldTextId(e).isEmpty()) {
						if (ids[i] == Integer.parseInt(this.mainFrame.email.getJfieldTextId(e))) {
							chk = 1;
						}
					}
				}

				if (chk == 0) {
					this.recipientsRepo.deleteRecord(ids[i]);
				}
				ids[i] = 0;
			}
		}
	}

	/**
	 * Save Recipients Email address to recipient Entity.
	 *
	 * @return
	 */
	public String saveRecipientEmail() {
		this.checkDeletedEmails();
		Recipients recipient;
		List l = this.mainFrame.email.getList();
		for (Object e : l) {
			if (this.mainFrame.email.getJfieldTextId(e) == "" || this.mainFrame.email.getJfieldTextId(e) == null || this.mainFrame.email.getJfieldTextId(e).isEmpty()) {
				recipient = this.recipientsRepo.getOneOrCreateOne("");
			} else {
				recipient = this.recipientsRepo.getOneRecord(Integer.parseInt(this.mainFrame.email.getJfieldTextId(e)));
			}
			recipient.setEmail(this.mainFrame.email.getJfieldTextValue(e));
			this.recipientsRepo.save(recipient);
			this.mainFrame.email.setJFieldId(e, String.valueOf(recipient.getId()));
		}
		int counter = 0;
		List<Recipients> totalRec = this.recipientsRepo.getAll();
		int size = totalRec.size();
		this.mainFrame.emailIdList = new int[size];

		for (Recipients b : totalRec) {
			try {
				int x = b.getId().intValue();
				this.mainFrame.emailIdList[counter] = x;
			} catch (NullPointerException p) {
				Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, p);
				System.out.println(p);
			}
			counter++;
		}
		return "Saved successfully.";
	}

	/**
	 * validate email id
	 *
	 * @return
	 */
	public boolean validateEmails() {
		String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w-]+\\.)+[\\w]+[\\w]$";
		List<String> emails = new ArrayList<String>();
		List l = this.mainFrame.email.getList();
		for (Object e : l) {
			if (this.mainFrame.email.getJfieldTextValue(e) != null && this.mainFrame.email.getJfieldTextValue(e) != "") {
				emails.add(this.mainFrame.email.getJfieldTextValue(e));
			}
		}
		for (String n : emails) {
			if (!n.matches(EMAIL_REGEX)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * get the destination of transfer.
	 *
	 * @return
	 */
	public String getDestinationPath() {
		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
		return configurations.getDropLocation();
	}

	/**
	 * Save ftp settings after validation in db.
	 *
	 * @return
	 * @throws IOException
	 */
	public boolean saveFTPSettings() throws IOException {
		String result = this.validateFTPSettings();
		if (result.equals("FTPES") || result.equals("FTP")) {

			FTP ftp = this.FTPRepo.getOneOrCreateOne();
			ftp.setHostName(mainFrame.ftpHost.getText());
			ftp.setUsername(mainFrame.ftpUser.getText());
			ftp.setPassword(new String(mainFrame.ftpPass.getPassword()));
			ftp.setDestination(mainFrame.ftpDestination.getText());
			ftp.setSecurityType(result);
			int port = Integer.parseInt(mainFrame.ftpPort.getText());
			ftp.setPort(port);
			if (mainFrame.activeMode.isSelected()) {
				ftp.setMode("active");
			} else {
				ftp.setMode("passive");
			}
			this.FTPRepo.save(ftp);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * validate ftp settings.
	 *
	 * @return
	 * @throws IOException
	 */
	public String validateFTPSettings() throws IOException {
		String userName = mainFrame.ftpUser.getText();
		String host = mainFrame.ftpHost.getText();
		String destination = mainFrame.ftpDestination.getText();
		String password = new String(mainFrame.ftpPass.getPassword());
		int port = 21;

		try {
			port = Integer.parseInt(mainFrame.ftpPort.getText());
		} catch (java.lang.NumberFormatException ex) {
			Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		}
		String mode = "passive";
		if (mainFrame.passiveMode.isSelected()) {
			mode = "passive";
		} else if (mainFrame.activeMode.isSelected()) {
			mode = "active";
		}

		if (userName.isEmpty() || host.isEmpty() || password.isEmpty()) {
			return "false";
		}
		FTPConnection ftp = new FTPConnection(this.mainFrame, host, userName, password, port, mode, destination, "FTPES");
		return ftp.validateCon();

	}

	/**
	 * get source paths which are dynamically added to UI
	 *
	 * @return
	 */
	public List getInputDirectories() {
		List<String> directories = new ArrayList<String>();
		List l = this.mainFrame.list.getList();
		for (Object e : l) {
			if (this.mainFrame.list.getJfieldText(e) != null && this.mainFrame.list.getJfieldText(e) != "") {
				directories.add(this.mainFrame.list.getJfieldText(e));
			}
		}
		return directories;
	}

	/**
	 * Validate Metadata fields both label and field value are required
	 *
	 * @return
	 */
	public boolean validateBageInfo() {
		List l = this.mainFrame.bagInfo.getList();
		for (Object e : l) {
			if (this.mainFrame.bagInfo.getJfieldTextLabel(e) == null || this.mainFrame.bagInfo.getJfieldTextLabel(e) == "" || this.mainFrame.bagInfo.getJfieldTextLabel(e).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Export exactly info saved in db in xml
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws Exception
	 */
	public boolean exportInfo(String path) throws IOException, ParserConfigurationException, Exception {
		List<BagInfo> bagInfo = this.bagInfoRepo.getOneOrCreateOne();
		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
		List<Recipients> recipients = this.recipientsRepo.getAll();
		FTP ftp = this.FTPRepo.getOneOrCreateOne();
		SFTP sftp = this.sftpRepo.getOneOrCreateOne();
		this.commonUtil.createXMLExport(recipients, ftp, sftp, configurations, bagInfo, path, false);
		return true;
	}

	/**
	 * show fields of metadata
	 */
	public void enableORdisableFields() {
		List<String> allFields = new ArrayList<String>();
		allFields.add("Source-Organization");
		allFields.add("Organization-Address");
		allFields.add("Contact-Name");
		allFields.add("Contact-Phone");
		allFields.add("Contact-Email");
		allFields.add("External-Description");
		allFields.add("External-Identifier");
		allFields.add("Bag-Group-Identifier");
		allFields.add("Bag-Count");
		allFields.add("Internal-Sender-Identifier");
		allFields.add("Internal-Sender-Description");
		List l = this.mainFrame.bagInfo.getList();
		int check;
		if (this.mainFrame.enableBagFields.isSelected()) {

			for (String s : allFields) {
				check = 0;
				for (Object e : l) {
					if (this.mainFrame.bagInfo.getJfieldTextLabel(e).equalsIgnoreCase(s)) {
						check = 1;
					}
				}
				if (check == 0) {
					this.mainFrame.bagInfo.addEntry(s, "");
				}
			}
		}
	}

	/**
	 * import all info from xml and show changes in UI.
	 *
	 * @param path
	 * @return
	 */
	public String importXml(String path) {
		this.mainFrame.jPanel11.setVisible(false);
		DefaultTemplate defaultTemplate = this.defaultTemplateRepo.getOneOrCreateOne();
		if (defaultTemplate.getTemplate().equals("")) {
			File file = new File(path);
			List<BagInfo> bagInfo = this.bagInfoRepo.getOneOrCreateOne();
			Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
			List<Recipients> recipients = this.recipientsRepo.getAll();
			FTP ftp = this.FTPRepo.getOneOrCreateOne();
			SFTP sftp = this.sftpRepo.getOneOrCreateOne();
			defaultTemplate.setCurrentTemplate(file.getName());
			defaultTemplate.setTemplate(this.commonUtil.createXMLExport(recipients, ftp, sftp, configurations, bagInfo, "", true));
			this.defaultTemplateRepo.save(defaultTemplate);
		} else {
			File file = new File(path);
			defaultTemplate.setCurrentTemplate(file.getName());
			this.defaultTemplateRepo.save(defaultTemplate);
		}
		String result = this.commonUtil.importXML(path, "");
		if (!result.equals("")) {
			this.mainFrame.bagInfo.resetEntryList();
			this.mainFrame.email.resetEntryList();
			this.setBagInfoFields(false);
			this.setConfigurationFields();
			this.setFtpFields();
			this.setEmailFields();
			try {
				Thread.sleep(3000);
			} catch (InterruptedException ex) {
				Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, ex);
			}
			List<BagInfo> bagInfo = this.bagInfoRepo.getOneOrCreateOne();
			this.mainFrame.bag_size = new int[bagInfo.size()];
			if (bagInfo.size() > 0) {
				int counter = 0;
				for (BagInfo bag : bagInfo) {
					this.mainFrame.bag_size[counter] = bag.getId().intValue();
					counter++;
				}
				this.mainFrame.hideTransfer.setVisible(true);
				this.mainFrame.show.setVisible(true);
				this.mainFrame.hide.setVisible(false);
				this.mainFrame.showTransfer.setVisible(false);
				this.mainFrame.jPanel11.setVisible(true);
				this.mainFrame.note.setVisible(true);
			}
			return result;
		}
		return "Invalid xml format";
	}

	public void resetDefaultTemplate() {
		this.mainFrame.jPanel11.setVisible(false);
		DefaultTemplate defaultTemplate = this.defaultTemplateRepo.getOneOrCreateOne();
		String result = this.commonUtil.importXML("", defaultTemplate.getTemplate());
		if (!result.equals("")) {
			this.mainFrame.bagInfo.resetEntryList();
			this.mainFrame.email.resetEntryList();
			this.setBagInfoFields(false);
			this.setConfigurationFields();
			this.setFtpFields();
			this.setEmailFields();
			this.mainFrame.jPanel11.setVisible(false);
			this.mainFrame.hideTransfer.setVisible(false);
			this.mainFrame.show.setVisible(false);
			this.mainFrame.hide.setVisible(true);
			this.mainFrame.showTransfer.setVisible(true);
			this.mainFrame.note.setVisible(false);
		}
		this.defaultTemplateRepo.truncate();
	}

	public void saveEmailNotification() {
		Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
		configurations.setEmailNotifications(mainFrame.emailNotifications.isSelected());
		this.configurationsRepo.save(configurations);
	}

	public void resetMetadataValues(boolean condition) {
		List<BagInfo> bagInfo = this.bagInfoRepo.getOneOrCreateOne();
		for (BagInfo b : bagInfo) {
			b.setValue("");
			this.bagInfoRepo.save(b);
		}
		if (condition) {
			this.mainFrame.bagInfo.resetEntryList();
			this.setBagInfoFields(false);
			List<BagInfo> bagInfo1 = this.bagInfoRepo.getOneOrCreateOne();
			if (bagInfo1.size() > 0) {
				this.mainFrame.hideTransfer.setVisible(true);
				this.mainFrame.show.setVisible(true);
				this.mainFrame.hide.setVisible(false);
				this.mainFrame.showTransfer.setVisible(false);
				this.mainFrame.jPanel11.setVisible(true);
				this.mainFrame.note.setVisible(true);
			}
		}
	}

	public void resetMetadata(boolean condition) {
		if (this.mainFrame.bag_size.length > 0) {
			int[] ids = new int[this.mainFrame.bag_size.length];
			System.arraycopy(this.mainFrame.bag_size, 0, ids, 0, this.mainFrame.bag_size.length);
			String myIds = "(";
			for (int i = 0; i < ids.length; i++) {
				if (i == ids.length - 1) {
					myIds = myIds + ids[i] + ")";
				} else {
					myIds = myIds + ids[i] + ",";
				}
			}

			this.bagInfoRepo.deleteRecordById(myIds);
			if (condition) {
				this.mainFrame.bagInfo.resetEntryList();
				this.setBagInfoFields(false);
//				try {
//					Thread.sleep(1000);
//				} catch (InterruptedException ex) {
//					Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, ex);
//				}
				List<BagInfo> bagInfo = this.bagInfoRepo.getOneOrCreateOne();
				if (bagInfo.size() > 0) {
					this.mainFrame.hideTransfer.setVisible(true);
					this.mainFrame.show.setVisible(true);
					this.mainFrame.hide.setVisible(false);
					this.mainFrame.showTransfer.setVisible(false);
					this.mainFrame.jPanel11.setVisible(true);
					this.mainFrame.note.setVisible(true);
				}
			}
		}
	}

	public boolean isDefaultTemplate() {
		DefaultTemplate defaultTemplate = this.defaultTemplateRepo.getOneOrCreateOne();
		if (defaultTemplate.getTemplate().equals("")) {
			return true;
		}
		return false;
	}

	public boolean saveSFTPSettings() throws IOException {
		String result = this.validateSFTPSettings();
		if (result.equals("true")) {
			SFTP Sftp = this.sftpRepo.getOneOrCreateOne();
			Sftp.setHost(this.mainFrame.sftp_host.getText());
			Sftp.setUsername(mainFrame.sftp_user.getText());
			Sftp.setDestination(mainFrame.sftp_dest.getText());
			Sftp.setType(this.mainFrame.sftp_type.getSelectedIndex());
			int port = Integer.parseInt(mainFrame.sftp_port.getText());
			Sftp.setPort(port);
			if (this.mainFrame.sftp_type.getSelectedIndex() == 1) {
				Sftp.setPrivateKey(this.mainFrame.sftp_privateKey.getText());
				Sftp.setPassPhrase(new String(mainFrame.sftp_passPhrase.getPassword()));
				Sftp.setPassword("");
			} else {
				Sftp.setPrivateKey("");
				Sftp.setPassPhrase("");
				Sftp.setPassword(new String(mainFrame.sftp_pass.getPassword()));
			}

			this.sftpRepo.save(Sftp);
			return true;
		} else {
			return false;
		}
	}

	public String validateSFTPSettings() throws IOException {
		String userName = mainFrame.sftp_user.getText();
		String host = mainFrame.sftp_host.getText();
		String destination = mainFrame.sftp_dest.getText();
		String password = new String(mainFrame.sftp_pass.getPassword());
		String privateKey = this.mainFrame.sftp_privateKey.getText();
		String passPhrase = new String(this.mainFrame.sftp_passPhrase.getPassword());
		int port = 22;
		int type = this.mainFrame.sftp_type.getSelectedIndex();
		try {
			port = Integer.parseInt(mainFrame.sftp_port.getText());
		} catch (java.lang.NumberFormatException ex) {
			Logger.getLogger(UIManager.class.getName()).log(Level.SEVERE, null, ex);
			return "false";
		}

		if (userName.isEmpty() || host.isEmpty()) {
			return "false";
		}
		this.sftp = new SFTPUtil(this.mainFrame, host, userName, password, port, type, destination, privateKey, passPhrase);
		return this.sftp.validateCon();

	}

	public String output = "";
	public boolean validateFolderName(File file) {
		StringBuilder invalidNames = new StringBuilder();
		if(file.isFile()){
			if(!this.validateTitle(file.getName())){
			invalidNames.append(file.getName());
					invalidNames.append(System.getProperty("line.separator"));
					this.output += invalidNames.toString();
		}
		}else if(file.isDirectory()){
			if(!this.validateTitle(file.getName())){
			invalidNames.append(file.getName());
					invalidNames.append(System.getProperty("line.separator"));
					this.output += invalidNames.toString();
		}
			for (File f : file.listFiles()) {
			this.validateFolderName(f);
			}
		}
		
		
		return true;
	}
	
	public boolean validateTitle(String name) {
		char[] charArray = {'<', '>', '"', '\\', '*', ':', '/', '?', '|'};
			char[] txt = name.toCharArray();
		for (int i = 0; i < name.length(); i++) {
			for (int j = 0; j < charArray.length; j++) {
				if (txt[i] == charArray[j]) {
					return false;
				}
			}
		}
		
		
		return true;
	}

}
