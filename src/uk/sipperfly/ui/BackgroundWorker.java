/* 
 * Exactly Digital Files Transfer Tool
 * Author: Nouman Tayyab (nouman@avpreserve.com)
 * Version: 1.0
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the Gates Archive.
 * Support: info@gatesarchive.com
 * Copyright 2013 Gates Archive
 */
package uk.sipperfly.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import static uk.sipperfly.ui.MainFrame.GACOM;

// Bagit imports
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;

import gov.loc.repository.bagit.PreBag;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.loc.repository.bagit.verify.impl.CompleteVerifierImpl;
import gov.loc.repository.bagit.verify.impl.ParallelManifestChecksumVerifier;
import gov.loc.repository.bagit.verify.impl.ValidVerifierImpl;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class implements the background worker thread.
 * Work happens on this thread so that the MainFraim GUI remains responsive.
 *
 * @author Nouman Tayyab
 */
class BackgroundWorker extends SwingWorker<Integer, Void> {

	private final List<String> sources;
	private final MainFrame parent;
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
	private String unbagDestination = "";

	/**
	 * Constructor for BackgroundWorker
	 *
	 * @param sources
	 * @param parent
	 * @param process
	 * @throws IOException
	 */
	public BackgroundWorker(final List<String> sources, MainFrame parent, int process) throws IOException {
		if (sources == null || sources.isEmpty()
				|| parent == null) {
			throw new IllegalArgumentException();
		}
		ConfigurationsRepo configRepo = new ConfigurationsRepo();
		FTPRepo ftpRepo = new FTPRepo();
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

	}

