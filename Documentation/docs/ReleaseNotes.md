# Release notes

Here you can find the release notes of all PowerDeComposer releases up till the release this documentation applies to.
Click on the header of a version number to go to the documentation of that specific version.

[//]: # (Use the following example to create the release notes for a new release.)
[//]: # ()
[//]: # (## Version X.X <sup>[docs](../X.X/)</sup>)
[//]: # ()
[//]: # (- [ ] x.x.x <sup>xx-xx-xxxx</sup>)
[//]: # (>)
[//]: # (> !!! success "New features")
[//]: # (>     * [ ] Template)
[//]: # (>         * [ ] XML Template)
[//]: # (>             - [X] Some new feature...)
[//]: # (> !!! success "Enhanced features")
[//]: # (>     * [ ] Model)
[//]: # (>         - [X] Some enhanced feature...)
[//]: # (> !!! warning "Bug fixes")
[//]: # (>     * [ ] Binding)
[//]: # (>         * [X] Some issue...)

## Version 1.4.0

- [ ] 1.4.0 <sup>30-03-2022</sup>
> !!! success "Enhanced features"
>     * [ ] Decompose model
>         - [X] Added option on TargetFolderName to overrideParent so the parent element name is overriden with the configuration, is stead of nesting the configured target folder name in a folder named to the parent element.
>     * [ ] PowerShell module
>         - [X] Added a PowerShell module to simplify PowerDeComposer execution.
> !!! warning "Bug fixes"
>     * [ ] Decompose model
>         - [X] Fixed case sensitivity issues with folders and files. All filepath compares are now case-insensitive.

## Version 1.3.3

- [ ] 1.3.3 <sup>22-03-2022</sup>
> !!! warning "Bug fixes"
>     * [ ] Decompose model
>         - [X] Fixed error in NodeRemoval on processing-instruction attribute.
>         - [X] Fixed error in deriving folder and file names based on TargetFolderName and TargetFileName configuration.

## Version 1.3.2

- [ ] 1.3.2 <sup>20-10-2021</sup>
> !!! success "Enhanced features"
>     * [ ] Decompose model
>         - [X] Moved node removal to be first step, before identifier replacement to solve possible duplicate identifier replacement issues.

## Version 1.3.1

- [ ] 1.3.1 <sup>12-10-2021</sup>
> !!! warning "Bug fixes"
>     * [ ] Decompose model
>         - [X] Fixed problem where TargetFileName was not always derived correctly.

## Version 1.3.0

- [ ] 1.3.0 <sup>13-04-2021</sup>
> !!! success "Enhanced features"
>     * [ ] Decompose model
>         - [X] Added support for specifying the target folder.
>         - [X] Changed target folder and file name so one can specify multiple options and use first with a valid value.
>         - [X] Added functionality to remove former decomposed files which aren't part of the latest decomposed model.
>         - [X] Added support for running a decompose without decomposable element conditions.
>         - [X] Changed identifier replacement in the config to be in a collection so one can define multiple replacements.
>         - [X] Added change detection logic and logging.
>         - [X] Added throwing error when decomposing leads to writing the same file.
>         - [X] Added throwing error when identifier replacement values leads to a duplicate.
>     * [ ] Configuration
>         - [X] Updated default configuration so the decomposed folder structure and file names are more readable.

## Version 1.2.1

- [ ] 1.2.1 <sup>25-11-2020</sup>
> !!! success "Enhanced features"
>     * [ ] Decompose model
>         - [X] Fixed encoding of decomposed files so it always takes encoding of original file.

## Version 1.2.0

- [ ] 1.2.0 <sup>14-10-2020</sup>
> !!! success "Enhanced features"
>     * [ ] Decompose model
>         - [X] Specify node to use for the target file names.
>         - [X] Specify condition for which nodes to decompose.
>         - [X] Specify attributes to add on the xi:include elements.
>         - [X] Added support for removing elements and attribute while decomposing.
>     * [ ] Configuration
>         - [X] Fully refactored config so decomposing can be configured in more detail. (See [Configuration](Configuration.md))

## Version 1.1.2

- [ ] 1.1.2 <sup>25-08-2020</sup>
> !!! success "Enhanced features"
>     * [ ] Configuration
>         - [X] Added configuration of decompose using config file. (See [Configuration](Configuration.md))

## Version 1.1.1

- [ ] 1.1.1 <sup>22-05-2020</sup>
> !!! warning "Bug fixes"
>     * [ ] Decompose model
>         - [X] Fixed encoding of file so special characters are handled correctly.
>     * [ ] Compose model
>         - [X] Fixed encoding of file so special characters are handled correctly.

## Version 1.1

- [ ] 1.1 <sup>18-04-2020</sup>
> !!! success "Enhanced features"
>     * [ ] Decompose model
>         - [X] Decompose entities and relationships of logical models into separate files
>     * [ ] Documentation
>         - [X] Added Maven run example to getting started
>
> !!! warning "Bug fixes"
>     * [ ] Documentation and downloads
>         - [X] Fixed jar in Maven repository so it is executable        
>         - [X] Added standalone download

## Version 1.0

- [ ] 1.0 <sup>06-03-2020</sup>
> Initial release
> !!! success "New features"
>     * [ ] Decompose model
>         - [X] Decompose each element with an ObjectID into its own file.
>         - [X] Replace local ids (id="o#") with their ObjectID (also in references).
>     * [ ] Compose model
>         * [X] Compose a PowerDesigner model file from the decomposed files and folders.