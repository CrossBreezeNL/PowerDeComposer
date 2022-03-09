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
	private static final Logger logger = Logger.getGlobal();

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
		
		
		/**
		// Create a PdcXmlHandler which implements ContentHandler, LexicalHandler and DeclHandler.
		PdcXmlHandler pdcXmlHandler = new PdcXmlHandler();
		// Set the content handler.
		xmlReader.setContentHandler(pdcXmlHandler);
		// Set the dtd handler.
		xmlReader.setDTDHandler(pdcXmlHandler);
		// Set the lexical handler.
		xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", pdcXmlHandler);
		// Set the declaration handler.
		xmlReader.setProperty("http://xml.org/sax/properties/declaration-handler", pdcXmlHandler);
		// Parse the decomposed root file (which will also parse all it includes for us).
		xmlReader.parse(xmlFile.getAbsolutePath());
		*/
		
		// Create a SAX handler so the SAX Parser can give the SAX events to this handler.
		// XmlOptions can be added as argument for newXmlSaxHandler here.
		XmlSaxHandler xmlSaxHandler = XmlObject.Factory.newXmlSaxHandler();
		// new XmlOptions().setLoadUseLocaleCharUtil(false)
		// XmlObject.Factory.newXmlSaxHandler(); -> https://github.com/apache/xmlbeans/blob/2900cf0995cba9b3ee0e460da06ac1d87e8937ec/src/main/java/org/apache/xmlbeans/impl/schema/XmlObjectFactory.java#L307
		//  > XmlBeans.getContextTypeLoader().newXmlSaxHandler(getInnerType(), null); -> getInnerType()=null; https://github.com/apache/xmlbeans/blob/2900cf0995cba9b3ee0e460da06ac1d87e8937ec/src/main/java/org/apache/xmlbeans/impl/schema/SchemaTypeLoaderBase.java#L262
		//   > Locale.newSaxHandler(this, type, options); -> type and options are null -> https://github.com/apache/xmlbeans/blob/2900cf0995cba9b3ee0e460da06ac1d87e8937ec/src/main/java/org/apache/xmlbeans/impl/store/Locale.java#L894
		//    >  syncWrap(stl, options, (l) -> new XmlSaxHandlerImpl(l, type, options)) -> https://github.com/apache/xmlbeans/blob/2900cf0995cba9b3ee0e460da06ac1d87e8937ec/src/main/java/org/apache/xmlbeans/impl/store/Locale.java#L834
		//     > saxHandlerOptions.setLoadUseLocaleCharUtil(true); -> Due to this line of code setting this on the XmlOptions during constructor will always be ignored: https://github.com/apache/xmlbeans/blob/2900cf0995cba9b3ee0e460da06ac1d87e8937ec/src/main/java/org/apache/xmlbeans/impl/store/Locale.java#L845
		// There is a comment in the above code explaining why the CharUtils are overwritten there.
		// I don't know why this is an issue anymore, because I haven't looked in this code in a while.
		// If this is an issue in XmlBeans, the issue can be reported at: https://issues.apache.org/jira/browse/XMLBEANS-601?jql=project%20%3D%20XMLBEANS
		//     > SaxHandler::initSaxHandler(l, saxHandlerOptions); -> https://github.com/apache/xmlbeans/blob/2900cf0995cba9b3ee0e460da06ac1d87e8937ec/src/main/java/org/apache/xmlbeans/impl/store/Locale.java#L2273
		
		// Set the SaxHandler as the content handler for the XML Reader.
		xmlReader.setContentHandler(xmlSaxHandler.getContentHandler());
		// Set the xml sax handler also as the lexical handler so it can also retrieve file elements which are not passed to the content handler (like comments). 
		xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", xmlSaxHandler.getLexicalHandler());
		xmlReader.setProperty("http://xml.org/sax/properties/declaration-handler", xmlSaxHandler);
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
