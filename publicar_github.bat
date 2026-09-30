@echo off
setlocal EnableExtensions
title Festalia ERP - Publicacao GitHub (@matheusxdbr12)
cd /d "%~dp0"

echo ========================================================
echo   FESTALIA ERP - PUBLICACAO GITHUB (@matheusxdbr12)
echo ========================================================
echo.

where powershell.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Executando rotina automatica via PowerShell...
    powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0publicar_github.ps1"
    if %ERRORLEVEL% EQU 0 goto :fim
)

echo.
echo [INFO] Executando via CMD direto...
echo.

echo [1/5] Vinculando credenciais GitHub CLI...
gh auth setup-git

echo [2/5] Configurando autor Git...
git config --global user.name "Matheus De Souza"
git config --global user.email "15384914+matheusxdbr12@users.noreply.github.com"

echo [3/5] Inicializando repositorio e commit...
if not exist ".git" git init
git branch -M main
git add -A
git commit -m "fix(contrato): v2.1.7 - Correção de flush e map nulo no iText 7 para geração de PDF de contratos"

echo [4/5] Publicando no GitHub...
git remote remove origin 2>nul
git remote add origin https://github.com/matheusxdbr12/programa-buffet-festalia-erp.git
gh repo create matheusxdbr12/programa-buffet-festalia-erp --public --description "Festalia ERP - Sistema Integrado de Gestao para Buffets e Eventos com Emissao de OS em PDF e Marca D'Agua customizavel" --source=. --remote=origin --push
gh repo edit matheusxdbr12/programa-buffet-festalia-erp --description "Festalia ERP - Sistema Integrado de Gestao para Buffets e Eventos com Emissao de OS em PDF e Marca D'Agua customizavel" 2>nul
git push -u origin main

echo.
echo [5/5] Resumo:
git status -s
git log -1 --oneline

:fim
echo.
echo ========================================================
echo   PROCESSO FINALIZADO!
echo   https://github.com/matheusxdbr12/programa-buffet-festalia-erp
echo ========================================================
echo.
pause
