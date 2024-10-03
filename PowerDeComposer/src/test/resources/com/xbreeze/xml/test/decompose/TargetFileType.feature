@Unit
Feature: Configure Decompoe TargetFileType
  Here we test the working of the targetFileType attribute of the Decompose configuration.

  Background: 
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement id="SecondId">
      			<Name>FirstName</Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """

  Scenario: TargetFileType is XML
    Given the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose targetFileType="xml">
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file 'InlineFile.xml' with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement id="SecondId">
      			<Name>FirstName</Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """
      
  @Debug
  Scenario: TargetFileType is YaML
    Given the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose targetFileType="yaml">
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file 'InlineFile.yaml' with the following content:
      """
      RootElement:
        - ChildElements:
          - @id: "SecondId"
            Name: "FirstName"
      """
