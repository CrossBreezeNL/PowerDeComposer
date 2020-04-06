@echo off

REM The following command will build the HTML version of the documentation based on the current docs files.
REM The output will be written in the 'site' folder.
mkdocs build --clean
