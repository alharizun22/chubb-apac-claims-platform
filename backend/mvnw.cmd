@echo off
setlocal
if not defined JAVA_HOME (
    set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.11"
)
set "MAVEN_DIR=%~dp0.tools\apache-maven-3.9.6\bin"
if exist "%MAVEN_DIR%\mvn.cmd" (
    call "%MAVEN_DIR%\mvn.cmd" %*
) else (
    call mvn %*
)
