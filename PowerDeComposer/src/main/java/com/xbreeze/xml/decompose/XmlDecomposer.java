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
package com.xbreeze.xml.decompose;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.xbreeze.xml.config.AbstractConfigElementWithXPathAttributeAndCondition;
import com.xbreeze.xml.decompose.config.DecomposableElementConfig;
import com.xbreeze.xml.decompose.config.DecomposeConfig;
import com.xbreeze.xml.decompose.config.IdentifierReplacementConfig;
import com.xbreeze.xml.decompose.config.IncludeAttributeConfig;
import com.xbreeze.xml.decompose.config.NodeRemovalConfig;
import com.xbreeze.xml.decompose.config.TargetFolderNameConfig;
import com.xbreeze.xml.utils.FileContentAndCharset;
import com.xbreeze.xml.utils.FileUtils;
import com.xbreeze.xml.utils.XMLUtils;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathParseException;

public class XmlDecomposer {
	private static final Logger logger = Logger.getGlobal();
	
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
		FileContentAndCharset xmlFileContentsAndCharset = FileUtils.getFileContent(xmlFile);
		logger.fine("Start of file contents:");
		logger.fine("--------------------------------------------------");
		logger.fine(xmlFileContentsAndCharset.getFileContents().replaceAll("\n", "[LF]\n").replaceAll("\r", "[CR]"));
		logger.fine("--------------------------------------------------");
		logger.fine("End of file contents:");
		
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
		
		// Construct a Path from the target directory.
		// We don't convert it to a real path (this way the paths are always in full), because this fails if a directory doesn't exist at this point.
		// As long as all uses of the File object use this relative path it works.
		Path targetDirectoryPath = Paths.get(targetDirectory);
		logger.fine(String.format("Target directory path: '%s'", targetDirectoryPath));
		File targetFile = targetDirectoryPath.resolve(xmlFile.getName()).toFile();
		logger.fine(String.format("Target file: '%s'", targetFile));
		
		// Create the TargetFileInfo object for the original file.
		TargetFileInfo targetFileInfo = new TargetFileInfo();
		targetFileInfo.FileNameWithoutExtension = FilenameUtils.getBaseName(xmlFile.getName());
		targetFileInfo.FileExtension = FilenameUtils.getExtension(xmlFile.getName());
		targetFileInfo.FolderPath = targetDirectoryPath;
		targetFileInfo.FilePathWithoutChildren = targetDirectoryPath.resolve(xmlFile.getName());
		logger.fine(String.format("File path without children: '%s'", targetFileInfo.FilePathWithoutChildren));
		targetFileInfo.FilePathWithChildren = targetDirectoryPath.resolve(xmlFile.getName());
		logger.fine(String.format("File path with children: '%s'", targetFileInfo.FilePathWithChildren));
		
		// If configured, perform changes detection here before doing anything else.
		if (decomposeConfig.getChangeDetectionConfig() != null) {
			
			// Check if the target file exists.
			if (targetFile.exists()) {
				String changeDetectionXPath = decomposeConfig.getChangeDetectionConfig().getXPath();
				
				// Get a VTDNav on the former decomposed root file.
				FileContentAndCharset formerFcac = FileUtils.getFileContent(targetFile);
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
				logger.warning(String.format("Change detection is configured, but there is no decomposed file yet (%s).", targetFile.toString()));
			}
		}
		
		// Perform the node removals (It's important this is done before the identifier replacement, otherwise it might lead to duplicate key problems).
		if (decomposeConfig.getNodeRemovalConfigs() != null && decomposeConfig.getNodeRemovalConfigs().size() > 0) {
			nv = removeNodes(nv, decomposeConfig.getNodeRemovalConfigs());
		}
		
		// Replace the identifiers in the XML Document, if specified in the config.
		if (decomposeConfig.getIdentifierReplacementConfigs() != null && decomposeConfig.getIdentifierReplacementConfigs().size() > 0) {
			for (IdentifierReplacementConfig identifierReplacementConfig : decomposeConfig.getIdentifierReplacementConfigs()) {
				nv = replaceIdentifiers(nv, identifierReplacementConfig);
			}
		}
		
