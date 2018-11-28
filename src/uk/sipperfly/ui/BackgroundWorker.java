/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@weareavp.com)
 * Author: Rimsha Khalid (rimsha@weareavp.com)
 * Version: 0.1.5
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@weareavp.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.ui;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import static uk.sipperfly.ui.Exactly.GACOM;

// Bagit imports
import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.domain.Metadata;
import gov.loc.repository.bagit.exceptions.*;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.hash.SupportedAlgorithm;
import gov.loc.repository.bagit.reader.BagReader;
import gov.loc.repository.bagit.verify.BagVerifier;

import org.zeroturnaround.zip.ZipUtil;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.persistent.Recipients;
import uk.sipperfly.repository.BagInfoRepo;
import uk.sipperfly.repository.ConfigurationsRepo;
import uk.sipperfly.repository.RecipientsRepo;
import uk.sipperfly.utils.CommonUtil;
import uk.sipperfly.utils.ZipUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import uk.sipperfly.persistent.FTP;
import uk.sipperfly.repository.FTPRepo;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.sipperfly.persistent.BagInfo;
import uk.sipperfly.persistent.SFTP;
import uk.sipperfly.repository.SFTPRepo;
import static uk.sipperfly.utils.CommonUtil.copyFileAttributes;
import uk.sipperfly.utils.SFTPUtil;

/**
 * This class implements the background worker thread.
 * Work happens on this thread so that the MainFraim GUI remains responsive.
 *
 * @author Nouman Tayyab
 */
class BackgroundWorker extends SwingWorker<Integer, Void> {

	private final List<String> sources;
	private final Exactly parent;
	private int numberOfFiles;
	private final Configurations config;
	private final FTP ftp;
	private CommonUtil commonUtil;
	private ZipUtils zipUtil;
	private Path target;
	private String bagSize = "";
	private int bagCount = 0;
	private String inputFolder = "";
	private String destFolder = "";
	private int process = 0;
	private int fileCounter = 1;
	private int ftpProcess = 0;
	private int sftpProcess = 0;
	private String unbagDestination = "";
	private UIManager uIManager;
	private final SFTP sftp;
	private static String targetChecksum;
	String content = "";
	String payLoad = "";
	String bagDate = "";
	String bagitSize = "";
	String manifest = "";
	int totalFiles;
	private int totalTries = 1;

//	public BackgroundWorker(int process) {
//		this.process = process;
//	}
	/**
	 * Constructor for BackgroundWorker
	 *
	 * @param sources
	 * @param parent
	 * @param process
	 * @throws IOException
	 */
	public BackgroundWorker(final List<String> sources, Exactly parent, int process) throws IOException {
		if (sources == null || sources.isEmpty()
				|| parent == null) {
			throw new IllegalArgumentException();
		}
		ConfigurationsRepo configRepo = new ConfigurationsRepo();
		FTPRepo ftpRepo = new FTPRepo();
		SFTPRepo sftpRepo = new SFTPRepo();
		this.sources = sources;
		this.parent = parent;
		this.config = configRepo.getOneOrCreateOne();
		this.ftp = ftpRepo.getOneOrCreateOne();
		numberOfFiles = 0;
		this.zipUtil = new ZipUtils();
		this.commonUtil = new CommonUtil();
		this.inputFolder = this.parent.inputLocationDir.getText();
		this.destFolder = this.parent.destDirLocation.getText();
		this.process = process;
		this.uIManager = new UIManager(parent);
		this.sftp = sftpRepo.getOneOrCreateOne();
		this.totalFiles = this.parent.totalFiles;
	}

