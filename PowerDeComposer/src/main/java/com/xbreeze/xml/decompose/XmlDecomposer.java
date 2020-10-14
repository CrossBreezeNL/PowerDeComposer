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
package com.xbreeze.xml.decompose;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xbreeze.xml.decompose.config.DecomposableElementConfig;
import com.xbreeze.xml.decompose.config.DecomposeConfig;
import com.xbreeze.xml.decompose.config.IncludeAttributeConfig;
import com.xbreeze.xml.decompose.config.NodeRemovalConfig;
import com.xbreeze.xml.utils.FileUtils;
import com.xbreeze.xml.utils.XMLUtils;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathParseException;

public class XmlDecomposer {
	private static final Logger logger = Logger.getLogger(XmlDecomposer.class.getName());
	
	private static final String STR_PREFIX_SPACER = "  ";
	
	public XmlDecomposer(String xmlFilePath, String targetDirectory, DecomposeConfig decomposeConfig) throws Exception {
		decomposeXml(xmlFilePath, targetDirectory, decomposeConfig);
	}
	
	private void decomposeXml(String xmlFilePath, String targetDirectory, DecomposeConfig decomposeConfig) throws Exception {
		
		logger.info(String.format("Starting Xml Decomposer for '%s'", xmlFilePath));
		
		File xmlFile = new File(xmlFilePath);
		Path targetDirectoryPath = Paths.get(targetDirectory);
		
		// Check whether the XML file to decompose exists.
		if (!xmlFile.exists())
			throw new Exception(String.format("The specified xml file doesn't exist '%s'.", xmlFilePath));
		
		// Read the xml file into a string.
		logger.fine("Getting file contents...");
		String xmlFileContents = FileUtils.getFileContent(xmlFile.toURI());
		//logger.fine("Start of file contents:");
		//logger.fine("--------------------------------------------------");
		//logger.fine(xmlFileContents);
		//logger.fine("--------------------------------------------------");
		//logger.fine("End of file contents:");
		
		// Create a VTDNav for navigating the document.
		VTDNav nv;
		try {
			logger.fine("Creating VTDNav on file contents...");
			// We need to set the VTDNav to namespace unaware, since we will also process parts of the XML document
			// in the recursive parseAndWriteDocumentParts function. In the document parts the xmlns is not defined on
			// the elements anywhere, so it can't parse the XML with namespaces.
			nv = XMLUtils.getVTDNav(xmlFileContents, false);
		} catch (Exception e) {
			throw new Exception(String.format("Error while parsing Xml document: %s", e.getMessage()), e);
		}
		
		// Prepare the Xml Document.
		nv = prepareDocument(nv);
		
		if (decomposeConfig.getNodeRemovalConfigs() != null && decomposeConfig.getNodeRemovalConfigs().size() > 0) {
			nv = removeNodes(nv, decomposeConfig.getNodeRemovalConfigs());
		}
		
		// Parse and write document parts.
		logger.info("Parsing and writing document parts...");
		parseAndWriteDocumentParts(nv, null, xmlFile.getName(), targetDirectoryPath, 0, decomposeConfig);
		logger.info("Done parsing and writing document parts.");
		
		// Done
		logger.info("Done.");
	}
	
