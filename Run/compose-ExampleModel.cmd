@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G .\PowerDecomposer.jar

echo Composing ExampleModel...
echo.

REM Run XmlComposer
%PowerDeCompose% compose ^
 "..\Output\ExampleModel\ExampleModel_original.pdm" ^
 "..\Output\ExampleModel_Full.pdm"

echo.
pause