set HotSpotMksHome=C:\cygwin\bin
set JAVA_HOME=%cd%\java
set ORIG_PATH=%PATH%
set path=%JAVA_HOME%\bin;%path%;C:\cygwin\bin

set OrigPath=%cd%
cd make\windows

mkdir %OrigPath%\work
call create.bat %OrigPath%

set PATH=%ORIG_PATH%
cd %OrigPath%
pause
