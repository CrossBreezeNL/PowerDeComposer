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

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import com.xbreeze.xml.DeComposerException;
import com.xbreeze.xml.utils.FileContentAndCharset;
import com.xbreeze.xml.utils.FileUtils;
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
		FileContentAndCharset xmlFileContentsAndCharset = FileUtils.getFileContent(xmlFile);

		// Get the XmlObject for the xmlFile.
		XmlObject composedXmlObject = XMLUtils.parseXmlFile(xmlFile);
		
		// Loop over all ExtendedAttributes elements for de-formalize them.
		for (XmlObject extAttrsXmlObject : composedXmlObject.selectPath("//ExtendedAttributes")) {
			// Create a string buffer for the extended attribute text.
			StringBuffer extendedAttributeText = new StringBuffer();
			
			// Find the OriginatingExtension elements.
			for (XmlObject origExtXmlObect : extAttrsXmlObject.selectPath("OriginatingExtension")) {
				// Get the ObjectID and Name of the Extension.
				String extObjectID = origExtXmlObect.selectAttribute(QName.valueOf("ObjectID")).toString();
				String extName = origExtXmlObect.selectAttribute(QName.valueOf("Name")).toString();
				
				// Create a buffer for the extended attributes of the current extension.
				StringBuffer extensionExtAttrTextBuffer = new StringBuffer();
				
				for (XmlObject extAttrXmlObject : origExtXmlObect.selectPath("ExtendedAttribute")) {
					// Get the ObjectID, Name and Value of the ExtendedAttribute.
					String extAttrObjectID = extAttrXmlObject.selectAttribute(QName.valueOf("ObjectID")).toString();
					String extAttrName = extAttrXmlObject.selectAttribute(QName.valueOf("Name")).toString();
					String extAttrValue = extAttrXmlObject.xmlText();
					
					// Add the current extended attribute to the list for the current extension.
					// For the length we use the unescaped version of the extended attribute text.
					extensionExtAttrTextBuffer.append(String.format("{%s},%s,%d=%s", extAttrObjectID, extAttrName, XMLUtils.unescapeXMLChars(extAttrValue).length(), extAttrValue));
					extensionExtAttrTextBuffer.append(xmlFileContentsAndCharset.getLineSeparator());
					
				}
				// Add a line separator.
				extendedAttributeText.append(xmlFileContentsAndCharset.getLineSeparator());
				// Get the value for the extended attributes for the current extension.
				String extensionExtAttrText = extensionExtAttrTextBuffer.toString();
				// Add the extension extended attributes to the extended attributes buffer.
				// For the length we use the unescaped version of the extended attribute text.
				// The length is minus 2, to compensate for the trailing CRLF.
				extendedAttributeText.append(String.format("{%s},%s,%d=%s", extObjectID, extName, XMLUtils.unescapeXMLChars(extensionExtAttrText).length() - 2, extensionExtAttrText));
			}
			
			// Create a new XmlCursor to insert the de-formalized extended attribute text.
			XmlCursor xmlCursor = composedXmlObject.newCursor();
			xmlCursor.beginElement(QName.valueOf("a:ExtendedAttributesText"));
			xmlCursor.setTextValue(extendedAttributeText.toString());
		}
			
		try {
			// Save the resulting composed file.
			// XmlOption could be added as extra parameter for the save method: , new XmlOptions().setSaveOptimizeForSpeed(true)
			composedXmlObject.save(new File(xmlTargetFilePath), new XmlOptions().setSavePrettyPrint());
		} catch (IOException ioe) {
			throw new DeComposerException(String.format("Error writing to target file %s: %s", xmlTargetFilePath, ioe.getMessage()));
		}
	}
}