	/**
	 * Prepare the PowerDesigner Xml Document to be parsed and chopped into pieces.
	 * In this procedure the following steps are done:
	 *  - Find all ObjectID's and its internal Id values.
	 *  - Replace all internal id values and references with the ObjectIDs.
	 * @param docXml
	 * @return
	 * @throws Exception 
	 */
	private VTDNav prepareDocument(VTDNav nv) throws Exception {
		logger.info("Preparing PowerDesigner model document...");
		
		// We are going to replace all Id="o?" and Ref="o?" values, so we need an XmlModifier.
		logger.info(" - Overwriting local ids with global ids...");
		XMLModifier xm;
		try {
			xm = new XMLModifier(nv);
		} catch (Exception e) {
			throw new Exception("Error while initializing XMLModifier");
		}
		
		AutoPilot ap = getAutoPilot(nv);
		
		// Select all elements which have an Id and an ObjectID.
		ap.selectXPath("//*[./@Id and ./ObjectID]");
		
		// Create a list of local and global ids.
		HashMap<String, String> localToGlobalIds = new HashMap<String, String>();
		while ((ap.evalXPath()) != -1) {
			String elementName = nv.toString(nv.getCurrentIndex());
			int localObjectIdIndex = nv.getAttrVal("Id");
	    	String localObjectId = nv.toString(localObjectIdIndex);
	    	String globalObjectId = XMLUtils.getSubElementText(nv, "ObjectID");

	    	logger.fine(String.format("Found local id '%s' with global id '%s' (element: '%s')", localObjectId, globalObjectId, elementName));
	    	localToGlobalIds.put(localObjectId, globalObjectId);
	    	
	    	xm.updateToken(localObjectIdIndex, globalObjectId);
		}
		
		// Loop through all local ids and replaces all references to it with the global.
		logger.info(" - Overwriting local refs with global ids...");
		// In stead of looping through specific refs, loop through all refs and replace them there.
		try {
			ap.resetXPath();
			ap.selectXPath("//*/@Ref");
		} catch (XPathParseException e) {
			throw new Exception(String.format("Error while replacing @Ref attribute values: %s", e.getMessage()), e);
		}
		
		// Find all references on the local id and replace it with the global id.
		while ((ap.evalXPath()) != -1) {
			
			// Get the current index and add 1, because the current is the attribute name, and we want the attribute value.
			int localObjectRefIndex = nv.getCurrentIndex() + 1;
			String localObjectId = nv.toString(localObjectRefIndex);
			// Replace the local id with the global id if it is in the collection.
			if (localToGlobalIds.containsKey(localObjectId)) {
				String globalObjectId = localToGlobalIds.get(localObjectId);
		    	logger.fine(String.format("Found reference id '%s' with global id '%s' (index: %d)", localObjectId, globalObjectId, localObjectRefIndex));
		    	// Update local reference to the global GUID.
		    	xm.updateToken(localObjectRefIndex, globalObjectId);
			}
		}
		
		logger.info("Done preparing PowerDesigner model document.");
		
		// Output and reparse the modifier xml to the VtdNav.
		return xm.outputAndReparse();
	}
	
	/**
	 * Create an autopilot for PowerDesigner models.
	 * @param nv The VTDNav object.
	 * @return The AutoPilot where the namespaces for PowerDesigner models are registered.
	 */
	private static AutoPilot getAutoPilot(VTDNav nv) {
		AutoPilot ap = new AutoPilot(nv);
		/**
		 * xmlns:a="attribute" xmlns:c="collection" xmlns:o="object"
		 */
		ap.declareXPathNameSpace("a", "attribute");
		ap.declareXPathNameSpace("c", "collection");
		ap.declareXPathNameSpace("o", "object");
		
		return ap;
	}
	
