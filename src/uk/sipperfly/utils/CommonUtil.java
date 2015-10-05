/*
 * GA Digital Files Transfer Tool
 * Author: Nouman Tayyab
 * Version: 1.0
 * Requires: JDK 1.7 or higher
 * Description: This tool transfers digital files to the Gates Archive.
 * Support: info@gatesarchive.com
 * Copyright 2013 Gates Archive
 */
package uk.sipperfly.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import uk.sipperfly.persistent.BagInfo;
import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.persistent.FTP;
import uk.sipperfly.persistent.Recipients;
import uk.sipperfly.repository.BagInfoRepo;
import uk.sipperfly.repository.ConfigurationsRepo;
import uk.sipperfly.repository.FTPRepo;
import uk.sipperfly.repository.RecipientsRepo;

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
		System.out.println(count);
		return count;
	}

	/**
	 * Check the file against ignore filter.
	 * Return false if file is not in ignore list and vice versa.
	 *
	 * @param fileName Name of the file including extention
	 * @param dbFilters Comma separated ignore file names
	 * @return boolean
	 */
	public static boolean checkIgnoreFiles(String fileName, String dbFilters) {
		boolean ignore = false;

		if (dbFilters != null) {
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
	 * It contains the name of the user that initiated the transfer, Transfer title, local and ftp location  and a timestamp.
	 * 
	 * @param username  The name of the user that initiated the transfer.
	 * @param transferName transfer title
	 * @param source local path for transfer
	 * @param ftp ftp path for transfer if ft deliver is checked
	 * @param bagSize size in bytes
	 * @param bagCount total files
	 * @param zip 1 if zip is checked, 0 otherwise
	 * @return True if the semaphore was created successfully, false otherwise.
	 */
	public boolean CreateSuccessSemaphore(String username, String transferName, Path source, String ftp, String bagSize, int bagCount, int zip) {
		try {
			File trasferFile = new File(transferSemaphore);
			Path semaphorePath = this.combine(source, trasferFile.toPath());
			File semaphore = new File(semaphorePath.toString());
			semaphore.createNewFile();
			String target = source.toString();
			if (zip == 1) {
				transferName = transferName + ".zip";
				target = target + ".zip";
			}
			try (PrintStream ps = new PrintStream(semaphore)) {
				if (ftp != "") {
					if (!ftp.startsWith("/")) {
						ftp = "/" + ftp;
					}
					if (ftp.endsWith("/")) {
						ftp = ftp + transferName;
					} else {
						ftp = ftp + "/" + transferName;
					}
					ps.printf("Transfer completed: %s %nTransfer name: %s %nTarget: %s  %nFTP Target: %s %nApplication used: %s %nUser: %s %nTotal File count: %s%nTotal Bytes: %s\n",
							new Date(), transferName, target, ftp, "Exactly", username, bagCount, bagSize);
				} else {
					ps.printf("Transfer completed: %s %nTransfer name: %s %nTarget: %s %nApplication used: %s %nUser: %s %nTotal File count: %s%nTotal Bytes: %s\n",
							new Date(), transferName, target, "Exactly", username, bagCount, bagSize);
				}
			}
			return true;
		} catch (IOException ex) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "Failed to create transfer complete semaphore", ex);
		}
		return false;
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
			stringBuilder.append(b.getLabel().concat(": ").concat(b.getValue()));
			stringBuilder.append(System.getProperty("line.separator"));
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
	 * @param source path of source bag
	 * @param destination where it should be unpacked
	 * @param folder name of the folder after unpack
	 * @throws IOException 
	 */
	public void unBag(String source, String destination, String folder) throws IOException {
		File workDir = new File(source);
		File sourceDir = new File(source + File.separator + "data");
		File target = new File(destination);
		FileUtils.copyDirectory(sourceDir, target);
		FileUtils.deleteDirectory(workDir);
		File dir = new File(target.getParent() + File.separator + folder);
		target.renameTo(dir);
	}

	/**
	 * Create xml file to export
	 * @param recipient
	 * @param ftp
	 * @param config
	 * @param bagInfo
	 * @param path 
	 */
	public void createXMLExport(List<Recipients> recipient, FTP ftp, Configurations config, List<BagInfo> bagInfo, String path) {
		try {
			char[] charArray = {'<', '>', '&', '"', '\\', '!', '#', '$', '%', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '=', '?', '@', '[', ']', '^', '`', '{', '|', '}', '~'};
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
				char[] txt =  Normalizer.normalize(b.getLabel(), Normalizer.Form.NFD).toCharArray();
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

			if (ftp.getPassword() == null || ftp.getPassword() == "") {
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

			if (config.getPassword() == null || config.getPassword() == "") {
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
			StreamResult result = new StreamResult(new File(path + File.separator + name));
			transformer.transform(source, result);
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(CommonUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Import xml file. Truncate all info in db and add new info from xml.
	 * 
	 * @param path input xml file location
	 * @return message string whether import is successful or not
	 */
	public String importXML(String path) {
			String message = "";
			try {

				String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w-]+\\.)+[\\w]+[\\w]$";
				File fXmlFile = new File(path);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				dbFactory.setValidating(true);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				dBuilder.setErrorHandler(new SimpleErrorHandler());
				Document doc = dBuilder.parse(fXmlFile);

				doc.getDocumentElement().normalize();

				NodeList nList = doc.getElementsByTagName("Metadata").item(0).getChildNodes();
				this.bagInfoRepo.truncate();
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						BagInfo bagInfo = new BagInfo();
						if (!eElement.getNodeName().isEmpty()) {
							bagInfo.setLabel(eElement.getNodeName().replace("-", " "));
							bagInfo.setValue(eElement.getTextContent());
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
						this.FTPRepo.save(ftp);
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
}