	/**
	 * The main logic for the work that the background thread does.
	 * This method is automatically called by the threading framework.
	 * <p> The following actions are performed:
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
		try {
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
//					Border border = BorderFactory.createLineBorder(Color.BLUE, 2);				

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
					return -1;
				}
				this.parent.UpdateResult("Verifying Transfer...", 1);
				if (this.config.getEmailNotifications()) {
					// validate email auth
					if (!ValidateCredentials()) {
						this.parent.UpdateResult("Credentials not valid. Please update Email Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update Email settings.");
						return -1;
					}
				}
				// check if drop location folder is not set.

				if (this.parent.editInputDir1.getText() == null || this.parent.editInputDir1.getText().isEmpty()) {
					this.parent.UpdateResult("Please select Transfer destination.", 0);
					Logger.getLogger(GACOM).log(Level.SEVERE, "Please select Transfer destination.");
					return -1;
				}
				// validate bag name.
				if (this.parent.bagNameField.getText() == null || this.parent.bagNameField.getText().isEmpty()) {
					this.parent.UpdateResult("Please provide Transfer name.", 0);
					Logger.getLogger(GACOM).log(Level.SEVERE, "Please provide Transfer name.");
					return -1;
				}
				if (this.config.getEmailNotifications()) {
					RecipientsRepo recipientsRepo = new RecipientsRepo();
					List<Recipients> recipients = recipientsRepo.getAll();
					if (recipients.size() < 1) {
						this.parent.UpdateResult("Please add at least one recipient.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Please add at least one recipient.");
						return -1;
					}
				}
				if (this.parent.ftpDelivery.isSelected()) {
					if (!ValidateFTPCredentials()) {
						this.parent.UpdateResult("Credentials not valid. Please update FTP Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update FTP settings.");
						return -1;
					}
				}
				// Set the tragetPath of bag.
				this.setTragetPath();
				//transfer
				this.parent.UpdateResult("Transfering files...", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Transfering files...");
				Path target = TransferFiles();
				if (this.isCancelled()) {
					Logger.getLogger(GACOM).log(Level.INFO, "Canceling Transfer Files task.");
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

				if (this.parent.ftpDelivery.isSelected()) {
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling Upload data to FTP");
						return -1;
					}
					if (!ValidateFTPCredentials()) {
						this.parent.UpdateResult("Credentials not valid. Please update FTP Settings.", 0);
						Logger.getLogger(GACOM).log(Level.SEVERE, "Credentials not valid. Please update FTP settings.");
						return -1;

					}
					this.parent.jProgressBar2.setValue(this.parent.totalFiles + 1);
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling Upload data to FTP");
						return -1;
					}
					this.parent.UpdateResult("Uploading data on FTP ...", 0);
					Logger.getLogger(GACOM).log(Level.INFO, "Uploading data on FTP ...");
					UploadFilesFTP();
					this.parent.jProgressBar2.setValue(this.parent.totalFiles + 2);
				}
				if (this.isCancelled()) {
					Logger.getLogger(GACOM).log(Level.INFO, "Canceling send notification email(s).");
					return -1;
				}
				// send email to GA
				if (this.config.getEmailNotifications()) {
					this.parent.UpdateResult("Preparing to send notification email(s)...", 0);
					Logger.getLogger(GACOM).log(Level.INFO, "Preparing to send notification email(s)...");
					SendMail(target);
					if (this.isCancelled()) {
						Logger.getLogger(GACOM).log(Level.INFO, "Canceling send notification email(s)");
						return -1;
					}
					if (this.parent.ftpDelivery.isSelected()) {
						this.parent.jProgressBar2.setValue(this.parent.totalFiles + 3);
					} else {
						this.parent.jProgressBar2.setValue(this.parent.totalFiles + 1);
					}
				}
				Thread.sleep(2000);
				this.parent.btnCancel.setVisible(false);
				// update UI
				this.parent.list.resetEntryList();
				this.resetTransferFiles();
				this.parent.UpdateResult("Session complete.", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Session complete.");

			}
			return 1;
		} catch (Exception ex) {
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
		this.parent.btnCancel.setVisible(false);
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
		// create it if it doesn't exist
		if (!Files.exists(targetDirPath)) {
			Files.createDirectory(targetDirPath);
		}

		// use the bag name that User selected instead of Source directory/file name.
		String name = this.parent.bagNameField.getText();
		File bagName = new File(name);
		this.target = this.commonUtil.combine(targetDirPath, bagName.toPath());

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
		this.target = this.commonUtil.combine(targetDirPath, bagName.toPath());
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
		} else if (this.process == 1 && this.ftpProcess == 0) {
			this.parent.UpdateResult("Transfer completed successfully.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "Transfer completed successfully.");
		} else if (this.process == 1 && this.ftpProcess == 1) {
			this.parent.UpdateResult("FTP transfer failed; local transfer completed successfully.", 0);
			Logger.getLogger(GACOM).log(Level.INFO, "FTP transfer failed; local transfer completed successfully.");
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
			Logger.getLogger(GACOM).log(Level.INFO, "Successfully unpacked Bagit bag at " + this.unbagDestination);
		}
		this.ftpProcess = 0;
		this.unbagDestination = "";
	}

	/**
	 * Bags the input folder using the bagit Java library from the Library of Congress.
	 * Any errors are reported to the parent UI.
	 * Creates a success semaphore in the source directory if the transfer succeeded.
	 *
	 * @see* http://www.digitalpreservation.gov/documents/bagitspec.pdf
	 */
	public void BagFolder() throws NoSuchAlgorithmException {
		// bag the folder
		BagFactory bagFactory = new BagFactory();

		// make the bag
		PreBag preBag = bagFactory.createPreBag(this.target.toFile());

		// keep empty folders?
		Bag bag = preBag.makeBagInPlace(BagFactory.LATEST, false);
		BagInfoTxt bagInfoTxt = bag.getBagInfoTxt();
		BagInfoRepo bagInfoRepo = new BagInfoRepo();
//		String bagInfoText = "";
		String bagInfoText = this.commonUtil.createBagInfoTxt(bagInfoRepo.getOneOrCreateOne());
		String originalChecksum = this.commonUtil.checkSum(this.target.toString().concat("/bag-info.txt"));
		try {
			FileWriter fileWritter = new FileWriter(this.target.toString().concat("/bag-info.txt"), true);
			BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
			bufferWritter.write(bagInfoText);
			bufferWritter.close();
		} catch (IOException e) {
			e.printStackTrace();
			Logger.getLogger(GACOM).log(Level.INFO, "Issue while writing file.", e);
		}
		this.bagCount = bag.getPayload().size();
		String payloadOxum = bagInfoTxt.getPayloadOxum();

		List<String> payload = Arrays.asList(payloadOxum.split("\\."));
		if (payload.size() > 0) {
			this.bagSize = payload.get(0);
		}
		int zip = 0;
		if (this.parent.serializeBag.isSelected()) {
			zip = 1;
		}
		if (this.parent.ftpDelivery.isSelected()) {
			this.commonUtil.CreateSuccessSemaphore(this.config.getUsername(), this.parent.bagNameField.getText(), this.target, this.ftp.getDestination(), this.bagSize, bag.getPayload().size(), zip);
		} else {
			this.commonUtil.CreateSuccessSemaphore(this.config.getUsername(), this.parent.bagNameField.getText(), this.target, "", this.bagSize, bag.getPayload().size(), zip);
		}

		String newChecksum = this.commonUtil.checkSum(this.target.toString().concat("/bag-info.txt"));
		try {
			this.commonUtil.replaceTextInFile(this.target.toString().concat("/tagmanifest-md5.txt"), originalChecksum, newChecksum);
		} catch (IOException e) {
			Logger.getLogger(GACOM).log(Level.INFO, "Issue while updating tagmanifest-md5.txt", e);
		}

		this.createXML();
		//		this.parent.UpdateProgressBar(this.parent.tranferredFiles);
		//		this.parent.UpdateProgressBar(this.parent.tranferredFiles);

		try {
			bag.makeComplete();
			bag.close();

// verify the bag
//			CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
//			ParallelManifestChecksumVerifier manifestVerifier = new ParallelManifestChecksumVerifier();
//			ValidVerifierImpl validVerifier = new ValidVerifierImpl(completeVerifier, manifestVerifier);
//			SimpleResult result = validVerifier.verify(bagFactory.createBag(bag));
			numberOfFiles = bag.getPayload().size(); // get the number of payload files
			numberOfFiles += 4; // add the standard bagit files

//			///if (result.isSuccess()) {
			Logger.getLogger(GACOM).log(Level.INFO, "Bag created. Number of files is {0}", numberOfFiles);

			// enable file transfer
//			this.parent.jProgressBar2.setMaximum(numberOfFiles);
//			} else {
//				this.parent.UpdateResult("Bag creation failed. Contact support.");
//				Logger.getLogger(GACOM).log(Level.SEVERE, "Bag creation failed. Contact support.");
//			}
			if (this.parent.serializeBag.isSelected()) {
				this.parent.UpdateResult("Serializing bag...", 0);
				Logger.getLogger(GACOM).log(Level.INFO, "Serializing bag...");
				ZipUtils zipUtil = new ZipUtils();
				zipUtil.setSourceFolder(this.target.toString());
				zipUtil.setOutputZipFile(this.target.toString().concat(".zip"));
				zipUtil.setBagName(this.parent.bagNameField.getText());
				zipUtil.zip();
				FileUtils.deleteDirectory(this.target.toFile());
//				ZipWriter zipWriter = new ZipWriter(bagFactory);
//				zipWriter.write(bag, bag.getFile());

//				this.parent.tranferredFiles += 5;
//				this.parent.UpdateProgressBar(this.parent.tranferredFiles);
			}
		} catch (IOException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "Error closing the bag", ex);
		}
		if (this.parent.totalFiles > this.parent.tranferredFiles) {
			this.parent.UpdateProgressBar(this.parent.totalFiles);
		}
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
		int extra = 0;
//		if (this.parent.serializeBag.isSelected()) {
//			extra = 10;
//		}
		int totalFiles = this.parent.totalFiles;
		Logger.getLogger(GACOM).log(Level.INFO, "Max Progress bar count: ".concat(Integer.toString(this.parent.totalFiles)));
		if (this.parent.ftpDelivery.isSelected()) {
			totalFiles = totalFiles + 2;
		}
		if (this.config.getEmailNotifications()) {
			totalFiles = totalFiles + 1;
		}
		this.parent.jProgressBar2.setMaximum(totalFiles);
		for (String source : this.sources) {

			File sourceFile = new File(source);
			File folder = new File(sourceFile.getName());
			Path folderTarget = this.commonUtil.combine(this.target, folder.toPath());
			if (!Files.exists(folderTarget)) {
				Files.createDirectory(folderTarget);
			}
			ft.setTargetPath(folderTarget);
			this.parent.UpdateResult("Transfering files...", 0);
			Path inputSource = sourceFile.toPath();
			ft.setSourcePath(inputSource);
			ft.Perform();

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
		int port = this.ftp.getPort();
		String mode = this.ftp.getMode();
		String location = this.target.toString();
		FTPConnection ftpCon = new FTPConnection(host, userName, password, port, mode, destination);
		if (this.parent.serializeBag.isSelected()) {
			location = location.concat(".zip");
			if (ftpCon.uploadFiles(location, "zip")) {
				this.parent.UpdateResult("File uploaded successfully.", 0);
			} else {
				this.ftpProcess = 1;
				this.parent.UpdateResult("An error occured while uploading on FTP. Cannot upload file.", 0);
			}
		} else {
			if (ftpCon.uploadFiles(location, "")) {
				this.parent.UpdateResult("File uploaded successfully.", 0);
			} else {
				this.ftpProcess = 1;
				this.parent.UpdateResult("An error occured while uploading on FTP. Cannot upload folder.", 0);
			}
		}

	}

