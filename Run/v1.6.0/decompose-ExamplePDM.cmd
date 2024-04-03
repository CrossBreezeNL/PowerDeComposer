@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G -Dfile.encoding=UTF-8 .\PowerDecomposer.jar

echo Decomposing ExampleModel...
echo.

REM Run XmlDecomposer
%PowerDeCompose% decompose ^
 "..\..\Output\ExamplePDM_original.pdm" ^
 "..\..\Output\ExamplePDM" 

echo.
pause