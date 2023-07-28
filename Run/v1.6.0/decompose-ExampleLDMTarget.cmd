@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G -Dfile.encoding=UTF-8 .\PowerDecomposer.jar

echo Decomposing ExampleTarget...
echo.

REM Run XmlDecomposer
%PowerDeCompose% decompose ^
 "..\..\Output\ExampleLDMTarget.ldm" ^
 "..\..\Output\ExampleLDMTarget" 

echo.
pause