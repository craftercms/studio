@echo off
setlocal
if "%JAVA_HOME%"=="" goto NoJavaHome
if not exist %JAVA_HOME%\bin\java.exe goto InvalidJavaHome
goto StartServer

:NoJavaHome
echo.
echo Error: JAVA_HOME environment variable is not set.
goto End

:InvalidJavaHome
echo.
echo Error: JAVA_HOME '%JAVA_HOME%' does not contain a valid Java installation.
goto End

:StartServer
echo .
echo ======================================
echo = Stop CStudio Publishing Receiver   =
echo ======================================
echo .

"%JAVA_HOME%\bin\java" -Djava.ext.dirs=. org.craftercms.cstudio.publishing.StopServiceMain

:End
endlocal