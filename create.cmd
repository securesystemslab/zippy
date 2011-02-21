set HotSpotMksHome=C:\Cygwin\bin
set path=%JAVA_HOME%\bin;C:\Cygwin\bin
call "%VS_VCVARS%\vsvars32.bat"

set OrigPath=%cd%
cd make\windows
call create.bat %OrigPath%

cd %OrigPath%
pause
