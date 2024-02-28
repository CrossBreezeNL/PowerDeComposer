@Unit
Feature: Configure ElementCondition
  Here we test the usage of the ElementCondition configuration during decompose.

  Scenario Outline: ElementCondition using <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<c:ChildElements>
      		<FirstElement id="1"/>
      		<SecondElement id="2"/>
      	</c:ChildElements>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<DecomposableElement>
      			<!-- Decompose elements with the scenario condition. -->
      			<ElementCondition xpath="<elementConditionXPath>" />
      			<!-- Use the id attribute as file name. -->
      			<TargetFileNames>
      				<TargetFileName xpath="@id" />
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
      	<c:ChildElements>
      		<FirstElementOutput>
      		<SecondElementOutput>
      	</c:ChildElements>
      </RootElement>
      """

    Examples: 
      | Scenario                      | elementConditionXPath                               | FirstElementOutput                        | SecondElementOutput                       |
      | attribute existence           | ./@id                                               | <xi:include href="ChildElements/1.xml" /> | <xi:include href="ChildElements/2.xml" /> |
      | attribute condition           | @id &lt; 2                                          | <xi:include href="ChildElements/1.xml" /> | <SecondElement id="2"/>                   |
      | element condition             | starts-with(name(), 'Second')                       | <FirstElement id="1"/>                    | <xi:include href="ChildElements/2.xml" /> |
      | parent condition              | parent::node()[starts-with(name(), 'c:')]           | <xi:include href="ChildElements/1.xml" /> | <xi:include href="ChildElements/2.xml" /> |
      | multiple conditions existence | ./@id and parent::node()[starts-with(name(), 'c:')] | <xi:include href="ChildElements/1.xml" /> | <xi:include href="ChildElements/2.xml" /> |
