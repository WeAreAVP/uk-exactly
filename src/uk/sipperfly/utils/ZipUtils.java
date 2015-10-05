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

/**
 *
 * @author noumantayyab
 */
//Import all needed packages
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

	public void zipIt(String zipFile) {
		byte[] buffer = new byte[1024];
		String source = "";
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		try {
			try {
				source = sourceFolder.substring(sourceFolder.lastIndexOf("\\") + 1, sourceFolder.length());
			} catch (Exception e) {
				source = sourceFolder;
			}
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);

			System.out.println("Output to Zip : " + zipFile);
			FileInputStream in = null;

			for (String file : this.fileList) {
				System.out.println("File Added : " + file);
				System.out.println("Source : " + source);

				ZipEntry ze = new ZipEntry(this.bagName + File.separator + file);
				zos.putNextEntry(ze);
				try {
					in = new FileInputStream(sourceFolder + File.separator + file);
					int len;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} finally {
					in.close();
				}
			}

			zos.closeEntry();
			System.out.println("Folder successfully compressed");

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

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
}