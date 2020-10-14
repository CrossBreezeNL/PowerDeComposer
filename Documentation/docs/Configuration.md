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
| TargetFileName           | The node contents to use as the filename (without extension) for the extracted elements. For PowerDesigner objects this can for examle be ./ObjectID, ./Code or ./Name. The xpath can be a simple node selection like "./ObjectID", or it can also contain XPath 1.0 functions like "concat(./ObjectID, '_', ./Code)". |

### Default config
When no configuration file is specified on the command, the following configuration will be used by default.

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<PowerDeComposerConfig>
	<Decompose>
	
		<!-- Specify the nodes to remove before decomposing. -->
		<NodeRemovals>
			<!-- Remove the object count on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@Objects" />
			<!-- Remove the symbols count on the PowerDesigner processing instruction. -->
			<NodeRemoval xpath="/processing-instruction('PowerDesigner')/@Symbols" />
			
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
			<IncludeAttributes>
				<IncludeAttribute name="ObjectID" xpath="./ObjectID" />
			</IncludeAttributes>
			<!-- Here we specify the xpath to execute on a decomposable element to get the file name (without .xml). -->
			<TargetFileName xpath="./Code" />
		</DecomposableElement>

	</Decompose>
</PowerDeComposerConfig>
```