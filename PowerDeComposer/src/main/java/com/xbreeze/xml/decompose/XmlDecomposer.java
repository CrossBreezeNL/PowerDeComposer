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
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xbreeze.xml.config.AbstractConfigElementWithXPathAttributeAndCondition;
import com.xbreeze.xml.decompose.config.DecomposableElementConfig;
import com.xbreeze.xml.decompose.config.DecomposeConfig;
import com.xbreeze.xml.decompose.config.IdentifierReplacementConfig;
import com.xbreeze.xml.decompose.config.IncludeAttributeConfig;
import com.xbreeze.xml.decompose.config.NodeRemovalConfig;
import com.xbreeze.xml.utils.FileContentAndCharset;
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
		
		// Check whether the XML file to decompose exists.
		File xmlFile = new File(xmlFilePath);
		if (!xmlFile.exists())
			throw new Exception(String.format("The specified xml file doesn't exist '%s'.", xmlFilePath));
		
		// Read the xml file into a string.
		logger.fine("Getting file contents...");
		FileContentAndCharset xmlFileContentsAndCharset = FileUtils.getFileContent(xmlFile.toURI());
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
			nv = XMLUtils.getVTDNav(xmlFileContentsAndCharset, false);
		} catch (Exception e) {
			throw new Exception(String.format("Error while parsing Xml document: %s", e.getMessage()), e);
		}
		
		Path targetDirectoryPath = Paths.get(targetDirectory);
		Path targetFilePath = targetDirectoryPath.resolve(xmlFile.getName());
		
		// If configured, perform changes detection here before doing anything else.
		if (decomposeConfig.getChangeDetectionConfig() != null) {
			
			// Check if the target file exists.
			if (targetFilePath.toFile().exists()) {
				String changeDetectionXPath = decomposeConfig.getChangeDetectionConfig().getXPath();
				
				// Get a VTDNav on the former decomposed root file.
				FileContentAndCharset formerFcac = FileUtils.getFileContent(targetFilePath.toUri());
				VTDNav formerNv = XMLUtils.getVTDNav(formerFcac, false);
				
				// Evaluate the change detection XPath on both sides to get the values.
				String decomposedCDValue = XMLUtils.getXPathText(formerNv, changeDetectionXPath);
				logger.fine(String.format("Change detection decomposed value: %s", decomposedCDValue));
				String composedCDValue = XMLUtils.getXPathText(nv, changeDetectionXPath);
				logger.fine(String.format("Change detection composed value: %s", composedCDValue));
				
				// If the value doesn't exist on both sides we do nothing.
				if (
					(decomposedCDValue == null && composedCDValue == null)
					||
					(decomposedCDValue != null && composedCDValue != null && decomposedCDValue.length() == 0 && composedCDValue.length() == 0)
				) {
					logger.info("The change detection value is empty on both sides, so stopping.");
					return;
				}
				// If one of the values is null, we decompose.
				else if (decomposedCDValue == null || composedCDValue == null) {
					logger.info("The change detection value is empty on one side, so decomposing.");
				}
				// If the value is equal, we do nothing.
				else if (decomposedCDValue.equals(composedCDValue)) {
					logger.info("The change detection value is equal on both sides, so stopping.");
					return;
				}
				// If the value exists on one side but not on the other side, we decompose.
				// If the value exists on both sides, we decompose if the value is different.
				else {
					logger.info("The composed model is different then the decomposed model, so decomposing.");
				}
			}
			// If the target file doesn't exist, log a warning.
			else {				
				logger.warning(String.format("Change detection is configured, but there is no decomposed file yet (%s).", targetFilePath.toString()));
			}
		}
		
		// Get the existing list of files in the decomposed model (if it exists). This is needed to track files which are written and which need to be deleted.
		HashSet<URI> formerDecomposedFilePaths = new HashSet<URI>();
		addFormerFilePaths(targetFilePath, formerDecomposedFilePaths, null);
		
		// Replace the identifiers in the XML Document, if specified in the config.
		if (decomposeConfig.getIdentifierReplacementConfigs() != null && decomposeConfig.getIdentifierReplacementConfigs().size() > 0) {
			for (IdentifierReplacementConfig identifierReplacementConfig : decomposeConfig.getIdentifierReplacementConfigs()) {
				nv = replaceIdentifiers(nv, identifierReplacementConfig);
			}
		}
		
		if (decomposeConfig.getNodeRemovalConfigs() != null && decomposeConfig.getNodeRemovalConfigs().size() > 0) {
			nv = removeNodes(nv, decomposeConfig.getNodeRemovalConfigs());
		}
		
		// Parse and write document parts, if specified in the config.
		// Keep track of the decomposed files, so we can:
		// - Detect whether a file is written multiple times in one run
		// - Detect which files were in the previous decomposed file, but aren't anymore.
		HashSet<URI> currentDecomposedFilePaths = new HashSet<URI>();
		if (decomposeConfig.getDecomposableElementConfig() != null) {
			logger.info("Parsing and writing document parts...");
			parseAndWriteDocumentParts(nv, xmlFileContentsAndCharset.getFileCharset(), null, xmlFile.getName(), targetDirectoryPath, 0, decomposeConfig.getDecomposableElementConfig(), currentDecomposedFilePaths);
			logger.info("Done parsing and writing document parts.");
		}

		// Remove all file paths which are written in the current run in the formerDecomposedFilePaths collections, so we keep a collection of files which were part of the decomposed model and aren't anymore.
		formerDecomposedFilePaths.removeAll(currentDecomposedFilePaths);
		// If there are files in the former file hierarchy which aren't written, remove them.
		if (formerDecomposedFilePaths.size() > 0) {
			logger.info(String.format("Deleting %d former decomposed files...", formerDecomposedFilePaths.size()));
			for (URI formerFileUri : formerDecomposedFilePaths) {
				try {
					// Remove the file using the Files class i.s.o. the File object, since this throws an exception containing information about the problem.
					Files.delete(Path.of(formerFileUri));
					logger.fine(String.format("Removed former decomposed file '%s'", formerFileUri));
				} catch (IOException ex) {
					logger.warning(String.format("Error while removing former decomposed file '%s': %s", formerFileUri, ex.getMessage()));
				}
			}
		}
		
		// Done
		logger.info("Done.");
	}
	
	/**
	 * Add the former decompose file paths to the filePathsSet collection.
	 * @param fileWithIncludesPath The current file.
	 * @param filePathsSet The collection of file paths found this far.
	 * @param fileCharset The file charset to use.
	 * @throws Exception
	 */
	private void addFormerFilePaths(Path fileWithIncludesPath, HashSet<URI> filePathsSet, Charset fileCharset) throws Exception {
		// Only go further when the file exists.
		if (fileWithIncludesPath.toFile().exists()) {
			// Resolve the path to a real path URI so this can be used as a key of the hashset.
			URI fileUri = fileWithIncludesPath.toAbsolutePath().normalize().toUri();
			// Check whether the file URI is already in the set.
			if (!filePathsSet.contains(fileUri)) {
				// If not, add the file URI to the set.
				filePathsSet.add(fileUri);
				logger.fine(String.format("File exists and wasn't in the set yet: '%s'", fileUri));
				
				// Get the file base path.
				Path basePath = FileUtils.getBasePath(fileWithIncludesPath);
				
				// Read the XML file into a string, if the charset is known from a parent file, use it.
				FileContentAndCharset fcac;
				if (fileCharset == null) {
					fcac = FileUtils.getFileContent(fileUri);
				} else {
					fcac = FileUtils.getFileContent(fileUri, fileCharset);
				}
				
				// Open the file and look for includes
				// TODO: Make this XPath namespace aware so it actually looks for xi:include instead of include in all namespaces
				// Currently this doesn't work, because the namespace declaration is missing in the Model element.
				VTDNav nav = XMLUtils.getVTDNav(fcac, false);
				AutoPilot ap = new AutoPilot(nav);
				// Declare the XInclude namespace.
				//ap.declareXPathNameSpace("xi", "http://www.w3.org/2001/XInclude");
				
				// Search for all xi:include elements.
				ap.selectXPath("//include");
				while ((ap.evalXPath()) != -1) {
					// Obtain the filename of include
					AutoPilot ap_href = new AutoPilot(nav);
					ap_href.selectXPath("@href");
					String includeFileLocation = ap_href.evalXPathToString();
					logger.fine(String.format("Found include '%s'", includeFileLocation));
					
					// Resolve the included file path.
					Path includeFilePath = basePath.resolve(includeFileLocation);
					// Add the file path of the included file (and scan its contents for more includes).
					addFormerFilePaths(includeFilePath, filePathsSet, fcac.getFileCharset());
				}
			}
		}
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
	private VTDNav replaceIdentifiers(VTDNav nv, IdentifierReplacementConfig identifierReplacementConfig) throws Exception {
		logger.info("Replacing identifiers in document...");

		// We are going to replace all Id="o?" and Ref="o?" values, so we need an XmlModifier.
		logger.info(" - Overwriting local ids with global ids...");
		XMLModifier xm;
		try {
			xm = new XMLModifier(nv);
		} catch (Exception e) {
			throw new Exception("Error while initializing XMLModifier");
		}
		
		AutoPilot ap = getAutoPilot(nv);
		
		// Select all elements using the identifier node xpath.
		ap.selectXPath(identifierReplacementConfig.getIdentifierNodeXPath());
		
		// Create a list of local and global ids.
		HashMap<String, String> localToGlobalIds = new HashMap<String, String>();
		while ((ap.evalXPath()) != -1) {
			// Get the current index
			int identifierNodeIndex = nv.getCurrentIndex();
			// If the token is an attribute value, add 1 to the index to get to the attribute value.
			if (nv.getTokenType(identifierNodeIndex) == VTDNav.TOKEN_ATTR_NAME) {
				identifierNodeIndex += 1;
			}
	    	String identifierOriginalValue = nv.toString(identifierNodeIndex);
	    	String identifierReplacementValue = XMLUtils.getXPathText(nv, identifierReplacementConfig.getReplacementValueXPath());

	    	logger.fine(String.format("Found identifier '%s' and replaced with value '%s'", identifierOriginalValue, identifierReplacementValue));
	    	if (!localToGlobalIds.containsKey(identifierOriginalValue)) {
	    		localToGlobalIds.put(identifierOriginalValue, identifierReplacementValue);
	    	}
	    	// If we reach this code, there is a duplicate identifier found, which should never happen.
	    	else {
	    		throw new Exception(String.format("A duplicate identifier was found while replacing identifiers (%s). This should never happen!", identifierOriginalValue));
	    	}
	    	
	    	// Update the value of the identifier node.
	    	xm.updateToken(identifierNodeIndex, identifierReplacementValue);
		}
		
		// If the referencingNodeXPath is present, replace the referencing values using the key collection of the previous step.
		if (identifierReplacementConfig.getReferencingNodeXPath() != null) {
			// Loop through all referencing nodes and replaces their values with the replacement value belonging to the original identifier.
			logger.info(" - Overwriting identifier references with replacement value...");
			// In stead of looping through specific refs, loop through all refs and replace them there.
			try {
				ap.resetXPath();
				ap.selectXPath(identifierReplacementConfig.getReferencingNodeXPath());
			} catch (XPathParseException e) {
				throw new Exception(String.format("Error while replacing referencing node values: %s", e.getMessage()), e);
			}
			
			// Find all references on the local id and replace it with the global id.
			while ((ap.evalXPath()) != -1) {
				
				// Get the current index
				int localObjectRefIndex = nv.getCurrentIndex();
				// If the token is an attribute value, add 1 to the index to get to the attribute value.
				if (nv.getTokenType(nv.getCurrentIndex()) == VTDNav.TOKEN_ATTR_NAME) {
					localObjectRefIndex += 1;
				}
				String referencingOriginalIdentifierValue = nv.toString(localObjectRefIndex);
				// Replace the local id with the global id if it is in the collection.
				if (localToGlobalIds.containsKey(referencingOriginalIdentifierValue)) {
					String referencingIdentiierReplacementValue = localToGlobalIds.get(referencingOriginalIdentifierValue);
			    	logger.fine(String.format("Found reference id '%s' with global id '%s' (index: %d)", referencingOriginalIdentifierValue, referencingIdentiierReplacementValue, localObjectRefIndex));
			    	// Update local reference to the global GUID.
			    	xm.updateToken(localObjectRefIndex, referencingIdentiierReplacementValue);
				}
			}
		}
		
		logger.info("Done replacing identifiers in document.");
		
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
				int currentTokenType = nv.getTokenType(currentNodeIndex);
				logger.fine(String.format(" - Removing node '%d' (offset=%d;length=%d;type=%d)...", currentNodeIndex, nv.getTokenOffset(currentNodeIndex), nv.getTokenLength(currentNodeIndex), currentTokenType));
	        	// If the node is an element, expand the element offset with the leading whitespace, so we also remove whitespace before this node.
	        	if (currentTokenType == VTDNav.TOKEN_STARTING_TAG) {
	        		xm.remove(nv.expandWhiteSpaces(nv.getElementFragment(), VTDNav.WS_LEADING));
	        	}
	        	// If the token is an attribute name, we need to remove the attribute name, the equals sign and its value (we use removeAttribute function for this).
	        	else if (currentTokenType == VTDNav.TOKEN_ATTR_NAME) {
	        		// Store the attribute name index.
	        		int attributeNameIndex = nv.getTokenOffset(currentNodeIndex);
	        		// Remove the attribute with value.
	        		xm.remove();
	        		// Remove the space before the attribute name (if it's there).
	        		if (nv.toString(attributeNameIndex - 1, 1).equals(" ")) {
	        			xm.removeContent(attributeNameIndex - 1, 1);
	        		}
	        	}
	        	// If the token is a processing-instruction name, we need to remove the token before and after as well.
	        	else if (currentTokenType == VTDNav.TOKEN_PI_NAME) {
	        		// If there is an attribute specified on the processing instruction, find the attribute in the processing instruction.
	        		if (piAttributeToRemove != null) {
        				// The processing instruction value is in the token after the name.
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
	        		xm.remove();
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
	 * @param currentTargetFileName
	 * @param targetDirectoryPath
	 * @param currentPartIsRoot
	 * @throws Exception
	 */
	private static Path parseAndWriteDocumentParts(VTDNav nv, Charset fileCharset, String currentObjectName, String currentTargetFileName, Path targetDirectoryPath, int depth, DecomposableElementConfig decomposableElementConfig, HashSet<URI> currentDecomposedFilePaths) throws Exception {
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
		int extractedChildCount = 0;
		
		// Only decompose elements if conditions are configured.
		if (decomposableElementConfig.getElementConditionsAndGroups() != null && decomposableElementConfig.getElementConditionsAndGroups().size() > 0) {
			
			AutoPilot ap = getAutoPilot(nv);
			// Select all elements which conform to the elements conditions as specified in the config.
			ap.selectXPath(String.format("//*[%s]", decomposableElementConfig.getXPathExpression()));
			int minimumNextOffset = 0;
			// Loop through the found elements.
			while ((ap.evalXPath()) != -1) {
				// skip the root element.
				if (nv.getCurrentIndex() == nv.getRootIndex())
					continue;
				
		    	// Get the parent element name, to be used as the folder name.
		    	String parentElementName = XMLUtils.getParentElementName(nv);
		    	String elementName = XMLUtils.getElementName(nv);
				
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
		    	
		    	// Make sure the target file name config is set.
		    	if (decomposableElementConfig.getTargetFileNameConfigs() == null || decomposableElementConfig.getTargetFileNameConfigs().size() == 0)
		    		throw new Exception("The TargetFileName configuration isn't set!");
		    	
		    	// Get the target folder name for the current decomposable element.
		    	// If the parent element name contains a namespace part, remove it.
				String childTargetFolderName = FileUtils.getLegalFileName(XMLUtils.getElementNameWithoutNameSpace(parentElementName));
		    	// Derive the target folder name using the configuration.
		    	String childTargetSubFolderName = deriveFirstValidConfiguredValue(nv, decomposableElementConfig.getTargetFolderNameConfigs());
		    	// If the target sub folder is found using the configuration, we concatenate it with the parent element name folder.
		    	if (childTargetSubFolderName != null && childTargetSubFolderName.length() > 0) {
		    		childTargetFolderName = childTargetFolderName.concat("/").concat(FileUtils.getLegalFileName(childTargetSubFolderName));
		    	}
		    	// If a target sub folder can't be found, we use the parent element name only.
		    	else {
		    		logger.fine(String.format("The target folder name is not found for %s at %d, using parent element name %s.", elementName, elementOffset, parentElementName));
		    	}
		    	
		    	// Derive the target file name for the current decomposable element.
		    	Path targetSubFolderPath = targetDirectoryPath.resolve(childTargetFolderName);
				String childTargetFileName = deriveFirstValidConfiguredValue(nv, decomposableElementConfig.getTargetFileNameConfigs(), targetSubFolderPath, "xml", currentDecomposedFilePaths);
		    	// If the target folder configuration doesn't yield a valid result, throw an exception.
		    	if (childTargetFileName == null || childTargetFileName.length() == 0) {
		    		throw new Exception(String.format("A valid child target file name is not found for element %s at %s", elementName, elementOffset));
		    	}
		    	
				// Go through the configured includes attributes if they exist.
	    		HashMap<String, String> includeAttributesWithValues = new HashMap<String, String>();
				if (decomposableElementConfig.getIncludeAttributeConfigs() != null) {
					// Loop over the sub elements to include to fill the hashmap.
					// We have to do this loop here, and can't do it later in the code, since the pointer is now on the right spot in the model file.
					for (IncludeAttributeConfig includeAttributeConfig : decomposableElementConfig.getIncludeAttributeConfigs()) {
						String subElementText = XMLUtils.getXPathText(nv, includeAttributeConfig.getXPath());
						// Only include the attribute if it contains a value.
						if (subElementText.length() > 0)
							includeAttributesWithValues.put(includeAttributeConfig.getName(), subElementText);
					}
				}
		    	
		    	// Get the contents of the XML Fragment.
		    	String objectXmlPart = nv.toRawString(elementOffset, elementLength);
		    	// Parse the XML Fragment and write it to its own file.
		    	logger.fine(String.format("Target folder name: '%s'; child target file name: '%s'", childTargetFolderName, childTargetFileName));
		    	
				// Remove the xml node which is being referenced.
				xm.removeContent(elementOffset, elementLength);
				
				// Create a VTDNav for navigating the document.
				VTDNav partNv;
				try {
					partNv = XMLUtils.getVTDNav(objectXmlPart, fileCharset, false);
				} catch (Exception e) {
					throw new Exception(String.format("Error while parsing Xml Part: %s", e.getMessage()), e);
				}
				
				// Set the relative path with a parent folder of the current element name.
				Path childTargetDirectory = targetSubFolderPath.resolve(childTargetFileName);
				// Derive the object file name.
				String childObjectFileNameWithExtension = String.format("%s.xml", childTargetFileName);
				Path childFileLocation = parseAndWriteDocumentParts(partNv, fileCharset, childTargetFileName, childObjectFileNameWithExtension, childTargetDirectory, depth + 1, decomposableElementConfig, currentDecomposedFilePaths);
				
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
		}
		
        // Get the root element of the document.
		logger.fine(String.format("%s - Writing file: %s", prefix, currentTargetFileName));
		Path targetFilePath = targetDirectoryPath.resolve(currentTargetFileName);
		// If the current element doesn't have extracted child elements, store the file in the parent folder.
		if (depth != 0 && extractedChildCount == 0) {
			targetFilePath = targetDirectoryPath.getParent().resolve(currentTargetFileName);
		}
		
		// Add the file to the list of decomposed file paths of the current run.
		// We need to normalize the absolute path to get a comparable path without relative bits like ..
		URI targetFileUri = targetFilePath.toAbsolutePath().normalize().toUri();
		if (!currentDecomposedFilePaths.contains(targetFileUri)) {
			currentDecomposedFilePaths.add(targetFileUri);
		}
		// Tried to write a file twice in one run, this should never happen.
		else {
			throw new Exception(String.format("Tried to write a file twice, this should never happen ('%s').", targetFileUri.toString()));
		}
		
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
	
	private static String deriveFirstValidConfiguredValue(VTDNav nv, List<? extends AbstractConfigElementWithXPathAttributeAndCondition> configuredOptions) throws XPathParseException {
		return deriveFirstValidConfiguredValue(nv, configuredOptions, null, null, null);
	}
	
	private static String deriveFirstValidConfiguredValue(VTDNav nv, List<? extends AbstractConfigElementWithXPathAttributeAndCondition> configuredOptions, Path targetDirectoryPath, String targetFileExtension, HashSet<URI> unallowedFilePaths) throws XPathParseException {
		if (configuredOptions != null && configuredOptions.size() > 0) {
	    	AutoPilot sap = new AutoPilot(nv);
	    	for (AbstractConfigElementWithXPathAttributeAndCondition co : configuredOptions) {
	    		logger.fine(String.format("Checking configured option '%s' with condition '%s'", co.getXPath(), co.getCondition()));
	    		// Check whether the condition of the TargetFileName config is met.
	    		if (co.getCondition() != null)
	    			sap.selectXPath(co.getCondition());
	    		if (co.getCondition() == null || sap.evalXPathToBoolean()) {
	    			// If the condition is met, try to get the value.
	    			sap.selectXPath(co.getXPath());
	    			String foundValue = sap.evalXPathToString();
	    			// If the value is found go on.
	    			if (foundValue != null && foundValue.length() > 0) {
	    				// Strip the found value of illegal file characters.
	    				foundValue = FileUtils.getLegalFileName(foundValue);
	    				logger.fine(String.format("Found value: '%s'", foundValue));
	    				// If there are no unallowedValues, return the found value.
	    				if (unallowedFilePaths == null) {
	    					logger.fine("There are no unallowed values, so returning found value.");
	    					return foundValue;
	    				// Otherwise, check whether the value is unallowed, if not return the value.
	    				} else {
	    					// Derive the object file name.
	    					String foundFileName = foundValue;
	    					// If the target file extensions is defined, use it.
	    					if (targetFileExtension != null && targetFileExtension.length() > 0)
	    						foundFileName = String.format("%s.%s", foundValue, targetFileExtension);
	    					
	    					URI targetFilePath = targetDirectoryPath.resolve(foundFileName).toUri().normalize();
	    					logger.fine(String.format("Resolved target file name: '%s'", targetFilePath.toString()));
	    					// If the found file path is valid, return the found value (not the file name!).
	    					if (!unallowedFilePaths.contains(targetFilePath)) {
	    						logger.fine("The resolved target file name doesn't exist yet, so returning value.");
	    						return foundValue;
	    					}
	    				}
	    			}
	    		}
	    	}
		}
    	// If we reach this point, no (allowed) value has been found, so we return null;
    	return null;
	}

}