		// Transform the ExtendedAttributeText elements to separate XML elements.
		if (decomposeConfig.formalizeExtendedAttributes()) {
			nv = formalizeExtendedAttributesText(nv);
		}
		
		// Get the existing list of files in the decomposed model (if it exists). This is needed to track files which are written and which need to be deleted.
		TreeSet<File> formerDecomposedFiles = GetCaseInsensitiveFileSet();

		logger.info("Collecting previously decomposed files for potential removal...");
		//Based on configuration, decide what strategy to use for removing old files.
		switch(decomposeConfig.getFileRemovalStrategy()){
			case "files":
				logger.info("- Using the files strategy...");
				formerDecomposedFiles.addAll(getFormerFilesTree(targetFile.getParent()));
				break;
			case "includes":
			default:
				logger.info("- Using the includes strategy...");
				addFormerFilePaths(targetFile, formerDecomposedFiles, null);
				break;
		}
		logger.info("Done collecting previously decomposed files for potential removal...");

		// Parse and write document parts, if specified in the config.
		// Keep track of the decomposed files, so we can:
		// - Detect whether a file is written multiple times in one run
		// - Detect which files were in the previous decomposed file, but aren't anymore.
		TreeSet<File> currentDecomposedFiles = GetCaseInsensitiveFileSet();
		if (decomposeConfig.getDecomposableElementConfig() != null) {
			logger.info("Parsing and writing document parts...");
			parseAndWriteDocumentParts(nv, xmlFileContentsAndCharset.getFileCharset(), targetFileInfo, 0, decomposeConfig.getDecomposableElementConfig(), currentDecomposedFiles);
			logger.info("Done parsing and writing document parts.");
		}

		// Remove all file paths which are written in the current run in the formerDecomposedFilePaths collections, so we keep a collection of files which were part of the decomposed model and aren't anymore.
		formerDecomposedFiles.removeAll(currentDecomposedFiles);
		// If there are files in the former file hierarchy which aren't written, remove them.
		if (formerDecomposedFiles.size() > 0) {
			logger.info(String.format("Deleting %d former decomposed files...", formerDecomposedFiles.size()));
			for (File formerFile : formerDecomposedFiles) {
				
				// Before we relied on File objects we used URI's. At that point we removed the file using the Files class i.s.o. the File object, since this throws an exception containing information about the problem.
				//Files.delete(formerFile.toPath());
				
				// Check whether we can change the file, and if so delete it.
				if (formerFile.canWrite()) {
					formerFile.delete();
					logger.fine(String.format("Removed former decomposed file '%s'", formerFile));
				}
				// If we can't change the file, report a warning.
				else {
					logger.warning(String.format("Can't remove forder decomposed file: %s", formerFile));
				}
			}
		}
		
