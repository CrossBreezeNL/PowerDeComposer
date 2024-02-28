@Unit
Feature: Configure TargetFileName
  Here we test the usage of the TargetFileNames configuration during decompose.

  Scenario Outline: TargetFileName is value of <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement id="<FirstId>">
      			<Name><FirstName></Name>
      		</ChildElement>
      		<ChildElement id="<SecondId>">
      			<Name><SecondName></Name>
      		</ChildElement>
      	</ChildElements>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement>
      			<!-- Decompose elements with a Name child-element. -->
      			<ElementCondition xpath="./Name" />
      			<!-- The target file name should be the value of the Name element. If this doesn't exists or isn't unique, then use the Id attribute. -->
      			<TargetFileNames>
      				<TargetFileName xpath="./Name" condition="string-length(./Name) > 0" />
      				<TargetFileName xpath="./@id" />
      			</TargetFileNames>
      		</DecomposableElement>
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<xi:include href="<FirstFileName>.xml" />
      		<xi:include href="<SecondFileName>.xml" />
      	</ChildElements>
      </RootElement>
      """
    And I expect a decomposed file '<FirstFileName>.xml' with the following content:
      """
      <ChildElement id="<FirstId>">
      			<Name><FirstName></Name>
      		</ChildElement>
      """
    And I expect a decomposed file '<SecondFileName>.xml' with the following content:
      """
      <ChildElement id="<SecondId>">
      			<Name><SecondName></Name>
      		</ChildElement>
      """

    Examples: 
      | Scenario             | FirstId | FirstName | SecondId | SecondName | FirstFileName           | SecondFileName           |
      | unique names         |       1 | FirstName |        2 | SecondName | ChildElements/FirstName | ChildElements/SecondName |
      | non-unique names     |       1 | FirstName |        2 | FirstName  | ChildElements/FirstName | ChildElements/2          |
      | empty name           |       1 | FirstName |        2 |            | ChildElements/FirstName | ChildElements/2          |
      | case different names |       1 | firstname |        2 | FirstName  | ChildElements/firstname | ChildElements/2          |
