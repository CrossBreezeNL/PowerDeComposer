# PowerDeComposer
This repository contains the source code for PowerDeComposer.
PowerDeComposer is a utility that can be used to manage PowerDesigner models with the version control system of your choice.
The utility can compose and decompose PowerDesigner models.

## Why PowerDeComposer
When multiple people work on the same PowerDesigner model, applying version control on the model as a whole leads to a lot of merge conflicts.
PowerDeComposer breaks a model down into files that contain only one table, data source etc which reduces the amount of potential merge conflicts.
To make the model usable in PowerDesigner again, PowerDeCompose can also compose the complete model back from the individual files that are in version control.

## How to get PowerDeComposer
PowerDeComposer is open source, released under the GPLv3 license. 
The source code and executable Jar file can be found in this repository and in the Maven public repository.
Information about PowerDeComposer can also be found on the [website](http://powerdecomposer.x-breeze.com/)


# How to publish to sonatype nexus using Maven

For this we use a docker image for maven
- https://hub.docker.com/_/maven
    -> docker pull maven