	/**
	 * Function to remove all nodes as specified in the config.
	 * @param nv The VTDNav object.
	 * @param decomposeConfig The decompose config.
	 * @return The VTDNav on the model object where the nodes are removed.
	 * @throws Exception
	 */
	private VTDNav removeNodes(VTDNav nv, List<NodeRemovalConfig> nodeRemovalConfigs) throws Exception {
		logger.info("Removing nodes from the PowerDesigner model document...");
		
		XMLModifier xm;
		try {
			xm = new XMLModifier(nv);
		} catch (Exception e) {
			throw new Exception("Error while initializing XMLModifier");
		}
		
		// Create a pattern to recognize XPath expressions on processing instructions.
		Pattern piAttributeRemovalPattern = Pattern.compile("(?<PIXPath>/?/processing-instruction\\(.+\\))/@(?<PIAttribute>.+)");
		
		for (NodeRemovalConfig nodeRemovalConfig : nodeRemovalConfigs) {
			AutoPilot ap = getAutoPilot(nv);
			String nodeRemovalXPath = nodeRemovalConfig.getXPath();
			logger.fine(String.format("Removing nodes using XPath expression '%s'...", nodeRemovalXPath));
			
			// If the node removal instruction is on a processing instruction, find the attribute which needs to be removed.
			String piAttributeToRemove = null;
			Matcher piAttributeRemovalMatcher = piAttributeRemovalPattern.matcher(nodeRemovalXPath);
			if (piAttributeRemovalMatcher.matches()) {
				nodeRemovalXPath = piAttributeRemovalMatcher.group("PIXPath");
				piAttributeToRemove = piAttributeRemovalMatcher.group("PIAttribute");
				logger.fine(String.format(" - The node removal is in a processing instruction (PIXPath='%s';PIAttribute='%s').", nodeRemovalXPath, piAttributeToRemove));
			}
			ap.selectXPath(nodeRemovalXPath);
			
			// Execute the XPath expression and loop through the results.
			boolean removedNodes = false;
	        while ((ap.evalXPath()) != -1) {
	        	int currentNodeIndex = nv.getCurrentIndex();
				logger.fine(String.format(" - Removing node '%d' (offset=%d;length=%d;type=%d)...", currentNodeIndex, nv.getTokenOffset(currentNodeIndex), nv.getTokenLength(currentNodeIndex), nv.getTokenType(currentNodeIndex)));
	        	// If the node is an element, expand the element offset with the leading whitespace, so we also remove whitespace before this node.
	        	if (nv.getTokenType(currentNodeIndex) == VTDNav.TOKEN_STARTING_TAG) {
	        		xm.remove(nv.expandWhiteSpaces(nv.getElementFragment(), VTDNav.WS_LEADING));
	        	}
	        	// If the token is a processing-instruction name, we need to remove the token before and after as well.
	        	else if (nv.getTokenType(currentNodeIndex) == VTDNav.TOKEN_PI_NAME) {
	        		// If there is an attribute specified on the processing instruction, find the attribute in the processing instruction.
	        		if (piAttributeToRemove != null) {
        				// The processing instruction value is in the token after te name.
	        			String piValue = nv.toRawString(currentNodeIndex + 1);
	        			int piValueOffset = nv.getTokenOffset(currentNodeIndex + 1);
	        			logger.fine(String.format(" - Processing instruction value: '%s'", piValue));
	        			Pattern piAttributePattern = Pattern.compile(String.format(" %s=\\\"[a-zA-Z0-9]+\\\"", piAttributeToRemove));
	        			Matcher piAttributeMatcher = piAttributePattern.matcher(piValue);
	        			if (piAttributeMatcher.find()) {
	        				logger.fine(String.format(" - Removing processing instruction attribute: '%s'", piAttributeMatcher.group()));
	        				// Remove the processing instruction attribute from the xml document.
	        				xm.removeContent(piValueOffset + piAttributeMatcher.start(), piAttributeMatcher.end() - piAttributeMatcher.start());
	        			}
	        		}
	        		// If there is no attribute removal specified on the XPath on the processing instruction, remove the whole processing instruction.
	        		else {
		        		// The processing instruction offset is the start of the processing instruction name minus 2 characters (<?).
		        		int piOffset = nv.getTokenOffset(currentNodeIndex) - 2;
		        		int piLength = nv.getTokenOffset(currentNodeIndex + 1) + nv.getTokenLength(currentNodeIndex + 1) + 2 - piOffset;
		        		
		        		logger.fine(String.format(" - Node content: '%s'", nv.toRawString(piOffset, piLength)));
		    	    	long piFragment = ((long)piLength)<<32| piOffset;
		    	    	xm.remove(nv.expandWhiteSpaces(piFragment, VTDNav.WS_LEADING));
	        		}
	        	}
	        	// If the node is not an element, remove the whole token.
	        	else {
	        		xm.removeToken(currentNodeIndex);
	        	}
	        	// Update removedNodes to true.
	        	removedNodes = true;
	        }
	        
	        // If nodes were removed, re-init the VTDNav and reset the XMLModifier.
	        if (removedNodes) {
		        // Output and re-parse the document for the next injection.
		        // This is necessary, otherwise exceptions will be thrown when injecting attributes for the same element.
		        nv = xm.outputAndReparse();
		        // Reset and bind the modifier to the new VTDNav object.
		        xm.reset();
		        xm.bind(nv);
	        } else {
	        	logger.warning(String.format("The NodeRemoval instruction yielded no nodes ('%s').", nodeRemovalXPath));
	        }
		}
		
		logger.info("Done removing nodes from the PowerDesigner model document.");
		
		// We can return the nv object since it is already reparsed, reset and bind in the latest iteration of the config loop.
		return nv;
	}
	