	/**
	 * Validate ftp Credentials 
	 * 
	 * @return
	 * @throws IOException 
	 */
	public boolean ValidateFTPCredentials() throws IOException {

		String userName = this.ftp.getUsername();
		String host = this.ftp.getHostName();
		String destination = this.ftp.getDestination();
		String password = this.ftp.getPassword();
		int port = this.ftp.getPort();
		String mode = this.ftp.getMode();

		if (userName == null) {
			return false;
		}
		FTPConnection ftpCon = new FTPConnection(host, userName, password, port, mode, destination);
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
		String from = this.config.getUsername();
		String transferName = this.parent.bagNameField.getText();
		String targetS = this.target.toString();
		if (this.parent.serializeBag.isSelected()) {
			transferName = transferName + ".zip";
			targetS = targetS + ".zip";
		}
		String msg = "";
		if (this.ftpProcess == 1) {
			msg = "FTP transfer failed.";
		}
		// from, to, subject, body
		if (this.parent.ftpDelivery.isSelected()) {
			String ftpLocation = this.ftp.getDestination();
			if (!ftpLocation.startsWith("/")) {
				ftpLocation = "/" + ftpLocation;
			}
			if (ftpLocation.endsWith("/")) {
				ftpLocation = ftpLocation + transferName;
			} else {
				ftpLocation = ftpLocation + "/" + transferName;
			}
			ms.SetMessage(from, toEmail,
					"Exactly Digital Transfer",
					"Transfer completed at: " + new Date()
					+ "\nTransfer Name: " + transferName
					+ "\nTarget: " + targetS
					+ "\nFTP Target: " + ftpLocation
					+ "\nApplication Used: Exactly"
					+ "\nUser: " + this.parent.userNameField.getText()
					+ "\nTransfer Size: " + this.bagSize + " bytes"
					+ "\nFiles count: " + this.bagCount
					+ "\n" + msg);
		} else {
			ms.SetMessage(from, toEmail,
					"Exactly Digital Transfer",
					"Transfer completed at: " + new Date()
					+ "\nTransfer Name: " + transferName
					+ "\nTarget: " + targetS
					+ "\nApplication Used: Exactly"
					+ "\nUser: " + this.parent.userNameField.getText()
					+ "\nTransfer Size: " + this.bagSize + " bytes"
					+ "\nFiles count: " + this.bagCount);
		}
		// attached bagit if needed in future.
//		ms.AttachFile(source.toString() + File.separator + "bag-info.txt");
		// Disabled as this file may be too big for the mail server.
		// ms.AttachFile(source.toString() + "\\manifest-md5.txt");
		String result = ms.Send();
		this.parent.UpdateResult(result, 0);
		Logger.getLogger(GACOM).log(Level.INFO, "Mail send result: {0}", result);
	}

