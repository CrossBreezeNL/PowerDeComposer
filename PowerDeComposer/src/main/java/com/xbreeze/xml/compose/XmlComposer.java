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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlSaxHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.xbreeze.xml.DeComposerException;
import com.xbreeze.xml.utils.FileUtils;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

import net.sf.saxon.jaxp.SaxonTransformerFactory;

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
		composeUsingXmlBeans(xmlFile, xmlTargetFilePath);
		
		// Compose using Xslt.
		//composeUsingXslt(xmlFile, xmlTargetFilePath);
		
		// Compose using own XInclude implementation.
		//composeUsingOwnIncludeImplementation(xmlFile, xmlTargetFilePath);

		// Done
		logger.info("Done.");

	}
	
	/**
	 * Use the Apache XmlBeans for resolving all includes. For some strange reason it will at a carriage return after the XML declaration.
	 * @param xmlFile
	 * @param xmlTargetFilePath
	 * @throws Exception
	 */
	private void composeUsingXmlBeans(File xmlFile, String xmlTargetFilePath) throws Exception {
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
		XmlSaxHandler xmlSaxHandler = XmlObject.Factory.newXmlSaxHandler();
		// Set the SaxHandler as the content handler for the XML Reader.
		xmlReader.setContentHandler(xmlSaxHandler.getContentHandler());
		// Parse the decomposed root file (which will also parse all it includes for us).
		xmlReader.parse(xmlFile.getAbsolutePath());
		// Get the XmlObject which was just loaded using the sax handler.
		XmlObject composedXmlObject = xmlSaxHandler.getObject();
		
		try {
			// Save the resulting composed file.
			composedXmlObject.save(new File(xmlTargetFilePath), new XmlOptions().setSaveOptimizeForSpeed(true));
		} catch (IOException exc) {
			throw new Exception(
					String.format("Error writing to target file %s: %s", xmlTargetFilePath, exc.getMessage()));
		}
	}
	
	/**
	 * With XSLT the SAXParser solves the includes for us, but it will also indent the output file, making it different from the original.
	 * @param xmlFile
	 * @param xmlTargetFilePath
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws FileNotFoundException
	 * @throws TransformerException
	 */
	private void composeUsingXslt(File xmlFile, String xmlTargetFilePath) throws ParserConfigurationException, SAXException, FileNotFoundException, TransformerException {
		// Create a new InputSource on the root decomposed file.
		// Setting the system id is needed, so the sax parser is aware of the path of the file. This way it can resolve relative file paths.
		InputSource inputSrc = new InputSource(xmlFile.getAbsolutePath());
		// Create a XmlReader.
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		// Enable namespaces.
		saxParserFactory.setNamespaceAware(true);
		// Enable XInclude on the sax parser factory.
		saxParserFactory.setXIncludeAware(true);
		// Don't fixup base URI's and language. Otherwise the resulting XML file will contain extra (unwanted) attributes.
		saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
		// Create a new SAX Parser.
		SAXParser saxParser = saxParserFactory.newSAXParser();
		SAXSource src = new SAXSource(saxParser.getXMLReader(), inputSrc); 
		
		TransformerFactory xmlTransformFactory = SaxonTransformerFactory.newInstance();
		//xmlTransformFactory.setFeature("http://apache.org/xml/features/xinclude", true);
		Transformer xmlTransformer = xmlTransformFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream("compose.xslt")));
		StreamResult res = new StreamResult(new FileOutputStream(xmlTargetFilePath));
		xmlTransformer.transform(src, res);
	}
	
	private void composeUsingOwnIncludeImplementation(File xmlFile, String xmlTargetFilePath) throws Exception {
		// Read the xml file into a string.
		//FileInputStreamAndCharset fisac = FileUtils.getFileInputStreamAndCharset(xmlFile.toURI());
		// Create a collection for all includes which are resolved. The key is the URI of the resolved file and the value is the level (or depth) where the file is resolved.
		HashMap<URI, Integer> resolvedIncludes = new HashMap<URI, Integer>();

		//URI xmlFileUri = xmlFile.toURI();
		//Path basePath = FileUtils.getBasePath(Path.of(xmlFile.toURI()));

		// Create a XmlReader.
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		// Enable namespaces.
		//saxParserFactory.setNamespaceAware(true);
		// Enable XInclude on the sax parser factory.
		//saxParserFactory.setXIncludeAware(true);
		// Don't fixup base URI's and language. Otherwise the resulting XML file will contain extra (unwanted) attributes.
		//saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		//saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-language", false);
		// Create a new SAX Parser.
		SAXParser saxParser = saxParserFactory.newSAXParser();
		// Create a XML Reader.
		XMLReader xmlReader = saxParser.getXMLReader();
		
		// Initialize the XmlOptions.
		//HashMap<String, String> namespaces = new HashMap<String, String>();
		//namespaces.put("xi", "http://www.w3.org/2001/XInclude");
		// Create the XmlOptions for the XmlObject during composing.
		XmlOptions xmlOptions = new XmlOptions()
				.setLoadLineNumbers()
				//.setCharacterEncoding(fisac.getFileCharset().toString())
				//.setDocumentSourceName(xmlFile.getAbsolutePath())
				.setLoadUseXMLReader(xmlReader)
				//.setBaseURI(basePath.toUri())
				//.setLoadAdditionalNamespaces(namespaces);
		;
		// Resolve all includes
		XmlObject composedXmlObject = this.resolveIncludes(xmlFile.toURI(), 0, resolvedIncludes, xmlOptions, null);
//		XmlObject composedXmlObject = XmlObject.Factory.parse(xmlFile, xmlOptions);
		
		try {
			// Save the resulting composed file.
			composedXmlObject.save(new File(xmlTargetFilePath));
		} catch (IOException exc) {
			throw new Exception(
					String.format("Error writing to target file %s: %s", xmlTargetFilePath, exc.getMessage()));
		}
	}
	
	private XmlObject resolveIncludes(URI xmlFileUri, int level, HashMap<URI, Integer> resolvedIncludes, XmlOptions xmlOptions, XmlCursor parentCursor) throws Exception {
		logger.fine(String.format("Scanning file %s for includes", xmlFileUri.toString()));
		// Check for cycle detection, e.g. an include that is already included previously
		if (resolvedIncludes.containsKey(xmlFileUri) && resolvedIncludes.get(xmlFileUri) != level) {
			throw new Exception(
					String.format("Include cycle detected at level %d, file %s is already included previously", level,
							xmlFileUri.toString()));
		} else if (!resolvedIncludes.containsKey(xmlFileUri)) {
			resolvedIncludes.put(xmlFileUri, level);
		}

		// Get basePath of the file.
		// If the provided URI refers to a file, use its parent path, if it refers to a folder use it as base path
		try {
			Path basePath = FileUtils.getBasePath(Path.of(xmlFileUri));
			// Load the xml file.
			XmlObject decomposedXmlObject = XmlObject.Factory.parse(xmlFileUri.toURL(), xmlOptions);
			
			// Loop through all xi:include elements.
			// The XInclude W3C Reference: https://www.w3.org/TR/xinclude-11/
			int includeCount = 0;
			XmlCursor decomposedXmlCursor = decomposedXmlObject.newCursor();
			decomposedXmlCursor.selectPath("declare namespace xi='http://www.w3.org/2001/XInclude' //xi:include");
			while (decomposedXmlCursor.hasNextSelection()) {
				// Move to the next include element.
				decomposedXmlCursor.toNextSelection();
				// Obtain the filename of include
				String includeFileLocation = decomposedXmlCursor.getAttributeText(QName.valueOf("href"));
				
				// If the href attribute isn't found, throw an exception.
				if (includeFileLocation == null) {
					throw new DeComposerException("Error finding href attribute on include", decomposedXmlCursor);
				}
				
				logger.fine(String.format("Found include for %s in config file %s", includeFileLocation, xmlFileUri.toString()));
				// Resolve include to a valid path against the basePath
				logger.fine(String.format("base path %s", basePath.toString()));
				URI includeFileUri = null;
				try {
					includeFileUri = basePath.resolve(includeFileLocation).toRealPath(LinkOption.NOFOLLOW_LINKS).toUri();
				} catch (IOException e) {
					throw new XmlException(XmlError.forCursor(String.format("Error resolving found include %s for %s to canonical path: %s", includeFileLocation, xmlFileUri.toString(), e.getMessage()), decomposedXmlCursor));
				}
				logger.fine(String.format("Resolved include to %s", includeFileUri.toString()));

				try {
					// Remove the include element.
					decomposedXmlCursor.removeXml();
					
					// Let the child resolve it's own include and copy it's contents after resolving into the current cursor position.
					this.resolveIncludes(includeFileUri, level + 1, resolvedIncludes, xmlOptions, decomposedXmlCursor);

				} catch (IOException e) {
					throw new Exception(String.format("Could not read contents of included file %s", includeFileUri.toString()), e);
				}
				includeCount++;
			}
			// Dispose the cursor.
			decomposedXmlCursor.dispose();
			
			logger.fine(String.format("Found %d includes in XML file %s", includeCount, xmlFileUri.toString()));
			// Output and parse the modifier and return it.
			if (parentCursor != null) {
				//String resolvedXML = decomposedXmlObject.toString();
				XmlCursor childCopyCursor = decomposedXmlObject.newCursor();
				// Copy the whole contents of the current document to the parent.
				childCopyCursor.copyXmlContents(parentCursor);
				// Dispose the copy cursor.
				childCopyCursor.dispose();
			}
			// Return the resulting decomposed XML object.
			return decomposedXmlObject;
		} catch (URISyntaxException e) {
			throw new Exception(String.format("Could not extract base path from XML file %s", xmlFileUri.toString()),
					e);
		} catch (XPathParseException | XPathEvalException e) {
			throw new Exception(String.format("XPath error scanning for includes in %s", xmlFileUri.toString()), e);
		}
	}
}
