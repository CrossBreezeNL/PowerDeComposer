package com.xbreeze.xml.decompose.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;

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
	private String pdcConfigLocation = "InlineFeatureConfig.xml";
	// The default value for the input location is set. This is used when the input is specified inline.
	private String inputFileLocation = "InlineFeatureInput.xml";
	private String targetFolderLocation = "Decomposed";
	private Path _featureResourcePath;

	@Before
	public void before(final Scenario scenario) throws URISyntaxException, MalformedURLException {
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		// Get the classpath path of the current scenario.
		// Create a path of it and get the parent path (so we have the classpath folder the resource files are in).
		_featureResourcePath = Path.of(loader.getResource(scenario.getUri().getRawSchemeSpecificPart()).toURI()).getParent();
		//scenario.log(String.format("Feature resource path: %s", _featureResourcePath.toString()));
	}

	@Given("^the config file '(.*)'$")
	public void givenTheConfigFileLocation(String pdcConfigLocation) throws Throwable {
		this.pdcConfigLocation = pdcConfigLocation;
	}
	
	@Given("^the config file:$")
	public void givenTheConfigFileContents(String pdcConfigFileContents) throws Throwable {
		// Write the contents of the config to a file.
		FileWriter configFileWrite = new FileWriter(_featureResourcePath.resolve(this.pdcConfigLocation).toFile());
		IOUtils.write(pdcConfigFileContents, configFileWrite);
		configFileWrite.close();
	}

	@Given("^the input file '(.*)'$")
	public void givenTheXmlInputFileLocation(String xmlFileLocation) throws Throwable {
		this.inputFileLocation = xmlFileLocation;
	}
	
	@Given("^the input file '(.*)':$")
	public void givenTheXmlInputFileContents(String inputFileLocation, String inputFileContents) throws Throwable {
		this.givenTheXmlInputFileLocation(inputFileLocation);
		// Write the contents of the input to a file.
		FileWriter inputFileWrite = new FileWriter(_featureResourcePath.resolve(this.inputFileLocation).toFile());
		IOUtils.write(inputFileContents, inputFileWrite);
		inputFileWrite.close();
	}

	@Given("^the target folder location '(.*)'$")
	public void givenTheTargetFolderLocation(String targetFolderLocation) throws Throwable {
		this.targetFolderLocation = targetFolderLocation;
	}

	@When("^I execute PowerDeComposer$")
	public void iExecutePowerDeComposer() throws Throwable {
		// Execute PowerDeComposer.
		Executor.main(new String[] { "decompose", _featureResourcePath.resolve(inputFileLocation).toString(),
				_featureResourcePath.resolve(this.targetFolderLocation).toString(),
				_featureResourcePath.resolve(this.pdcConfigLocation).toString() });
	}

	@Then("^I expect a target file '(.*)' with the following content:$")
	public void thenIExpectTheFileWithFollowingContent(String targetFileLocation, String expectdFileContents)
			throws Throwable {
		//Open the expected output file and read to string
		FileInputStream fis = new FileInputStream(_featureResourcePath.resolve(targetFolderLocation).resolve(targetFileLocation).toFile());
		BOMInputStream bomInputStream = new BOMInputStream(fis);		
		String actualResultContent = IOUtils.toString(bomInputStream, bomInputStream.getBOMCharsetName());
		// Assert the expected and actual file contents are the same.
		assertEquals(
				expectdFileContents,
				actualResultContent,
				"The expected and actual file content is different"
		);
	}

}