	/**
	 * validate input bag
	 * 
	 * @param path
	 * @return 
	 */
	public int ValidateBag(String path) {
		BagFactory bagfactory = new BagFactory();
		File file = new File(path);
		Bag cbag = bagfactory.createBag(file);//(Bag) file;
		CompleteVerifierImpl completeVerifier = new CompleteVerifierImpl();
		ParallelManifestChecksumVerifier manifestVerifier = new ParallelManifestChecksumVerifier();
		ValidVerifierImpl validVerifier = new ValidVerifierImpl(completeVerifier, manifestVerifier);
		SimpleResult result = validVerifier.verify(cbag);
		if (result.isSuccess()) {
			return 1;
		} else {
			this.parent.UpdateResult(result.getMessages().toString(), 0);
			return 0;
		}
	}

	/**
	 * Recognize bag structure.
	 * 
	 * @param path
	 * @return 
	 */
	public int BagRecognition(String path) {
		BagFactory bagfactory = new BagFactory();
		boolean result = true;
		File file = new File(path);
		Bag cbag = bagfactory.createBag(file);//(Bag) file;
		List<String> errorMessages = cbag.verifyComplete().getMessages();
		int index = errorMessages.size();
		if (index != 0) {
			int _index = index - 1;
			if (errorMessages.get(_index).contains("Bag does not have any payload manifests.") || errorMessages.get(_index).contains("Bag does not have bagit.txt.")) {
				result = false;
			}
		}

		if (result) {
			return 1;
		} else {
			this.parent.UpdateResult(errorMessages.toString(), 0);
			return 0;
		}
	}

