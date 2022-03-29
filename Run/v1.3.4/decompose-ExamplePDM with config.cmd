@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G .\PowerDecomposer.jar

echo Decomposing ExampleModel...
echo.

REM Run XmlDecomposer
%PowerDeCompose% decompose ^
 "..\..\Output\ExamplePDM_original.pdm" ^
 "..\..\Output\ExamplePDM" ^
 "..\..\Config\ExampleConfig.xml"

echo.
pause