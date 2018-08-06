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
package uk.sipperfly.ui;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.sipperfly.persistent.Configurations;
import uk.sipperfly.repository.ConfigurationsRepo;
import static uk.sipperfly.ui.Exactly.GACOM;
import uk.sipperfly.utils.CommonUtil;

/**
 * This class implements the visitor used by the FileTransfer class.
 *
 * @author Nouman Tayyab
 */
class CopyDirVisitor extends SimpleFileVisitor<Path> {

	private final Exactly parent;
	private final Path fromPath;
	private final Path toPath;

	/**
	 * Constructor for CopyDirVisitor
	 *
	 * @param parent Pointer to the parent GUI for status updates
	 * @param fromPath The source of the copy
	 * @param toPath The destination of the copy
	 */
	public CopyDirVisitor(Exactly parent, Path fromPath, Path toPath) {
		if (parent == null
				|| fromPath == null
				|| toPath == null) {
			throw new IllegalArgumentException();
		}

		this.parent = parent;
		this.fromPath = fromPath;
		this.toPath = toPath;

	}

	/**
	 * Creates the target directories as we walk the tree.
	 *
	 * @param dir The target directory path.
	 * @param attrs Default attributes (unused)
	 * @return CONTINUE in all cases
	 * @throws IOException If the target directory cannot be created
	 */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (dir == null
				|| attrs == null) {
			throw new IllegalArgumentException();
		}

		Path targetPath = toPath.resolve(fromPath.relativize(dir));
		if (!Files.exists(targetPath)) {
			Files.createDirectory(targetPath);
		}
		return FileVisitResult.CONTINUE;
	}

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            //need to apply directory metadata after walk finishes so it persists
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            CommonUtil.copyFileAttributes(fromPath, targetPath);
            return super.postVisitDirectory(dir, exc);
        }

	/**
	 * Performs the actual file copy from the source to the target. Updates the parent GUI progress bar as well.
	 *
	 * @param file The path of the file to copy
	 * @param attrs Default attributes (unused)
	 * @return CONTINUE in all cases
	 * @throws IOException If the file cannot be copied
	 */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		if (file == null
				|| attrs == null) {
			throw new IllegalArgumentException();
		}

		if (this.parent.GetBackgroundWorker().isCancelled()) {
			Logger.getLogger(GACOM).log(Level.SEVERE, "TERMINATING file visitor");
			return FileVisitResult.TERMINATE;
		}
		ConfigurationsRepo configRepo = new ConfigurationsRepo();
		Configurations config = configRepo.getOneOrCreateOne();
		config.getFilters();
		CommonUtil commonUtil = new CommonUtil();

		System.out.println(file.getFileName().toString());
		boolean ignore = commonUtil.checkIgnoreFiles(file.getFileName().toString(), config.getFilters());
		if (!ignore) {
			Files.copy(file, toPath.resolve(fromPath.relativize(file)), REPLACE_EXISTING);
			this.parent.tranferredFiles = this.parent.tranferredFiles + 1;
			this.parent.UpdateProgressBar(this.parent.tranferredFiles);
			
		}
		Logger.getLogger(GACOM).log(Level.INFO, "Count of Files: ".concat(Integer.toString(this.parent.tranferredFiles)));

		return FileVisitResult.CONTINUE;
	}
}