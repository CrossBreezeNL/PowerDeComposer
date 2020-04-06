# Getting started
On this wiki you will find all needed documentation to get started with PowerDeComposer.

[//]: #(test met comment)

## How to get PowerDeComposer
PowerDeComposer is available as a [Maven package](https://mvnrepository.com/artifact/com.x-breeze.powerdecomposer). You can either download the latest jar from the Maven repository, or create a Maven pom.xml file to let the Maven utility download the latest jar.

## How to use PowerDeComposer
In order to use PowerDeComposer you need to call the utility using the command line.

### Decompose a model
Example command line call to decompose a PowerDesigner model file into a decomposed model:

``` xml
java -jar XmlDeComposer.jar decompose "C:\Path\To\ExamplePowerDesignerModelFile.ldm" "C:\Path\To\Decomposed\Output"
```

### Compose a model
Example command line call to compose a PowerDesigner model file based on the decomposed model:

``` xml
java -jar XmlDeComposer.jar compose "C:\Path\To\Decomposed\Output" "C:\Path\To\ExamplePowerDesignerModelFile.ldm"
```

## Bugs & issues
When you encounter an issue while using PowerDeComposer please report it by sending an e-mail to [info@x-breeze.com](mailto:info@x-breeze.com?SUBJECT=PowerDeComposer%20-%20Bug%20report) with the subject 'PowerDeComposer - Bug report'.

Please provide the following information:

- Steps to reproduce
- Expected behaviour
- PowerDeComposer version used