		// Done
		logger.info("Done.");
	}
	
	/**
	 * Static function to get a TreeSet containing File objects which are case insensitively compared.
	 * @return The case-insensitive TreeSet for File objects.
	 */
	private static TreeSet<File> GetCaseInsensitiveFileSet() {
		return new TreeSet<File>(new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				// First try to use the canonical path to compare.
				try {
					//logger.fine(String.format("Trying to compare files using canonical path. f1: '%s'; f2: '%s'", f1.toString(), f2.toString()));
					return f1.getCanonicalPath().compareToIgnoreCase(f2.getCanonicalPath());
				}
				// If this doesn't work, use the string version of the file (which can be a relative path.
				// For PowerDeComposer this won't be an issue, since all paths are relative to the same base path.
				catch (IOException e) {
					logger.warning(String.format("Can't resolve file to canonical path, comparing with relative path. Error: %s", e.getMessage()));
					e.printStackTrace();
					return f1.toString().compareToIgnoreCase(f2.toString());
				}
				
			}
		});
	}

	 /**
	 * Return a TreeSet containing all filepaths from the base decompose folder.
	 * It collects the toFile value and filters out all directories.
	 * @param fileWithIncludesPath The base decompose folder.
	 */
	private TreeSet<File> getFormerFilesTree(String baseFolder) throws Exception {
		TreeSet<File> filePathsSet = Files.walk(Paths.get(baseFolder))
									.filter(file -> !Files.isDirectory(file))
									.map(Path::toFile)
									.collect(Collectors.toCollection(TreeSet::new));
		return filePathsSet;
	}

	/**
	 * Add the former decompose file paths to the filePathsSet collection.
	 * @param fileWithIncludesPath The current file.
	 * @param filesSet The collection of file paths found this far.
	 * @param fileCharset The file charset to use.
	 * @throws Exception
	 */
	private void addFormerFilePaths(File fileWithIncludes, TreeSet<File> filesSet, Charset fileCharset) throws Exception {
		// Only go further when the file exists.
		if (fileWithIncludes.exists()) {
			// Check whether the file is already in the set.
			if (!filesSet.contains(fileWithIncludes)) {
				// If not, add the file to the set.
				filesSet.add(fileWithIncludes);
				logger.fine(String.format("File exists and wasn't in the set yet: '%s'", fileWithIncludes.toString()));
				
				// Get the file base path.
				Path basePath = FileUtils.getBasePath(fileWithIncludes);
				
				// Read the XML file into a string, if the charset is known from a parent file, use it.
				FileContentAndCharset fcac;
				if (fileCharset == null) {
					fcac = FileUtils.getFileContent(fileWithIncludes);
				} else {
					fcac = FileUtils.getFileContent(fileWithIncludes, fileCharset);
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
					File includedFile = basePath.resolve(includeFileLocation).toFile();
					// Add the file path of the included file (and scan its contents for more includes).
					addFormerFilePaths(includedFile, filesSet, fcac.getFileCharset());
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

		//Create a list of global Id's to verify for duplicates
		//This needs to be added for performance reasons, since containsValue is slow in a HashMap structure.
		HashSet<String> globalIds = new HashSet<String>();

		// Loop thru the set of identifier nodes.
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

			// If there is a duplicate key, throw an exception
			if (localToGlobalIds.containsKey(identifierOriginalValue))
				throw new Exception(String.format("A duplicate identifier was found while replacing identifiers (%s). This should never happen!", identifierOriginalValue));
			// Throw an exception if a duplicate replacement value has been found.
			else if (globalIds.contains(identifierReplacementValue))
			 	throw new Exception(String.format("A duplicate replacement-identifier was found while replacing identifiers (%s). This should never happen!", identifierReplacementValue));
			// Otherwise store the key and value in the hashmap data structure.
			else {
				//Add the key-value combination to the map.
				localToGlobalIds.put(identifierOriginalValue, identifierReplacementValue);
				//Add the identifier replacement value to the HashSet for duplicate check.
				globalIds.add(identifierReplacementValue);
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
	
	private VTDNav formalizeExtendedAttributesText(VTDNav nv) throws Exception {
		logger.info("Formalizing extended attributes in document...");
		
		// We are going to replace all ExtnededAttributeText elements with it's formal representation, so we need an XmlModifier.
		XMLModifier xm;
		try {
			xm = new XMLModifier(nv);
		} catch (Exception e) {
			throw new Exception("Error while initializing XMLModifier");
		}
		
		AutoPilot ap = getAutoPilot(nv);
		// Select all a:ExtendedAttributesText elements.
		ap.selectXPath("//ExtendedAttributesText");
		
		// Loop thru the set of identifier nodes.
		while ((ap.evalXPath()) != -1) {
			int extAttrsTextNodeIndex = nv.getCurrentIndex();
			// The found token should be a starting tag.
			if (nv.getTokenType(extAttrsTextNodeIndex) == VTDNav.TOKEN_STARTING_TAG) {
				// Get the extended attribute text.
				// Replace LF with CRLF, since VTD-Nav removes the carriage returns in the file (and PowerDesigner always has CRLF).
				String extendedAttributesText = nv.toString(nv.getText()).replace("\n", "\r\n");
				logger.fine(String.format("Found extended attributes text: %s", extendedAttributesText.replaceAll("\n", "[LF]\n").replaceAll("\r", "[CR]")));
				
				// The extended attribute text needs to be parsed so we can create the new XML elements.
				// The format of the text is as follows:
				// {<extension-guid>},<extension-name>,<length>=<extension-extended-attribute[s]>\n
				// The format of <extension-extended-attribute[s]> is as follows:
				// {<extended-attr-guid>},<ext-attr-name>,<length>=<ext-attr-value>\n
				// Both patterns are almost the same, so we can use the same regex recursively.
				String extAttrRegex = "\\{([0-9A-F-]{36})\\},([a-zA-Z -_]+),([0-9]+)=";
				// So we start with a regex pattern for the extension level format.
				Pattern extensionExtAttrsPattern = Pattern.compile(extAttrRegex);
				Matcher extExtAttrsMatcher = extensionExtAttrsPattern.matcher(extendedAttributesText);
				StringBuffer extExtAttrsXml = new StringBuffer();
				extExtAttrsXml.append("\r\n<ExtendedAttributes>");
				int currentExtensionExtAttrEnd = -1;
				while (extExtAttrsMatcher.find()) {
					String guid = extExtAttrsMatcher.group(1);
					String name = extExtAttrsMatcher.group(2);
					int extAttrLength = Integer.parseInt(extExtAttrsMatcher.group(3));
					int extAttrStart = extExtAttrsMatcher.end();
					int extAttrsEnd = extAttrStart + extAttrLength;
					
					if (extAttrsEnd > extendedAttributesText.length())
						throw new Exception("Error while formalizing extended attributes text: The extended attribute length is longer then the contents of the string. This should never happen!");
					
					String extExtAttrContent = extendedAttributesText.substring(extAttrStart, extAttrsEnd);
					
					// If we are inside a extension section, so currentExtensionExtAttrEnd != -1. And we find a match where the the end index is after the end of extension section we have a problem.
					// The end of an extension section should always be equal or after any child sections.
					if (currentExtensionExtAttrEnd != -1 && extAttrStart < currentExtensionExtAttrEnd && extAttrsEnd > currentExtensionExtAttrEnd)
						throw new Exception("Error while formalizing extended attributes text, the current extension end index is between the start and end of the new extended attribute match. This should never happen!");
					
					// If we reached the end of a previous extension list, we update the end to -1 so this match is handled as a OriginatingExtension.
					if (currentExtensionExtAttrEnd != -1 && extAttrStart >= currentExtensionExtAttrEnd) {
						logger.fine("The new match is outside of the extension section, so resetting end index.");
						extExtAttrsXml.append("\r\n</OriginatingExtension>");
						currentExtensionExtAttrEnd = -1;
					}
					
					// If we outside of a extension attribute list, a new extension part is started.
					if (currentExtensionExtAttrEnd == -1) {
						logger.fine(String.format("Found extention [ObjectID=%s;Name=%s;Length=%d;Content='%s'", guid, name, extAttrLength, extExtAttrContent));
						extExtAttrsXml.append(String.format("\r\n<OriginatingExtension ObjectID=\"%s\" Name=\"%s\">", guid, name));
						// Now we have added the element for the OriginatingExtension, we need to loop over the matches within the content part of the extension extended attributes.
						// For each extended attribute we find, we add a separate XML element.
						// Update the end of the extension extended attribute list to the current one. This way in the next loop we know we are handling an extended attribute part.
						currentExtensionExtAttrEnd = extAttrsEnd;						
					}
					// We are inside a extension section, so we treat the match as an extended attribute within the extension.
					else {
						logger.fine(String.format("Found extended attributes [ObjectID=%s;Name=%s;Length=%d;Value='%s'", guid, name, extAttrLength, extExtAttrContent));
						extExtAttrsXml.append(String.format("\r\n<ExtendedAttribute ObjectID=\"%s\" Name=\"%s\">%s</ExtendedAttribute>", guid, name, extExtAttrContent));
						// Update the region to scan to after the current extended attribute.
						extExtAttrsMatcher.region(extAttrsEnd, extExtAttrsMatcher.regionEnd());
					}
				}
				// If we exited the while loop and the end index is not -1, we need to add the ending tag of the extension element.
				if (currentExtensionExtAttrEnd != -1) {
					extExtAttrsXml.append("\r\n</OriginatingExtension>");
				}
				extExtAttrsXml.append("\r\n</ExtendedAttributes>");
				xm.insertAfterElement(extExtAttrsXml.toString());
				// Now we added the replacement of the textual extended attributes, we can remove the ExtendedAttributesText element.
				xm.remove(nv.expandWhiteSpaces(nv.getElementFragment(), VTDNav.WS_LEADING));
			}
		}
		
		logger.info("Done formalizing extended attributes in document.");
		
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
				logger.fine(String.format(" - Removing node with index %d (offset=%d;length=%d;type=%d)...", currentNodeIndex, nv.getTokenOffset(currentNodeIndex), nv.getTokenLength(currentNodeIndex), currentTokenType));
	        	// If the node is an element, expand the element offset with the leading whitespace, so we also remove whitespace before this node.
	        	if (currentTokenType == VTDNav.TOKEN_STARTING_TAG) {
	        		xm.remove(nv.expandWhiteSpaces(nv.getElementFragment(), VTDNav.WS_LEADING));
	        		// Update removedNodes to true.
	        		removedNodes = true;
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
	        		// Update removedNodes to true.
	        		removedNodes = true;
	        	}
	        	// If the token is a processing-instruction name, we need to remove the token before and after as well.
	        	else if (currentTokenType == VTDNav.TOKEN_PI_NAME) {
	        		// If there is an attribute specified on the processing instruction, find the attribute in the processing instruction.
	        		if (piAttributeToRemove != null) {
        				// The processing instruction value is in the token after the name (prefix with space so the first attribute can also be found using the attribute pattern).
	        			String piValue = nv.toString(currentNodeIndex + 1);
	        			// Get the offset minus 1 (minus 1 because of the space we added in the line above here).
	        			//int piValueOffset = nv.getTokenOffset(currentNodeIndex + 1) - 1;
	        			logger.fine(String.format(" - Processing instruction value: '%s'", piValue));
	        			// Match anything between double quotes after the attribute name and equals sign. This will also include newlines.
	        			Pattern piAttributePattern = Pattern.compile(String.format(" %s=\\\"([^\"])*\\\"", piAttributeToRemove));
	        			Matcher piAttributeMatcher = piAttributePattern.matcher(" " + piValue);
	        			
	        			// Perform the replace if the regex matches at least once.
	        			if (piAttributeMatcher.find()) {
		        			// Replace all matches with empty string.
		        			String newPiValue = piAttributeMatcher.replaceAll("");
		        			// If the new pi value starts with a space, remove it (because we added a space when creating the Matcher.
		        			if (newPiValue.length() > 0 && newPiValue.substring(0, 1).equals(" ")) {
		        				newPiValue = newPiValue.substring(1);
		        			}
		        			
		        			// Insert the new PI value and remove the old one.
	        				xm.insertBytesAt(nv.getTokenOffset(currentNodeIndex + 1), newPiValue.getBytes());
	        				xm.removeToken(currentNodeIndex + 1);
	    	        		// Update removedNodes to true.
	    	        		removedNodes = true;
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
		        		// Update removedNodes to true.
		        		removedNodes = true;
	        		}
	        	}
	        	// If the node is not an element, remove the whole token.
	        	else {
	        		xm.remove();
	        		// Update removedNodes to true.
	        		removedNodes = true;
	        	}
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
	private Path parseAndWriteDocumentParts(VTDNav nv, Charset fileCharset, TargetFileInfo currentFileInfo, int depth, DecomposableElementConfig decomposableElementConfig, TreeSet<File> currentDecomposedFiles) throws Exception {
		// Create the prefix string based on the depth.
		String prefix = String.join("", Collections.nCopies(depth, STR_PREFIX_SPACER));
		logger.fine(String.format("%s> %s", prefix, currentFileInfo.FolderPath.toString()));
		
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
		    	Path childTargetSubFolderPath = deriveTargetFolderPath(nv, decomposableElementConfig.getTargetFolderNameConfigs(), currentFileInfo.FolderPath, childTargetFolderName);
		    	
		    	// Derive the target file name for the current decomposable element.
				TargetFileInfo childFileInfo = deriveTargetFileAndFolderPath(nv, decomposableElementConfig.getTargetFileNameConfigs(), childTargetSubFolderPath, "xml", currentDecomposedFiles);
		    	// If the target folder configuration doesn't yield a valid result, throw an exception.
		    	if (childFileInfo == null) {
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
		    	byte[] xmlFragmentBytes = nv.getXML().getBytes(elementOffset, elementLength);
		    	// Parse the XML Fragment and write it to its own file.
		    	//logger.fine(String.format("Target folder name: '%s'; child target file name: '%s'", childTargetFolderName, childTargetFileName));
		    	
				// Remove the xml fragment, which will be written in a separate file.
				xm.removeContent(elementOffset, elementLength);
				
				// Create a VTDNav for navigating the document.
				VTDNav partNv;
				try {
					partNv = XMLUtils.getVTDNav(xmlFragmentBytes, false);
				} catch (Exception e) {
					throw new Exception(String.format("Error while parsing Xml Part: %s", e.getMessage()), e);
				}
				
				// Set the relative path with a parent folder of the current element name.
				//Path childTargetDirectory = targetSubFolderPath.resolve(childTargetFileName);
				// Derive the object file name.
				//String childObjectFileNameWithExtension = String.format("%s.xml", childTargetFileName);
				Path childFileLocation = parseAndWriteDocumentParts(partNv, fileCharset, childFileInfo, depth + 1, decomposableElementConfig, currentDecomposedFiles);
				
				// Insert the include tag for the found object.
				String actualRelativePath = currentFileInfo.FolderPath.relativize(childFileLocation).toString();
				// If the file system separator is not a slash, replace the actualRelativePath file system separator with slash.
				if (!currentFileInfo.FolderPath.getFileSystem().getSeparator().equals("/")) {
					actualRelativePath = actualRelativePath.replace(currentFileInfo.FolderPath.getFileSystem().getSeparator(), "/");
				}
				// Construct the include tag contents.
				StringBuffer includeElementStringBuffer = new StringBuffer();
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
		
        // Get the current file path from the currentFileInfo object and whether children where decomposed.
		Path targetFilePath = currentFileInfo.FilePathWithChildren;
		// If the current element doesn't have extracted child elements, store the file in the parent folder.
		if (depth != 0 && extractedChildCount == 0) {
			targetFilePath = currentFileInfo.FilePathWithoutChildren;
		}
		
		// Add the file to the list of decomposed file paths of the current run.
		// We need to normalize the absolute path to get a comparable path without relative bits like '..'.
		File targetFile = targetFilePath.toFile();
		if (!currentDecomposedFiles.contains(targetFile)) {
			currentDecomposedFiles.add(targetFile);
		}
		// Tried to write a file twice in one run, this should never happen.
		else {
			throw new Exception(String.format("Tried to write a file twice, this should never happen ('%s').", targetFile.toString()));
		}
		
		// Get the target folder of the target file.
		File targetFolder = targetFile.getParentFile();
		// Create the target folder(s) if they don't exist.
		if (!targetFolder.exists()) {
			if (!targetFolder.mkdirs()) {
				throw new Exception(String.format("Error while creating parent directories for '%s'", targetFile.toString()));
			}
		}
		// Write the target Xml file.
		logger.fine(String.format("%s - Writing file: %s", prefix, targetFile.toString()));
		// Write the XML file into a array output stream.
		xm.output(targetFilePath.toString());
		//logger.fine(String.format("%s< %s", prefix, targetDirectoryPath));
		
		return targetFilePath;
	}
	
	private static Path deriveTargetFolderPath(VTDNav nv, List<? extends AbstractConfigElementWithXPathAttributeAndCondition> configuredOptions, Path targetDirectoryPath, String parentElementFolderName) throws XPathParseException {
		if (configuredOptions != null && configuredOptions.size() > 0) {
	    	AutoPilot sap = new AutoPilot(nv);
	    	for (AbstractConfigElementWithXPathAttributeAndCondition co : configuredOptions) {
	    		logger.fine(String.format("Checking configured option '%s' with condition '%s'", co.getXPath(), co.getCondition()));
	    		// Check whether the condition of the TargetFileName or TargetFolderName config is met.
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
	    				Path baseTargetDirectory = targetDirectoryPath;
	    				// If the configuration is a TargetFolderNameConfig and the setting override parent is set to false, then we include the parent element as folder.
	    				if (co instanceof TargetFolderNameConfig && ((TargetFolderNameConfig)co).getOverrideParent().equals(Boolean.FALSE)) {
	    					baseTargetDirectory = baseTargetDirectory.resolve(parentElementFolderName);
	    				}
	    				// Resolve the found value as folder on the base target directory.
						return baseTargetDirectory.resolve(foundValue);
	    			} else {
	    				logger.fine("No value found for option.");
	    			}
	    		} else {
	    			logger.fine("Option skipped due to condition.");
	    		}
	    	}
		}
		// If a target sub folder can't be found, we use the parent element name only.
		logger.fine(String.format("The target folder name is not found, using parent element name %s.", parentElementFolderName));
		return targetDirectoryPath.resolve(parentElementFolderName);
	}
	
	private TargetFileInfo deriveTargetFileAndFolderPath(VTDNav nv, List<? extends AbstractConfigElementWithXPathAttributeAndCondition> configuredOptions, Path targetDirectoryPath, String targetFileExtension, TreeSet<File> unallowedFiles) throws XPathParseException {
		if (configuredOptions != null && configuredOptions.size() > 0) {
	    	AutoPilot sap = new AutoPilot(nv);
	    	for (AbstractConfigElementWithXPathAttributeAndCondition co : configuredOptions) {
	    		logger.fine(String.format("Checking configured option '%s' with condition '%s'", co.getXPath(), co.getCondition()));
	    		// Check whether the condition of the TargetFileName or TargetFolderName config is met.
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
	    				
    					// Create the object type to return.
    					TargetFileInfo fileAndFolderPath = new TargetFileInfo();
    					fileAndFolderPath.FileNameWithoutExtension = foundValue;
    					fileAndFolderPath.FileExtension = targetFileExtension;
    					fileAndFolderPath.FolderPath = targetDirectoryPath.resolve(fileAndFolderPath.FileNameWithoutExtension);
    					// If the current element is decompose without children, the file will be as follows.
						fileAndFolderPath.FilePathWithoutChildren = targetDirectoryPath.resolve(fileAndFolderPath.getTargetFileName());
						logger.fine(String.format("Resolved target file name: '%s'", fileAndFolderPath.FilePathWithoutChildren.toString()));
    					// If the current element is decomposed with children (so the current element has children which are decomposed as well) the file path will be as follows.
    					// The difference between for former is that when the current element has children the element file is written into it's own subfolder.
						fileAndFolderPath.FilePathWithChildren = fileAndFolderPath.FolderPath.resolve(fileAndFolderPath.getTargetFileName());
						logger.fine(String.format("Resolved target file name with children: '%s'", fileAndFolderPath.FilePathWithChildren.toString()));
	    				
	    				// If there are no unallowedValues, return the found value.
	    				// unallowedValues is always empty for TargetFolderName configurations.
	    				if (unallowedFiles == null) {
	    					logger.fine("There are no unallowed values, so returning found value.");
	    					return fileAndFolderPath;
	    				// Otherwise, check whether the value is unallowed, if not return the value.
	    				} else {
	    					// If the found file path is valid, return the found value (not the file name!).
	    					if (!unallowedFiles.contains(fileAndFolderPath.FilePathWithoutChildren.toFile()) && !unallowedFiles.contains(fileAndFolderPath.FilePathWithChildren.toFile())) {
	    						logger.fine("The resolved target file name doesn't exist yet, so returning value.");
	    						return fileAndFolderPath;
	    					} else {
	    						logger.fine("The resolved target file name already exists.");
	    					}
	    				}
	    			} else {
	    				logger.fine("No value found for option.");
	    			}
	    		}
	    		else {
	    			logger.fine("Option skipped due to condition.");
	    		}
	    	}
		}
    	// If we reach this point, no (allowed) value has been found, so we return null;
    	return null;
	}
	
	private class TargetFileInfo {
		public Path FolderPath;
		public Path FilePathWithoutChildren;
		public Path FilePathWithChildren;
		
		public String FileNameWithoutExtension;
		public String FileExtension;
		
		public String getTargetFileName() {
			// If the target file extensions is defined, use it.
			if (this.FileExtension != null && this.FileExtension.length() > 0)
				return String.format("%s.%s", this.FileNameWithoutExtension, this.FileExtension);
			return null;
		}
	}

}
