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
package com.xbreeze.xml.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@XmlRootElement(name = "PowerDeComposerConfig")
@XmlAccessorType(XmlAccessType.NONE)
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
public class PowerDeComposerConfig {
	// The logger for this class.
	private static final Logger logger = Logger.getLogger(PowerDeComposerConfig.class.getName());
	
	// The namespace URI.
	public static final String NAMESPACE_URI = "https://x-breeze.com/PowerDeComposer/config";

	/**
	 * @see #getElementsToExtract()
	 */
	private List<String> _elementsToExtract;
	
	/**
	 * @see #getTargetFileNameSubElement()
	 */
	private String _targetFileNameSubElement;
	
	/**
	 * @see #getIncludeSubElements()
	 */
	private List<String> _includeSubElements;

	/**
	 * List of document nodes to extract (XPath expression per node).
	 * The default list of elements to extract are:
	 *  - c:DBMS, c:ExtendedModelDefinitions, c:PhysicalDiagrams, c:DataSources, c:Packages, c:Mappings, c:Tables, c:References, c:Reports, c:SourceModels, c:Users, c:TargetModels, c:Entities, c:Relationships, c:LogicalDiagrams
	 * @return the ElementsToExtract
	 */
	@XmlElement(name = "Element")
	@XmlElementWrapper(name = "ElementsToExtract")
	public List<String> getElementsToExtract() {
		return this._elementsToExtract;
	}
	
	/**
	 * The sub element contents to use as the filename (without extension) for the extracted elements.
	 * The default value is ObjectID (so it will use the ObjectID of any extracted element as it's file name).
	 * Possible options are: ObjectID, Code & Name.
	 * @return the TargetFileNameSubElement
	 */
	@XmlElement(name = "TargetFileNameSubElement")
	public String getTargetFileNameSubElement() {
		return this._targetFileNameSubElement;
	}
	
	/**
	 * List of sub elements to include as an attribute on the include instruction.
	 * The default value is Name.
	 * Possible options are: ObjectID, Code & Name.
	 * @return The IncludeSubElements
	 */
	@XmlElement(name = "SubElement")
	@XmlElementWrapper(name = "IncludeSubElements")
	public List<String> getIncludeSubElements() {
		return this._includeSubElements;
	}
	
	/**
	 * Setter for ElementsToExtract.
	 * @param elementsToExtract
	 */
	public void setElementsToExtract(List<String> elementsToExtract) {
		this._elementsToExtract = elementsToExtract;
	}

	/**
	 * Setter for TargetFileNameSubElement.
	 * @param targetFileNameSubElement
	 */
	public void setTargetFileNameSubElement(String targetFileNameSubElement) {
		this._targetFileNameSubElement = targetFileNameSubElement;
	}

	/**
	 * Setter for IncludeSubElements.
	 * @param includeSubElements
	 */
	public void setIncludeSubElements(List<String> includeSubElements) {
		this._includeSubElements = includeSubElements;
	}
	
	/**
	 * Default constructor.
	 */
	public PowerDeComposerConfig() { }
	
	/**
	 * Create the default config object for user which execute PowerDeComposer without a config file.
	 * @throws ConfigException 
	 */
	public static PowerDeComposerConfig GetDefaultConfig() throws ConfigException {
		String defaultConfigFile = String.format("Default%s.xml", PowerDeComposerConfig.class.getSimpleName());
		// Get an stream on the default config file.
		InputStream defaultConfigStream = PowerDeComposerConfig.class.getResourceAsStream(defaultConfigFile);
		// Create the config object based on the default config file.
		PowerDeComposerConfig config = PowerDeComposerConfig.fromInputSource(new InputSource(defaultConfigStream));
		// Return the config.
		return config;
	}

	/**
	 * Unmarshal a app config from a String.
	 * @param AppConfigFileContent The String object to unmarshal.
	 * @return The unmarshelled XGenAppConfig object.
	 * @throws ConfigException
	 */
	public static PowerDeComposerConfig fromString(String pdcConfigFileContent) throws ConfigException {
		return fromInputSource(new InputSource(new StringReader(pdcConfigFileContent)));
	}
	
	/**
	 * Unmarshal a file into a PowerDeComposerConfig object.
	 * @param configFileUri The file to unmarshal.
	 * @return The unmarshalled PowerDeComposerConfig object.
	 * @throws ConfigException 
	 */
	public static PowerDeComposerConfig fromFile(URI pdcConfigFileUri) throws ConfigException {
		logger.fine(String.format("Creating PowerDeComposerConfigFile object from '%s'", pdcConfigFileUri));
		File pdcConfigFile = new File(pdcConfigFileUri);
		PowerDeComposerConfig pdcConfig;
		try {
			pdcConfig = fromInputSource(new InputSource(new FileReader(pdcConfigFile)));
		} catch (ConfigException e) {
			// Catch the config exception here to add the filename in the exception text.
			throw new ConfigException(String.format("%s (%s)", e.getMessage(), pdcConfigFileUri.toString()), e.getCause());
		} catch (FileNotFoundException e) {
			throw new ConfigException(String.format("Couldn't find the config file (%s)", pdcConfigFileUri.toString()), e);
		}

		return pdcConfig;
	}
	
	/**
	 * Create a PowerDeComposerConfig object using a InputSource.
	 * @param inputSource The InputSource.
	 * @return The PowerDeComposerConfig object.
	 * @throws ConfigException
	 */
	private static PowerDeComposerConfig fromInputSource(InputSource inputSource) throws ConfigException {
		PowerDeComposerConfig pdcConfig;
		// Create a resource on the schema file.
		// Schema file generated using following tutorial: https://examples.javacodegeeks.com/core-java/xml/bind/jaxb-schema-validation-example/
		String pdcConfigXsdFileName = String.format("%s.xsd", PowerDeComposerConfig.class.getSimpleName());
		URL pdcConfigXsdResource = PowerDeComposerConfig.class.getResource(pdcConfigXsdFileName);
		// If the schema file can't be found, throw an exception.
		if (pdcConfigXsdResource == null) {
			throw new ConfigException(String.format("Can't find the schema file '%s'", pdcConfigXsdFileName));
		}
		
		// Try to load the schema.
		Schema configSchema;
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			configSchema = sf.newSchema(pdcConfigXsdResource);
		} catch (SAXException e) {
			throw new ConfigException(String.format("Couldn't read the schema file (%s)", pdcConfigXsdResource.toString()), e);
		}
		
		// Try to unmarshal the config file.
		try {
			// Create the JAXB context.
			JAXBContext jaxbContext = JAXBContext.newInstance(PowerDeComposerConfig.class);
			Unmarshaller pdcConfigUnmarshaller = jaxbContext.createUnmarshaller();
			// Set the schema on the unmarshaller.
			pdcConfigUnmarshaller.setSchema(configSchema);
			// Set the event handler.
			pdcConfigUnmarshaller.setEventHandler(new UnmarshallValidationEventHandler());
			// Unmarshal the config.
			pdcConfig = (PowerDeComposerConfig) pdcConfigUnmarshaller.unmarshal(inputSource);
		} catch (UnmarshalException e) {
			// If the linked exception is a sax parse exception, it contains the error in the config file.
			if (e.getLinkedException() instanceof SAXParseException) {
				throw new ConfigException(String.format("Error in config file: %s", e.getLinkedException().getMessage()), e);
			} else {
				throw new ConfigException(String.format("Error in config file: %s", e.getMessage()), e);
			}
		} catch (JAXBException e) {
			throw new ConfigException(String.format("Couldn't read the config file"), e);
		}
		
		// Return the pdc config.
		return pdcConfig;
	}
	
}
