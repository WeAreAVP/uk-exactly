/* 
 * Exactly
 * Author: Nouman Tayyab (nouman@avpreserve.com)
 * Author: Rimsha Khalid (rimsha@avpreserve.com)
 * Version: 0.1.5
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the UK Exactly
 * Support: info@avpreserve.com
 * License: Apache 2.0
 * Copyright: University of Kentucky (http://www.uky.edu). All Rights Reserved
 *
 */
package uk.sipperfly.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uk.sipperfly.persistent.BagInfo;
import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.persistent.FTP;
import uk.sipperfly.persistent.Recipients;
import uk.sipperfly.persistent.SFTP;
import uk.sipperfly.repository.BagInfoRepo;
import uk.sipperfly.repository.ConfigurationsRepo;
import uk.sipperfly.repository.FTPRepo;
import uk.sipperfly.repository.RecipientsRepo;
import uk.sipperfly.repository.SFTPRepo;

/**
 *
 * @author noumantayyab
 */
public class CommonUtil {

	private static String GACOM = "com.UKExactly";
	/**
	 * Bytes conversion value in GB.
	 */
	private final long GB = 1024 * 1024 * 1024;
	private final String transferSemaphore = "TransferComplete.txt";
	BagInfoRepo bagInfoRepo = new BagInfoRepo();
	RecipientsRepo recipientsRepo = new RecipientsRepo();
	FTPRepo FTPRepo = new FTPRepo();
	SFTPRepo SFTPRepo = new SFTPRepo();
	ConfigurationsRepo configurationsRepo = new ConfigurationsRepo();

	/**
	 * Convert the bytes size to GB.
	 *
	 * @param size in bytes
	 * @return size in GB
	 */
	public long convertBytestoGB(long size) {
		size = size / this.GB;
		return size;
	}

