/*******************************************************************************
 * Copyright (c) 2022 CrossBreeze
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
package com.xbreeze.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.xbreeze.xml.compose.XmlComposer;
import com.xbreeze.xml.config.PowerDeComposerConfig;
import com.xbreeze.xml.decompose.XmlDecomposer;

public class Executor {

	public static final Logger logger = Logger.getGlobal();
	
	/**
	 * Main function to start the Xml Compose/Decompose process.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		// Setup the global LogManager.
		LogManager logManager = LogManager.getLogManager();
		// Read the logging configuration from the resource file.
		try {			
			logManager.readConfiguration(Executor.class.getResourceAsStream("logging.properties"));
		} catch (SecurityException | IOException e) {
			System.err.println(String.format("Error while getting logging configuration", e.getMessage()));
		}
		
		// Add a logger for the console to log message below warning (and error).
		ConsoleHandler outputConsoleHandler = new ConsoleHandler()
		{
			@Override
			protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
				super.setOutputStream(System.out);
			}
		};
		// Set the console handler to handle all levels (the shown log levels are steered using the logging.properties file).
		outputConsoleHandler.setLevel(Level.ALL);
		// Add the new console handler to the global logger.
		logger.addHandler(outputConsoleHandler);
		// Disable using any parent handlers (to make sure there not logged twice).
		logger.setUseParentHandlers(false);
		
		// Check the passed arguments.
		if (args.length == 3 || args.length == 4) {
			String operationType = args[0];
			
			// Parse the config.
			PowerDeComposerConfig pdcConfig;
			if (args.length == 4) {
				pdcConfig = PowerDeComposerConfig.fromFile(Paths.get(args[3]).toFile());
			}
			else {
				// Create the default PowerDeComposerConfig.
				pdcConfig = PowerDeComposerConfig.GetDefaultConfig();
			}
			
			// Perform the operation. 
			if (operationType.equalsIgnoreCase("decompose")) {
				String xmlFilePath = args[1].trim();
				String targetDirectory = args[2].trim();
				new XmlDecomposer(xmlFilePath, targetDirectory, pdcConfig.getDecomposeConfig());
			} else
				if (operationType.equalsIgnoreCase("compose")) {
					String xmlSourceFile = args[1].trim();
					String xmlTargetFile = args[2].trim();
					new XmlComposer(xmlSourceFile, xmlTargetFile);
				} else {
					throw new Exception("First argument should be compose or decompose");
				}
		} else {
			throw new Exception("Expecting exactly 3 or 4 arguments: (decompose, xml-file-path, target-directory[, config-file-location]) or (compose, xml-source-file, xml-target-file[, config-file-location]).");
		}
	}
}
