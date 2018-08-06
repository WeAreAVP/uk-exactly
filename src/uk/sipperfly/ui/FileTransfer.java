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
package uk.sipperfly.ui;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.logging.Level;
import java.util.logging.Logger;
import static uk.sipperfly.ui.Exactly.GACOM;

/**
 * This class walks the file tree of the source, invoking a visitor pattern on each file.
 *
 * @author Nouman Tayyab
 */
class FileTransfer {

	private Path source;
	private Path target;
	private final Exactly parent;

	/**
	 * Constructor for FileTransfer
	 *
	 * @param target The destination of the copy
	 * @param parent Pointer to the parent GUI for status updates
	 */
	public FileTransfer(Exactly parent) {
		if (parent == null) {
			throw new IllegalArgumentException();
		}
		this.parent = parent;

	}

	/**
	 * Set Source path for file transfer.
	 *
	 * @param source
	 */
	public void setTargetPath(Path target) {
		this.target = target;
	}

	/**
	 * Set Source path for file transfer.
	 *
	 * @param source
	 */
	public void setSourcePath(Path source) {
		this.source = source;
	}

	/**
	 * Performs the actual file transfer
	 *
	 * @return True
	 * @throws Exception If anything goes wrong
	 */
	protected boolean Perform() throws Exception {
		String[] params = new String[]{source.toString(), target.toString()};
		Logger.getLogger(GACOM).log(Level.INFO, "Copying from {0} to {1}", params);
		// walk the tree copying each subfolder and files
		Files.walkFileTree(source, new CopyDirVisitor(parent, source, target));
		return true;
	}

}
