@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G .\PowerDecomposer.jar

echo Decomposing ExampleModel...
echo.

REM Run XmlDecomposer
%PowerDeCompose% decompose ^
 "..\Output\ExampleModel_original.pdm" ^
 "..\Output\ExampleModel" 

echo.
pause