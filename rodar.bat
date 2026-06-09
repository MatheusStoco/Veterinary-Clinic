@echo off
title Clinica Veterinaria

echo Iniciando Clinica Veterinaria...
echo.

:: Java 11
set JAVA_HOME=C:\Program Files\Microsoft\jdk-11.0.31.11-hotspot
set PATH=%JAVA_HOME%\bin;C:\maven\bin;%PATH%

:: Verifica se PostgreSQL 17 esta rodando
sc query postgresql-x64-17 | find "RUNNING" >nul
if %errorlevel% neq 0 (
    echo Iniciando banco de dados...
    net start postgresql-x64-17
    timeout /t 3 /nobreak >nul
)

:: Roda o projeto
cd /d "%~dp0form"
mvn javafx:run

pause
