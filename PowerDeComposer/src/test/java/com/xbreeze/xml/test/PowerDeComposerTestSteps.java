package com.xbreeze.xml.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;

import com.xbreeze.xml.Executor;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class PowerDeComposerTestSteps {
	// The default value for the config location is set. This is used when the config is specified inline.
	private Path _pdcConfigPath;
	// The default value for the input location is set. This is used when the input is specified inline.
	private Path _composedFilePath;
	// The default value for the decomposed file path.
	private Path _decomposedFilePath;
	// The folder of the config files.
	private Path _configFolderPath;
	// The folder of the target files.
	private Path _composedFolderPath;
	// The folder of the target files.
	private Path _decomposedFolderPath;
	// The resource path of the feature under test.
	private Path _scenarioResourcePath;
	// The resource path for runtime composed and decomposed files for the feature under test.
	private Path _scenarioRuntimeResourcePath;

	@Before
	public void before(final Scenario scenario) throws Exception {
	
		// Get the class loader.
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		// Get the classpath path of the current scenario.
		// Create a path of it and get the parent path (so we have the classpath folder the resource files are in).
		Path featureFilePath = Path.of(loader.getResource(scenario.getUri().getRawSchemeSpecificPart()).toURI());
		String featureName = featureFilePath.getFileName().toFile().toString().replace(".feature", "");
		this._scenarioResourcePath = featureFilePath.getParent().resolve(featureName).resolve(scenario.getName().replace(' ', '_'));
		this._scenarioRuntimeResourcePath = _scenarioResourcePath.resolve(scenario.getId());
		// Create the file resource
		this.createDirectoryIfItDoesntExist(this._scenarioRuntimeResourcePath);
		
		// Initialize the paths to the default values.
		_configFolderPath = this._scenarioRuntimeResourcePath.resolve("Config");
		_composedFolderPath = this._scenarioRuntimeResourcePath.resolve("Composed");
		_decomposedFolderPath = this._scenarioRuntimeResourcePath.resolve("Decomposed");
		
		// Init the file paths (these can be overridden when the composed or decompose folder path is changes using a phrase.
		initFilePaths();
		
		// Log  the paths.
		scenario.log(String.format("Feature resource path: %s", _scenarioResourcePath.toString()));
		scenario.log(String.format("Feature file-resource path: %s", _scenarioRuntimeResourcePath.toString()));
	}
	
	public void createDirectoryIfItDoesntExist(Path directoryPath) throws Exception {
		File directoryFile = directoryPath.toFile();
		// If the directory exists, remove it.
		if (directoryFile.exists() && directoryFile.isDirectory()) {
			FileUtils.deleteDirectory(directoryFile);
		} else if (directoryFile.exists() && directoryFile.isFile()) {
			FileUtils.delete(directoryFile);
		}
		
		if (!directoryFile.mkdirs()) {
			throw new Exception(String.format("Couldn't create folder '%s'", directoryPath.toString()));
		}
	}
	
	private void initFilePaths() throws Exception {
		// Set the default compose and decompose file paths.
		this._composedFilePath = this._composedFolderPath.resolve("InlineFile.xml");
		this._decomposedFilePath = this._decomposedFolderPath.resolve("InlineFile.xml");
		
		// Create the directories if they don't exist yet (and empty if they do).
		this.createDirectoryIfItDoesntExist(this._configFolderPath);
		this.createDirectoryIfItDoesntExist(this._composedFolderPath);
		this.createDirectoryIfItDoesntExist(this._decomposedFolderPath);
	}
	
	@Given("^the config folder location '(.*)'$")
	public void givenTheConfigFolderLocation(String configFolderLocation) throws Throwable {
		this._configFolderPath = this._scenarioResourcePath.resolve(configFolderLocation);
		initFilePaths();
	}
	
	@Given("^the composed folder location '(.*)'$")
	public void givenTheComposedFolderLocation(String composedFolderLocation) throws Throwable {
		this._composedFolderPath = this._scenarioResourcePath.resolve(composedFolderLocation);
		initFilePaths();
	}

	@Given("^the decomposed folder location '(.*)'$")
	public void givenTheDecomposedFolderLocation(String decomposedFolderLocation) throws Throwable {
		this._decomposedFolderPath = this._scenarioResourcePath.resolve(decomposedFolderLocation);
		initFilePaths();
	}

	@Given("^the config file '(.*)'$")
	public void givenTheConfigFileLocation(String pdcConfigLocation) throws Throwable {
		this._pdcConfigPath = this._scenarioResourcePath.resolve(pdcConfigLocation);
	}
	
	@Given("^the config file:$")
	public void givenTheConfigFileContents(String pdcConfigFileContents) throws Throwable {
		// Write the contents of the config to a file.
		this._pdcConfigPath = this._configFolderPath.resolve("InlineConfigFile.xml");
		this.writeXmlFile(this._pdcConfigPath, pdcConfigFileContents);
	}

	@Given("^the composed file '(.*)'$")
	public void givenTheXmlComposedFileLocation(String composedFileLocation) throws Throwable {
		this._composedFilePath = this._scenarioResourcePath.resolve(composedFileLocation);
	}
	
	@Given("^the composed file '(.*)':$")
	public void givenTheXmlComposedFileContents(String composedFileName, String composedFileContents) throws Throwable {
		// Write the contents of the input to a file.
		Path composedFilePath = _composedFolderPath.resolve(composedFileName);
		this.writeXmlFile(composedFilePath, composedFileContents);
		// Set the input file location.
		//this._composedFilePath = composedFilePath;
	}
	
	@Given("^the composed file:$")
	public void givenTheXmlComposedFileContents(String composedFileContents) throws Throwable {
		// Write the composed file contents to a file.
		this.writeXmlFile(this._composedFilePath, composedFileContents);
	}
	
	@Given("^the decomposed file '(.*)'$")
	public void givenTheDecomposedFileLocation(String xmlFileLocation) throws Throwable {
		this._decomposedFilePath = this._scenarioResourcePath.resolve(xmlFileLocation);
	}
	
	@Given("^the decomposed file '(.*)':$")
	public void givenTheDecomposedFileContents(String decomposedFileLocation, String decomposedFileContents) throws Throwable {
		// Write the contents of the target to a file.
		Path decomposedFilePath = this._decomposedFolderPath.resolve(decomposedFileLocation);
		this.writeXmlFile(decomposedFilePath, decomposedFileContents);
		// Add the target file to the set.
		//this._decomposedFilePath = decomposedFilePath;
	}
	
	@Given("^the decomposed file:$")
	public void givenTheDecomposedFileContents(String decomposedFileContents) throws Throwable {
		// Write the decomposed file contents to a file.
		this.writeXmlFile(this._decomposedFilePath, decomposedFileContents);
	}
	
	public void writeXmlFile(Path targetFilePath, String fileContents) throws Exception {
		// If the parent folder doesn't exist, create it.
		File parentFolder = targetFilePath.getParent().toFile();
		if (!parentFolder.exists()) {
			if (!parentFolder.mkdirs()) {
				throw new Exception(String.format("Error while creating directory: %s", parentFolder.toString()));
			}
		}
		// Write the file.
		FileWriter targetFileWrite = new FileWriter(targetFilePath.toFile(), Charset.forName("UTF-8"));
		//System.out.println(String.format("Writing file contents: %s", fileContents));
		// Replacing LF with preceding CR with CRLF (since Cucumber remove's it from the string.
		IOUtils.write(fileContents.replaceAll("(?<!\r)\n", "\r\n"), targetFileWrite);
		targetFileWrite.close();
	}
	
	@When("^I perform a compose$")
	public void iExecuteCompose() throws Throwable {
		// Execute PowerDeComposer.
		if (this._pdcConfigPath != null) {
			Executor.main(
				new String[] {
					"compose",
					this._decomposedFilePath.toString(),
					this._composedFilePath.toString(),
					this._pdcConfigPath.toString()
				}
			);
		} else {
			Executor.main(
				new String[] {
					"compose",
					this._decomposedFilePath.toString(),
					this._composedFilePath.toString()
				}
			);
		}
	}

	@When("^I perform a decompose$")
	public void iExecuteDecompose() throws Throwable {
		// Execute PowerDeComposer.
		Executor.main(
			new String[] {
				"decompose",
				this._composedFilePath.toString(),
				this._decomposedFolderPath.toString(),
				this._pdcConfigPath.toString()
			}
		);
	}
	
	@Then("^I expect a composed file '(.*)' with the following content:$")
	public void thenIExpectComposedFileWithFollowingContent(String composedFileLocation, String expectedComposedFileContents)
			throws Throwable {
		thenIExpectTheFileWithFollowingContent(this._composedFolderPath.resolve(composedFileLocation).toFile(), expectedComposedFileContents);
	}
	
	@Then("^I expect a composed file with the following content:$")
	public void thenIExpectComposedFileWithFollowingContent(String expectedComposedFileContents)
			throws Throwable {
		thenIExpectTheFileWithFollowingContent(this._composedFilePath.toFile(), expectedComposedFileContents);
	}
	
	@Then("^I expect a composed file with the content equal to '(.*)'$")
	public void thenIExpectComposedFileWithContentEqualToFile(String expectedComposedFileLocation)
			throws Throwable {
		File expectedComposedFile = this._scenarioResourcePath.resolve(expectedComposedFileLocation).toFile();
		String expectedComposedFileContents = PowerDeComposerTestSteps.getFileContents(expectedComposedFile);
		thenIExpectTheFileWithFollowingContent(this._composedFilePath.toFile(), expectedComposedFileContents);
	}

	@Then("^I expect a decomposed file '(.*)' with the following content:$")
	public void thenIExpectDecomposedFileWithFollowingContent(String decomposedFileLocation, String expectedDecomposedFileContents)
			throws Throwable {
		thenIExpectTheFileWithFollowingContent(this._decomposedFolderPath.resolve(decomposedFileLocation).toFile(), expectedDecomposedFileContents);
	}
	
	@Then("^I expect a decomposed file with the following content:$")
	public void thenIExpectDecomposedFileWithFollowingContent(String expectedDecomposedFileContents)
			throws Throwable {
		thenIExpectTheFileWithFollowingContent(this._decomposedFilePath.toFile(), expectedDecomposedFileContents);
	}
	
	// Compare the actual and expected file contents. If it differs throw an assertion error.
	public void thenIExpectTheFileWithFollowingContent(File targetFile, String expectedFileContents)
			throws Throwable {
		// Check the file exists.
		assertTrue(
				targetFile.exists(),
				String.format("The expected file '%s' doesn't exist!", targetFile)
		);
		
		// Get the actual output file and read to string.
		String actualResultContent = PowerDeComposerTestSteps.getFileContents(targetFile);
		// Assert the expected and actual file contents are the same.
		assertEquals(
				// When comparing the decomposed results, we need to replace LF with CRLF, since Cucumber will remove the CR.
				expectedFileContents.replaceAll("(?<!\r)\n", "\r\n"),
				actualResultContent,
				"The expected and actual file content is different"
		);
	}
	
	private static String getFileContents(File file) throws IOException {
		//Open the file and read to string
		FileInputStream fis = new FileInputStream(file);
		BOMInputStream bomInputStream = new BOMInputStream(fis);
		return IOUtils.toString(bomInputStream, bomInputStream.getBOMCharsetName());
	}
	
	@Then("^I (do not|do) expect the file '(.*)'$")
	public void thenIDoExpectTheFile(String doOrDont,String targetFile)
			throws Throwable {
		
		File expectedFile = this._decomposedFolderPath.resolve(targetFile).toFile();
		
		if (doOrDont.equalsIgnoreCase("do")){
			assertTrue(
					expectedFile.exists(),
					String.format("The expected file '%s' doesn't exist!", targetFile)
			);
		}else if (doOrDont.equalsIgnoreCase("do not")){
			assertFalse(
					expectedFile.exists(),
					String.format("The not expected file '%s' does exist!", targetFile)
			);
		}
	}

}