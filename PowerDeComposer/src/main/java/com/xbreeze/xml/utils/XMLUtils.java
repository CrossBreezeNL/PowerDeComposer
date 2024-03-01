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
package com.xbreeze.xml.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	 * Get the VTDNav object for a XML document namespace-unaware.
	 * @param xmlDocument The XML document as a String.
	 * @param charsetToUse The character set to use for the file contents.
	 * @return The VTDNav.
	 * @throws GeneratorException
	 */
	public static VTDNav getVTDNav(String xmlDocument, Charset charsetToUse) throws Exception {
		return getVTDNav(new FileContentAndCharset(xmlDocument, charsetToUse), false);
	}
	
	/**
	 * Get the VTDNav object for a XML document.
	 * @param xmlDocument The XML document as a String.
	 * @param charsetToUse The character set to use for the file contents.
	 * @param namespaceAware Whether VTD-Nav should be namespace aware.
	 * @return The VTDNav.
	 * @throws GeneratorException
	 */
	public static VTDNav getVTDNav(String xmlDocument, Charset charsetToUse, boolean namespaceAware) throws Exception {
		return getVTDNav(new FileContentAndCharset(xmlDocument, charsetToUse), namespaceAware);
	}
	
	/**
	 * Get the VTDNav object for a XML document.
	 * @param xmlFileContentsAndCharset The XML document wrapped in a FileContentAndCharset object.
	 * @param namespaceAware Whether the parser is namespace aware.
	 * @return The VTDNav.
	 * @throws GeneratorException
	 */
	public static VTDNav getVTDNav(FileContentAndCharset xmlFileContentsAndCharset, boolean namespaceAware) throws Exception {
		return getVTDNav(xmlFileContentsAndCharset.getBytes(), namespaceAware);
	}
	
	/**
	 * Get the VTDNav object for a XML document.
	 * See: https://vtd-xml.sourceforge.io/javadoc/.
	 * @param xmlFileBytes The XML document as a byte[].
	 * @param namespaceAware Whether the parser is namespace aware.
	 * @return The VTDNav.
	 * @throws GeneratorException
	 */
	public static VTDNav getVTDNav(byte[] xmlFileBytes, boolean namespaceAware) throws Exception {
		// Create a VTGGen object.
		VTDGen vg = new VTDGen();
		
		// Enable collecting all whitespaces.
		vg.enableIgnoredWhiteSpace(true);
		
		// Set the document (in original file encoding).
		vg.setDoc(xmlFileBytes);
		
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
	 * @throws Exception 
	 */
	public static String getXPathText(VTDNav nv, String xpath) throws Exception {
		// Create a pattern to recognize XPath expressions on processing instructions.
		Pattern piAttributeXPathPattern = Pattern.compile("(?<PIXPath>/?/processing-instruction\\(.+\\))/@(?<PIAttribute>.+)");
		// Get the matcher for the PI attribute xpath.
		Matcher piAttributeXPathMatcher = piAttributeXPathPattern.matcher(xpath);
		// Check whether the XPath matches a processing-instruction selection.
		if (piAttributeXPathMatcher.matches()) {
			String piNodeXPath = piAttributeXPathMatcher.group("PIXPath");
			String piAttributeToRemove = piAttributeXPathMatcher.group("PIAttribute");
			AutoPilot sap = new AutoPilot(nv);
			sap.selectXPath(piNodeXPath);
			if (sap.evalXPath() != -1) {
				int currentNodeIndex = nv.getCurrentIndex();
				// If the token is a processing-instruction name, we can select the value for the specific attribute.
				if (nv.getTokenType(currentNodeIndex) == VTDNav.TOKEN_PI_NAME) {
    				// The processing instruction value is in the token after the name.
        			String piValue = nv.toRawString(currentNodeIndex + 1);
        			Pattern piAttributePattern = Pattern.compile(String.format(" %s=\\\"[a-zA-Z0-9]+\\\"", piAttributeToRemove));
        			Matcher piAttributeMatcher = piAttributePattern.matcher(piValue);
        			if (piAttributeMatcher.find()) {
        				// Return the found processing instruction attribute.
        				return piAttributeMatcher.group();
        			}
        			// If the processing instruction attribute can't be found, return null.
        			else {
        				return null;
        			}
				} else {
					throw new Exception(String.format("The processing instruction XPath didn't result in a processing instruction token for: %s.", xpath));
				}
			}
			else {
				throw new Exception(String.format("The processing instruction wasn't found for: %s.", xpath));
			}
			
		}
		// If the XPath isn't a processing instruction attribute selection, perform the normal XPath on the AutoPilot.
		else {
			AutoPilot sap = new AutoPilot(nv);
			sap.selectXPath(xpath);
			return sap.evalXPathToString();			
		}
	}
	
	/**
	 * Get the element name.
	 * @param nv
	 * @param subElementName
	 */
	public static String getElementName(VTDNav nv) throws XPathParseException, NavException, XPathEvalException {
		AutoPilot sap = new AutoPilot(nv);
		sap.selectXPath("name()");
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
