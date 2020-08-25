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
package com.xbreeze.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.xbreeze.xml.compose.XmlComposer;
import com.xbreeze.xml.config.PowerDeComposerConfig;
import com.xbreeze.xml.decompose.XmlDecomposer;

public class Executor {

	private static final Logger logger = Logger.getLogger("");
	
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
		ConsoleHandler outputConsoleHandler = new ConsoleHandler() {
			@Override
			protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
				super.setOutputStream(System.out);
			}
		};
		Level consoleLogLevel = getLogLevel("INFO");
		outputConsoleHandler.setLevel(consoleLogLevel);
		// Only log message with a lower level then warning.
		outputConsoleHandler.setFilter(new Filter() {
			@Override
			public boolean isLoggable(LogRecord record) {
				return record.getLevel().intValue() < Level.SEVERE.intValue();
			}
		});
		// Update the log level to the lowest level.
		logger.setLevel((consoleLogLevel.intValue() < logger.getLevel().intValue()) ? consoleLogLevel : logger.getLevel());
		logger.addHandler(outputConsoleHandler);
		if (args.length == 3 || args.length == 4) {
			String operationType = args[0];
			PowerDeComposerConfig pdcConfig;
			if (args.length == 4) {
				pdcConfig = PowerDeComposerConfig.fromFile(Paths.get(args[3]).toUri());
			}
			else {
				// Create the default PowerDeComposerConfig.
				pdcConfig = PowerDeComposerConfig.GetDefaultConfig();
			}
			if (operationType.equalsIgnoreCase("decompose")) {
				String xmlFilePath = args[1].trim();
				String targetDirectory = args[2].trim();
				new XmlDecomposer(xmlFilePath, targetDirectory, pdcConfig);
			} else
				if (operationType.equalsIgnoreCase("compose")) {
					String xmlSourceFile = args[1].trim();
					String xmlTargetFile = args[2].trim();
					new XmlComposer(xmlSourceFile, xmlTargetFile, pdcConfig);
				} else {
					throw new Exception("First argument should be compose or decompose");
				}
		} else {
			throw new Exception("Expecting exactly 3 or 4 arguments: (decompose, xml-file-path, target-directory[, config-file-location]) or (compose, xml-source-file, xml-target-file[, config-file-location]).");
		}
	}
	/**
	 * Get the log level using the textual representation from the config.
	 * @param level The log level
	 * @return The Level constant.
	 * @throws GeneratorException
	 */
	private static Level getLogLevel(String level) throws Exception {
		try {
			return Level.parse(level.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new Exception(String.format("Unknown LogLevel specified: '%s'", level), e);
		}		
	}
}
