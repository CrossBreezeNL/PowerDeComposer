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
package com.xbreeze.xml.compose;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.logging.Logger;

import com.xbreeze.xml.utils.FileContentAndCharset;
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
	private static final Logger logger = Logger.getGlobal();

	public XmlComposer(String xmlFilePath, String xmlTargetFilePath) throws Exception {
		composeXml(xmlFilePath, xmlTargetFilePath);
	}

	private void composeXml(String xmlFilePath, String xmlTargetFilePath) throws Exception {

		logger.info(String.format("Starting Xml Composer for '%s'", xmlFilePath));

		File xmlFile = new File(xmlFilePath);

		if (!xmlFile.exists())
			throw new Exception(String.format("The specified xml file doesn't exist '%s'.", xmlFilePath));

		// Read the xml file into a string.
		FileContentAndCharset fcac = FileUtils.getFileContent(xmlFile); 
		HashMap<File, Integer> resolvedIncludes = new HashMap<File, Integer>();
		// Resolve all includes
		String resolvedXmlFileContents = this.resolveIncludes(fcac, xmlFile, 0, resolvedIncludes);

		try {
			Files.write(
					Paths.get(xmlTargetFilePath),
					resolvedXmlFileContents.getBytes(fcac.getFileCharset()),
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING
			);

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

	private String resolveIncludes(FileContentAndCharset xmlFileContentsAndCharset, File xmlFile, int level, HashMap<File, Integer> resolvedIncludes) throws Exception {
		logger.fine(String.format("Scanning file %s for includes", xmlFile.toString()));
		
		// Check for cycle detection, e.g. an include that is already included previously
		if (resolvedIncludes.containsKey(xmlFile) && resolvedIncludes.get(xmlFile) != level) {
			throw new Exception(String.format("Include cycle detected at level %d, file %s is already included previously", level, xmlFile.toString()));
		} else if (!resolvedIncludes.containsKey(xmlFile)) {
			resolvedIncludes.put(xmlFile, level);
		}

		// Get basePath of the file. If the provided File refers to a file, use its
		// parent path, if it refers to a folder use it as base path
		try {
			Path basePath = FileUtils.getBasePath(xmlFile);

			// Open the file and look for includes
			// TODO: Make this XPath namespace aware so it actually looks for xi:include instead of include in all namespaces
			VTDNav nav = XMLUtils.getVTDNav(xmlFileContentsAndCharset, false);
			AutoPilot ap = new AutoPilot(nav);
			// Declare the XInclude namespace.
			//ap.declareXPathNameSpace("xi", "http://www.w3.org/2001/XInclude");
			
			try {
				// Create a XMLModifier for changing the XML document.
				XMLModifier vm = new XMLModifier(nav);
				
				// De-Formalize all extended attributes.
				// Select all ExtendedAttributes elements.
				ap.selectXPath("//ExtendedAttributes");
				boolean deformalizedExtendedAttributes = false;
				// Loop thru the set of extended attributes.
				while ((ap.evalXPath()) != -1) {
					// Store the fragment of the ExtendedAttributes element (so we can remove it later.
					long extAttrsNodeFragment = nav.getElementFragment();
					
					// Create a string buffer for the extended attribute text.
					StringBuffer extendedAttributeText = new StringBuffer();
					extendedAttributeText.append(xmlFileContentsAndCharset.getLineSeparator());
					extendedAttributeText.append("<a:ExtendedAttributesText>");
					
					// Find the OriginatingExtension elements.
					AutoPilot ap_extension = new AutoPilot(nav);
					ap_extension.selectXPath("OriginatingExtension");
					// Loop thru the set of OriginatingExtension.
					while ((ap_extension.evalXPath()) != -1) {
						String extObjectID = nav.toString(nav.getAttrVal("ObjectID"));
						String extName = nav.toString(nav.getAttrVal("Name"));
						
						// Find the ExtendedAttribute elements.
						AutoPilot ap_extattribute = new AutoPilot(nav);
						ap_extattribute.selectXPath("ExtendedAttribute");
						// Create a buffer for the extended attributes of the current extension.
						StringBuffer extensionExtAttrTextBuffer = new StringBuffer();
						// Loop thru the set of ExtendedAttribute.
						while ((ap_extattribute.evalXPath()) != -1) {
							String extAttrObjectID = nav.toString(nav.getAttrVal("ObjectID"));
							String extAttrName = nav.toString(nav.getAttrVal("Name"));
							// Replace a LF without preceding LF to CRLF (since VTD-NAV removed it during parsing).
							int extendedAttributeTextIndex = nav.getText();
							String extAttrValue = new String(nav.getXML().getBytes(nav.getTokenOffset(extendedAttributeTextIndex), nav.getTokenLength(extendedAttributeTextIndex)));
							// Add the current extended attribute to the list for the current extension.
							// For the length we use the unescaped version of the extended attribute text.
							extensionExtAttrTextBuffer.append(String.format("{%s},%s,%d=%s", extAttrObjectID, extAttrName, XMLUtils.unescapeXMLChars(extAttrValue).length(), extAttrValue));
							extensionExtAttrTextBuffer.append(xmlFileContentsAndCharset.getLineSeparator());
						}
						extensionExtAttrTextBuffer.append(xmlFileContentsAndCharset.getLineSeparator());
						String extensionExtAttrText = extensionExtAttrTextBuffer.toString();
						
						// Add the extension extended attributes to the extended attributes buffer.
						// For the length we use the unescaped version of the extended attribute text.
						// The length is minus 2, to compensate for the trailing CRLF.
						extendedAttributeText.append(String.format("{%s},%s,%d=%s", extObjectID, extName, XMLUtils.unescapeXMLChars(extensionExtAttrText).length() - 2, extensionExtAttrText));
					}
					extendedAttributeText.append("</a:ExtendedAttributesText>");
					// Insert the ExtendedAttributesText element.
					vm.insertAfterElement(extendedAttributeText.toString());
					// Now we added the replacement of the textual extended attributes, we can remove the ExtendedAttributesText element.
					vm.remove(nav.expandWhiteSpaces(extAttrsNodeFragment, VTDNav.WS_LEADING));
					// Set the indicator whether we de-formalized anything to true.
					deformalizedExtendedAttributes = true;
				}
				
				// Search for all xi:include elements.
				ap.selectXPath("//include");
				int includeCount = 0;
				while ((ap.evalXPath()) != -1) {
					// Obtain the filename of include
					AutoPilot ap_href = new AutoPilot(nav);
					ap_href.selectXPath("@href");
					String includeFileLocation = ap_href.evalXPathToString();
					logger.fine(String.format("Found include for %s in config file %s", includeFileLocation, xmlFile.toString()));
					// Resolve include to a valid path against the basePath
					logger.fine(String.format("base path %s", basePath.toString()));
					File includeFile = basePath.resolve(includeFileLocation).toFile();
					logger.fine(String.format("Resolved include to %s", includeFile.toString()));

					try {
						// Get file contents, recursively processing any includes found
						String includeContents = this.resolveIncludes(FileUtils.getFileContent(includeFile, xmlFileContentsAndCharset.getFileCharset()), includeFile, level + 1, resolvedIncludes);

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
							includeContents = includeContents.replaceFirst("^<\\?xml.*\\?>\r?\n?", "");
						}

						// Replace the node with the include contents
						vm.insertAfterElement(includeContents);
						// Then remove the include node
						vm.remove();
					} catch (IOException e) {
						throw new Exception(String.format("Could not read contents of included file %s", includeFile.toString()), e);
					}
					includeCount++;
				}
				logger.fine(String.format("Found %d includes in XML file %s", includeCount, xmlFile.toString()));
				
				// if includes were found, output and parse the modifier and return it,
				// otherwise return the original one
				if (deformalizedExtendedAttributes || includeCount > 0) {
					String resolvedXML = XMLUtils.getResultingXml(vm);
					logger.fine(String.format("XML file %s with includes and formalized attributes resolved:", xmlFile.toString()));
					logger.fine("**** Begin of XML file ****");
					logger.fine(resolvedXML);
					logger.fine("**** End of XML file ****");
					return resolvedXML;
				} else {
					return xmlFileContentsAndCharset.getFileContents();
				}
			} catch (NavException e) {
				throw new Exception(String.format("Error scanning %s for includes", xmlFile.toString()), e);
			} catch (ModifyException e) {
				throw new Exception(String.format("Error modifying config file %s", xmlFile.toString()), e);
			}
		} catch (XPathParseException | XPathEvalException e) {
			throw new Exception(String.format("XPath error scanning for includes in %s", xmlFile.toString()), e);
		}
	}

}
