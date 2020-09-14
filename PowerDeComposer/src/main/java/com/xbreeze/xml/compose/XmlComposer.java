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
package com.xbreeze.xml.compose;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xbreeze.xml.utils.FileUtils;
import com.xbreeze.xml.utils.XMLUtils;
import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class XmlComposer {
	private static final Logger logger = Logger.getLogger("");

	public XmlComposer(String xmlFilePath, String xmlTargetFilePath) throws Exception {
		composeXml(xmlFilePath, xmlTargetFilePath);
	}

	private void composeXml(String xmlFilePath, String xmlTargetFilePath) throws Exception {

		logger.info(String.format("Starting Xml Composer for '%s'", xmlFilePath));

		File xmlFile = new File(xmlFilePath);

		if (!xmlFile.exists())
			throw new Exception(String.format("The specified xml file doesn't exist '%s'.", xmlFilePath));

		// Read the xml file into a string.
		String xmlFileContents = FileUtils.getFileContent(xmlFile.toURI());
		HashMap<URI, Integer> resolvedIncludes = new HashMap<>();
		// Resolve all inluces
		String resolvedXmlFileContents = this.resolveIncludes(xmlFileContents, xmlFile.toURI(), 0, resolvedIncludes);

		try {
			Files.write(Paths.get(xmlTargetFilePath), resolvedXmlFileContents.getBytes(),
					StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);

			// extra options
			// Files.write(Paths.get(path), content.getBytes(),
			// StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException exc) {
			throw new Exception(
					String.format("Error writing to target file %s: %s", xmlTargetFilePath, exc.getMessage()));
		}
		
		// Done
		logger.info("Done.");

	}

	private String resolveIncludes(String xmlFileContents, URI xmlFileUri, int level,
			HashMap<URI, Integer> resolvedIncludes) throws Exception {
		logger.fine(String.format("Scanning file %s for includes", xmlFileUri.toString()));
		// Check for cycle detection, e.g. an include that is already included
		// previously
		if (resolvedIncludes.containsKey(xmlFileUri) && resolvedIncludes.get(xmlFileUri) != level) {
			throw new Exception(
					String.format("Include cycle detected at level %d, file %s is already included previously", level,
							xmlFileUri.toString()));
		} else if (!resolvedIncludes.containsKey(xmlFileUri)) {
			resolvedIncludes.put(xmlFileUri, level);
		}

		// Get basePath of configFile. If the provided URI refers to a file, us its
		// parent path, if it refers to a folder use it as base path
		try {
			URI basePath = new URI("file:///../");
			File xmlFile = new File(xmlFileUri.getPath());
			if (xmlFile.isDirectory()) {
				basePath = xmlFileUri;
			} else if (xmlFile.isFile()) {
				String parentPath = xmlFile.getParent();
				if (parentPath != null) {
					basePath = Paths.get(parentPath).toUri();
				}
			}
			// Resolve basePath to absolute/real path
			try {
				basePath = Paths.get(basePath).toRealPath(LinkOption.NOFOLLOW_LINKS).toUri();
			} catch (IOException e) {
				throw new Exception(
						String.format("Error resolving config basePath %s to canonical path", basePath.toString()), e);
			}

			// Open the config file and look for includes
			// Make this XPath namespace aware so it actually looks for xi:include instead
			// of include in all namespaces
			VTDNav nav = XMLUtils.getVTDNav(xmlFileContents, false);
			AutoPilot ap = new AutoPilot(nav);
			// Declare the XInclude namespace.
			//ap.declareXPathNameSpace("xi", "http://www.w3.org/2001/XInclude");
			
			// Search for all xi:include elements.
			ap.selectXPath("//include");
			int includeCount = 0;
			try {
				XMLModifier vm = new XMLModifier(nav);
				while ((ap.evalXPath()) != -1) {
					// Obtain the filename of include
					AutoPilot ap_href = new AutoPilot(nav);
					ap_href.selectXPath("@href");
					String includeFileLocation = ap_href.evalXPathToString();
					logger.fine(String.format("Found include for %s in config file %s", includeFileLocation,
							xmlFileUri.toString()));
					// Resolve include to a valid path against the basePath
					logger.fine(String.format("base path %s", basePath.toString()));
					Path p = Paths.get(basePath);
					URI includeFileUri = null;
					try {
						includeFileUri = p.resolve(Paths.get(includeFileLocation)).toRealPath(LinkOption.NOFOLLOW_LINKS)
								.toUri();
					} catch (IOException e) {
						throw new Exception(String.format("Error resolving found include %s for %s to canonical path",
								includeFileLocation, xmlFileUri.toString()), e);
					}
					logger.fine(String.format("Resolved include to %s", includeFileUri.toString()));

					try {
						// get file contents, recursively processing any includes found
						String includeContents = this.resolveIncludes(FileUtils.getFileContent(includeFileUri),
								includeFileUri, level + 1, resolvedIncludes);

						/* XPointer is not needed for now */
						/*
						 * // Check for xpointer and apply if found AutoPilot ap_xpoint = new
						 * AutoPilot(nav); ap_xpoint.selectXPath("@xpointer"); String xPoint =
						 * ap_xpoint.evalXPathToString(); if (xPoint != null && xPoint.length() > 0) {
						 * logger.fine(String.format("Found xpointer in include: %s", xPoint));
						 * includeContents = XMLUtils.getXmlFragment(includeContents, xPoint); }
						 */

						// If the file contains an XML declaration, remove it
						if (includeContents.startsWith("<?xml")) {
							includeContents = includeContents.replaceFirst("^<\\?xml.*\\?>", "");
						}

						// Replace the node with the include contents
						vm.insertAfterElement(includeContents);
						// Then remove the include node
						vm.remove();
					} catch (IOException e) {
						throw new Exception(
								String.format("Could not read contents of included file %s", includeFileUri.toString()),
								e);
					}
					includeCount++;
				}
				logger.fine(String.format("Found %d includes in XML file %s", includeCount, xmlFileUri.toString()));
				// if includes were found, output and parse the modifier and return it,
				// otherwise return the original one
				if (includeCount > 0) {
					String resolvedXML = XMLUtils.getResultingXml(vm);
					logger.fine(String.format("XML file %s with includes resolved:", xmlFileUri.toString()));
					logger.fine("**** Begin of XML file ****");
					logger.fine(resolvedXML);
					logger.fine("**** End of XML file ****");
					return resolvedXML;
				} else {
					return xmlFileContents;
				}
			} catch (NavException e) {
				throw new Exception(String.format("Error scanning %s for includes", xmlFileUri.toString()), e);
			} catch (ModifyException e) {
				throw new Exception(String.format("Error modifying config file %s", xmlFileUri.toString()), e);
			}
		} catch (URISyntaxException e) {
			throw new Exception(String.format("Could not extract base path from XML file %s", xmlFileUri.toString()),
					e);
		} catch (XPathParseException | XPathEvalException e) {
			throw new Exception(String.format("XPath error scanning for includes in %s", xmlFileUri.toString()), e);
		}
	}

}
