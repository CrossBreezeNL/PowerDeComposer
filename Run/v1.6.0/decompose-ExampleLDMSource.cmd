@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G -Dfile.encoding=UTF-8 .\PowerDecomposer.jar

echo Decomposing ExampleSource...
echo.

REM Run XmlDecomposer
%PowerDeCompose% decompose ^
 "..\..\Output\ExampleLDMSource.ldm" ^
 "..\..\Output\ExampleLDMSource" 

echo.
pause