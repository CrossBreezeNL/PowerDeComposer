/*******************************************************************************
 * Copyright (c) 2020 CrossBreeze
 *
 * This file is part of PowerDeComposer.
 *
 * PowerDeComposer is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your option) any 
 * later version.
 *
 * PowerDeComposer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PowerDeComposer.  If not, see <https://www.gnu.org/licenses/>.
 *      
 * Contributors:
 *   Harmen Wessels - CrossBreeze
 *   Willem Otten - CrossBreeze
 *******************************************************************************/
package com.xbreeze.xml.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

public class FileUtils {
	// The logger for this class.
	protected static final Logger logger = Logger.getLogger(XMLUtils.class.getName());
	
	private static final String XML_PROCESSING_INSTRUCTION_UTF8 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	
	public static FileContentAndCharset getFileContent(URI fileLocation) throws IOException {
		// Create a input stream from the template file.
		FileInputStream fis = new FileInputStream(new File(fileLocation));
		// Wrap the input stream in a BOMInputStream so it is invariant for the BOM.
		BOMInputStream bomInputStream = new BOMInputStream(fis);
		
		// Initialize the file charset to null.
		Charset fileCharset = null;
		// If the file has a BOM use the encoding according to the BOM.
		if (bomInputStream.hasBOM()) {
			// Get the charset from the bom of the input stream.
			fileCharset = Charset.forName(bomInputStream.getBOMCharsetName());
			logger.fine(String.format("The file has a BOM specifying charset '%s'", fileCharset.name()));
		}
		
		// If the file doesn't have a bom, check whether the first line of the file is a XML processing instruction.
		if (fileCharset == null) {
			// Read the first part of the file and check whether the first bytes equal the UTF-8 XML processing instruction.
			byte[] readNBytes = bomInputStream.readNBytes(XML_PROCESSING_INSTRUCTION_UTF8.getBytes(StandardCharsets.UTF_8).length);
			if (new String(readNBytes).equals(XML_PROCESSING_INSTRUCTION_UTF8)) {
				logger.fine("The file starts with a XML processing instruction with UTF-8 encoding.");
				fileCharset = StandardCharsets.UTF_8;
			}
			// Set the position for the file input stream back to 0, since we started reading the file here.
			fis.getChannel().position(0);
		}

		// If the charset hasen't been set yet, we aren't sure what the encoding is, defaulting to UTF-8.
		if (fileCharset == null) {
			logger.warning(String.format("The encoding of the file can't be detected, defaulting to UTF-8 (%s)", fileLocation.toString()));
			fileCharset = StandardCharsets.UTF_8;
		}
		
		// Read the file using the given charset.
		return getFileContent(bomInputStream, fileCharset);
	}
	
	public static FileContentAndCharset getFileContent(URI fileLocation, Charset fileCharset) throws IOException {
		// Create a input stream from the template file.
		FileInputStream fis = new FileInputStream(new File(fileLocation));
		// Read the file using the given charset.
		return getFileContent(fis, fileCharset);
	}
	
	public static FileContentAndCharset getFileContent(InputStream fileInputStream, Charset fileCharset) throws IOException {
		logger.fine(String.format("Get file contents using charset %s", fileCharset.name()));
		// Create a String using the InputStream and the Charset.
		String fileContents = IOUtils.toString(fileInputStream, fileCharset);
		// Return the FileContentAndCharset object.
		return new FileContentAndCharset(fileContents, fileCharset);
	}
	
	/**
	 * Function where the illegal characters for a file name are replaced with an underscore
	 * @param possiblyIllegalFileName The file name which might contain illegal characters.
	 * @return The legal file name.
	 */
	public static String getLegalFileName(String possiblyIllegalFileName) {
		return possiblyIllegalFileName.replaceAll("[:\\\\/*?|<>\"]", "_");
	}
	
	/**
	 * Function to get the base path of an URI.
	 * Get basePath of the file. If the provided URI refers to a file, use its parent path.
	 * If it refers to a folder use it as base path
	 * @param fileUri The file URI
	 * @return The base path URI
	 * @throws Exception
	 */
	public static Path getBasePath(Path filePath) throws Exception {
		Path basePath = filePath;
		// Check whether the path points to a file, and if so get the parent path.
		if (filePath.toFile().isFile()) {
			basePath = filePath.getParent();
		}
		// Resolve basePath to absolute/real path
		try {
			basePath = basePath.toRealPath(LinkOption.NOFOLLOW_LINKS);
		} catch (IOException e) {
			throw new Exception(
					String.format("Error resolving config basePath %s to canonical path", basePath.toString()), e);
		}
		return basePath;
	}
}
