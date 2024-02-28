@Unit
Feature: Configure NodeRemoval
  Here we test the usage of the NodeRemoval configuration during decompose.

  Scenario Outline: Remove element <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ElementXML>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="<RemoveXPath>" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      </RootElement>
      """

    Examples: 
      | Scenario                        | ElementXML                                                        | RemoveXPath                                     |
      | simple relative                 | <RemoveElement />                                                 | //RemoveElement                                 |
      | simple absolute                 | <RemoveElement />                                                 | /RootElement/RemoveElement                      |
      | simple with attributes absolute | <RemoveElement ChildAttribute="A" />                              | /RootElement/RemoveElement                      |
      | simple with attributes filtered | <RemoveElement ChildAttribute="A" />                              | /RootElement/RemoveElement[@ChildAttribute='A'] |
      | complex absolute                | <RemoveElement></RemoveElement>                                   | /RootElement/RemoveElement                      |
      | complex with children absolute  | <RemoveElement><ChildElementA /><ChildElementB /></RemoveElement> | /RootElement/RemoveElement                      |

  Scenario Outline: Remove element negative <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ElementXML>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="<RemoveXPath>" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ElementXML>
      </RootElement>
      """

    Examples: 
      | Scenario                        | ElementXML                           | RemoveXPath                                     |
      | simple relative                 | <RemoveElement />                    | //RemoveElemen                                  |
      | simple absolute                 | <RemoveElement />                    | /RootElement/RemoveElemen                       |
      | simple with attributes absolute | <RemoveElement ChildAttribute="A" /> | /RootElement/RemoveElemen                       |
      | simple with attributes filtered | <RemoveElement ChildAttribute="A" /> | /RootElement/RemoveElement[@ChildAttribute='B'] |

  Scenario Outline: Remove attribute <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Element RemoveAttribute="B"/>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="<RemoveXPath>" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Element/>
      </RootElement>
      """

    Examples: 
      | Scenario        | RemoveXPath                                  |
      | simple relative | //@RemoveAttribute                           |
      | simple absolute | /RootElement/Element/@RemoveAttribute        |
      | simple filtered | /RootElement/Element/@RemoveAttribute[.='B'] |

  Scenario Outline: Remove attribute negative <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Element RemoveAttribute="B"/>
      </RootElement>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="<RemoveXPath>" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<Element RemoveAttribute="B"/>
      </RootElement>
      """

    Examples: 
      | Scenario        | RemoveXPath                                  |
      | simple relative | //@RemoveAttribut                            |
      | simple absolute | /RootElement/Element/@RemoveAttribut         |
      | simple filtered | /RootElement/Element/@RemoveAttribute[.='A'] |

  Scenario Outline: Remove processing instruction element <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PIXML>
      <RootElement/>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="<RemoveXPath>" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement/>
      """

    Examples: 
      # Filtering on processing instructions is not supported (currently).
      #| simple   | <?RemovePI filter="true" ?> | /processing-instruction('RemovePI')[@filter='true'] |
      | Scenario        | PIXML                           | RemoveXPath                         |
      | simple          | <?RemovePI ?>                   | /processing-instruction('RemovePI') |
      | with attributes | <?RemovePI SomeAttribute="A" ?> | /processing-instruction('RemovePI') |

  Scenario Outline: Remove processing instruction attribute <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <InputPI>
      <RootElement/>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="<RemoveXPath>" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <OutputPI>
      <RootElement/>
      """

    Examples: 
      | Scenario                         | InputPI                                                                             | OutputPI                                                        | RemoveXPath                                           |
      | attribute simple                 | <?ExamplePI RemoveAttribute="A"?>                                                   | <?ExamplePI ?>                                                   | /processing-instruction('ExamplePI')/@RemoveAttribute |
      | attribute first attribute        | <?ExamplePI RemoveAttribute="A" DoNotRemoveAttribute="B"?>                          | <?ExamplePI DoNotRemoveAttribute="B"?>                          | /processing-instruction('ExamplePI')/@RemoveAttribute |
      | attribute middle attribute       | <?ExamplePI DoNotRemoveAttribute="A" RemoveAttribute="B" DoNotRemoveAttribute="C"?> | <?ExamplePI DoNotRemoveAttribute="A" DoNotRemoveAttribute="C"?> | /processing-instruction('ExamplePI')/@RemoveAttribute |
      | attribute last attribute         | <?ExamplePI DoNotRemoveAttribute="A" RemoveAttribute="B"?>                          | <?ExamplePI DoNotRemoveAttribute="A"?>                          | /processing-instruction('ExamplePI')/@RemoveAttribute |
      | attribute with special character | <?ExamplePI DoNotRemoveAttribute="Special’s" RemoveAttribute="B"?>                  | <?ExamplePI DoNotRemoveAttribute="Special’s"?>                  | /processing-instruction('ExamplePI')/@RemoveAttribute |

  Scenario: Remove multiline processing instruction attribute
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <?ExamplePI AttributeBefore="A" Label="This is
      some text
      which spreads accross
      multiple lines
      " AttributeAfter="B" ?>
      <RootElement/>
      """
    And the config file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <PowerDeComposerConfig>
      	<Decompose>
      		<NodeRemovals>
      			<NodeRemoval xpath="/processing-instruction('ExamplePI')/@Label" />
      		</NodeRemovals>
      		<DecomposableElement />
      	</Decompose>
      </PowerDeComposerConfig>
      """
    When I perform a decompose
    Then I expect a decomposed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <?ExamplePI AttributeBefore="A" AttributeAfter="B" ?>
      <RootElement/>
      """
