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
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlSaxHandler;
import org.xml.sax.XMLReader;

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
		
		// Compose using Xml Beans.
		composeXml(xmlFile, xmlTargetFilePath);
		
		// Done
		logger.info("Done.");

	}
	
	/**
	 * Use the Apache XmlBeans for resolving all includes.
	 * For some strange reason it will add a carriage return after the XML declaration.
	 * It seems this is OS dependent, because after checkin and running the build on linux the tests succeed.
	 * Anyway I think the XmlDocument object shouldn't change the XML declaration.
	 * @param xmlFile
	 * @param xmlTargetFilePath
	 * @throws Exception
	 */
	private void composeXml(File xmlFile, String xmlTargetFilePath) throws Exception {
		// Create a XmlReader.
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		// Enable namespaces.
		saxParserFactory.setNamespaceAware(true);
		// Enable XInclude on the SAX parser factory.
		saxParserFactory.setXIncludeAware(true);
		// Don't fixup base URI's and language. Otherwise the resulting XML file will contain extra (unwanted) attributes.
		saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
		// Create a new SAX Parser.
		SAXParser saxParser = saxParserFactory.newSAXParser();
		// Create a XML Reader.
		XMLReader xmlReader = saxParser.getXMLReader();
		
		// Create a SAX handler so the SAX Parser can give the SAX events to this handler.
		// XmlOptions can be added as argument for newXmlSaxHandler here.
		XmlSaxHandler xmlSaxHandler = XmlObject.Factory.newXmlSaxHandler();
		// Set the SaxHandler as the content handler for the XML Reader.
		xmlReader.setContentHandler(xmlSaxHandler.getContentHandler());
		// Set the xml sax handler also as the lexical handler so it can also retrieve file elements which are not passed to the content handler (like comments). 
		xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", xmlSaxHandler);
		// Parse the decomposed root file (which will also parse all it includes for us).
		xmlReader.parse(xmlFile.getAbsolutePath());
		// Get the XmlObject which was just loaded using the sax handler.
		XmlObject composedXmlObject = xmlSaxHandler.getObject();
		
		try {
			// Save the resulting composed file.
			// XmlOption could be added as extra parameter for the save method: , new XmlOptions().setSaveOptimizeForSpeed(true)
			composedXmlObject.save(new File(xmlTargetFilePath));
		} catch (IOException exc) {
			throw new Exception(
					String.format("Error writing to target file %s: %s", xmlTargetFilePath, exc.getMessage()));
		}
	}

}