	/**
	 * Recursively parse the docPartXml to created xi:include reference and write the resulting Xml document to a file.
	 * @param docPartXml
	 * @param targetFileName
	 * @param targetDirectoryPath
	 * @param currentPartIsRoot
	 * @throws Exception
	 */
	private static Path parseAndWriteDocumentParts(VTDNav nv, String currentObjectName, String targetFileName, Path targetDirectoryPath, int depth, DecomposeConfig decomposeConfig) throws Exception {
		// Create the prefix string based on the depth.
		String prefix = String.join("", Collections.nCopies(depth, STR_PREFIX_SPACER));
		logger.fine(String.format("%s> %s", prefix, targetDirectoryPath));
		
		// We are going to remove all nodes which are included, so we need a XmlModifier.
		XMLModifier xm;
		try {
			xm = new XMLModifier(nv);
		} catch (Exception e) {
			throw new Exception("Error while initializing XMLModifier");
		}
		
		// Get a reference to the decomposable element configuration.
		DecomposableElementConfig decomposableElementConfig = decomposeConfig.getDecomposableElementConfig();
		
		AutoPilot ap = getAutoPilot(nv);
		// Select all elements which conform to the elements conditions as specified in the config.
		ap.selectXPath(String.format("//*[%s]", decomposableElementConfig.getXPathExpression()));
		int minimumNextOffset = 0;
		int extractedChildCount = 0;
		// Loop through the found elements.
		while ((ap.evalXPath()) != -1) {
			// skip the root element.
			if (nv.getCurrentIndex() == nv.getRootIndex())
				continue;
			
	    	// Get the parent element name, to be used as the folder name.
	    	String parentElementName = XMLUtils.getParentElementName(nv);
	    	
	    	// Get the current element name without namespaces.
			//String elementName = XMLUtils.getElementNameWithoutNameSpace(nv.toString(nv.getCurrentIndex()));
			
	    	// Get the element offset and length (including whitespaces).
	    	long elementOffsetAndLength = nv.getElementFragment();
	    	int elementOffset = (int)elementOffsetAndLength;
	    	int elementLength = (int)(elementOffsetAndLength>>32);
	    	
	    	logger.fine(String.format("%s - Found element: '%s' at %d till %d", prefix, nv.toRawString(nv.getCurrentIndex()), elementOffset, elementOffset + elementLength));
	    	
	    	// If the current offset is not after the end of the previous fragment, skip this one (it is part of a section passed into the recursive call).
	    	if (elementOffset < minimumNextOffset)
    			continue;
	    	
	    	logger.fine(String.format("%s - After the offset of the previous element (%d < %d)...", prefix, elementOffset, minimumNextOffset));
	    	
	    	// Make sure the next index found by the AutoPilot is after the current fragment (used in the previous if condition).
	    	minimumNextOffset = elementOffset + elementLength;
	    	
	    	// Get the sub element contents to use as the child object name (and thus for the file name).
	    	String childObjectName = XMLUtils.getXPathText(nv, decomposableElementConfig.getTargetFileNameConfig().getXPath());
			HashMap<String, String> includeAttributesWithValues = new HashMap<String, String>();
			// Loop over the sub elements to include to fill the hashmap.
			// We have to do this loop here, and can't do it later in the code, since the pointer is now on the right spot in the model file.
			for (IncludeAttributeConfig includeAttributeConfig : decomposableElementConfig.getIncludeAttributeConfigs()) {
				String subElementText = XMLUtils.getXPathText(nv, includeAttributeConfig.getXPath());
				// Only include the attribute if it contains a value.
				if (subElementText.length() > 0)
					includeAttributesWithValues.put(includeAttributeConfig.getName(), subElementText);
			}
	    	
	    	if (parentElementName == null)
	    		logger.info(String.format("The parent element name is not found for %s: %s.", decomposableElementConfig.getTargetFileNameConfig().getXPath(), childObjectName));
			
	    	// Get the contents of the XML Fragment.
	    	String objectXmlPart = nv.toRawString(elementOffset, elementLength);
	    	// Parse the XML Fragment and write it to its own file.
	    	// If the parent element name contains a namespace part, remove it.
	    	String parentElementFolder = XMLUtils.getElementNameWithoutNameSpace(parentElementName);
	    	
			// Remove the xml node which is being referenced.
			xm.removeContent(elementOffset, elementLength);
			
			// Create a VTDNav for navigating the document.
			VTDNav partNv;
			try {
				partNv = XMLUtils.getVTDNav(objectXmlPart, false);
			} catch (Exception e) {
				throw new Exception(String.format("Error while parsing Xml Part: %s", e.getMessage()), e);
			}
			
			// Set the relative path with a parent folder of the current element name.
			String relativePath = String.format("./%s/%s/", parentElementFolder, childObjectName);
			//Path childTargetDirectory = targetDirectoryPath.resolve(parentElementFolder).resolve(objectName);
			Path childTargetDirectory = targetDirectoryPath.resolve(relativePath);
			// Derive the object file name.
			String childObjectFileName = String.format("%s.xml", childObjectName);
			Path childFileLocation = parseAndWriteDocumentParts(partNv, childObjectName, childObjectFileName, childTargetDirectory, depth + 1, decomposeConfig);
			
			// Insert the include tag for the found object.
			String actualRelativePath = targetDirectoryPath.relativize(childFileLocation).toString();
			
			// Construct the include tag contents.
			StringBuffer includeElementStringBuffer = new StringBuffer();
			//includeElementStringBuffer.append(String.format("<xi:include href=\"%s\" type=\"%s\"", actualRelativePath, elementName));
			includeElementStringBuffer.append(String.format("<xi:include href=\"%s\"", actualRelativePath));
			// Loop through the include attributes to add the min the include tag.
			for (String includeAttributeName : includeAttributesWithValues.keySet()) {
				// Insert the include sub element in the include tag.
				includeElementStringBuffer.append(String.format(" %s=\"%s\"", includeAttributeName, XMLUtils.excapeXMLChars(includeAttributesWithValues.get(includeAttributeName))));				
			}
			includeElementStringBuffer.append(" />");
			
			// Insert the full include element.
	    	xm.insertBeforeElement(includeElementStringBuffer.toString());
			
			// Increase the extracted child count.
			extractedChildCount++;
		}
		
		logger.fine(String.format("%s - Found %d childs", prefix, extractedChildCount));
		
        // Get the root element of the document.
		logger.fine(String.format("%s - Writing file: %s", prefix, targetFileName));
		Path targetFilePath = targetDirectoryPath.resolve(targetFileName);
		// If the current element does not have extracted child elements, store the file in the parent folder.
		if (depth != 0 && extractedChildCount == 0) {
			targetFilePath = targetDirectoryPath.getParent().resolve(targetFileName);
		}
		// Write the resulting XML file.
		// Get the target folder of the target file.
		File targetFolder = targetFilePath.getParent().toFile();
		// Create the target folder(s) if they don't exist.
		if (!targetFolder.exists()) {
			if (!targetFolder.mkdirs()) {
				throw new Exception(String.format("Error while creating parent directories for '%s'", targetFilePath.toString()));
			}
		}
		// Write the target Xml file.
		xm.output(new FileOutputStream(targetFilePath.toString()));
		//logger.fine(String.format("%s< %s", prefix, targetDirectoryPath));
		
		return targetFilePath;
	}

}
