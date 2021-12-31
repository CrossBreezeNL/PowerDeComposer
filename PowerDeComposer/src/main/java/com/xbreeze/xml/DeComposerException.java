package com.xbreeze.xml;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlLineNumber;

public class DeComposerException extends Exception {

	/**
	 * Generated serial version UID. 
	 */
	private static final long serialVersionUID = 3172138596951617184L;

	public DeComposerException(String message, XmlCursor xmlCursor) {
		this(message, (XmlLineNumber)xmlCursor.getBookmark(XmlLineNumber.class), xmlCursor.documentProperties().getSourceName());
	}
	
	public DeComposerException(String message, XmlCursor xmlCursor, String sourceName) {
		this(message, (XmlLineNumber)xmlCursor.getBookmark(XmlLineNumber.class), sourceName);		
	}
	
	public DeComposerException(String message, XmlLineNumber xmlLineNumber, String sourceName) {
		super(String.format("%s @ line %d column %d in %s", message, xmlLineNumber.getLine(), xmlLineNumber.getColumn(), sourceName));		
	}
}
