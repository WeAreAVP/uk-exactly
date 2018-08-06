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

//Import all needed packages
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

public class ZipUtils {

	private List<String> fileList;
	private String outputZipFile;
	private String sourceFolder; // SourceFolder path
	private String bagName; // SourceFolder path

	public void setSourceFolder(String sourceFolder) {
		this.sourceFolder = sourceFolder;
	}

	public void setOutputZipFile(String outputZipFile) {
		this.outputZipFile = outputZipFile;
	}

	public void setBagName(String bagName) {
		this.bagName = bagName;
	}

	public ZipUtils() {
		fileList = new ArrayList<String>();
	}

	public void zip() {

		this.generateFileList(new File(sourceFolder));
		this.zipIt(outputZipFile);
	}

//	public void zipIt(String zipFile) {
//		byte[] buffer = new byte[1024];
//		String source = "";
//		FileOutputStream fos = null;
//		ZipOutputStream zos = null;
//		try {
//			try {
//				source = sourceFolder.substring(sourceFolder.lastIndexOf("\\") + 1, sourceFolder.length());
//			} catch (Exception e) {
//				source = sourceFolder;
//			}
//			fos = new FileOutputStream(zipFile);
//			zos = new ZipOutputStream(fos);
//
//			System.out.println("Output to Zip : " + zipFile);
//			FileInputStream in = null;
//
//			for (String file : this.fileList) {
//				System.out.println("File Added : " + file);
//				System.out.println("Source : " + source);
//
//
//				ZipEntry ze = new ZipEntry(this.bagName + File.separator + file);
////				ze.setUnixMode();
//				zos.putNextEntry(ze);
//
//				try {
//					in = new FileInputStream(sourceFolder + File.separator + file);
//					int len;
//					while ((len = in.read(buffer)) > 0) {
//						zos.write(buffer, 0, len);
//					}
//				} finally {
//					in.close();
//				}
//			}
//			zos.closeEntry();
//			System.out.println("Folder successfully compressed");
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		} finally {
//			try {
//				zos.flush();
//				zos.close();
//				fos.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	public void generateFileList(File node) {

		// add file only
		if (node.isFile()) {
			fileList.add(generateZipEntry(node.toString()));

		}

		if (node.isDirectory()) {
			String[] subNote = node.list();
			for (String filename : subNote) {
				generateFileList(new File(node, filename));
			}
		}
	}

	private String generateZipEntry(String file) {
		return file.substring(sourceFolder.length() + 1, file.length());
	}

	public void unZipIt(String zipFile, String outputFolder) {

		byte[] buffer = new byte[1024];
		try {
			//create output directory is not exists
			File folder = new File(outputFolder);
			if (!folder.exists()) {
				folder.mkdir();
			}
			//get the zipped file list entry
			try ( //get the zip file content
					ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
				//get the zipped file list entry
				ZipEntry ze = zis.getNextEntry();
				while (ze != null) {
					File newFile;
					String fileName = ze.getName();
					if (ze.isDirectory()) {
						new File(outputFolder + File.separator + fileName).mkdir();
						//newFile
					} else {
						newFile = new File(outputFolder + File.separator + fileName);

						System.out.println("file unzip : " + newFile.getAbsoluteFile());
						//create all non exists folders
						//else you will hit FileNotFoundException for compressed folder
						new File(newFile.getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}
						fos.close();
					}
					ze = zis.getNextEntry();
				}
				zis.closeEntry();
			}
			System.out.println("Done");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void zipIt(String zipFile) {
		try {
			ZipOutputStream zip = null;
			FileOutputStream fileWriter = null;
			fileWriter = new FileOutputStream(zipFile);
			zip = new ZipOutputStream(fileWriter);
			addFolderToZip("", sourceFolder, zip);
			zip.flush();
			zip.close();
			fileWriter.flush();
			fileWriter.close();
		} catch (FileNotFoundException ex) {
			Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
		} catch (Exception ex) {
			Logger.getLogger(ZipUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
			throws Exception {

		File folder = new File(srcFile);
		if (folder.isDirectory()) {
			addFolderToZip(path, srcFile, zip);
		} else {
			byte[] buf = new byte[1024];
			int len;
			FileInputStream in = new FileInputStream(srcFile);
			zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
			while ((len = in.read(buf)) > 0) {
				zip.write(buf, 0, len);
			}
			in.close();
		}
	}

	static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
			throws Exception {
		File folder = new File(srcFolder);

		for (String fileName : folder.list()) {
			if (path.equals("")) {
				addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
			} else {
				addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
			}
		}
	}
}
