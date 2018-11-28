/*
 * UK Exactly
 * Author: Nouman Tayyab
 * Version: 0.1.6
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the weareavp
 * Support: info@weareavp.com
 * Copyright 2015 weareavp
 */
package uk.sipperfly.utils;

import com.jcraft.jsch.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import uk.sipperfly.ui.Exactly;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rimsha@geeks
 */
public class SFTPUtil {

	public String host;
	public String username;
	public String password;
	public int port;
	public String mode;
	public String destination;
	public String privateKey;
	public String passPhrase;
	public int type;
	private final Exactly parent;
	public Session session = null;
	public Channel channel = null;
	public ChannelSftp channelSftp = null;
	public JSch jsch;

	public SFTPUtil(Exactly parent, String host, String username, String password, int port, int type, String destination, String privateKey, String passPhrase) {
		if (parent == null || host == null || host.length() < 1 || username == null || username.length() < 1) {
			throw new IllegalArgumentException();
		}
		this.parent = parent;
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.destination = destination;
		this.type = type;
		this.jsch = new JSch();
		this.privateKey = privateKey;
		this.passPhrase = passPhrase;
	}

	public String validateCon() {
		if (this.connect()) {
			if (this.channelSftp.isConnected()) {
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "SFTP channel opened and connected");
				this.channelSftp.exit();
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "SFTP Channel exited.");
				System.out.println("sftp Channel exited.");
				this.channel.disconnect();
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "SFTP Channel disconnected.");
				System.out.println("Channel disconnected.");
				this.session.disconnect();
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "SFTP Session disconnected.");
				System.out.println("Host Session disconnected.");
			}
		} else {
			return "false";
		}
		return "true";
	}

	public boolean uploadFiles(File src) {
		this.connect();
		if (!upload(src)) {
			return false;
		}
		try {
			if (this.channelSftp.isConnected()) {
				this.channelSftp.exit();
				this.channel.disconnect();
				this.session.disconnect();
				System.out.println("Host Session disconnected.");
			}
		} catch (NullPointerException ex) {
			Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return true;
	}

	public boolean upload(File src) {
		try {
			if (!this.channelSftp.isConnected()) {
				this.connect();
			}
		} catch (NullPointerException ex) {
			Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
			this.connect();
		}
		if (src.isDirectory()) {
			try {
				System.out.println("current dir1: " + this.channelSftp.pwd());
				this.channelSftp.mkdir(src.getName());
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "Directory created: ".concat(src.getName()));
				this.channelSftp.cd(src.getName());
				System.out.println("current dir2: " + this.channelSftp.pwd());
				System.out.println("this.destination1 == " + this.destination);
				if (this.destination.equals("/")) {
					this.destination += src.getName();
				} else if (this.destination.endsWith("/")) {
					this.destination += src.getName();
				} else {
					this.destination += "/" + src.getName();
				}
				System.out.println("this.destination2 == " + this.destination);
				for (File file : src.listFiles()) {
					upload(file);
				}
				this.destination = this.destination.substring(0, this.destination.lastIndexOf('/'));
				System.out.println("this.destination == " + this.destination);
				channelSftp.cd("..");
			} catch (SftpException ex) {
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			}

		} else {
			try {
				System.out.println("uploading src file == " + src.getAbsolutePath());
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "Uploading src file: ".concat(src.getAbsolutePath()));
				this.channelSftp.put(new FileInputStream(src), src.getName());
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.INFO, "Uploaded src file: ".concat(src.getAbsolutePath()));
				int progress = this.parent.jProgressBar2.getValue();
				this.parent.jProgressBar2.setValue(progress + 1);
			} catch (SftpException ex) {
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			} catch (FileNotFoundException ex) {
				Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
				return false;
			}
		}
		return true;
	}

	private boolean connect() {
		try {
			if (this.port <= 0) {
				this.port = 22;
			}

			if (this.type == 0) {
				this.session = this.jsch.getSession(this.username, this.host, this.port);
				this.session.setPassword(this.password);
			} else {
				this.jsch.addIdentity(this.privateKey, this.passPhrase);
				this.session = this.jsch.getSession(this.username, this.host, this.port);
			}
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			this.session.setConfig(config);
			this.session.connect();
			System.out.println("Host connected.");
			this.channel = this.session.openChannel("sftp");
			this.channel.connect();
			System.out.println("sftp channel opened and connected.");
			this.channelSftp = (ChannelSftp) this.channel;
			if (this.destination.isEmpty()) {
				this.destination = "/";
			}
			this.channelSftp.cd(this.destination);
		} catch (JSchException ex) {
			Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (SftpException ex) {
			Logger.getLogger(SFTPUtil.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
		return true;
	}

}
