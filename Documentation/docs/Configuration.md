# Configuration for PowerDeComposer

## PowerDeComposerConfig
The PowerDeComposer configuration for composing and decomposing model files.

### Example
```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>

	<Decompose>

		<NodeRemovals>
			<NodeRemoval xpath="..." />
			<NodeRemoval xpath="..." />
			...
		</NodeRemovals>

		<DecomposableElement>
			<ElementCondition xpath="..." />
			<ElementCondition xpath="..." />
			...

			<OneOffConditions>
				...
			</OneOffConditions>

			<AllConditions>
				...
			</AllConditions>

			<!-- Here we specify which information to include as an attribute on the xi:include tag. -->
			<IncludeAttributes>
				<IncludeAttribute name="..." xpath="..." />
			</IncludeAttributes>

			<!-- Here we specify the xpath to execute on a decomposable element to get the file name (without .xml). -->
			<TargetFileName xpath="..." />

			<!-- Here we specify the xpath to execute on a decomposable element to get the folder name to store the file in. -->
			<TargetFolderName xpath="..." />

		</DecomposableElement>

	</Decompose>

</PowerDeComposerConfig>
```

### Config elements
| Config element           | Description |
|:---                      |:---         |
| NodeRemoval              | Instruction to remove nodes before decomposing. The XPath is evaluated to find the nodes to remove. An example for such an instruction is to remove the modification date on referenced objects. |
| ElementCondition         | A condition for an element which need to be extracted into seperate files (XPath expression per node). All conditions together inside the DecomposableElement element are evaluated against a model element to decide whether it needs to be decomposed. Element conditions can be grouped in OneOffConditions or AllConditions elements to specify whether one or all conditions should be met within the container.  |
| IncludeAttribute         | Instruction to include a certain attribute in the xi:include tag. The name is the name for the xi:include attribute. The xpath expressions will be evaluated to get the value for the new attribute. The xpath can be a simple node selection like "./ObjectID", or it can also contain XPath 1.0 functions like "concat('ID=', ./ObjectID)". |
| TargetFileName           | The node contents to use as the file name (without extension) for the extracted elements. For PowerDesigner objects this can for example be ./ObjectID, ./Code or ./Name. The xpath can be a simple node selection like "./ObjectID", or it can also contain XPath 1.0 functions like "concat(./ObjectID, '_', ./Code)". |
| TargetFolderName         | The node contents to use as the folder name for the extracted elements. For PowerDesigner objects this can for example be ./Stereotype. The xpath can be a simple node selection like "./Stereotype", or it can also contain XPath 1.0 functions like "concat(./name(), '_', ./Stereotype)". |

