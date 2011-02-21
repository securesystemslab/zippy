set HotSpotMksHome=C:\cygwin\bin
set JAVA_HOME=%cd%\java64
set ORIG_PATH=%PATH%
set path=%JAVA_HOME%\bin;%path%;C:\cygwin\bin

set OrigPath=%cd%
cd make\windows
call create.bat %OrigPath%

set PATH=%ORIG_PATH%
cd %OrigPath%
pause
