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
[//]: # (> !!! "Enhanced features")
[//]: # (>     * [ ] Model)
[//]: # (>         - [X] Some enhanced feature...)
[//]: # (> !!! warning "Bug fixes")
[//]: # (>     * [ ] Binding)
[//]: # (>         * [X] Some issue...)

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