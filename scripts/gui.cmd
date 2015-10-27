@echo off

for /f %%j in ("java.exe") do (
    set JAVA_HOME=%%~dp$PATH:j
)

if %JAVA_HOME%.==. (
    @echo java.exe not found
) else (
    @echo JAVA_HOME = %JAVA_HOME%
)

java -cp "..\lib\*" application.Main