	/**
	 * Count files in a directory (including files in all subdirectories)
	 *
	 * @param directory the directory to start in
	 * @return the total number of files
	 */
	public static int countFilesInDirectory(File directory, String filters) {
		int count = 0;
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				String fileName = file.getName();
				boolean ignore = checkIgnoreFiles(fileName, filters);
				if (!ignore) {
					count++;
				}
			}
			if (file.isDirectory()) {
				count += countFilesInDirectory(file, filters);
			}
		}
		return count;
	}

	/**
	 * Check the file against ignore filter.
	 * Return false if file is not in ignore list and vice versa.
	 *
	 * @param fileName  Name of the file including extention
	 * @param dbFilters Comma separated ignore file names
	 * @return boolean
	 */
	public static boolean checkIgnoreFiles(String fileName, String dbFilters) {
		boolean ignore = false;

		if (dbFilters != null && !dbFilters.equals("")) {
			List<String> filterList = Arrays.asList(dbFilters.split(","));

			int lastIndex = fileName.lastIndexOf('.');
			String extension = "";
			if (lastIndex != -1) {
				extension = fileName.substring(lastIndex);
			}
			if (filterList.size() > 0) {
				for (String filter : filterList) {
					filter = filter.replace("*", "");
					if (fileName.equalsIgnoreCase(filter) || extension.equals(filter)) {
						ignore = true;

					}
				}
			}
		}
		return ignore;
	}

	/**
	 * Combines two paths into one.
	 *
	 * @param path1 The root path.
	 * @param path2 The filename to append to the root.
	 * @return The combined path
	 */
	public static Path combine(Path path1, Path path2) {
		if (path1 == null
				|| path2 == null) {
			throw new IllegalArgumentException();
		}

		return new File(path1.toString(), path2.toString()).toPath();
	}

	/**
	 * Creates a semaphore text file in the source directory to let the user know that the transfer has completed successfully.
	 * It contains the name of the user that initiated the transfer, Transfer title, local and ftp location and a timestamp.
	 *
	 * @param username     The name of the user that initiated the transfer.
	 * @param transferName transfer title
	 * @param source       local path for transfer
	 * @param ftp          ftp path for transfer if ftp deliver is checked
	 * @param sftp         SFTP path for transfer if SFTP deliver is checked
	 * @param bagSize      size in bytes
	 * @param bagCount     total files
	 * @param zip          1 if zip is checked, 0 otherwise
	 * @return True if the semaphore was created successfully, false otherwise.
	 */
	public boolean CreateSuccessSemaphore(String username, String transferName, Path source, String ftp, String sftp, String bagSize, int bagCount, int zip, String sender, String recipients, int sendEmail) {
		try {
			File trasferFile = new File(transferSemaphore);
			Path semaphorePath = this.combine(source, trasferFile.toPath());
			File semaphore = new File(semaphorePath.toString());
			semaphore.createNewFile();
			String target = source.toString();
			if (zip == 1) {
				target = target + ".zip";
			}
			try (PrintStream ps = new PrintStream(semaphore)) {
				if (!ftp.equals("") && !sftp.equals("")) {
					ftp = validDestination(ftp, transferName);
					sftp = validDestination(sftp, transferName);
					ps.printf("Transfer completed: %s %nTransfer name: %s %nTarget: %s  %nFTP Target: %s %nSFTP Target: %s %nApplication used: %s %nUser: %s %nTotal file count: %s%nTotal Bytes: %s\n",
							new Date(), transferName, target, ftp, sftp, "Exactly", username, bagCount, bagSize);
				} else if (!ftp.equals("")) {
					ftp = validDestination(ftp, transferName);
					ps.printf("Transfer completed: %s %nTransfer name: %s %nTarget: %s  %nFTP Target: %s %nApplication used: %s %nUser: %s %nTotal file count: %s%nTotal Bytes: %s\n",
							new Date(), transferName, target, ftp, "Exactly", username, bagCount, bagSize);
				} else if (!sftp.equals("")) {
					sftp = validDestination(sftp, transferName);
					ps.printf("Transfer completed: %s %nTransfer name: %s %nTarget: %s  %nSFTP Target: %s %nApplication used: %s %nUser: %s %nTotal file count: %s%nTotal Bytes: %s\n",
							new Date(), transferName, target, sftp, "Exactly", username, bagCount, bagSize);
				} else {
					ps.printf("Transfer completed: %s %nTransfer name: %s %nTarget: %s %nApplication used: %s %nUser: %s %nTotal file count: %s%nTotal Bytes: %s\n",
							new Date(), transferName, target, "Exactly", username, bagCount, bagSize);
				}
				if (sendEmail == 1) {
					ps.printf("%nEmail address of sender: %s %nEmail address of recipient(s): %s", sender, recipients);
				}
				ps.flush();
				ps.close();
			}
			return true;
		} catch (IOException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "Failed to create transfer complete semaphore", ex);
		}
		return false;
	}

	public String validDestination(String ftp, String name) {
		if (!ftp.startsWith("/")) {
			ftp = "/" + ftp;
		}
		if (ftp.endsWith("/")) {
			ftp = ftp + name;
		} else {
			ftp = ftp + "/" + name;
		}
		return ftp;
	}

	/**
	 * Create bag info text for bag-info.txt file.
	 *
	 * @param bagInfo
	 * @return text for bag-info.txt file.
	 */
	public String createBagInfoTxt(List<BagInfo> bagInfo) {
		StringBuilder stringBuilder = new StringBuilder();
		for (BagInfo b : bagInfo) {
			String text = b.getLabel().concat(": ").concat(b.getValue()).replaceAll("\\s+", " ").trim();
			int startIndex = 0;
			int endIndex = 78;
			if (text.length() > 79) {
				//put a newline and tab every 79 characters as outlined in bag-it spec section 2.2.2
				String seperatedString = text.replaceAll(
						"(.{79})",
						"$1" + System.getProperty("line.separator") + "\t"
				);
				stringBuilder.append(seperatedString);
				stringBuilder.append(System.getProperty("line.separator"));
			} else {
				stringBuilder.append(b.getLabel().concat(": ").concat(b.getValue()));
				stringBuilder.append(System.getProperty("line.separator"));
			}
		}
		String finalString = stringBuilder.toString();
		return finalString;
	}

	/**
	 * Search and replace text in file.
	 *
	 * @param filePath
	 * @param oldString
	 * @param newString
	 * @throws IOException
	 */
	public void replaceTextInFile(String filePath, String oldString, String newString) throws IOException {
		Path path = Paths.get(filePath);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll(oldString, newString);
		Files.write(path, content.getBytes(charset));
	}

	/**
	 * Copy file from source to destination.
	 *
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	public static void copyFileUsingJava7Files(File source, File dest)
			throws IOException {

		Files.copy(source.toPath(), dest.toPath());

	}

	/**
	 * Calculate checksum of a File using MD5 algorithm
	 *
	 * @param path
	 * @return
	 */
	public static String checkSum(String path) {
		String checksum = null;
		try {
			FileInputStream fis = new FileInputStream(path);
			MessageDigest md = MessageDigest.getInstance("MD5");

			//Using MessageDigest update() method to provide input
			byte[] buffer = new byte[8192];
			int numOfBytesRead;
			while ((numOfBytesRead = fis.read(buffer)) > 0) {
				md.update(buffer, 0, numOfBytesRead);
			}
			fis.close();
			byte[] hash = md.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1) {
					hexString.append('0');
				}
				hexString.append(hex);
			}
			checksum = hexString.toString();
