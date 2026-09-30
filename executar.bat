@echo off
title Festalia ERP v2.0
cd /d "%~dp0"

echo [1/2] Sincronizando logo oficial Festalia ERP...
if exist "C:\Users\matheus.souza\.gemini\antigravity-ide\brain\6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61\.user_uploaded\media_1790699283882.jpg" (
    copy /y "C:\Users\matheus.souza\.gemini\antigravity-ide\brain\6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61\.user_uploaded\media_1790699283882.jpg" "src\main\resources\festalia_logo.jpg" >nul 2>&1
    copy /y "C:\Users\matheus.souza\.gemini\antigravity-ide\brain\6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61\.user_uploaded\media_1790699283882.jpg" "festalia_logo.jpg" >nul 2>&1
    copy /y "C:\Users\matheus.souza\.gemini\antigravity-ide\brain\6dae6d28-dee6-46ad-97ac-f0cbf5f1cd61\.user_uploaded\media_1790699283882.jpg" "target\classes\festalia_logo.jpg" >nul 2>&1
)

echo [2/2] Iniciando Festalia ERP...
mvn compile exec:java
if %ERRORLEVEL% NEQ 0 (
    if exist "target\festalia-erp-1.0.0-jar-with-dependencies.jar" (
        start "" javaw -jar "target\festalia-erp-1.0.0-jar-with-dependencies.jar"
    )
)
