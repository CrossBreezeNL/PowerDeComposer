/**
 * 
 */
package com.xbreeze.xml.compose;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author HarmenWessels
 *
 */
public class PdcXmlHandler implements ContentHandler, LexicalHandler, DeclHandler, DTDHandler {
	private static final Logger logger = Logger.getGlobal();
	
	private Locator _locator;
	
	private void logEvent(String interfaceName, String message, Object... args) {
		logger.log(Level.INFO, interfaceName.concat("::").concat(message), args);
		if (_locator != null) {
			logger.log(Level.INFO, "\tLocation:");
			if (_locator.getPublicId() != null)
				logger.log(Level.INFO, "\t\tpublic-id=\"{0}\"", new Object[] {_locator.getPublicId()});
			if (_locator.getSystemId() != null)
				logger.log(Level.INFO, "\t\tsystem-id=\"{0}\"", new Object[] {_locator.getSystemId()});
			if (_locator.getLineNumber() != -1)
				logger.log(Level.INFO, "\t\tline-number={0}", new Object[] {_locator.getLineNumber()});
			if (_locator.getColumnNumber() != -1)
				logger.log(Level.INFO, "\t\tcolumn-number={0}", new Object[] {_locator.getColumnNumber()});
		}
	}
	
	private void logContentHandlerEvent(String message, Object... args) {
		logEvent("ContentHandler", message, args);
	}
	
	private void logLexicalHandlerEvent(String message, Object... args) {
		logEvent("LexicalHandler", message, args);
	}
	
	private void logDeclHandlerEvent(String message, Object... args) {
		logEvent("DeclHandler", message, args);
	}

	private void logDTDHandlerEvent(String message, Object... args) {
		logEvent("DTDHandler", message, args);
	}
	
	public PdcXmlHandler() {
		logEvent("PdcXmlHandler", "constructor");
	}
	
	/**
	 * ContentHandler methods
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		logContentHandlerEvent("setDocumentLocator");
		if (this._locator == null)
			this._locator = locator;
	}

	@Override
	public void startDocument() throws SAXException {
		logContentHandlerEvent("startDocument");
	}

	@Override
	public void endDocument() throws SAXException {
		logContentHandlerEvent("endDocument");
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		logContentHandlerEvent("startPrefixMapping prefix=\"{0}\"; uri=\"{1}\"", prefix, uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		logContentHandlerEvent("endPrefixMapping prefix=\"{0}\"", prefix);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		logContentHandlerEvent("startElement uri=\"{0}\"; localName=\"{1}\"; qName=\"{2}\"; atts.length={3}", uri, localName, qName, atts.getLength());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		logContentHandlerEvent("endElement uri=\"{0}\"; localName=\"{1}\"; qName=\"{2}\"", uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		logContentHandlerEvent("characters ch=\"{0}\"; start={1}; length={2}", new String(ch).substring(start, start + length), start, length);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		logContentHandlerEvent("ignorableWhitespace ch=\"{0}\"; start={1}; length={2}", new String(ch), start, length);
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		logContentHandlerEvent("processingInstruction target=\"{0}\"; data=\"{1}\"", target, data);

	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		logContentHandlerEvent("skippedEntity name=\"{0}\"", name);
	}

	/**
	 * LexicalHandler methods
	 */
	@Override
	public void startDTD(String name, String publicId, String systemId) throws SAXException {
		logLexicalHandlerEvent("startDTD name=\"{0}\"; publicId=\"{1}\"; systemId=\"{2}\"", name, publicId, systemId);
	}

	@Override
	public void endDTD() throws SAXException {
		logLexicalHandlerEvent("endDTD");
	}

	@Override
	public void startEntity(String name) throws SAXException {
		logLexicalHandlerEvent("startEntity name=\"{0}\"", name);
	}

	@Override
	public void endEntity(String name) throws SAXException {
		logLexicalHandlerEvent("endEntity name=\"{0}\"", name);
	}

	@Override
	public void startCDATA() throws SAXException {
		logLexicalHandlerEvent("startCDATA");

	}

	@Override
	public void endCDATA() throws SAXException {
		logLexicalHandlerEvent("endCDATA");

	}

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		logLexicalHandlerEvent("comment ch=\"{0}\"; start={1}; length={2}", new String(ch).substring(start, length), start, length);
	}

	/**
	 * DeclHandler methods.
	 */
	@Override
	public void elementDecl(String name, String model) throws SAXException {
		logDeclHandlerEvent("elementDecl name=\"{0}\"; model=\"{1}\"", name, model);
	}

	@Override
	public void attributeDecl(String eName, String aName, String type, String mode, String value) throws SAXException {
		logDeclHandlerEvent("attributeDecl eName=\"{0}\"; aName=\"{1}\", type=\"{2}\", mode=\"{3}\", value=\"{4}\"", eName, aName, type, mode, value);
	}

	@Override
	public void internalEntityDecl(String name, String value) throws SAXException {
		logDeclHandlerEvent("internalEntityDecl name=\"{0}\"; value=\"{1}\"", name, value);
	}

	@Override
	public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException {
		logDeclHandlerEvent("externalEntityDecl name=\"{0}\"; publicId=\"{1}\"; systemId=\"{2}\"", name, publicId, systemId);
	}
	
	/**
	 * DTDHandler methods.
	 */
	@Override
	public void notationDecl(String name, String publicId, String systemId) throws SAXException {
		logDTDHandlerEvent("notationDecl name=\"{0}\"; publicId=\"{1}\"; systemId=\"{2}\"", name, publicId, systemId);
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
			throws SAXException {
		logDTDHandlerEvent("unparsedEntityDecl name=\"{0}\"; publicId=\"{1}\"; systemId=\"{2}\"; notationName=\"{3}\"", name, publicId, systemId, notationName);		
	}

}