//			checksum = new BigInteger(1, hash).toString(16); //don't use this, truncates leading zero
		} catch (IOException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "IOException: ", ex);
		} catch (NoSuchAlgorithmException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "NoSuchAlgorithmException: ", ex);
		}

		return checksum;
	}

	/**
	 * Unpack bag.
	 *
	 * @param source      path of source bag
	 * @param destination where it should be unpacked
	 * @param folder      name of the folder after unpack
	 * @throws IOException
	 */
	public void unBag(String source, String destination, String folder) throws IOException {
		File workDir = new File(source);
		File sourceDir = new File(source + File.separator + "data");
		File target = new File(destination);
		File metadata = new File(destination + File.separator + folder + "_metadata");
		FileUtils.copyDirectory(sourceDir, target);
		FileUtils.copyFile(new File(source + File.separator + "bag-info.txt"), new File(metadata + File.separator + "bag-info.txt"));
		FileUtils.copyFile(new File(source + File.separator + "bag-info.xml"), new File(metadata + File.separator + "bag-info.xml"));
		FileUtils.copyFile(new File(source + File.separator + "bag-info.csv"), new File(metadata + File.separator + "bag-info.csv"));
		FileUtils.copyFile(new File(source + File.separator + "manifest-md5.txt"), new File(metadata + File.separator + "manifest-md5.txt"));
		FileUtils.copyFile(new File(source + File.separator + "TransferComplete.txt"), new File(metadata + File.separator + "TransferComplete.txt"));
		FileUtils.copyFile(new File(source + File.separator + "FileSystemData.txt"), new File(metadata + File.separator + "FileSystemData.txt"));
		FileUtils.deleteDirectory(workDir);
		File dir = new File(target.getParent() + File.separator + folder);
		target.renameTo(dir);
	}

	/**
	 * Create xml file to export
	 *
	 * @param recipient
	 * @param ftp
	 * @param config
	 * @param bagInfo
	 * @param path
	 */
	public String createXMLExport(List<Recipients> recipient, FTP ftp, SFTP sftp, Configurations config, List<BagInfo> bagInfo, String path, Boolean template) {
		try {
			char[] charArray = {'<', '>', '&', '"', '\\', '!', '#', '$', '%', '\'', '(', ')', '*', '+', ',', '/', ':', ';', '=', '?', '@', '[', ']', '^', '`', '{', '|', '}', '~'};
			String name = "Exactly_Configuration_" + System.currentTimeMillis() + ".xml";
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element temElement = doc.createElement("Exactly");
			doc.appendChild(temElement);
			//////bag info
			Element bagElement = doc.createElement("Metadata");
			temElement.appendChild(bagElement);

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
				firstname.appendChild(doc.createTextNode(b.getValue()));
				Attr attr = doc.createAttribute("label");
				attr.setValue(Normalizer.normalize(b.getLabel(), Normalizer.Form.NFD).toString());
				firstname.setAttributeNode(attr);
				bagElement.appendChild(firstname);
			}
			//////emails
			Element recipientElement = doc.createElement("Recipients");
			temElement.appendChild(recipientElement);

			for (Recipients r : recipient) {
				if (r.getEmail() != null) {
					Element mail = doc.createElement("Email");
					mail.appendChild(doc.createTextNode(r.getEmail()));
					recipientElement.appendChild(mail);
				}
			}
			//////////ftp
			Element ftpElement = doc.createElement("FTP");
			temElement.appendChild(ftpElement);

			Element server = doc.createElement("Host");
			if (ftp.getHostName() != null) {
				server.appendChild(doc.createTextNode(ftp.getHostName()));
			} else {
				server.appendChild(doc.createTextNode(""));
			}
			ftpElement.appendChild(server);

			Element user = doc.createElement("Username");
			if (ftp.getUsername() != null) {
				user.appendChild(doc.createTextNode(ftp.getUsername()));
			} else {
				user.appendChild(doc.createTextNode(""));
			}
			ftpElement.appendChild(user);

			Element password1 = doc.createElement("Password");

			if (ftp.getPassword() == null || ftp.getPassword().equals("")) {
				password1.appendChild(doc.createTextNode(""));
			} else {
				password1.appendChild(doc.createTextNode(EncryptDecryptUtil.encrypt(ftp.getPassword())));
			}
			ftpElement.appendChild(password1);

			Element port = doc.createElement("Port");
			port.appendChild(doc.createTextNode(String.valueOf(ftp.getPort())));
			ftpElement.appendChild(port);

			Element protocol = doc.createElement("Mode");
			protocol.appendChild(doc.createTextNode(ftp.getMode()));
			ftpElement.appendChild(protocol);

			Element email = doc.createElement("Destination");
			if (ftp.getDestination() != null) {
				email.appendChild(doc.createTextNode(ftp.getDestination()));
			} else {
				email.appendChild(doc.createTextNode(""));
			}
			ftpElement.appendChild(email);

			//////////sftp
			Element sftpElement = doc.createElement("SFTP");
			temElement.appendChild(sftpElement);

			Element host = doc.createElement("Host");
			if (sftp.getHost() != null) {
				host.appendChild(doc.createTextNode(sftp.getHost()));
			} else {
				host.appendChild(doc.createTextNode(""));
			}
			sftpElement.appendChild(host);

			Element sftp_user = doc.createElement("Username");
			if (sftp.getUsername() != null) {
				sftp_user.appendChild(doc.createTextNode(sftp.getUsername()));
			} else {
				sftp_user.appendChild(doc.createTextNode(""));
			}
			sftpElement.appendChild(sftp_user);

			Element sftp_password = doc.createElement("Password");

			if (sftp.getPassword() == null || sftp.getPassword().equals("")) {
				sftp_password.appendChild(doc.createTextNode(""));
			} else {
				sftp_password.appendChild(doc.createTextNode(EncryptDecryptUtil.encrypt(sftp.getPassword())));
			}
			sftpElement.appendChild(sftp_password);

			Element sftp_port = doc.createElement("Port");
			sftp_port.appendChild(doc.createTextNode(String.valueOf(sftp.getPort())));
			sftpElement.appendChild(sftp_port);

			Element privateKey = doc.createElement("Private-Key");
			if (sftp.getPrivateKey() == null || sftp.getPrivateKey().equals("")) {
				privateKey.appendChild(doc.createTextNode(""));
			} else {
				privateKey.appendChild(doc.createTextNode(sftp.getPrivateKey()));
			}
			sftpElement.appendChild(privateKey);
			Element sftp_passPhrase = doc.createElement("Pass-Phrase");
			if (sftp.getPassPhrase() == null || sftp.getPassPhrase().equals("")) {
				sftp_passPhrase.appendChild(doc.createTextNode(""));
			} else {
				sftp_passPhrase.appendChild(doc.createTextNode(EncryptDecryptUtil.encrypt(sftp.getPassPhrase())));
			}
			sftpElement.appendChild(sftp_passPhrase);
			Element destination = doc.createElement("Destination");
			if (sftp.getDestination() != null) {
				destination.appendChild(doc.createTextNode(sftp.getDestination()));
			} else {
				destination.appendChild(doc.createTextNode(""));
			}
			sftpElement.appendChild(destination);
			int type = sftp.getType();
			Element con_type = doc.createElement("Connection-Type");
			if (type == 0) {
				con_type.appendChild(doc.createTextNode("User/Password"));
			} else {
				con_type.appendChild(doc.createTextNode("User/Private Key"));
			}
			sftpElement.appendChild(con_type);

			//////configurations
			Element configElement = doc.createElement("configurations");
			temElement.appendChild(configElement);

			Element server1 = doc.createElement("Server-Name");
			if (config.getServerName() != null) {
				server1.appendChild(doc.createTextNode(config.getServerName()));
			} else {
				server1.appendChild(doc.createTextNode(""));
			}
			configElement.appendChild(server1);

			Element user1 = doc.createElement("Username");
			if (config.getUsername() != null) {
				user1.appendChild(doc.createTextNode(config.getUsername()));
			} else {
				user1.appendChild(doc.createTextNode(""));
			}
			configElement.appendChild(user1);

			Element password = doc.createElement("Password");

			if (config.getPassword() == null || config.getPassword().equals("")) {
				password.appendChild(doc.createTextNode(""));
			} else {
				password.appendChild(doc.createTextNode(EncryptDecryptUtil.encrypt(config.getPassword())));
			}
			configElement.appendChild(password);

			Element port1 = doc.createElement("Port");
			port1.appendChild(doc.createTextNode(config.getServerPort()));
			configElement.appendChild(port1);

			Element protocol1 = doc.createElement("Protocol");
			protocol1.appendChild(doc.createTextNode(config.getServerProtocol()));
			configElement.appendChild(protocol1);

			Element email1 = doc.createElement("Email-Notification");
			if (config.getEmailNotifications()) {
				email1.appendChild(doc.createTextNode("true"));
			} else {
				email1.appendChild(doc.createTextNode("false"));
			}

			configElement.appendChild(email1);
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			if (template) {
				StringWriter writer = new StringWriter();
				transformer.transform(source, new StreamResult(writer));
				String output = writer.getBuffer().toString();
				return output;
			} else {
				StreamResult result = new StreamResult(new File(path + File.separator + name));
				transformer.transform(source, result);
			}
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return "XML exported";
	}

	/**
	 * Import xml file. Truncate all info in db and add new info from xml.
	 *
	 * @param path input xml file location
	 * @return message string whether import is successful or not
	 */
	public String importXML(String path, String defaultTemplate) {
		String message = "";
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setValidating(true);
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			dBuilder.setErrorHandler(new SimpleErrorHandler());
			Document doc;
			String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w-]+\\.)+[\\w]+[\\w]$";
			if (!defaultTemplate.equals("")) {
				doc = dBuilder.parse(new InputSource(new StringReader(defaultTemplate)));
			} else {
				File fXmlFile = new File(path);
				doc = dBuilder.parse(fXmlFile);
			}
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("Metadata").item(0).getChildNodes();
			this.bagInfoRepo.truncate();
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					BagInfo bagInfo = new BagInfo();
					if (!eElement.getNodeName().isEmpty()) {
						if (eElement.getAttribute("label").isEmpty()) {
							bagInfo.setLabel(eElement.getNodeName().replace("-", " "));
						} else {
							bagInfo.setLabel(eElement.getAttribute("label"));
						}
						if (!defaultTemplate.equals("")) {
							bagInfo.setValue("");
						} else {
							bagInfo.setValue(eElement.getTextContent().replaceAll("\\s+", " ").trim());
						}
						this.bagInfoRepo.save(bagInfo);
					}
				}
			}

			NodeList recList = doc.getElementsByTagName("Recipients").item(0).getChildNodes();
			this.recipientsRepo.truncate();
			for (int temp = 0; temp < recList.getLength(); temp++) {
				Recipients recipient = this.recipientsRepo.getOneOrCreateOne("");
				Node nNode = recList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (!eElement.getTextContent().isEmpty()) {
						if (eElement.getTextContent().matches(EMAIL_REGEX)) {
							recipient.setEmail(eElement.getTextContent());
							this.recipientsRepo.save(recipient);
						} else {
							message = message + "Can't save " + eElement.getTextContent() + "because of invalid email format. ";
						}
					}
				}
			}

			this.FTPRepo.truncate();
			NodeList ftpList = doc.getElementsByTagName("FTP");
			for (int temp = 0; temp < ftpList.getLength(); temp++) {
				FTP ftp = this.FTPRepo.getOneOrCreateOne();
				Node nNode = ftpList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("Host").getLength() == 1) {
						ftp.setHostName(eElement.getElementsByTagName("Host").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Username").getLength() == 1) {
						ftp.setUsername(eElement.getElementsByTagName("Username").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Password").getLength() == 1) {
						ftp.setPassword(EncryptDecryptUtil.decrypt(eElement.getElementsByTagName("Password").item(0).getTextContent()));
					}
					if (eElement.getElementsByTagName("Port").getLength() == 1) {
						ftp.setPort(Integer.parseInt(eElement.getElementsByTagName("Port").item(0).getTextContent()));
					}
					if (eElement.getElementsByTagName("Mode").getLength() == 1) {
						ftp.setMode(eElement.getElementsByTagName("Mode").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Destination").getLength() == 1) {
						ftp.setDestination(eElement.getElementsByTagName("Destination").item(0).getTextContent());
					}
					ftp.setSecurityType("FTPES");
					this.FTPRepo.save(ftp);
				}
			}

			this.SFTPRepo.truncate();
			NodeList sftpList = doc.getElementsByTagName("SFTP");
			for (int temp = 0; temp < sftpList.getLength(); temp++) {
				SFTP sftp = this.SFTPRepo.getOneOrCreateOne();
				Node nNode = sftpList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("Host").getLength() == 1) {
						sftp.setHost(eElement.getElementsByTagName("Host").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Username").getLength() == 1) {
						sftp.setUsername(eElement.getElementsByTagName("Username").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Password").getLength() == 1) {
						sftp.setPassword(EncryptDecryptUtil.decrypt(eElement.getElementsByTagName("Password").item(0).getTextContent()));
					}
					if (eElement.getElementsByTagName("Port").getLength() == 1) {
						sftp.setPort(Integer.parseInt(eElement.getElementsByTagName("Port").item(0).getTextContent()));
					}
					if (eElement.getElementsByTagName("Private-Key").getLength() == 1) {
						sftp.setPrivateKey(eElement.getElementsByTagName("Private-Key").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Pass-Phrase").getLength() == 1) {
						sftp.setPassPhrase(EncryptDecryptUtil.decrypt(eElement.getElementsByTagName("Pass-Phrase").item(0).getTextContent()));
					}
					if (eElement.getElementsByTagName("Destination").getLength() == 1) {
						sftp.setDestination(eElement.getElementsByTagName("Destination").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Connection-Type").getLength() == 1) {
						String type = eElement.getElementsByTagName("Connection-Type").item(0).getTextContent();
						if (type.equals("User/Password")) {
							sftp.setType(0);
						} else {
							sftp.setType(1);
						}
					}
					this.SFTPRepo.save(sftp);
				}
			}

			NodeList confList = doc.getElementsByTagName("configurations");
			this.configurationsRepo.truncate();
			for (int temp = 0; temp < confList.getLength(); temp++) {
				Configurations configurations = this.configurationsRepo.getOneOrCreateOne();
				Node nNode = confList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (eElement.getElementsByTagName("Server-Name").getLength() == 1) {
						configurations.setServerName(eElement.getElementsByTagName("Server-Name").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Username").getLength() == 1) {
						configurations.setUsername(eElement.getElementsByTagName("Username").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Password").getLength() == 1) {
						configurations.setPassword(EncryptDecryptUtil.decrypt(eElement.getElementsByTagName("Password").item(0).getTextContent()));
					}
					if (eElement.getElementsByTagName("Port").getLength() == 1) {
						configurations.setServerPort(eElement.getElementsByTagName("Port").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Protocol").getLength() == 1) {
						configurations.setServerProtocol(eElement.getElementsByTagName("Protocol").item(0).getTextContent());
					}
					if (eElement.getElementsByTagName("Email-Notification").getLength() == 1) {
						configurations.setEmailNotifications(Boolean.valueOf(eElement.getElementsByTagName("Email-Notification").item(0).getTextContent()));
					}
				}
				this.configurationsRepo.save(configurations);
			}
			message = message + "Successfully imported xml";

		} catch (ParserConfigurationException ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		} catch (SAXParseException ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		} catch (SAXException ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		} catch (IOException ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		} catch (Exception ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
			return "";
		}
		return message;
	}

	public static String getDirectoryChecksum(File directory) {
		String out = "";
		for (File file : directory.listFiles()) {
			String fileName = file.getName();
			if (!file.exists()) {
				out += fileName + "_|_" + "not found" + "\n";
			} else if (file.isFile()) {
				out += fileName + "_|_" + checkSum(file.getAbsolutePath()) + "\n";
			} else if (file.isDirectory()) {
				out += getDirectoryChecksum(file);
			}
		}
		return out;
	}
}
