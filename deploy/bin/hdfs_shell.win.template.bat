@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..
set LIB_DIR=%APP_HOME%/lib

echo Launching HDFS Shell with HADOOP_CONF_DIR: %HADOOP_DIR%
java.exe -Xms200m -Xmx400m -cp "%LIB_DIR%/*;%HADOOP_DIR%:%HADOOP_CLASSPATH%" ${mainClassName} %CMD_LINE_ARGS%
if "%OS%"=="Windows_NT" endlocal