### Default config
When no configuration file is specified on the command, the following configuration will be used by default.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>
	<Decompose>
	
		<IdentifierReplacement identifierNodeXPath="//*/@Id[../ObjectID]" replacementValueXPath="../ObjectID" referencingNodeXPath="//*/@Ref" />
	
		<!-- Specify the nodes to remove before decomposing. -->
		<NodeRemovals>
			<!-- Remove the object count on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@Objects" />
			<!-- Remove the symbols count on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@Symbols" />
			<!--  Remove the last modification date on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@LastModificationDate" />
			
			<!-- Remove the CreationDate, Creator, ModificationDate & Modifier from all Shortcuts. -->
			<NodeRemoval xpath="//Shortcut/CreationDate" />
			<NodeRemoval xpath="//Shortcut/Creator" />
			<NodeRemoval xpath="//Shortcut/ModificationDate" />
			<NodeRemoval xpath="//Shortcut/Modifier" />
			
			<!-- Remove the TargetModelLastModificationDate from all TargetModels. -->
			<NodeRemoval xpath="//TargetModel/TargetModelLastModificationDate" />
		</NodeRemovals>
		
		<!-- Specify which elements should be decomposed. -->
		<!-- The DecomposableElement is implicitly a AllConditions group. -->
		<DecomposableElement>
			<!-- The element must have a ObjectID and a Code element as childs. -->
			<ElementCondition xpath="./ObjectID and ./Code" />
			<!-- Use the parent axis on the current node to set a predicate on the parent. -->
			<!-- Decompose all elements where the parent name starts with 'c:' (these are the collections in PowerDesigner). -->
			<ElementCondition xpath="parent::node()[starts-with(name(), 'c:')]" />
			<!-- Don't decompose the Children collection (this is the higher level collection of the model). -->
			<ElementCondition xpath="parent::node()[name() != 'c:Children']" />
			<!-- LDM collections not to decompose. -->
			<ElementCondition xpath="parent::node()[name() != 'c:Attributes']" />
			<ElementCondition xpath="parent::node()[name() != 'c:Identifiers']" />
			<!-- PDM collections not to decompose. -->
			<ElementCondition xpath="parent::node()[name() != 'c:Columns']" />
			<ElementCondition xpath="parent::node()[name() != 'c:Keys']" />
			<!-- Don't decompose sub shortcuts. -->
			<ElementCondition xpath="parent::node()[name() != 'c:SubShortcuts']" />
			
			<!-- Example for comparing on the element name. -->
			<!-- <ElementCondition xpath="name() = 'o:Entity'" /> -->
			<!-- Example for using starts-with with the name() function. -->
			<!-- <ElementCondition xpath="starts-with(name(), 'o:')" /> -->
			
			<!-- You can nest conditions groups (OneOfConditions and AllConditions). See the example below. -->
			<!--
			<OneOffConditions>
				<ElementCondition xpath="name() = 'SomeElementName'" />
				<ElementCondition xpath="name() = 'SomeOtherElementName'" />
				<AllConditions>
					<ElementCondition xpath="parent::node()[name() = 'c:Entities'" />
					<ElementCondition xpath="starts-with(./Code, 'ENT')" />
				</AllConditions>
			</OneOffConditions>
			 -->
			 
			<!-- Here we specify which information to include as an attribute on the xi:include tag. -->
			<IncludeAttributes />
			<!--<IncludeAttributes>
				<IncludeAttribute name="ObjectID" xpath="./ObjectID" />
			</IncludeAttributes>-->
			
			<!-- Here we specify the xpath to execute on a decomposable element to get the file name (without .xml). -->
			<!-- For items with a Code element and it is unique in its scope, we use the Code. -->
			<!-- For items with a Code and ObjectID element and it is NOT unique in its scope, we use the Code and ObjectID. -->
            <!-- For items without a Code element, but with an ObjectID we use the ObjectID. -->
            <!-- For items which don't have a Code and ObjectID element, we use the name of the element. -->
            <TargetFileName xpath="concat(
                    substring(translate(normalize-space(./Code), ' ', '_'),                          1, count(./Code) * number(not(./Code = preceding-sibling::*/child::Code)) * string-length(translate(normalize-space(./Code), ' ', '_'))),
                    substring(concat(translate(normalize-space(./Code), ' ', '_'), '_', ./ObjectID), 1, count(./Code) * number(./Code = preceding-sibling::*/child::Code) * string-length(concat(translate(normalize-space(./Code), ' ', '_'), '_', ./ObjectID))),
                    substring(./ObjectID,                                                            1, abs(count(./Code) - 1) * count(./ObjectID) * string-length(./ObjectID)),
                    substring(translate(name(), ':', '_'),                                           1, abs(count(./Code) - 1) * abs(count(./ObjectID) - 1) * string-length(name()))
                )" />
                
			<!-- Here we specify the xpath to execute on a decomposable element to get the folder name to store the file in. -->
            <!-- For items with a Stereotype element, we use the Stereotype. -->
            <!-- For items with a TargetStereotype element, we use the TargetStereotype (the TargetStereotype is specified in case of shortcuts). -->
            <!-- For items which don't have a Stereotype or TargetStereotype element, we use the name of the element. -->
            <TargetFolderName xpath="concat(
                    substring(./Stereotype,                1, count(./Stereotype[text()]) * string-length(./Stereotype)),
                    substring(./TargetStereotype,          1, abs(count(./Stereotype[text()]) - 1) * count(./TargetStereotype[text()]) * string-length(./Stereotype)),
                    substring(translate(name(), ':', '_'), 1, abs(count(./Stereotype[text()]) - 1) * abs(count(./TargetStereotype[text()]) - 1) * string-length(name()))
                )" />
		</DecomposableElement>

	</Decompose>
</PowerDeComposerConfig>
```