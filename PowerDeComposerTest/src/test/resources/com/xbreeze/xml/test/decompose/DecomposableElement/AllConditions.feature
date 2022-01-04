@Unit
Feature: Configure AllConditions
  Here we test the usage of the AllConditions configuration during decompose.

  Scenario Outline: AllConditions <Scenario>
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
      			<AllConditions>
      				<!-- Decompose elements with the scenario condition. -->
      				<ElementCondition xpath="<firstConditionXPath>" />
      				<ElementCondition xpath="<secondConditionXPath>" />
      			</AllConditions>
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
      | Scenario            | firstConditionXPath     | secondConditionXPath                      | FirstElementOutput                         | SecondElementOutput                        |
      | always both true    | ./@id                   | parent::node()[starts-with(name(), 'c:')] | <xi:include href="ChildElements\\1.xml" /> | <xi:include href="ChildElements\\2.xml" /> |
      | always one true     | ./@id                   | @nonExistingAttribute                     | <FirstElement id="1"/>                     | <SecondElement id="2"/>                    |
      | always none true    | ./@nonExistingAttribute | @anotherNonExistingAttribute              | <FirstElement id="1"/>                     | <SecondElement id="2"/>                    |
      | sometimes both true | ./@id                   | starts-with(name(), 'Second')             | <FirstElement id="1"/>                     | <xi:include href="ChildElements\\2.xml" /> |
      | sometimes one true  | @id &lt; 2              | starts-with(name(), 'Second')             | <FirstElement id="1"/>                     | <SecondElement id="2"/>                    |
