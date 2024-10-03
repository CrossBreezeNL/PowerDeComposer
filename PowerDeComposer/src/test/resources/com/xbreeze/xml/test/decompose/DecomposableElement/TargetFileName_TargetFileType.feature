@Unit
Feature: Configure TargetFileName TargetFileType
  Here we test the usage of the TargetFileNames targetFileType configuration during decompose.

  Background: 
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement id="1">
      			<Name>FirstName</Name>
      		</ChildElement>
      		<ChildElement id="2">
      			<Name>SecondName</Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """

  Scenario: Decompose targetFileType is YaML and TargetFileName targetFileType is empty
    Given the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose targetFileType="yaml">
      		<DecomposableElement>
      			<!-- Decompose elements with a Name child-element. -->
      			<ElementCondition xpath="./Name" />
      			<!-- The target file name should be the value of the Name element. If this doesn't exists or isn't unique, then use the Id attribute. -->
      			<TargetFileNames>
      				<TargetFileName xpath="./Name" />
      			</TargetFileNames>
      		</DecomposableElement>
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file 'InlineFile.yaml' with the following content:
      """
      RootElement
        - ChildElements:
          - xi:include
              - @href: "ChildElements/FirstName.yaml"
              - @href: "ChildElements/SecondName.yaml"
      """
    And I expect a decomposed file 'ChildElements/FirstName.yaml' with the following content:
      """
      ChildElement
        - id: 1
          Name: FirstName
      """
    And I expect a decomposed file 'ChildElements/SecondName.yaml' with the following content:
      """
      ChildElement
        - id: 2
          Name: SecondName
      """

  Scenario: Decompose targetFileType is empty and TargetFileName targetFileType is empty
    Given the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement>
      			<!-- Decompose elements with a Name child-element. -->
      			<ElementCondition xpath="./Name" />
      			<!-- The target file name should be the value of the Name element. If this doesn't exists or isn't unique, then use the Id attribute. -->
      			<TargetFileNames>
      				<TargetFileName xpath="./Name" targetFileType="xml" />
      			</TargetFileNames>
      		</DecomposableElement>
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file 'InlineFile.xml' with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<xi:include href="ChildElements/FirstName.xml" />
      		<xi:include href="ChildElements/SecondName.xml" />
      	</ChildElements>
      </RootElement>
      """
    And I expect a decomposed file 'ChildElements/FirstName.xml' with the following content:
      """
      <ChildElement id="1">
      			<Name>FirstName</Name>
      		</ChildElement>
      """
    And I expect a decomposed file 'ChildElements/SecondName.xml' with the following content:
      """
      <ChildElement id="2">
      			<Name>SecondName</Name>
      		</ChildElement>
      """

  Scenario: Decompose targetFileType is empty and TargetFileName targetFileType is YaML
    Given the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement>
      			<!-- Decompose elements with a Name child-element. -->
      			<ElementCondition xpath="./Name" />
      			<!-- The target file name should be the value of the Name element. If this doesn't exists or isn't unique, then use the Id attribute. -->
      			<TargetFileNames>
      				<TargetFileName xpath="./Name" targetFileType="yaml" />
      			</TargetFileNames>
      		</DecomposableElement>
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file 'InlineFile.xml' with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<xi:include href="ChildElements/FirstName.yaml" />
      		<xi:include href="ChildElements/SecondName.yaml" />
      	</ChildElements>
      </RootElement>
      """
    And I expect a decomposed file 'ChildElements/FirstName.yaml' with the following content:
      """
      ChildElement
        - id: 1
          Name: FirstName
      """
    And I expect a decomposed file 'ChildElements/SecondName.yaml' with the following content:
      """
      ChildElement
        - id: 2
          Name: SecondName
      """