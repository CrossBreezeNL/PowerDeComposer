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
package com.xbreeze.xml.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import com.ximpleware.AutoPilot;
import com.ximpleware.ModifyException;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.TranscodeException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;

public class XMLUtils {
	// The logger for this class.
	protected static final Logger logger = Logger.getLogger(XMLUtils.class.getName());
	
	/**
	 * Escape XML characters.
	 * @param input The text to escape.
	 * @return The escaped input.
	 */
	public static String excapeXMLChars(String input) {
		return input.replaceAll("\\<", "&lt;").replaceAll("\\>", "&gt;");
	}
	
	/**
	 * Get the VTDNav object for a XML document.
	 * @param xmlDocument The XML document as a String.
	 * @return The VTDNav.
	 * @throws GeneratorException
	 */
	public static VTDNav getVTDNav(String xmlDocument) throws Exception {
		return getVTDNav(xmlDocument, false);
	}
	
	/**
	 * Get the VTDNav object for a XML document.
	 * See: https://vtd-xml.sourceforge.io/javadoc/.
	 * @param xmlDocument The XML document as a String.
	 * @param namespaceAware Whether the parser is namespace aware.
	 * @return The VTDNav.
	 * @throws GeneratorException
	 */
	public static VTDNav getVTDNav(String xmlDocument, boolean namespaceAware) throws Exception {
		// Create a VTGGen object.
		VTDGen vg = new VTDGen();
		
		// Enable collecting all whitespaces.
		vg.enableIgnoredWhiteSpace(true);
		
		// Set the document (in UTF-8 encoding).
		// Currently the encoding is set to US_ASCII, cause this solves the issue for special characters and doesn't seem to break anything.
		// Question is asked to the vtd-gen developer if this is a bug in vtg-gen.
		// https://stackoverflow.com/questions/51507388/vtd-xml-element-fragment-incorrect
		//vg.setDoc(xmlDocument.getBytes(StandardCharsets.US_ASCII));
		// Disabled the bytes retrieval using a specific charset, because this breaks the PowerDesigner special characters.
		vg.setDoc(xmlDocument.getBytes());
		
		// When enabling namespace awareness, you must map the URLs of all used namespaces here.
		try {
			vg.parse(namespaceAware);
		} catch (ParseException e) {
			throw new Exception(String.format("Error while reading file as XML document: %s.", e.getMessage()), e);
		}
		
		// Create a VTDNav for navigating the document.
		return vg.getNav();
	}
	
	/**
	 * Write the new XML structure to a String.
	 * @param xm The XMLModifier
	 * @return The resulting XML document as a String.
	 * @throws GeneratorException 
	 */
	public static String getResultingXml(XMLModifier xm) throws Exception {
		// Write the XML document into a ByteArray.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
			xm.output(baos);
		} catch (ModifyException | TranscodeException | IOException e) {
			throw new Exception("Error while getting resulting XML document after modification(s).", e);
		}
        // Store the ByteArray into a String.
        String modifiedTemplate = baos.toString();
        // Close the output stream.
        try {
			baos.close();
		} catch (IOException e) {
			throw new Exception("Error while closing ByteArrayOutputStream after writing XML document.", e);
		}
        // Return the XML document as a String.
        return modifiedTemplate;
	}
	
	/**
	 * Function to get a more informative error message when a XPathParseException is fired while using the VTD-Gen AutoPilot.
	 * @param xPath The XPath which was parsed.
	 * @param e The exception which was thrown.
	 * @return The improved exception message.
	 */
	public static String getAutopilotExceptionMessage(String xPath, Exception e) {
		// Init the exception message.
		String exceptionMessage = e.getMessage();
		// If the exception is a XPathParseException, create a exception message using the offset.
		if (e instanceof XPathParseException && ((XPathParseException) e).getOffset() > 0) {
			int substringEnd = ((XPathParseException) e).getOffset();
			int substringStart = (substringEnd <= 10) ? 0 : substringEnd - 10;
			exceptionMessage = String.format("Syntax error after or around the end of ´%s´", xPath.substring(substringStart, substringEnd));
		}
		// Return the exception message.
		return exceptionMessage;
	}
	
	/**
	 * Get the text value of a sub element.
	 * @param nv
	 * @param subElementName
	 * @return
	 * @throws XPathParseException
	 */
	public static String getSubElementText(VTDNav nv, String subElementName) throws XPathParseException {
		AutoPilot sap = new AutoPilot(nv);
		sap.selectXPath(String.format("./%s", subElementName));
		return sap.evalXPathToString();
	}
	
	/**
	 * Get the text value of a sub element.
	 * @param nv
	 * @param subElementName
	 * @return
	 * @throws XPathParseException
	 * @throws XPathEvalException 
	 * @throws NavException 
	 */
	public static String getParentElementName(VTDNav nv) throws XPathParseException, NavException, XPathEvalException {
		VTDNav parentNav = nv.cloneNav();
		AutoPilot sap = new AutoPilot(parentNav);
		sap.selectXPath("..");
		int parentElementIndex = sap.evalXPath();
		if (parentElementIndex != -1)
			return parentNav.toString(parentElementIndex);
		else
			return null;
	}
	
	/**
	 * Get the given element name without namespace (if there is a namespace included).
	 * @param elementNameWithNamespace
	 * @return
	 */
	public static String getElementNameWithoutNameSpace(String elementNameWithNamespace) {
    	String elementNameWithoutNameSpace = elementNameWithNamespace;
    	int namespaceSeperatorIndex = elementNameWithNamespace.indexOf(':');
    	if (namespaceSeperatorIndex != -1) {
    		elementNameWithoutNameSpace = elementNameWithNamespace.substring(namespaceSeperatorIndex+1);
    	}
    	return elementNameWithoutNameSpace;
	}
	
	
}
