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

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.sipperfly.ui.FTPConnection;

/**
 *
 * @author noumantayyab
 */
public class MyTransferListener implements FTPDataTransferListener {

	private static String GACOM = "com.UKExactly";
	private String filePath;
//	private FTPClient client;
	private int totalBytes = 0;

	public MyTransferListener(String filePath) {
		this.filePath = filePath;
//		this.client = ftp;
	}

	public void started() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer started: ".concat(this.filePath));
		// Transfer started
	}

	public void transferred(int length) {
		// Yet other length bytes has been transferred since the last time this
		// method was called
		this.totalBytes += length;
//		Logger.getLogger(GACOM).log(Level.INFO, "Filename: ".concat(this.filePath));
//		Logger.getLogger(GACOM).log(Level.INFO, "transferred size: ".concat(String.valueOf(this.totalBytes)));
		System.out.println("file: "+this.filePath);
		System.out.println("total bytes: "+ this.totalBytes);
	}

	public void completed() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer completed: ".concat(this.filePath));
		// Transfer completed
	}

	public void aborted() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer aborted: ".concat(this.filePath));
		// Transfer aborted
	}

	public void failed() {
		Logger.getLogger(GACOM).log(Level.INFO, "File transfer failed: ".concat(this.filePath));
		// Transfer failed
	}

}
