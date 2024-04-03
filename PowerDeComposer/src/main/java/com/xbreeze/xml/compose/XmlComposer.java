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
import java.util.logging.Logger;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.xbreeze.xml.DeComposerException;
import com.xbreeze.xml.utils.XMLUtils;

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
		
		// Set the system line seperator to only newline (so on Windows it doesn't include the carriage return).
		System.setProperty("line.separator", "\n");
		
		// Compose using Xml Beans.
		composeXml(xmlFile, xmlTargetFilePath);
		
		// Done
		logger.info("Done.");

	}
	
	/**
	 * Use the Apache XmlBeans for resolving all includes.
	 * @param xmlFile
	 * @param xmlTargetFilePath
	 * @throws Exception
	 */
	private void composeXml(File xmlFile, String xmlTargetFilePath) throws Exception {
		try {
			Path basePath = FileUtils.getBasePath(xmlFile);

			// Open the file and look for includes
			// TODO: Make this XPath namespace aware so it actually looks for xi:include instead of include in all namespaces
			VTDNav nav = XMLUtils.getVTDNav(xmlFileContentsAndCharset, false);
			AutoPilot ap = new AutoPilot(nav);
			// Declare the XInclude namespace.
			//ap.declareXPathNameSpace("xi", "http://www.w3.org/2001/XInclude");
			
			String deformalizedXml = xmlFile;
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

				// Update the XML to process after deformalizing.
				if (deformalizedExtendedAttributes)
					deformalizedXml = XMLUtils.getResultingXml(vm);
			} catch (ModifyException e) {
				throw new Exception(String.format("Error modifying config file %s", xmlFile.toString()), e);
			} catch (XPathParseException | XPathEvalException e) {
				throw new Exception(String.format("XPath error scanning for includes in %s", xmlFile.toString()), e);
			}

			// Get the XmlObject for the xmlFile.
			XmlObject composedXmlObject = XMLUtils.parseXmlFile(deformalizedXml);
				
			try {
				// Save the resulting composed file.
				// XmlOption could be added as extra parameter for the save method: , new XmlOptions().setSaveOptimizeForSpeed(true)
				composedXmlObject.save(new File(xmlTargetFilePath), new XmlOptions().setSavePrettyPrint());
			} catch (IOException ioe) {
				throw new DeComposerException(String.format("Error writing to target file %s: %s", xmlTargetFilePath, ioe.getMessage()));
			}
	}

}
