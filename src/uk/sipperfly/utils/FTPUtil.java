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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import uk.sipperfly.ui.FTPConnection;

public class FTPUtil {

	private static String GACOM = "com.UKExactly";
	static String host;
	static String username;
	static String password;
	static int port;
	static String mode;
	static String destination;
	FTPConnection ftpCon;

	public FTPUtil(FTPConnection ftpConnect) {
		this.ftpCon = ftpConnect;
		host = this.ftpCon.host;
		username = this.ftpCon.username;
		password = this.ftpCon.password;
		port = this.ftpCon.port;
		mode = this.ftpCon.mode;
		destination = this.ftpCon.destination;
	}

	/**
	 * Upload whole directory (including its nested sub directories and files) to FTP server.
	 *
	 * @param ftpClient       an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteDirPath   Path of the destination directory on the server.
	 * @param localParentDir  Path of the local directory being uploaded.
	 * @param remoteParentDir Path of the parent directory of the current directory on the server (used by recursive calls).
	 * @throws IOException if any network or IO error occurred.
	 */
	public static boolean uploadDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir, String remoteParentDir) throws IOException {

		System.out.println("LISTING directory: " + localParentDir);
		Logger.getLogger(GACOM).log(Level.INFO, "LISTING directory: {0}".concat(localParentDir));
		File localDir = new File(localParentDir);
		File[] subFiles = localDir.listFiles();
		if (subFiles != null && subFiles.length > 0) {
			for (File item : subFiles) {
			boolean answer = ftpClient.sendNoOp();
			if(!answer){
				reconnect();
			}
			String status= ftpClient.getStatus();
			boolean a= ftpClient.isAvailable();
//				if (!ftpClient.isConnected()) {
//					reconnect();
//				}
				String remoteFilePath = remoteDirPath + "/" + remoteParentDir + "/" + item.getName();
				if (remoteParentDir.equals("")) {
					remoteFilePath = remoteDirPath + "/" + item.getName();
				}
				if (item.isFile()) {
					// upload the file
					String localFilePath = item.getAbsolutePath();
					Logger.getLogger(GACOM).log(Level.INFO, "About to upload the file: ".concat(localFilePath));
					System.out.println("About to upload the file: " + localFilePath);
					boolean uploaded = uploadSingleFile(ftpClient, localFilePath, remoteFilePath);

					if (uploaded) {
						Logger.getLogger(GACOM).log(Level.INFO, "UPLOADED a file to: ".concat(remoteFilePath));
						System.out.println("UPLOADED a file to: " + remoteFilePath);
					} else {
						System.out.println("COULD NOT upload the file: " + localFilePath);
						Logger.getLogger(GACOM).log(Level.INFO, "COULD NOT upload the file: ".concat(localFilePath));
						Logger.getLogger(GACOM).log(Level.INFO, ftpClient.getReplyString());
						return false;
					}
				} else {
					// create directory on the server
					boolean created = ftpClient.makeDirectory(remoteFilePath);
					if (created) {
						System.out.println("CREATED the directory: " + remoteFilePath);
						Logger.getLogger(GACOM).log(Level.INFO, "CREATED the directory: ".concat(remoteFilePath));
					} else {
						System.out.println("COULD NOT create the directory: " + remoteFilePath);
						Logger.getLogger(GACOM).log(Level.INFO, "COULD NOT create the directory: ".concat(remoteFilePath));
						Logger.getLogger(GACOM).log(Level.INFO, ftpClient.getReplyString());
						return false;
					}

					// upload the sub directory
					String parent = remoteParentDir + "/" + item.getName();
					if (remoteParentDir.equals("")) {
						parent = item.getName();
					}

					localParentDir = item.getAbsolutePath();
					uploadDirectory(ftpClient, remoteDirPath, localParentDir, parent);
				}
			}
		}
		return true;
	}

	/**
	 * Upload a single file to the FTP server.
	 *
	 * @param ftpClient      an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param localFilePath  Path of the file on local computer
	 * @param remoteFilePath Path of the file on remote the server
	 * @return true if the file was uploaded successfully, false otherwise
	 * @throws IOException if any network or IO error occurred.
	 */
	public static boolean uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath) throws FileNotFoundException, IOException {
		File localFile = new File(localFilePath);
		try (InputStream inputStream = new FileInputStream(localFile)) {
			try {
				return ftpClient.storeFile(remoteFilePath, inputStream);
			} 
			catch (SocketTimeoutException e) {
				Logger.getLogger(GACOM).log(Level.SEVERE, "upload Single File: ", e.getCause());
				return false;
			}
			catch (Exception e) {
				e.printStackTrace();
				if (e.getCause() != null) {
					e.getCause().printStackTrace();
					Logger.getLogger(GACOM).log(Level.SEVERE, "upload Single File: ", e.getCause());
				}
				return false;
			}
		}
	}

	public static void reconnect() throws SocketException, IOException {
		FTPClient ftpClient = new FTPClient();
		ftpClient.setControlEncoding("UTF-8");
		ftpClient.connect(host, port);
		ftpClient.login(username, password);
		if (mode.equalsIgnoreCase("passive")) {
			ftpClient.enterLocalPassiveMode();
		} else if (mode.equalsIgnoreCase("active")) {
			ftpClient.enterLocalActiveMode();
		}
		int reply = ftpClient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			Logger.getLogger(GACOM).log(Level.INFO, "FTP Login: ".concat(ftpClient.getReplyString()));
			ftpClient.disconnect();
		}
		ftpClient.setKeepAlive(true);
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
		ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlKeepAliveTimeout(300);
//		ftpClient.sendSiteCommand("RECFM=FB");
//		ftpClient.sendSiteCommand("LRECL=2000");
//		ftpClient.sendSiteCommand("BLKSIZE=27000");
//		ftpClient.sendSiteCommand("CY");
//		ftpClient.sendSiteCommand("PRI= 50");
//		ftpClient.sendSiteCommand("SEC=25");

//		ftpClient.sendSiteCommand("RECFM=FB");
//		ftpClient.sendSiteCommand("LRECL=2000");
//		ftpClient.sendSiteCommand("BLOCKSIZE=27000");
//		ftpClient.sendSiteCommand("SPACE=(CYL,(30,300),RLSE)"); 
//		ftpClient.sendSiteCommand("TR");
//		ftpClient.sendSiteCommand("PRI=450");
//		ftpClient.sendSiteCommand("SEC=4500");
		
		Logger.getLogger(GACOM).log(Level.INFO, "Reconnected FTP");
		System.out.println("reconnected");
	}
}
