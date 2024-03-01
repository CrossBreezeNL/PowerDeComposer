@Unit
Feature: Configure IncludeAttribute
  Here we test the usage of the IncludeAttributes configuration during decompose.

  Scenario Outline: IncludeAttribute using <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<FirstElement id="1">
      			<Name>FirstName</Name>
      		</FirstElement>
      		<SecondElement id="2">
      			<Name>SecondName</Name>
      		</SecondElement>
      	</ChildElements>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement>
      			<!-- Decompose elements with a id attribute. -->
      			<ElementCondition xpath="./@id" />
      			<!-- Use the name of the element as file name. -->
      			<TargetFileNames>
      				<TargetFileName xpath="translate(name(), ':', '_')" />
      			</TargetFileNames>
      			<!-- Add the Name as an attribute on the include. -->
      			<IncludeAttributes>
      				<IncludeAttribute name="<IncludeAttributeName>" xpath="<IncludeAttributeXPath>" />
      			</IncludeAttributes>
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
      		<xi:include href="ChildElements/FirstElement.xml" <IncludeAttributeName>="<IncludeAttributeFirstValue>" />
      		<xi:include href="ChildElements/SecondElement.xml" <IncludeAttributeName>="<IncludeAttributeSecondValue>" />
      	</ChildElements>
      </RootElement>
      """

    Examples: 
      | Scenario  | IncludeAttributeName | IncludeAttributeXPath | IncludeAttributeFirstValue | IncludeAttributeSecondValue |
      | attribute | Id                   | ./@id                 |                          1 |                           2 |
      | element   | Name                 | ./Name                | FirstName                  | SecondName                  |