	/**
	 * The main logic for the work that the background thread does.
	 * This method is automatically called by the threading framework.
	 * <p>
	 * The following actions are performed:
	 * 1. Recognize whether bag is organized in bagit structure or not.
	 * 2. Validate bag.
	 * 3. Validates the user can connect to the email server.
	 * 4. Bags the input folder using the bagit Java library from the Library of Congress
	 * 5. Transfer the folder and all subfolders to the target.
	 * 6. Check ftp connection if file or folder is supposed to upload on ftp server
	 * 7. upload files on ftp server
	 * 8. Sends summary email to the UK Exactly
	 *
	 * @return 1 for success and -1 for failure
	 * @see http://www.digitalpreservation.gov/documents/bagitspec.pdf
	 */
	@Override
	protected Integer doInBackground() {
		int progress;
		try {
			if (this.process == 0) {
				this.parent.uIManager.setConfigurationFields();
				this.parent.uIManager.setEmailFields();
				this.parent.uIManager.setFtpFields();
				this.parent.uIManager.setBagInfoFields(true);
				this.parent.uIManager.setTemplate();
				this.parent.uIManager.setSftpFields();
			}
			String workingPath;
			if (this.process == 2) {
				if (!this.inputFolder.isEmpty() || this.inputFolder != null) {
					if (!this.parent.editCurrentStatus.getText().isEmpty() && this.parent.editCurrentStatus.getText() != null) {
						this.parent.UpdateResult("Recognizing Bag...", 1);
					} else {
						this.parent.UpdateResult("Recognizing Bag...", 1);
					}
					Logger.getLogger(GACOM).log(Level.INFO, "Recognizing Bag");
					if (this.BagRecognition(this.inputFolder) == 0) {
						this.parent.UpdateResult("Bag Recognition: Not organized in BagIt structure", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Bag Recognition: Not organized in BagIt structure.");
						return -1;
					}
				}
			}
			if (this.process == 3 || this.process == 4) {
				if (!this.inputFolder.isEmpty() || this.inputFolder != null) {
					if (this.process == 4) {
						this.parent.UpdateResult("Validating Bag Before Unbagging...", 1);
						Logger.getLogger(GACOM).log(Level.INFO, "Validating bag before Unbagging");
					} else {
						this.parent.UpdateResult("Validating Bag...", 1);
						Logger.getLogger(GACOM).log(Level.INFO, "Validating Bag");
					}
					if (this.ValidateBag(this.inputFolder) == 0) {
						Border border = BorderFactory.createLineBorder(Color.red, 2);
						this.parent.inputLocationDir.setBorder(border);
						this.parent.UpdateResult("Invalid bag.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Invalid bag.");
						return -1;
					}
				}
			}

			if (this.process == 4) {
				int isExisted = 0;
				this.parent.unBaggingProgress.setMaximum(3);
				this.parent.UpdateResult("Copying data...", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Copying data...");
				File folder = new File(inputFolder);
				String name = FilenameUtils.removeExtension(folder.getName());
				workingPath = destFolder + File.separator + name;
				File dest = new File(destFolder + File.separator + FilenameUtils.removeExtension(folder.getName()));
				if (dest.exists()) {
					this.getFileSuffix(dest.toString());
					name = name + "_" + fileCounter;
					this.fileCounter = 1;
					workingPath = destFolder + File.separator + name;
					isExisted = 1;
				}
				if (folder.getName().toLowerCase().endsWith(".zip")) {
					String zipPath = "";
					Logger.getLogger(GACOM).log(Level.INFO, "Extracting files from zip folder");
					if (isExisted == 1) {
						this.zipUtil.unZipIt(inputFolder, workingPath);
						zipPath = workingPath;
						workingPath = workingPath + File.separator + FilenameUtils.removeExtension(folder.getName());
					} else {
						this.zipUtil.unZipIt(inputFolder, destFolder);
					}

					this.parent.unBaggingProgress.setValue(1);
					if (this.validateAndUnbag(workingPath, name, zipPath) == 0) {
						return -1;
					}
				} else {
					Logger.getLogger(GACOM).log(Level.INFO, "Copying data to destination");
					File targetDir = new File(workingPath);
					FileUtils.copyDirectory(folder, targetDir);
					this.parent.unBaggingProgress.setValue(1);
					if (this.validateAndUnbag(workingPath, name, "") == 0) {
						return -1;
					}
				}
				this.resetFiles();
			}

			if (this.process == 1) {
				if (this.validateBagName()) {
					this.parent.UpdateResult("Folder already existed in destination with this title. Please change the title.", 0);
					Logger.getLogger(GACOM).log(Level.SEVERE, "Folder already existed in destination with this title. Please change the title.");
					this.parent.btnTransferFiles.setEnabled(true);
					return -1;
				}
				this.parent.UpdateResult("Verifying Transfer...", 1);
				if (this.config.getEmailNotifications()) {
					// validate email auth
					if (!ValidateCredentials()) {
						this.parent.UpdateResult("Credentials not valid. Please update Email Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update Email settings.");
						this.parent.btnTransferFiles.setEnabled(true);
						return -1;
					}
				}
				// check if drop location folder is not set.

				if (this.parent.editInputDir1.getText() == null || this.parent.editInputDir1.getText().isEmpty()) {
					this.parent.UpdateResult("Please select Transfer destination.", 0);
					Logger.getLogger(GACOM).log(Level.SEVERE, "Please select Transfer destination.");
					this.parent.btnTransferFiles.setEnabled(true);
					return -1;
				}
				// validate bag name.
				if (this.parent.bagNameField.getText() == null || this.parent.bagNameField.getText().isEmpty()) {
					this.parent.UpdateResult("Please provide Transfer name.", 0);
					Logger.getLogger(GACOM).log(Level.SEVERE, "Please provide Transfer name.");
					this.parent.btnTransferFiles.setEnabled(true);
					return -1;
				}
				if (this.config.getEmailNotifications()) {
					RecipientsRepo recipientsRepo = new RecipientsRepo();
					List<Recipients> recipients = recipientsRepo.getAll();
					if (recipients.size() < 1) {
						this.parent.UpdateResult("Please add at least one recipient.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Please add at least one recipient.");
						this.parent.btnTransferFiles.setEnabled(true);
						return -1;
					}
				}
				if (this.parent.ftpDelivery.isSelected()) {
					String result = ValidateFTPCredentials();
					if (!(result.equals("FTPES") || result.equals("FTP"))) {
						this.parent.UpdateResult("Credentials not valid. Please update FTP Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update FTP settings.");
						this.parent.btnTransferFiles.setEnabled(true);
						return -1;
					}
				}
				if (this.parent.sftpDelivery.isSelected()) {
					String result = ValidateSFTPCredentials();
					if (!result.equals("true")) {
						this.parent.UpdateResult("Credentials not valid. Please update SFTP Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update SFTP settings.");
						this.parent.btnTransferFiles.setEnabled(true);
						return -1;
					}
				}
				if (this.isCancelled()) {
					Logger.getLogger(GACOM).log(Level.INFO, "Transfer canceled.");
					this.parent.UpdateResult("Transfer canceled.", 0);
					return -1;
				}

				// Set the tragetPath of bag.
				this.setTragetPath();
				//transfer
				Logger.getLogger(GACOM).log(Level.INFO, "Transfering files...");
				Path target = TransferFiles();
				if (!getTargetChecksum(this.target.toFile()).equals("success")) {
					this.parent.UpdateResult("Something went wrong while copying files again trying to transfer files...", 0);
					FileUtils.deleteDirectory(this.target.toFile());
					this.totalTries = this.totalTries + 1;
					this.setTragetPath();
					target = TransferFiles();
					if (!getTargetChecksum(this.target.toFile()).equals("success")) {
						this.parent.UpdateResult("Something went wrong while copying files again trying to transfer files...", 0);
						FileUtils.deleteDirectory(this.target.toFile());
						this.setTragetPath();
						this.totalTries = this.totalTries + 1;
						target = TransferFiles();
						if (!getTargetChecksum(this.target.toFile()).equals("success")) {
							this.parent.UpdateResult("Please try again.", 0);
							this.totalTries = 1;
							FileUtils.deleteDirectory(this.target.toFile());
							return -1;
						}
					}
				}
				if (this.isCancelled()) {
					Logger.getLogger(GACOM).log(Level.INFO, "Canceling Transfer Files task.");
					Logger.getLogger(GACOM).log(Level.INFO, "Transfer canceled.");
					this.parent.UpdateResult("Transfer canceled.", 0);
					return -1;
				}
				// bagit
				this.parent.UpdateResult("Preparing Bag...", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Preparing Bag...");
				BagFolder();
				if (this.isCancelled()) {
					Logger.getLogger(GACOM).log(Level.INFO, "Canceling Bagit task.");
					return -1;
				}
				this.parent.btnCancel.setVisible(false);
				if (this.parent.ftpDelivery.isSelected()) {
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling Upload data to FTP");
						return -1;
					}
					String result = ValidateFTPCredentials();
					if (!(result.equals("FTPES") || result.equals("FTP"))) {
						this.parent.UpdateResult("Credentials not valid. Please update FTP Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update FTP settings.");
						this.parent.btnTransferFiles.setEnabled(true);
						return -1;

					}
					progress = this.parent.jProgressBar2.getValue();
					this.parent.jProgressBar2.setValue(progress + 1);
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling Upload data to FTP");
						return -1;
					}

					this.parent.UpdateResult("Uploading data on FTP ...", 0);
					Logger.getLogger(GACOM).log(Level.INFO, "Uploading data on FTP ...");
					UploadFilesFTP();
				}
				if (this.parent.sftpDelivery.isSelected()) {
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling Upload data to SFTP");
						return -1;
					}
					String result = ValidateSFTPCredentials();
					if (!result.equals("true")) {
						this.parent.UpdateResult("Credentials not valid. Please update SFTP Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update SFTP settings.");
						this.parent.btnTransferFiles.setEnabled(true);
						return -1;

					}
					progress = this.parent.jProgressBar2.getValue();
					this.parent.jProgressBar2.setValue(progress + 1);
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling Upload data to SFTP");
						return -1;
					}

					this.parent.UpdateResult("Uploading data on SFTP ...", 0);
					Logger.getLogger(GACOM).log(Level.INFO, "Uploading data on SFTP ...");
					UploadFilesSFTP();
				}
				if (this.isCancelled()) {
					Logger.getLogger(GACOM).log(Level.INFO, "Canceling send notification email(s).");
					return -1;
				}
				// send email to GA
				if (this.config.getEmailNotifications()) {
					this.parent.UpdateResult("Preparing to send notification email(s)...", 0);
					Logger.getLogger(GACOM).log(Level.INFO, "Preparing to send notification email(s)...");
					progress = this.parent.jProgressBar2.getValue();
					this.parent.jProgressBar2.setValue(progress + 1);
					SendMail(target);
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling send notification email(s)");
						return -1;
					}
					progress = this.parent.jProgressBar2.getValue();
					this.parent.jProgressBar2.setValue(progress + 1);
				}
				this.parent.jProgressBar2.setValue(this.parent.jProgressBar2.getMaximum());
				Thread.sleep(2000);
				// update UI
				this.parent.list.resetEntryList();
				this.resetTransferFiles();
				this.parent.UpdateResult("Session complete.", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Session complete.");

			}
			return 1;
		} catch (Exception ex) {
			this.parent.btnTransferFiles.setEnabled(true);
			if (this.isCancelled()) {
				this.parent.UpdateResult("Transfer canceled. Clean up partially copied directories.", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Transfer canceled. Clean up partially copied directories.");
				return -1;
			}
			this.parent.UpdateResult("An error occurred. Please contact support.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "An error occurred. Please contact support.", ex);
			return -1;
		}
	}

	/**
	 * Firstly validate bag. If bag is valid then unpack it at specified destination.
	 *
	 * @param workingPath
	 * @param name
	 * @param zipPath
	 * @return 1 if valid bag and Unpack successfully, 0 otherwise
	 */
	private int validateAndUnbag(String workingPath, String name, String zipPath) {
		this.parent.UpdateResult("Validating bag after copying...", 0);
		Logger.getLogger(GACOM).log(Level.INFO, "Validating bag after copying");
		if (this.ValidateBag(workingPath) != 0) {
			String newPath = "";
			this.parent.unBaggingProgress.setValue(2);
			this.parent.UpdateResult("Unbagging Bagit Bag...", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "Unbagging Bagit Bag");
			if (zipPath != "") {
				newPath = zipPath;
			} else {
				newPath = destFolder + File.separator + "temp_data_folder";
			}

			try {
				this.commonUtil.unBag(workingPath, newPath, name);
			} catch (IOException ex) {
				Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
			}
			this.parent.unBaggingProgress.setValue(3);
			try {
				Thread.sleep(300);
			} catch (InterruptedException ex) {
				Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
			}
			return 1;
		} else {
			this.parent.unBaggingProgress.setMaximum(0);
			this.parent.UpdateResult("Invalid bag after coping to destination.", 0);
			Logger.getLogger(GACOM).log(Level.SEVERE, "Invalid bag after coping to destination");
			return 0;
		}
	}

	/**
	 * add int suffix with file name.
	 *
	 * @param folderPath
	 */
	public void getFileSuffix(String folderPath) {
		File f = new File(folderPath + "_" + fileCounter);
		if (f.exists()) {
			fileCounter = fileCounter + 1;
			getFileSuffix(folderPath);
		}
	}

	/**
	 * Reset the UI of transfer files
	 */
	public void resetTransferFiles() {
		this.parent.bagNameField.setText("");
		this.parent.serializeBag.setSelected(false);
		this.parent.editInputDir.setText("");
		this.parent.jProgressBar2.setMaximum(0);
		this.parent.UpdateProgressBar(0);
		this.parent.ftpDelivery.setSelected(false);
		this.parent.sftpDelivery.setSelected(false);
		this.parent.btnCancel.setVisible(false);
		this.parent.btnTransferFiles.setEnabled(true);
		this.parent.metadateUpdated = 0;
	}

	/**
	 * Reset the UI of Unpack bag.
	 */
	public void resetFiles() {
		this.unbagDestination = this.parent.destDirLocation.getText();
		this.parent.inputLocationDir.setText("");
		this.parent.destDirLocation.setText("");
		this.parent.unBaggingProgress.setMaximum(0);
		Border border = BorderFactory.createLineBorder(Color.lightGray, 1);
		this.parent.inputLocationDir.setBorder(border);
	}

	/**
	 * Set Target path to place bag after successful transfer.
	 *
	 * @return Path target.
	 * @throws Exception
	 */
	protected Path setTragetPath() throws Exception {
            // get the drop location path from database.
            Path targetDirPath = new File(this.config.getDropLocation()).toPath();
            
            
            if (!Files.exists(targetDirPath)) {
                try{
                    Files.createDirectory(targetDirPath);
                }catch(Exception ex){
                    ex.printStackTrace();
                    try(  PrintWriter out = new PrintWriter( "file_permission_sp_exception.txt" )  ){
                        out.println( ex.getCause() + "\n" + ex.getMessage() );
                    }
                    System.out.println("Ex: " + ex.getMessage() );
                    try{
                        File f = new File(targetDirPath.toString());
                        f.mkdirs();
                    }catch(Exception e){
                        FileUtils.forceMkdir(new File(targetDirPath.toString()));
                        e.printStackTrace();
                    }
                }
                
            }

            // use the bag name that User selected instead of Source directory/file name.
            String name = this.parent.bagNameField.getText();
            File bagName = new File(name);
            this.target = CommonUtil.combine(targetDirPath, bagName.toPath());

            // create it if it doesn't exist
            if (!Files.exists(this.target)) {
                Files.createDirectory(this.target);
            }
            return target;
	}

	/**
	 * check whether bag title is already existed in destination folder while file transfer.
	 *
	 * @return true if existed else false
	 */
	public boolean validateBagName() {
            Path targetDirPath = new File(this.config.getDropLocation()).toPath();
            String name = this.parent.bagNameField.getText();
            File bagName = new File(name);
            this.target = CommonUtil.combine(targetDirPath, bagName.toPath());
            if (Files.exists(this.target)) {
                    return true;
            }
            return false;
	}

	/**
	 * This is called when the thread has completed it's work or has been canceled.
	 * This method is automatically called by the threading framework.
	 */
	@Override
	protected void done() {
		try {
			// Transfer result already updated in worker thread
			if (this.get() < 0) {
				return;
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "InterruptedException", ex);
			return;
		} catch (ExecutionException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "ExecutionException", ex);
			return;
		}

		if (this.isCancelled()) {
			this.parent.UpdateResult("Transfer canceled. Clean up partially copied directories.", 0);
			Logger.getLogger(GACOM).log(Level.WARNING, "Transfer canceled. Clean up partially copied directories.");
		} else if (this.process == 1 && this.ftpProcess == 0 && this.sftpProcess == 0) {
			this.parent.UpdateResult("Transfer completed successfully.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "Transfer completed successfully.");
		} else if (this.process == 1 && this.ftpProcess == 1 && this.sftpProcess == 0) {
			this.parent.UpdateResult("FTP transfer failed; local transfer completed successfully.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "FTP transfer failed; local transfer completed successfully.");
		} else if (this.process == 1 && this.ftpProcess == 0 && this.sftpProcess == 1) {
			this.parent.UpdateResult("SFTP transfer failed; local transfer completed successfully.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "SFTP transfer failed; local transfer completed successfully.");
		} else if (this.process == 1 && this.ftpProcess == 1 && this.sftpProcess == 1) {
			this.parent.UpdateResult("FTP and SFTP transfer failed; local transfer completed successfully.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "FTP and SFTP transfer failed; local transfer completed successfully.");
		} else if (this.process == 2) {
			this.parent.UpdateResult("Bag Recognition: organized in BagIt structure", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "Bag Recognition: organized in BagIt structure");
		} else if (this.process == 3) {
			Border border = BorderFactory.createLineBorder(Color.GREEN, 2);
			this.parent.inputLocationDir.setBorder(border);
			this.parent.UpdateResult("Valid Bag", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "Valid Bag");
		} else if (this.process == 4) {
			this.parent.UpdateResult("Successfully unpacked Bagit bag at " + this.unbagDestination, 0);
			Logger.getLogger(GACOM).log(Level.INFO, "Successfully unpacked Bagit bag at ".concat(this.unbagDestination));
		}
		this.ftpProcess = 0;
		this.sftpProcess = 0;
		this.unbagDestination = "";

	}

	/**
	 * Bags the input folder using the bagit Java library from the Library of Congress.
	 * Any errors are reported to the parent UI.
	 * Creates a success semaphore in the source directory if the transfer succeeded.
	 *
	 * @see* http://www.digitalpreservation.gov/documents/bagitspec.pdf
	 */
	public void BagFolder() throws NoSuchAlgorithmException, IOException {
                Path folder = Paths.get(this.target.toFile().getAbsolutePath());
		long size = FileUtils.sizeOfDirectory(folder.toFile());
		Double truncatedDouble = BigDecimal.valueOf(((double) size / (double) 1024))
				.setScale(2, RoundingMode.CEILING)
				.doubleValue();
		this.bagitSize = truncatedDouble + " KB";
		Metadata extraMetadata = new Metadata();
		BagInfoRepo bagInfoRepo = new BagInfoRepo();
		List<BagInfo> bagInfoList = bagInfoRepo.getOneOrCreateOne();
		for (BagInfo bagInfo : bagInfoList) {
			extraMetadata.add(bagInfo.getLabel(), this.commonUtil.createBagInfoTxt(bagInfo));
		}
		extraMetadata.add("Bag-Size", this.bagitSize);

		Bag bag = BagCreator.bagInPlace(
			folder,
			Collections.singletonList((SupportedAlgorithm) StandardSupportedAlgorithms.MD5),
			true,
			extraMetadata
		);

		Charset charset;

		this.bagCount = bag.getPayLoadManifests().size();
		String payloadOxum = bag.getMetadata().get("Payload-Oxum").get(0);

		List<String> payload = Arrays.asList(payloadOxum.split("\\."));
		if (payload.size() > 0) {
			this.bagSize = payload.get(0);
		}
		int zip = 0;
		if (this.parent.serializeBag.isSelected()) {
			zip = 1;
		}
		int emailNotification = 0;
		String sender = "";
		StringBuilder recipients = new StringBuilder();
		if (this.config.getEmailNotifications()) {
			emailNotification = 1;
			sender = this.config.getUsername();
			RecipientsRepo recipientsRepo = new RecipientsRepo();
			List<Recipients> recipientsList = recipientsRepo.getAll();
			for (Recipients res : recipientsList) {
				recipients.append(res.getEmail()).append(", ");
			}
			if (recipients.length() > 0) {
				recipients = new StringBuilder(recipients.substring(0, recipients.length() - 2));
			}
		}
		if (this.parent.ftpDelivery.isSelected() && this.parent.sftpDelivery.isSelected()) {
			this.commonUtil.CreateSuccessSemaphore(this.config.getUsername(), this.parent.bagNameField.getText(), this.target, this.ftp.getDestination(), this.sftp.getDestination(), this.bagSize, bag.getPayLoadManifests().size(), zip, sender, recipients.toString(), emailNotification);
		} else if (this.parent.ftpDelivery.isSelected()) {
			this.commonUtil.CreateSuccessSemaphore(this.config.getUsername(), this.parent.bagNameField.getText(), this.target, this.ftp.getDestination(), "", this.bagSize, bag.getPayLoadManifests().size(), zip, sender, recipients.toString(), emailNotification);
		} else if (this.parent.sftpDelivery.isSelected()) {
			this.commonUtil.CreateSuccessSemaphore(this.config.getUsername(), this.parent.bagNameField.getText(), this.target, "", this.sftp.getDestination(), this.bagSize, bag.getPayLoadManifests().size(), zip, sender, recipients.toString(), emailNotification);
		} else {
			this.commonUtil.CreateSuccessSemaphore(this.config.getUsername(), this.parent.bagNameField.getText(), this.target, "", "", this.bagSize, bag.getPayLoadManifests().size(), zip, sender, recipients.toString(), emailNotification);
		}

		this.payLoad = payloadOxum;
		this.bagDate = bag.getMetadata().get("Bagging-Date").get(0);
		String payloadManifest = bag.getPayLoadManifests().toString();
		this.manifest = payloadManifest.substring(1, payloadManifest.length() - 1);
		this.generateSystemDataFile();
		this.generateCsvFile(this.payLoad, this.bagDate, bagitSize);
		this.createXML(this.payLoad, this.bagDate, bagitSize);
                BagVerifier verifier = new BagVerifier();
		try {
                    
                    BagReader reader = new BagReader();
                    bag = reader.read(folder);
                    verifier.isValid(bag, false);

                    numberOfFiles = bag.getPayLoadManifests().size(); // get the number of payload files
                    numberOfFiles += 4; // add the standard bagit files

                    Logger.getLogger(GACOM).log(Level.INFO, "Bag created. Number of files is {0}", numberOfFiles);

                    Path path = Paths.get(this.target + File.separator + "manifest-md5.txt");
                    charset = StandardCharsets.UTF_8;
                    this.content = new String(Files.readAllBytes(path), charset);
                    if (this.parent.serializeBag.isSelected()) {
                        this.parent.UpdateResult("Serializing bag...", 0);
                        Logger.getLogger(GACOM).log(Level.INFO, "Serializing bag...");
                        ZipUtil.pack(new File(this.target.toString()), new File(this.target.toString().concat(".zip")));
                        retryDelete(this.target.toAbsolutePath().toString());
                    }
		} catch (IOException | UnparsableVersionException | VerificationException | MaliciousPathException | MissingPayloadManifestException | UnsupportedAlgorithmException | CorruptChecksumException | MissingBagitFileException | InvalidBagitFileFormatException | MissingPayloadDirectoryException | InterruptedException | FileNotInPayloadDirectoryException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "Error closing the bag", ex);
			File newManifest = new File(this.target.toString() + File.separator + "manifest-md5.txt");
			Files.write(newManifest.toPath(), this.content.getBytes(StandardCharsets.UTF_8));
		} finally {
                    verifier.close();
                }
		if (this.parent.totalFiles > this.parent.tranferredFiles) {
			this.parent.UpdateProgressBar(this.parent.totalFiles);
		}
	}

	public String getTargetChecksum(File directory) {
		targetChecksum = "";
		for (File file : directory.listFiles()) {
			if (!file.exists()) {
				this.parent.UpdateResult("Something went wrong while copying data to destination.", 0);
				Logger.getLogger(GACOM).log(Level.SEVERE, "Something went wrong while copying data to staging.");
				Logger.getLogger(GACOM).log(Level.SEVERE, file.getName() + " not found at target destination");
				return "error";
			} else if (file.isFile()) {
				targetChecksum += file.getName() + "_|_" + this.commonUtil.checkSum(file.getAbsolutePath()) + "\n";
			} else if (file.isDirectory()) {
				targetChecksum += this.commonUtil.getDirectoryChecksum(file);
			}
		}
		List<String> filterList = Arrays.asList(this.parent.sourceChecksum.split("\n"));
		for (String str : filterList) {
			if (!targetChecksum.contains(str)) {
				String[] info = str.split("_|_");
				if (this.totalTries == 3) {
					this.parent.UpdateResult(info[0] + " either got corrupted or not copied to the destination", 0);
				}
				Logger.getLogger(GACOM).log(Level.SEVERE, info[0] + " either got corrupted or not copied to the destination");
				return "error";
			}
		}
		return "success";
	}

	/**
	 * Initiates the file transfer from the source to the target.
	 *
	 * @return The calculated target path
	 *
	 * @throws Exception If any errors occur
	 */
	public Path TransferFiles() throws Exception {
            FileTransfer ft = new FileTransfer(parent);
            if (this.totalTries == 1) {
                System.out.println("this.totalTries == " + this.totalTries);
                Logger.getLogger(GACOM).log(Level.INFO, "Max Progress bar count: ".concat(Integer.toString(this.parent.totalFiles)));
                if (this.parent.ftpDelivery.isSelected() && this.parent.serializeBag.isSelected()) {
                    this.totalFiles = this.totalFiles + 2;
                } else {
                    int newCount = this.parent.totalFiles + 8;
                    this.totalFiles = this.totalFiles + newCount;
                }
                if (this.config.getEmailNotifications()) {
                    this.totalFiles = this.totalFiles + 1;
                }
                if (this.parent.sftpDelivery.isSelected() && this.parent.serializeBag.isSelected()) {
                    this.totalFiles = this.totalFiles + 2;
                } else {
                    int newCount = this.parent.totalFiles + 8;
                    this.totalFiles = this.totalFiles + newCount;
                }
                this.parent.jProgressBar2.setMaximum(this.totalFiles);
            }
            Path folderTarget = Paths.get(this.target.toString(), "data");
            if (!Files.exists(folderTarget)) {
                Files.createDirectories(folderTarget);
            }
            int index = 0;
            for (String source : this.sources) {
                this.parent.UpdateResult(String.format("(%s/%s)Transfering files...", ++index, this.sources.size()), 0);
                File sourceFile = new File(source);
                Path targetPath = CommonUtil.combine(folderTarget, new File(sourceFile.getName()).toPath());
                if(sourceFile.isDirectory()){
                    Files.createDirectories(targetPath);
                }
                ft.setTargetPath(targetPath);
                Path inputSource = sourceFile.toPath();
                ft.setSourcePath(inputSource);
                ft.Perform();
                if(sourceFile.isDirectory()){
                    //restore the original attrs (including last modified date which would have just been modified)
                    copyFileAttributes(sourceFile.toPath(), targetPath);
                }
            }
            return target;
	}

	/**
	 * Validates that we can authenticate with the SMTP mail server over TLS.
	 *
	 * @return True if the validation was successful, false otherwise
	 */
	public boolean ValidateCredentials() {

		String password = this.config.getPassword();
		String username = this.config.getUsername();
		String mailHost = this.config.getServerName();
		String port = this.config.getServerPort();
		String protocol = this.config.getServerProtocol();
		if (username == null) {
			return false;
		}
		MailSender ms = new MailSender(mailHost, username, password, false, port, protocol);
		return ms.Validate();
	}

	/**
	 * Transfer file to ftp server.
	 *
	 * @throws IOException
	 */
	public void UploadFilesFTP() throws IOException {
		String userName = this.ftp.getUsername();
		String host = this.ftp.getHostName();
		String destination = this.ftp.getDestination();
		String password = this.ftp.getPassword();
		String securityType = this.ftp.getSecurityType();
		int port = this.ftp.getPort();
		String mode = this.ftp.getMode();
		String location = this.target.toString();
		FTPConnection ftpCon = new FTPConnection(this.parent, host, userName, password, port, mode, destination, securityType);
		if (this.parent.serializeBag.isSelected()) {
			location = location.concat(".zip");
			if (ftpCon.uploadFiles(location, "zip")) {
				this.parent.UpdateResult("File uploaded successfully.", 0);
			} else {
				this.ftpProcess = 1;
				this.parent.UpdateResult("An error occured while uploading on FTP. Cannot upload file.", 0);
			}
		} else if (ftpCon.uploadFiles(location, "")) {
			this.parent.UpdateResult("File uploaded successfully.", 0);
		} else {
			this.ftpProcess = 1;
			this.parent.UpdateResult("An error occured while uploading on FTP. Cannot upload folder.", 0);
		}

	}

	/**
	 * Validate ftp Credentials
	 *
	 * @return
	 * @throws IOException
	 */
	public String ValidateFTPCredentials() throws IOException {

		String userName = this.ftp.getUsername();
		String host = this.ftp.getHostName();
		String destination = this.ftp.getDestination();
		String password = this.ftp.getPassword();
		int port = this.ftp.getPort();
		String mode = this.ftp.getMode();
		String securityType = this.ftp.getSecurityType();

		if (userName == null) {
			return "false";
		}
		FTPConnection ftpCon = new FTPConnection(this.parent, host, userName, password, port, mode, destination, securityType);
		return ftpCon.validateCon();
	}

	/**
	 * Sends summary email to the UK Exactly.
	 * The text of that email is defined here.
	 *
	 * @param target The destination of the file copy
	 */
	public void SendMail(Path target) {
		if (target == null) {
			throw new IllegalArgumentException();
		}
		String host = this.config.getServerName();
		String username = this.config.getUsername();
		String password = this.config.getPassword();
		String port = this.config.getServerPort();
		String protocol = this.config.getServerProtocol();
		MailSender ms = new MailSender(host, username, password, false, port, protocol);
		// Send email to current user.
		this.PrepareAndSendMail(ms, username);
		RecipientsRepo recipientsRepo = new RecipientsRepo();
		List<Recipients> recipients = recipientsRepo.getAll();
		if (!recipients.isEmpty()) {
			for (Recipients recipient : recipients) {
				this.PrepareAndSendMail(ms, recipient.getEmail());
			}
		}

	}

	/**
	 * Prepare text of the email and sent to recipient.
	 *
	 * @param ms      MailSender
	 * @param toEmail string
	 */
	public void PrepareAndSendMail(MailSender ms, String toEmail) {
		PrintWriter writer = null;
		try {
			String from = this.config.getUsername();
			String transferName = this.parent.bagNameField.getText();
			String targetS = this.target.toString();
			if (this.parent.serializeBag.isSelected()) {
				transferName = transferName + ".zip";
				targetS = targetS + ".zip";
			}
			String msg = "";
			String message = "";
			BagInfoRepo bagInfoRepo = new BagInfoRepo();
			List<BagInfo> bagInfo = bagInfoRepo.getOneOrCreateOne();
			for (BagInfo b : bagInfo) {
				message = message + b.getLabel() + ": " + b.getValue() + "\n";
			}
			if (this.ftpProcess == 1) {
				msg = "FTP transfer failed.";
			}
			if (this.sftpProcess == 1) {
				if (msg.equals("")) {
					msg = "FTP transfer failed.";
				} else {
					msg = msg + "\n" + "SFTP transfer failed.";
				}
			}
			String loc = "";
			if (this.parent.ftpDelivery.isSelected() && this.parent.sftpDelivery.isSelected()) {
				String ftpLocation = this.commonUtil.validDestination(this.ftp.getDestination(), transferName);
				String sftpLocation = this.commonUtil.validDestination(this.sftp.getDestination(), transferName);
				loc = "\nFTP Target: " + ftpLocation + "\nSFTP Target: " + sftpLocation;
			} else if (this.parent.ftpDelivery.isSelected()) {
				String ftpLocation = this.commonUtil.validDestination(this.ftp.getDestination(), transferName);
				loc = "\nFTP Target: " + ftpLocation;
			} else if (this.parent.sftpDelivery.isSelected()) {
				String sftpLocation = this.commonUtil.validDestination(this.sftp.getDestination(), transferName);
				loc = "\nSFTP Target: " + sftpLocation;
			}
			// from, to, subject, body
			if (this.parent.ftpDelivery.isSelected() || this.parent.sftpDelivery.isSelected()) {
				String whole_message
						= "Transfer completed: " + new Date()
						+ "\nTransfer Name: " + transferName
						+ "\nTarget: " + targetS
						+ loc
						+ "\nApplication Used: Exactly"
						+ "\nUser: " + this.parent.userNameField.getText()
						+ "\nTotal file count: " + this.bagCount
						+ "\nTotal Bytes: " + this.bagSize
						+ "\nPayload Oxum: " + this.payLoad
						+ "\nBagging Date: " + this.bagDate
						+ "\nBag Size: " + this.bagitSize
						+ "\n" + message
						+ "\n" + msg;
				ms.SetMessage(from, toEmail, "Exactly Digital Transfer", whole_message);
			} else {
				String whole_message = "Transfer completed: " + new Date()
						+ "\nTransfer Name: " + transferName
						+ "\nTarget: " + targetS
						+ "\nApplication Used: Exactly"
						+ "\nUser: " + this.parent.userNameField.getText()
						+ "\nTotal file count: " + this.bagCount
						+ "\nTotal Bytes: " + this.bagSize
						+ "\nPayload Oxum: " + this.payLoad
						+ "\nBagging Date: " + this.bagDate
						+ "\nBag Size: " + this.bagitSize
						+ "\n" + message;
				ms.SetMessage(from, toEmail, "Exactly Digital Transfer", whole_message);
			}
			String name = this.target.getFileName().toString();
			Date dNow = new Date();
			SimpleDateFormat ft = new SimpleDateFormat("yyyyMMdd");
			String myFile = name + "-" + ft.format(dNow) + "-manifest-md5.txt";
			String myCopy = this.target.getParent().toString() + File.separator + myFile;
			File file = new File(this.target.getParent().toString() + File.separator + myFile);
			writer = new PrintWriter(myCopy, "UTF-8");
			List<String> filterList = Arrays.asList(this.manifest.split(", "));
			if (filterList.size() > 0) {
				for (String filter : filterList) {
					String[] data = filter.split("=");
					if (data.length > 1) {
						writer.println(data[1] + "  " + data[0]);
					}
				}
			}
			writer.close();
			String result;
			ms.AttachFile(this.target.getParent().toString() + File.separator + myFile);
			result = ms.Send();
			this.parent.UpdateResult(result, 0);
			file.delete();
			Logger.getLogger(GACOM).log(Level.INFO, "Mail send result: {0}", result);
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

/**
	 * validate input bag
	 *
	 * @param path
	 * @return
	 */
	public int ValidateBag(String path) {
            Path rootDir = Paths.get(path);
            BagReader reader = new BagReader();
            Bag bag;
            String unzippedPath = null;
            boolean requiresCleanUp = false;
            BagVerifier verifier = new BagVerifier();
            try {
                if(path.toLowerCase().endsWith(".zip")) {
                        unzippedPath = path.replace(".zip", "");
                        ZipUtil.unpack(new File(path), new File(unzippedPath));
                        rootDir = Paths.get(unzippedPath, Paths.get(unzippedPath).getFileName().toString());
                        requiresCleanUp = true;
                }
                bag = reader.read(rootDir);
               
                verifier.isValid(bag, false);
            } catch (IOException | UnparsableVersionException | UnsupportedAlgorithmException
                            | MaliciousPathException | InvalidBagitFileFormatException | VerificationException
                            | MissingBagitFileException | MissingPayloadManifestException | CorruptChecksumException
                            | MissingPayloadDirectoryException | InterruptedException | FileNotInPayloadDirectoryException e) {
                e.printStackTrace();
                this.parent.UpdateResult(e.getMessage(), 0);
                return 0;
            }
            finally {
                verifier.close();
                if(requiresCleanUp){
                    retryDelete(unzippedPath);
                    
                }
            }
            return 1;
	}

	/**
	 * Recognize bag structure.
	 *
	 * @param path
	 * @return
	 */
	public int BagRecognition(String path) {
            Path rootDir = Paths.get(path);
            BagReader reader = new BagReader();
            String unzippedPath = null;
            boolean requiresCleanUp = false;
            int success = 1;
            try {
                    if(path.toLowerCase().endsWith(".zip")){
                            unzippedPath = path.replace(".zip","");
                            ZipUtil.unpack(new File(path), new File(unzippedPath));
                            rootDir = Paths.get(unzippedPath, Paths.get(unzippedPath).getFileName().toString());
                            reader.read(rootDir);
                            requiresCleanUp = true;
                    }
                    reader.read(rootDir);
            } catch (IOException | InvalidBagitFileFormatException | UnparsableVersionException
                            | UnsupportedAlgorithmException | MaliciousPathException e) {
                    e.printStackTrace();
                    this.parent.UpdateResult(e.getMessage(), 0);
                    success = 0;
            }
            finally {
                    if(requiresCleanUp){
                        retryDelete(unzippedPath);
                    }
            }
            return success;
	}
        
            /**
     * Retry delete in case external program has lock on target file.
     *
     * @param path
     */
	private void retryDelete(String path){
            this.parent.UpdateResult("Cleaning up...", 0);
            int count = 0;
            int maxTries = 3;
            while (true) {
                try {
                    FileUtils.deleteDirectory(new File(path));
                    break;
                } catch (IOException e) {
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (++count == maxTries) {
                        this.parent.UpdateResult("Failed to clean up directory. If you are sending directly to dropbox or something similar, you will need to manually delete the unzipped version of this bag:", 0);
                        this.parent.UpdateResult(path, 0);
                    }
                }
            }
	}

	/**
	 * create bag-info.xml file at transfer destination
	 */
	public void createXML(String payload, String date, String size) {
		try {
			char[] charArray = {'<', '>', '&', '"', '\\', '!', '#', '$', '%', '\'', '(', ')', '*', '.', ':', '+', ',', '/', ';', '=', '?', '@', '[', ']', '^', '`', '{', '|', '}', '~'};
			//		List<char[]> asList = Arrays.asList(charArray);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("transfer_metadata");
			doc.appendChild(rootElement);

			Attr attr1 = doc.createAttribute("xmlns:xsi");
			attr1.setValue("http://www.w3.org/2001/XMLSchema-instance");
			rootElement.setAttributeNode(attr1);

			Element payLoad = doc.createElement("Payload-Oxum");
			payLoad.appendChild(doc.createTextNode(payload));
			rootElement.appendChild(payLoad);

			Element baggingDate = doc.createElement("Bagging-Date");
			baggingDate.appendChild(doc.createTextNode(date));
			rootElement.appendChild(baggingDate);

			Element bagsize = doc.createElement("Bag-Size");
			bagsize.appendChild(doc.createTextNode(size));
			rootElement.appendChild(bagsize);

			BagInfoRepo bagInfoRepo = new BagInfoRepo();
			List<BagInfo> bagInfo = bagInfoRepo.getOneOrCreateOne();

			for (BagInfo b : bagInfo) {
				StringBuilder stringBuilder = new StringBuilder();
				char[] txt = Normalizer.normalize(b.getLabel(), Normalizer.Form.NFD).toCharArray();
				for (int i = 0; i < b.getLabel().length(); i++) {
					int check = 0;
					for (int j = 0; j < charArray.length; j++) {
						if (txt[i] == charArray[j]) {
							check = 1;

						}
					}
					if (check == 0) {
						stringBuilder.append(txt[i]);
					}
				}
				Element firstname = doc.createElement(stringBuilder.toString().replace(" ", "-"));
				firstname.appendChild(doc.createTextNode(b.getValue().trim()));
				rootElement.appendChild(firstname);

			}
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(this.target.toString() + File.separator + "bag-info.xml"));
			transformer.transform(source, result);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (DOMException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void generateSystemDataFile() {
		try {
			FileWriter fileWritter = new FileWriter(this.target.toString().concat("/FileSystemData.txt"), true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			String fileSystem = this.parent.fileSystem.toString();
			bufferWritter.write(fileSystem);
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getLogger(GACOM).log(Level.INFO, "Issue while writing file system.", e);
		}
	}

	private void generateCsvFile(String payload, String date, String size) {
		BagInfoRepo bagInfoRepo = new BagInfoRepo();
		List<String> labels = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		labels.add("Payload Oxum");
		labels.add("Bagging Date");
		labels.add("Bag Size");
		values.add(payload);
		values.add(date);
		values.add(size);
		List<BagInfo> bagInfo = bagInfoRepo.getOneOrCreateOne();
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(this.target.toString() + File.separator + "bag-info.csv"));
			for (BagInfo b : bagInfo) {
				labels.add(b.getLabel().toString());
				values.add(b.getValue().toString());
			}
			labels.toArray();
			String[] newLabels = new String[labels.size()];
			newLabels = labels.toArray(newLabels);
			String[] newValues = new String[values.size()];
			newValues = values.toArray(newValues);
			writer.writeNext(newLabels);
			writer.writeNext(newValues);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, e);
		}
	}

	public String ValidateSFTPCredentials() {
		String userName = this.sftp.getUsername();
		String host = this.sftp.getHost();
		String destination = this.sftp.getDestination();
		String password = this.sftp.getPassword();
		int port = this.sftp.getPort();
		int type = this.sftp.getType();
		if (userName == null) {
			return "false";
		}
		String privateKey = this.sftp.getPrivateKey();
		String passPhrase = this.sftp.getPassPhrase();
		SFTPUtil util = new SFTPUtil(this.parent, host, userName, password, port, type, destination, privateKey, passPhrase);
		return util.validateCon();
	}

	public void UploadFilesSFTP() {
		String userName = this.sftp.getUsername();
		String host = this.sftp.getHost();
		String destination = this.sftp.getDestination();
		String password = this.sftp.getPassword();
		int port = this.sftp.getPort();
		int type = this.sftp.getType();
		String privateKey = this.sftp.getPrivateKey();
		String passPhrase = this.sftp.getPassPhrase();
		SFTPUtil util = new SFTPUtil(this.parent, host, userName, password, port, type, destination, privateKey, passPhrase);
		String location = this.target.toString();
		if (this.parent.serializeBag.isSelected()) {
			location = location.concat(".zip");
		}
		System.out.println(location);
		File file = new File(location);
		if (util.uploadFiles(file)) {
			this.parent.UpdateResult("File uploaded successfully on SFTP.", 0);
		} else {
			this.sftpProcess = 1;
			this.parent.UpdateResult("An error occured while uploading on SFTP. Cannot upload file.", 0);
		}
	}
}