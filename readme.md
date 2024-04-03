# PowerDeComposer

This repository contains the source code for PowerDeComposer.
PowerDeComposer is a utility that can be used to manage PowerDesigner models with the version control system of your choice.
The utility can compose and decompose PowerDesigner models.


## Why PowerDeComposer

When multiple people work on the same PowerDesigner model, applying version control on the model as a whole leads to a lot of merge conflicts.
PowerDeComposer breaks a model down into files that contain only one table, data source etc which reduces the amount of potential merge conflicts.
To make the model usable in PowerDesigner again, PowerDeComposer can also compose the complete model back from the individual files that are in version control.


## How to get PowerDeComposer

PowerDeComposer is open source, released under the GPLv3 license. 
The source code and executable Jar file can be found in this repository and in the Maven public repository.
Information about PowerDeComposer can also be found on the [website](http://powerdecomposer.x-breeze.com/)


## PowerShell module

In order to make the usage of PowerDeComposer a bit simplier a PowerShell module with example decompose configurations and scripts are created. These can all be found [here](./PowerShell/).


## How to publish to sonatype nexus using Maven

In order to release PowerDeComposer into Maven Central we use a docker container and a local certificate. Follow the sections below to publish a new release.

### Setup PGP certificate locally

In order to sign code for nexus, you need to have PGP certificates locally and public key shared with a keyserver. Follow the steps on [this](https://central.sonatype.org/publish/requirements/gpg/) website to setup PGP and generate a key-pair.

### Configure maven

You don't need to have Maven installed locally, it will be ran via Docker. We do need a Maven settings file for the authentication details.
For this, create the file .m2/settings.xml with the following contents and replace the placeholders with your credentials:

```xml
<settings>
    <servers>
        <server>
            <id>ossrh</id>
            <username>{NEXUS/JIRA USERNAME}</username>
            <password>{NEXUS/JIRA PASSWORD}</password>
        </server>
        <server>
            <id>pgp</id>
            <passphrase>{PGP KEY PASSPHRASE}</passphrase>
        </server>
    </servers>
</settings>
```

### Publish the release

In order to publish the release, execute the following command in the root of the repository:

```powershell
./publish-to-maven.ps1
```
