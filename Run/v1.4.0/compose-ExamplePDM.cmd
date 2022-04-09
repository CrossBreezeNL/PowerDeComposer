@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G .\PowerDecomposer.jar

echo Composing ExamplePDM...
echo.

REM Run XmlComposer
%PowerDeCompose% compose ^
 "..\..\Output\ExamplePDM\ExamplePDM_original.pdm" ^
 "..\..\Output\ExamplePDM_Full.pdm"

echo.
pause