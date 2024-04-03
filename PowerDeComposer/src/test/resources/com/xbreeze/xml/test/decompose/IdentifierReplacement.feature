@Unit
Feature: Configure IdentifierReplacement
  Here we test the usage of the IdentifierReplacement configuration during decompose.

  Scenario Outline: IdentifierReplacement on attribute using <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement id="original-id" rootAttribute="rootAttributeValue">
      	<childElementXML>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<IdentifierReplacements>
      			<IdentifierReplacement identifierNodeXPath="<identifierNodeXPath>" replacementValueXPath="<replacementValueXPath>" />
      		</IdentifierReplacements>
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement id="<replacedId>" rootAttribute="rootAttributeValue">
      	<childElementXML>
      </RootElement>
      """

    Examples: 
      | Scenario                | childElementXML                                                     | identifierNodeXPath | replacementValueXPath                  | replacedId                 |
      | root-attribute          |                                                                     | //*/@id             | ../@rootAttribute                      | rootAttributeValue         |
      | child-element           | <ChildElement>childElementValue</ChildElement>                      | //*/@id             | ../ChildElement                        | childElementValue          |
      | child-element-attribute | <ChildElement childElementAttribute="childElementAttributeValue" /> | //*/@id             | ../ChildElement/@childElementAttribute | childElementAttributeValue |

  Scenario Outline: IdentifierReplacement on element-text using <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement rootAttribute="rootAttributeValue">
      	<Id>original-id</Id>
      	<childElementXML>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<IdentifierReplacements>
      			<IdentifierReplacement identifierNodeXPath="<identifierNodeXPath>" replacementValueXPath="<replacementValueXPath>" />
      		</IdentifierReplacements>
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement rootAttribute="rootAttributeValue">
      	<Id><replacedId></Id>
      	<childElementXML>
      </RootElement>
      """

    Examples: 
      | Scenario                | childElementXML                                                     | identifierNodeXPath | replacementValueXPath                     | replacedId                 |
      | root-attribute          |                                                                     | //*/Id/text()       | ../../@rootAttribute                      | rootAttributeValue         |
      | child-element           | <ChildElement>childElementValue</ChildElement>                      | //*/Id/text()       | ../../ChildElement                        | childElementValue          |
      | child-element-attribute | <ChildElement childElementAttribute="childElementAttributeValue" /> | //*/Id/text()       | ../../ChildElement/@childElementAttribute | childElementAttributeValue |
      
  Scenario: IdentifierReplacement with referencing nodes
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<KeyElements>
      		<KeyElement id="1" alternateKey="a" />
      		<KeyElement id="2" alternateKey="b" />
      	</KeyElements>
      	<ReferencingElements>
      		<ReferencingElement ref="1" />
      		<ReferencingElement ref="2" />
      	</ReferencingElements>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<IdentifierReplacements>
      			<IdentifierReplacement identifierNodeXPath="//*/@id" replacementValueXPath="../@alternateKey" referencingNodeXPath="//*/@ref" />
      		</IdentifierReplacements>
      		<!-- The DecomposableElement element must be here, otherwise the decomposer won't write the composed file to the decomposed folder. -->
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<KeyElements>
      		<KeyElement id="a" alternateKey="a" />
      		<KeyElement id="b" alternateKey="b" />
      	</KeyElements>
      	<ReferencingElements>
      		<ReferencingElement ref="a" />
      		<ReferencingElement ref="b" />
      	</ReferencingElements>
      </RootElement>
      """
