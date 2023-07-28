@echo off
set PowerDeCompose=java -jar -Xms64M -Xmx2G -Dfile.encoding=UTF-8 .\PowerDecomposer.jar

echo Composing ExampleTarget...
echo.

REM Run XmlComposer
%PowerDeCompose% compose ^
 "..\..\Output\ExampleLDMTarget\ExampleLDMTarget.ldm" ^
 "..\..\Output\ExampleLDMTarget.ldm"

echo.
pause