@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G -Dfile.encoding=UTF-8 .\PowerDecomposer.jar

echo Composing ExampleSource...
echo.

REM Run XmlComposer
%PowerDeCompose% compose ^
 "..\..\Output\ExampleLDMSource\ExampleLDMSource.ldm" ^
 "..\..\Output\ExampleLDMSource.ldm"

echo.
pause