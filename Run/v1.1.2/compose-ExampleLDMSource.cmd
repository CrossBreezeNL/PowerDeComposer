@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G .\PowerDecomposer.jar

echo Composing ExampleSource...
echo.

REM Run XmlComposer
%PowerDeCompose% compose ^
 "..\..\Output\ExampleLDMSource\ExampleLDMSource.ldm" ^
 "..\..\Output\ExampleLDMSource.ldm"

echo.
pause