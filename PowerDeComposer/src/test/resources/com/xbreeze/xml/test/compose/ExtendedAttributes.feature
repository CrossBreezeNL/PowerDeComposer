@Unit
Feature: Compose Extended Attributes
  Here we test the compose of extended attributes. During decomposing these extended attributes are formalized toe XML elements, which we need to reverse during composing.

  Scenario: Compose with extended attributes
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      <ExtendedAttributes>
      <OriginatingExtension ObjectID="4202E4F4-4187-47CE-83BE-51088F229451" Name="TestExtension">
      <ExtendedAttribute ObjectID="DA1CC8BE-C80A-4B74-BB79-10F50BE06CBC" Name="TestBooleanField">true</ExtendedAttribute>
      <ExtendedAttribute ObjectID="18ECABD7-88E7-4D0E-8107-0382CDF1E4D3" Name="TestTextField">This is test text
      with NewLine.</ExtendedAttribute>
      </OriginatingExtension>
      <OriginatingExtension ObjectID="2ABE46A1-ED92-45C8-B191-7C85DD336346" Name="AnotherTestExtension">
      <ExtendedAttribute ObjectID="A2A57166-AB6A-4776-867B-95E7775EFC9F" Name="SecondTestFromAnotherExtension">23</ExtendedAttribute>
      <ExtendedAttribute ObjectID="49F389E0-A1B2-4FFB-BEF1-57FA2A8EBA45" Name="TestFromAnotherExtension">4/24/2023</ExtendedAttribute>
      <ExtendedAttribute ObjectID="C1179E53-39F4-461A-9349-EFF754344DD5" Name="ThirdTestField">{C1179E53-39F4-461A-9349-EFF754344DD5},ThirdTestField,76={49F389E0-A1B2-4FFB-BEF1-57FA2A8EBA45},TestFromAnotherExtension, 9=test text</ExtendedAttribute>
      </OriginatingExtension>
      <OriginatingExtension ObjectID="8D660A8B-DD11-4310-A56E-DA20411AD4A3" Name="LocalExtension">
      <ExtendedAttribute ObjectID="95993098-3FA0-4867-AE7B-29EA684DE890" Name="TestFieldFromLocal">LocalTestValue</ExtendedAttribute>
      </OriginatingExtension>
      </ExtendedAttributes>
      </RootElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      <a:ExtendedAttributesText>{4202E4F4-4187-47CE-83BE-51088F229451},TestExtension,154={DA1CC8BE-C80A-4B74-BB79-10F50BE06CBC},TestBooleanField,4=true
      {18ECABD7-88E7-4D0E-8107-0382CDF1E4D3},TestTextField,32=This is test text
      with NewLine.
      
      {2ABE46A1-ED92-45C8-B191-7C85DD336346},AnotherTestExtension,346={A2A57166-AB6A-4776-867B-95E7775EFC9F},SecondTestFromAnotherExtension,2=23
      {49F389E0-A1B2-4FFB-BEF1-57FA2A8EBA45},TestFromAnotherExtension,9=4/24/2023
      {C1179E53-39F4-461A-9349-EFF754344DD5},ThirdTestField,133={C1179E53-39F4-461A-9349-EFF754344DD5},ThirdTestField,76={49F389E0-A1B2-4FFB-BEF1-57FA2A8EBA45},TestFromAnotherExtension, 9=test text
      
      {8D660A8B-DD11-4310-A56E-DA20411AD4A3},LocalExtension,77={95993098-3FA0-4867-AE7B-29EA684DE890},TestFieldFromLocal,14=LocalTestValue
      
      </a:ExtendedAttributesText>
      </RootElement>
      """
      
  Scenario: Compose extended attributes in child element
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      <ChildElement>
      <ExtendedAttributes>
      <OriginatingExtension ObjectID="4202E4F4-4187-47CE-83BE-51088F229451" Name="TestExtension">
      <ExtendedAttribute ObjectID="DA1CC8BE-C80A-4B74-BB79-10F50BE06CBC" Name="TestBooleanField">true</ExtendedAttribute>
      </OriginatingExtension>
      </ExtendedAttributes>
      </ChildElement>
      </RootElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      <ChildElement>
      <a:ExtendedAttributesText>{4202E4F4-4187-47CE-83BE-51088F229451},TestExtension,64={DA1CC8BE-C80A-4B74-BB79-10F50BE06CBC},TestBooleanField,4=true
      
      </a:ExtendedAttributesText>
      </ChildElement>
      </RootElement>
      """
  
  Scenario: Compose extended attribute with special characters
    Given the decomposed file:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      <ChildElement>
      <ExtendedAttributes>
      <OriginatingExtension ObjectID="4202E4F4-4187-47CE-83BE-51088F229451" Name="TestExtension">
      <ExtendedAttribute ObjectID="38253E88-8698-4A5B-8398-0FA2B14556C0" Name="SqlExpression">-- Special characters: &lt;, &gt;, &amp;, &apos;, &quot;, ´, ~, !
      @AMOUNT &gt; 100</ExtendedAttribute>
      </OriginatingExtension>
      </ExtendedAttributes>
      </ChildElement>
      </RootElement>
      """
    When I perform a compose
    Then I expect a composed file with the following content:
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <RootElement>
      <ChildElement>
      <a:ExtendedAttributesText>{4202E4F4-4187-47CE-83BE-51088F229451},TestExtension,118={38253E88-8698-4A5B-8398-0FA2B14556C0},SqlExpression,60=-- Special characters: &lt;, &gt;, &amp;, &apos;, &quot;, ´, ~, !
      @AMOUNT &gt; 100
      
      </a:ExtendedAttributesText>
      </ChildElement>
      </RootElement>
      """