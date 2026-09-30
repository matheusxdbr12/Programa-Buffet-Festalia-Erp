# ==============================================================================
# Festalia ERP v2.0 - Publicacao no GitHub (@matheusxdbr12)
# ==============================================================================
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Set-Location -Path $PSScriptRoot

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "   FESTALIA ERP - PUBLICACAO NO GITHUB (@matheusxdbr12) " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$ghUsername = "matheusxdbr12"
$repoNome = "programa-buffet-festalia-erp"
$desc = "Festalia ERP - Sistema Integrado de Gestao para Buffets e Eventos com Gestao Financeira Avancada, Parcelamento, Recibos e Termos de Quitacao em PDF"

# 1. Configurar credenciais do Git usando o GitHub CLI
Write-Host "[1/5] Vinculando credenciais do GitHub CLI ao Git..." -ForegroundColor Yellow
gh auth setup-git
Write-Host "  -> Credenciais vinculadas com sucesso." -ForegroundColor Green

# 2. Configurar usuario/email do Git caso nao existam
Write-Host ""
Write-Host "[2/5] Verificando autor local do Git..." -ForegroundColor Yellow
$currentName = git config user.name
$currentEmail = git config user.email

if (-not $currentName) {
    git config --global user.name "Matheus De Souza"
    Write-Host "  -> Nome configurado: Matheus De Souza" -ForegroundColor Gray
}
if (-not $currentEmail) {
    git config --global user.email "matheusxp21@gmail.com"
    Write-Host "  -> Email configurado: matheusxp21@gmail.com" -ForegroundColor Gray
}
Write-Host "  -> Autor: $(git config user.name) <$(git config user.email)>" -ForegroundColor Green

# 3. Inicializar repositorio e commitar
Write-Host ""
Write-Host "[3/5] Preparando arquivos e commit..." -ForegroundColor Yellow
if (-not (Test-Path ".git")) {
    git init
    Write-Host "  -> Repositorio Git local inicializado." -ForegroundColor Gray
}
git branch -M main

Write-Host "  -> Adicionando todos os arquivos..." -ForegroundColor Gray
git add -A

$status = git status --porcelain
$hasCommits = git rev-parse --verify HEAD 2>$null

if ($status) {
    Write-Host "  -> Criando commit com Gestao Financeira Avancada..." -ForegroundColor Gray
    git commit -m "feat: Módulo de Gestão Financeira Avançada (Parcelamento, Vencimentos, Pagamentos Parciais, Recibos e Termo de Quitação em PDF)"
} elseif (-not $hasCommits) {
    git commit -m "feat: Módulo de Gestão Financeira Avançada (Parcelamento, Vencimentos, Pagamentos Parciais, Recibos e Termo de Quitação em PDF)" --allow-empty
} else {
    Write-Host "  -> Nenhuma alteracao pendente. Ultimo commit preservado." -ForegroundColor Gray
}

# 4. Criar ou sincronizar repositorio no GitHub
Write-Host ""
Write-Host "[4/5] Criando / Sincronizando com o GitHub..." -ForegroundColor Yellow

# Configura o remote origin
git remote remove origin 2>$null
git remote add origin "https://github.com/$ghUsername/$repoNome.git"

# Tenta criar o repositorio caso ainda nao exista
Write-Host "  -> Verificando criacao no GitHub..." -ForegroundColor Gray
gh repo create "$ghUsername/$repoNome" --public --description "$desc" --source=. --remote=origin --push 2>&1 | Out-String -Stream

# Se ja existia ou se falhou a criacao direta, garante o envio dos commits e atualizacao da descricao
Write-Host "  -> Sincronizando branch main e descricao..." -ForegroundColor Gray
gh repo edit "$ghUsername/$repoNome" --description "$desc" 2>$null
git push -u origin main

# 5. Status final
Write-Host ""
Write-Host "[5/5] Resumo da Publicacao:" -ForegroundColor Green
git status -s
git log -1 --oneline

Write-Host ""
Write-Host "========================================================" -ForegroundColor Green
Write-Host "   SUCESSO! O PROJETO FOI PUBLICADO NO GITHUB:         " -ForegroundColor Green
Write-Host "   https://github.com/$ghUsername/$repoNome             " -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Green
Write-Host ""
