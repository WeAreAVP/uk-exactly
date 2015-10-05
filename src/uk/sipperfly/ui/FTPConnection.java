/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.sipperfly.ui;

import uk.sipperfly.utils.FTPUtil;
import java.io.File;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import static uk.sipperfly.ui.MainFrame.GACOM;

/**
 *
 * @author Rimsha Khalid(rimsha@avpreserve.com)
 */
public class FTPConnection {

	String host;
	String username;
	String password;
	int port;
	String mode;
	String destination;

	public FTPConnection(String host, String username, String password, int port, String mode, String destination) {
		if (host == null || host.length() < 1 || username == null || username.length() < 1
				|| password == null || password.length() < 1
				|| mode == null || mode.length() < 1) {
			throw new IllegalArgumentException();
		}
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
		this.mode = mode;
		this.destination = destination;
	}

	/**
	 * Connect with ftp with given info.
	 * 
	 * @return true if connected successfully, false otherwise
	 */
	public boolean validateCon() {
		try {
			String server = this.host;
			int port = 21;
			String user = this.username;
			String pass = this.password;
			FTPClient ftp = new FTPClient();

			ftp.connect(server, port);
			System.out.println("Connected to " + server + ".");
			if (ftp.login(user, pass)) {
				ftp.logout();
				if (ftp.isConnected()) {
					try {
						ftp.disconnect();
						return true;
					} catch (IOException ioe) {
						Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ioe);
						return true;
					}
				}
			} else {
				Logger.getLogger(GACOM).log(Level.INFO, "FTP Login: ", "Invalid username or password");
				return false;
			}

			return true;
		} catch (SocketException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}

	/**
	 * Upload files to ftp server
	 * 
	 * @param location of the folder which have to upload on ftp
	 * @param type zip file or folder
	 * @return 
	 */
	public boolean uploadFiles(String location, String type) {
		try {
			this.validateCon();
			FTPClient ftpClient = new FTPClient();
			ftpClient.connect(this.host, this.port);
			ftpClient.login(this.username, this.password);
			if (this.mode == "passive") {
				ftpClient.enterLocalPassiveMode();
			} else if (this.mode == "active") {
				ftpClient.enterLocalActiveMode();
			}
			int reply = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				Logger.getLogger(GACOM).log(Level.INFO, "FTP Login: ", ftpClient.getReplyString());
				ftpClient.disconnect();
				return false;
			}

			ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
			ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			String remoteFile;
			File localFile = new File(location);
			if (this.destination != null) {
				if (this.destination.endsWith("/")) {
					remoteFile = this.destination + localFile.getName();
				} else {
					remoteFile = this.destination + "/" + localFile.getName();
				}
			} else {
				if (ftpClient.printWorkingDirectory().endsWith("/")) {
					remoteFile = ftpClient.printWorkingDirectory() + localFile.getName();
				} else {
					remoteFile = ftpClient.printWorkingDirectory() + "/" + localFile.getName();
				}
			}

			boolean done;
			if (type.equals("zip")) {
				done = FTPUtil.uploadSingleFile(ftpClient, location, remoteFile);
				if (done) {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.INFO, "FTP Upload: ", "The file is uploaded successfully.");
					System.out.println("The file is uploaded successfully.");
					return true;
				} else {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.SEVERE, "FTP Upload: ", "Error occured while uploading.");
					System.out.println("Error occured while uploading.");
					return false;
				}
			} else {
				ftpClient.makeDirectory(remoteFile);
				System.out.println(ftpClient.getReplyString());
				done = FTPUtil.uploadDirectory(ftpClient, remoteFile, location, "");
				if (done) {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.INFO, "FTP Upload: ", "The file is uploaded successfully.");
					System.out.println("The file is uploaded successfully.");
					return true;
				} else {
					ftpClient.logout();
					ftpClient.disconnect();
					Logger.getLogger(GACOM).log(Level.SEVERE, "FTP Upload: ", "Error occured while uploading.");
					System.out.println("Error occured while uploading.");
					return false;
				}
			}
		} catch (SocketException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		} catch (IOException ex) {
			Logger.getLogger(FTPConnection.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}

	}
}