	/**
	 * create bag-info.xml file at transfer destination
	 */
	public void createXML() {
		try {
			char[] charArray = {'<', '>', '&', '"', '\\', '!', '#', '$', '%', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '=', '?', '@', '[', ']', '^', '`', '{', '|', '}', '~'};
			//		List<char[]> asList = Arrays.asList(charArray);
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			String file = this.target.toString() + File.separator + "bag-info.txt";
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("transfer_metadata");
			doc.appendChild(rootElement);
			Attr attr = doc.createAttribute("xsi:noNamespaceSchemaLocation");
			attr.setValue("../bert/AVPS/Projects/UK-Sipperfly/bag-info.xsd");
			rootElement.setAttributeNode(attr);
			Attr attr1 = doc.createAttribute("xmlns:xsi");
			attr1.setValue("http://www.w3.org/2001/XMLSchema-instance");
			rootElement.setAttributeNode(attr1);

			while ((line = br.readLine()) != null) {
				StringBuilder stringBuilder = new StringBuilder();
				String[] text = line.split(": ");
				char[] txt = Normalizer.normalize(text[0], Normalizer.Form.NFD).toCharArray();
				for (int i = 0; i < text[0].length(); i++) {
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
				firstname.appendChild(doc.createTextNode(text[1]));
				//			firstname.appendChild(doc.createTextNode(text[1]));
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
		} catch (FileNotFoundException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerConfigurationException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		} catch (TransformerException ex) {
			Logger.getLogger(BackgroundWorker.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}