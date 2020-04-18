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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.xbreeze.xml.utils.FileUtils;
import com.xbreeze.xml.utils.XMLUtils;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDNav;
import com.ximpleware.XMLModifier;
import com.ximpleware.XPathParseException;

public class XmlDecomposer {
	private static final Logger logger = Logger.getLogger("");
	
	private static final String STR_PREFIX_SPACER = "  ";
	
	public XmlDecomposer(String xmlFilePath, String targetDirectory) throws Exception {
		decomposeXml(xmlFilePath, targetDirectory);
	}
	
	private void decomposeXml(String xmlFilePath, String targetDirectory) throws Exception {
		
		logger.info(String.format("Starting Xml Decomposer for '%s'", xmlFilePath));
		
		File xmlFile = new File(xmlFilePath);
		Path targetDirectoryPath = Paths.get(targetDirectory);
		
		// Check whether the XML file to decompose exists.
		if (!xmlFile.exists())
			throw new Exception(String.format("The specified xml file doesn't exist '%s'.", xmlFilePath));
		
		// Read the xml file into a string.
		logger.fine("Getting file contents...");
		String xmlFileContents = FileUtils.getFileContent(xmlFile.toURI());
		
		// Create a VTDNav for navigating the document.
		VTDNav nv;
		try {
			logger.fine("Creating VTDNav on file contents...");
			nv = XMLUtils.getVTDNav(xmlFileContents);
		} catch (Exception e) {
			throw new Exception(String.format("Error while parsing Xml document: %s", e.getMessage()), e);
		}
		
		// Prepare the Xml Document.
		VTDNav preparedNv = prepareDocument(nv);
		//VTDNav preparedNv = nv;
		
		// Parse and write document parts.
		logger.info("Parsing and writing document parts...");
		parseAndWriteDocumentParts(preparedNv, null, xmlFile.getName(), targetDirectoryPath, 0);
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
		
		AutoPilot ap = new AutoPilot(nv);
		// Select all elements which have an Id and an ObjectID.
		ap.selectXPath("//*[./@Id and ./ObjectID]");
		
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
	 * Recursively parse the docPartXml to created xi:include reference and write the resulting Xml document to a file.
	 * @param docPartXml
	 * @param targetFileName
	 * @param targetDirectoryPath
	 * @param currentPartIsRoot
	 * @throws Exception
	 */
	private static Path parseAndWriteDocumentParts(VTDNav nv, String currentObjectName, String targetFileName, Path targetDirectoryPath, int depth) throws Exception {
		
		String prefix = String.join("", Collections.nCopies(depth, STR_PREFIX_SPACER));
		logger.fine(String.format("%s> %s", prefix, targetDirectoryPath));
		
		// We are going to remove all nodes which are included, so we need a XmlModifier.
		XMLModifier xm;
		try {
			xm = new XMLModifier(nv);
		} catch (Exception e) {
			throw new Exception("Error while initializing XMLModifier");
		}
		
		// Create a list of collections which we want to extract.
		// c:DBMS, c:ExtendedModelDefinitions, c:PhysicalDiagrams, c:DataSources, c:Packages, c:PhysicalDiagrams, c:Mappings, c:Tables, c:References, c:Reports, c:SourceModels, c:Users, c:TargetModels
		List<String> collectionNodesToExtract = Arrays.asList(new String[] {"c:DBMS", "c:ExtendedModelDefinitions", "c:PhysicalDiagrams", "c:DataSources", "c:Packages", "c:Mappings", "c:Tables", "c:References", "c:Reports", "c:SourceModels", "c:Users", "c:TargetModels", "c:Entities", "c:Relationships", "c:LogicalDiagrams"});
		
		AutoPilot ap = new AutoPilot(nv);
		// Select all elements which have an ObjectID.
		ap.selectXPath("//*[./ObjectID and ./Name]");
		int minimumNextOffset = 0;
		int extractedChildCount = 0;
		// Loop through the found elements.
		while ((ap.evalXPath()) != -1) {
			// skip the root element.
			if (nv.getCurrentIndex() == nv.getRootIndex())
				continue;
			
	    	// Get the parent element name, to be used as the folder name.
	    	String parentElementName = XMLUtils.getParentElementName(nv);
	    	
	    	// If the parent element is not specified to be extracted, skip this one.
	    	if (!collectionNodesToExtract.contains(parentElementName))
	    		continue;
			
	    	// Get the current element name without namespaces.
			String elementName = XMLUtils.getElementNameWithoutNameSpace(nv.toString(nv.getCurrentIndex()));
			
	    	// Get the element offset and length (including whitespaces).
	    	long elementOffsetAndLength = nv.getElementFragment();
	    	int elementOffset = (int)elementOffsetAndLength;
	    	int elementLength = (int)(elementOffsetAndLength>>32);
	    	
	    	// If the current offset is not after the end of the previous fragment, skip this one (it is part of a section passed into the recursive call).
	    	if (elementOffset < minimumNextOffset)
    			continue;
	    	
	    	// Make sure the next index found by the AutoPilot is after the current fragment (used in the previous if condition).
	    	minimumNextOffset = elementOffset + elementLength;
	    	
	    	// Get the object id and name.
	    	String objectId = XMLUtils.getSubElementText(nv, "ObjectID");
	    	String name = XMLUtils.getSubElementText(nv, "Name");
	    	
	    	if (parentElementName == null)
	    		logger.info(String.format("The parent element name is not found for ObjectID: %s.", objectId));
			
	    	//logger.fine(String.format("Found element with ObjectID: %s; Element: %s; Name: %s; ParentElementName: %s (start=%d; length=%d)", objectId, elementName, name, parentElementName, elementOffset, elementLength));
	    	
	    	// Create a separate file for the element (replace the semi-colon of the namespace to be able to create the folders).
	    	//String objectName = String.format("%s_%s", elementName.replace(':', '_'), objectId);
	    	// Set the object name to the object id.
	    	String childObjectName = objectId;
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
				partNv = XMLUtils.getVTDNav(objectXmlPart);
			} catch (Exception e) {
				throw new Exception(String.format("Error while parsing Xml Part: %s", e.getMessage()), e);
			}
			
			// Set the relative path with a parent folder of the current element name.
			String relativePath = String.format("./%s/%s/", parentElementFolder, childObjectName);
			//Path childTargetDirectory = targetDirectoryPath.resolve(parentElementFolder).resolve(objectName);
			Path childTargetDirectory = targetDirectoryPath.resolve(relativePath);
			// Derive the object file name.
			String childObjectFileName = String.format("%s.xml", childObjectName);
			Path childFileLocation = parseAndWriteDocumentParts(partNv, childObjectName, childObjectFileName, childTargetDirectory, depth + 1);
			
			// Insert the include tag for the found object.
			String actualRelativePath = targetDirectoryPath.relativize(childFileLocation).toString();
	    	xm.insertBeforeElement(String.format("<xi:include href=\"%s\" type=\"%s\" name=\"%s\" />", actualRelativePath, elementName, XMLUtils.excapeXMLChars(name)));
			
			// Increate the extracted child count.
			extractedChildCount++;
		}
		
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
