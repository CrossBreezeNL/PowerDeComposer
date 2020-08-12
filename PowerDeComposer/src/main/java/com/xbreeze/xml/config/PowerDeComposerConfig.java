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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import com.xbreeze.xml.utils.FileUtils;

@XmlRootElement(name = "PowerDeComposerConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class PowerDeComposerConfig {
	// The logger for this class.
	private static final Logger logger = Logger.getLogger(PowerDeComposerConfig.class.getName());

	/**
	 * List of document nodes to extract (XPath expression per node).
	 * The default list of elements to extract are:
	 *  - c:DBMS, c:ExtendedModelDefinitions, c:PhysicalDiagrams, c:DataSources, c:Packages, c:Mappings, c:Tables, c:References, c:Reports, c:SourceModels, c:Users, c:TargetModels, c:Entities, c:Relationships, c:LogicalDiagrams
	 */
	private List<String> _elementsToExtract = Arrays.asList(new String[] { "c:DBMS", "c:ExtendedModelDefinitions", "c:PhysicalDiagrams", "c:DataSources", "c:Packages", "c:Mappings", "c:Tables", "c:References", "c:Reports", "c:SourceModels", "c:Users", "c:TargetModels", "c:Entities", "c:Relationships", "c:LogicalDiagrams" });
	
	/**
	 * The sub element contents to use as the filename (without extension) for the extracted elements.
	 * The default value is ObjectID (so it will use the ObjectID of any extracted element as it's file name).
	 * Possible options are: ObjectID, Code & Name.
	 */
	private String _targetFileNameSubElement = "ObjectID";
	
	/**
	 * List of sub elements to include as an attribute on the include instruction.
	 * The default value is Name.
	 * Possible options are: ObjectID, Code & Name.
	 */
	private List<String> _includeSubElements = Arrays.asList(new String[] { "Name" });

	@XmlElement(name = "Element")
	@XmlElementWrapper(name = "ElementsToExtract")
	public List<String> getElementsToExtract() {
		return _elementsToExtract;
	}
	
	@XmlElement(name = "TargetFileNameSubElement")
	public String getTargetFileNameSubElement() {
		return _targetFileNameSubElement;
	}
	
	@XmlElement(name = "SubElement")
	@XmlElementWrapper(name = "IncludeSubElements")
	public List<String> getIncludeSubElements() {
		return _includeSubElements;
	}

	/**
	 * Unmarshal a config from a String.
	 * 
	 * @param configFileContent The String object to unmarshal.
	 * @return The unmarshalled PowerDeComposerConfig object.
	 * @throws ConfigException
	 */
	public static PowerDeComposerConfig fromString(String configFileContent) throws ConfigException {
		PowerDeComposerConfig pdcConfig;
		// Create a resource on the schema file.
		// Schema file generated using following tutorial:
		// https://examples.javacodegeeks.com/core-java/xml/bind/jaxb-schema-validation-example/
		String pdcConfigXsdFileName = String.format("%s.xsd", PowerDeComposerConfig.class.getSimpleName());
		InputStream pdcConfigSchemaAsStream = PowerDeComposerConfig.class.getResourceAsStream(pdcConfigXsdFileName);
		// If the schema file can't be found, throw an exception.
		if (pdcConfigSchemaAsStream == null) {
			throw new ConfigException(String.format("Can't find the schema file '%s'", pdcConfigXsdFileName));
		}
		// Create the StreamSource for the schema.
		StreamSource pdcConfigXsdResource = new StreamSource(pdcConfigSchemaAsStream);

		// Try to load the schema.
		Schema configSchema;
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			configSchema = sf.newSchema(pdcConfigXsdResource);
		} catch (SAXException e) {
			throw new ConfigException(
					String.format("Couldn't read the schema file (%s)", pdcConfigXsdResource.toString()), e);
		}

		// Try to unmarshal the config file.
		try {
			// Create the JAXB context.
			JAXBContext jaxbContext = JAXBContext.newInstance(PowerDeComposerConfig.class);
			Unmarshaller pdcConfigUnmarshaller = jaxbContext.createUnmarshaller();
			// Create a SAXParser factory
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			spf.setSchema(configSchema);
			XMLReader xr = spf.newSAXParser().getXMLReader();
			SAXSource saxSource = new SAXSource(xr, new InputSource(new StringReader(configFileContent)));

			// Set the event handler.
			pdcConfigUnmarshaller.setEventHandler(new UnmarshallValidationEventHandler());

			// Unmarshal the config.
			pdcConfig = (PowerDeComposerConfig) pdcConfigUnmarshaller.unmarshal(saxSource);
		} catch (UnmarshalException | SAXException e) {
			// If the linked exception is a sax parse exception, it contains the error in the config file.
			if (e instanceof UnmarshalException
					&& ((UnmarshalException) e).getLinkedException() instanceof SAXParseException) {
				throw new ConfigException(String.format("Error in config file: %s",
						((UnmarshalException) e).getLinkedException().getMessage()), e);
			} else {
				throw new ConfigException(String.format("Error in config file: %s", e.getMessage()), e);
			}
		} catch (JAXBException e) {
			throw new ConfigException(String.format("Couldn't read the config file"), e);
		} catch (ParserConfigurationException e) {
			throw new ConfigException(String.format("Parser configuration error"), e);
		}
		logger.info("Reading config complete.");
		return pdcConfig;

	}

	/**
	 * Unmarshal a file into a PowerDeComposerConfig object.
	 * 
	 * @param configFileUri The file to unmarshal.
	 * @return The unmarshalled PowerDeComposerConfig object.
	 * @throws ConfigException
	 */
	public static PowerDeComposerConfig fromFile(URI configFileUri) throws ConfigException {
		logger.fine(String.format("Creating PowerDeComposerConfig object from '%s'", configFileUri));
		PowerDeComposerConfig pdcConfig;
		try {
			pdcConfig = fromString(FileUtils.getFileContent(configFileUri));
		} catch (ConfigException | IOException e) {
			// Catch the config exception here to add the filename in the exception text.
			throw new ConfigException(String.format("%s (%s)", e.getMessage(), configFileUri.toString()), e.getCause());
		}
		return pdcConfig;
	}
}
