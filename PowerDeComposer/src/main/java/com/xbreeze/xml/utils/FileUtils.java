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
import java.net.URI;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

public class FileUtils {
	// The logger for this class.
	protected static final Logger logger = Logger.getLogger(XMLUtils.class.getName());

	public static String getFileContent(URI fileLocation) throws IOException {
		// Create a input stream from the template file.
		FileInputStream fis = new FileInputStream(new File(fileLocation));
		// Wrap the input stream in a BOMInputStream so it is invariant for the BOM.
		BOMInputStream bomInputStream = new BOMInputStream(fis);
		// Get the charset from the bom of the input stream.
		String bomCharsetName = bomInputStream.getBOMCharsetName();
		logger.fine(String.format("Get file contents using charset %s", bomCharsetName));
		// Create a String using the BOMInputStream and the charset.
		// The charset can be null, this gives no errors.
		return IOUtils.toString(bomInputStream, bomCharsetName);
	}
}
