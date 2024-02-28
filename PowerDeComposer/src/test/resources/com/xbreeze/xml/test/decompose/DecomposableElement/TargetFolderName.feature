@Unit
Feature: Configure TargetFolderName
  Here we test the usage of the TargetFolderNames configuration during decompose.

  Scenario Outline: TargetFolderName is <Scenario>
    Given the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement type="<FirstType>" id="1">
      			<Name><FirstName></Name>
      		</ChildElement>
      		<ChildElement type="<SecondType>" id="2">
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
      			<!-- The target file name should be the value of the Name element. -->
      			<TargetFolderNames>
      				<TargetFolderName xpath="./@type" <ConfigOverrideParent> />
      			</TargetFolderNames>
      			<!-- The target file name should be the value of the Name element. -->
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
      <ChildElement type="<FirstType>" id="1">
      			<Name><FirstName></Name>
      		</ChildElement>
      """
    And I expect a decomposed file '<SecondFileName>.xml' with the following content:
      """
      <ChildElement type="<SecondType>" id="2">
      			<Name><SecondName></Name>
      		</ChildElement>
      """

    Examples: 
      | Scenario                         | ConfigOverrideParent   | FirstType | FirstName | SecondType | SecondName | FirstFileName                     | SecondFileName                      |
      | the same                         |                        | SameType  | FirstName | SameType   | SecondName | ChildElements/SameType/FirstName  | ChildElements/SameType/SecondName   |
      | different                        |                        | FirstType | FirstName | SecondType | SecondName | ChildElements/FirstType/FirstName | ChildElements/SecondType/SecondName |
      | the same and same filename       |                        | SameType  | SameName  | SameType   | SameName   | ChildElements/SameType/SameName   | ChildElements/SameType/2            |
      | different case and same filename |                        | sametype  | SameName  | SameType   | SameName   | ChildElements/sametype/SameName   | ChildElements/SameType/2            |
      | the same dont override parent    | overrideParent="false" | SameType  | FirstName | SameType   | SecondName | ChildElements/SameType/FirstName  | ChildElements/SameType/SecondName   |
      | the same override parent         | overrideParent="true"  | SameType  | FirstName | SameType   | SecondName | SameType/FirstName                | SameType/SecondName                 |

  Scenario Outline: TargetFolderName is <Scenario> with child-elements
    Given the decomposed folder location '<DecomposedFolder>'
    And the composed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      	<ChildElements>
      		<ChildElement type="<FirstType>" id="1">
      			<Name><FirstName></Name>
      			<ChildElements>
      				<ChildElement id="3">
      					<Name>ThirdName</Name>
      				</ChildElement>
      			</ChildElements>
      		</ChildElement>
      		<ChildElement type="<SecondType>" id="2">
      			<Name><SecondName></Name>
      			<ChildElements>
      				<ChildElement id="4">
      					<Name>FourthName</Name>
      				</ChildElement>
      			</ChildElements>
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
      			<!-- The target file name should be the value of the Name element. -->
      			<TargetFolderNames>
      				<TargetFolderName xpath="./@type" <ConfigOverrideParent> />
      			</TargetFolderNames>
      			<!-- The target file name should be the value of the Name element. -->
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
      		<xi:include href="<FirstFilePath><FirstFileName>.xml" />
      		<xi:include href="<SecondFilePath><SecondFileName>.xml" />
      	</ChildElements>
      </RootElement>
      """
    And I expect a decomposed file '<FirstFilePath><FirstFileName>.xml' with the following content:
      """
      <ChildElement type="<FirstType>" id="1">
      			<Name><FirstName></Name>
      			<ChildElements>
      				<xi:include href="<ThirdFileName>.xml" />
      			</ChildElements>
      		</ChildElement>
      """
    And I expect a decomposed file '<SecondFilePath><SecondFileName>.xml' with the following content:
      """
      <ChildElement type="<SecondType>" id="2">
      			<Name><SecondName></Name>
      			<ChildElements>
      				<xi:include href="<FourthFileName>.xml" />
      			</ChildElements>
      		</ChildElement>
      """
    And I expect a decomposed file '<FirstFilePath><ThirdFileName>.xml' with the following content:
      """
      <ChildElement id="3">
      					<Name>ThirdName</Name>
      				</ChildElement>
      """
    And I expect a decomposed file '<SecondFilePath><FourthFileName>.xml' with the following content:
      """
      <ChildElement id="4">
      					<Name>FourthName</Name>
      				</ChildElement>
      """

    Examples: 
      | Scenario                                                       | ConfigOverrideParent   | DecomposedFolder                                 | FirstType | FirstName | SecondType | SecondName | FirstFilePath                      | FirstFileName | SecondFilePath                       | SecondFileName | ThirdFileName           | FourthFileName           |
      | the same                                                       |                        | Decomposed                                       | SameType  | FirstName | SameType   | SecondName | ChildElements/SameType/FirstName/  | FirstName     | ChildElements/SameType/SecondName/   | SecondName     | ChildElements/ThirdName | ChildElements/FourthName |
      | different                                                      |                        | Decomposed                                       | FirstType | FirstName | SecondType | SecondName | ChildElements/FirstType/FirstName/ | FirstName     | ChildElements/SecondType/SecondName/ | SecondName     | ChildElements/ThirdName | ChildElements/FourthName |
      # The problem with this last scenario is that during decompose at the time the file name is derived it's not known yet whether the file will be written into a seperate folder if the current element contains childs.
      | the same and same filename                                     |                        | Decomposed                                       | SameType  | SameName  | SameType   | SameName   | ChildElements/SameType/SameName/   | SameName      | ChildElements/SameType/2/            |              2 | ChildElements/ThirdName | ChildElements/FourthName |
      | the same and same filename with relative backward slash folder |                        | Decomposed\\dummySubFolder\\..\\realTargetFolder | SameType  | SameName  | SameType   | SameName   | ChildElements/SameType/SameName/   | SameName      | ChildElements/SameType/2/            |              2 | ChildElements/ThirdName | ChildElements/FourthName |
      # Disabled the test below, since it fails in linux.
      #| the same and same filename with relative forward slash folder  | |Decomposed\\dummySubFolder/../realTargetFolder   | SameType  | SameName  | SameType   | SameName   | ChildElements/SameType/SameName/   | SameName      | ChildElements/SameType/2/            |              2 | ChildElements/ThirdName | ChildElements/FourthName |
      | the same dont override parent                                  | overrideParent="false" | Decomposed                                       | SameType  | FirstName | SameType   | SecondName | ChildElements/SameType/FirstName/  | FirstName     | ChildElements/SameType/SecondName/   | SecondName     | ChildElements/ThirdName | ChildElements/FourthName |
      | the same override parent                                       | overrideParent="true"  | Decomposed                                       | SameType  | FirstName | SameType   | SecondName | SameType/FirstName/                | FirstName     | SameType/SecondName/                 | SecondName     | ChildElements/ThirdName | ChildElements/FourthName |
