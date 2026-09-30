# 🧠 Sistema de Memória e Versionamento de Ações — Festalia ERP

Bem-vindo ao repositório de **Memória e Rastreabilidade do Festalia ERP**.  
Esta pasta (`memoria_versoes/`) foi concebida para documentar permanentemente **toda ação, atualização, melhoria, correção (fix) ou adição** realizada no projeto.

---

## 📌 1. Objetivo da Memória de Versões

1. **Rastreabilidade Contínua:** Saber com exatidão o que foi alterado, quando, por quem e qual o impacto no código e no banco de dados.
2. **Histórico de Decisões:** Manter o raciocínio arquitetural, regras de negócio e motivações por trás de cada mudança.
3. **Auditoria e Segurança:** Permitir rápida identificação e reversão em caso de regressões ou novos bugs.
4. **Sincronia com o Repositório:** Acompanhar cada versão de release e commits do GitHub com notas técnicas detalhadas.

---

## 🏷️ 2. Padrão de Versionamento (Semantic Versioning - SemVer)

O Festalia ERP adota o padrão `vMAJOR.MINOR.PATCH`:

* **MAJOR (ex: v3.0.0):** Mudanças estruturais de grande escala, refatoração de arquitetura de banco de dados ou redefinição geral da interface.
* **MINOR (ex: v2.1.0):** Adição de novos módulos completos (ex: Módulo de Contratos, Módulo Financeiro Parcelado, Calendário Visual).
* **PATCH (ex: v2.1.1):** Correções de bugs, pequenas melhorias visuais, ajustes de desempenho ou hotfixes.

---

## 📝 3. Nomenclatura dos Arquivos de Memória

Cada ciclo de ação ou atualização deve gerar um arquivo Markdown nesta pasta seguindo o padrão:

```text
memoria_versoes/
├── README.md                           <-- Este guia orientador
├── v2.1.0_modulo_contratos_fix.md      <-- Registro da versão/ação realizada
├── v2.1.1_ajustes_painel_contratos.md  <-- Próximo registro
└── ...
```

---

## 🔖 4. Categorias Obrigatórias em Cada Registro

Ao criar um novo arquivo de memória, utilize as tags padronizadas:

* `[ADICAO]` — Novas funcionalidades, telas, métodos, classes ou tabelas no banco de dados.
* `[FIX]` — Correções de bugs de compilação, regras de negócio quebradas, visual ou exceções em tempo de execução.
* `[ATUALIZACAO]` — Refatorações, melhorias de desempenho, modernização de UI/UX ou upgrades de bibliotecas.
* `[LOG]` — Registros técnicos, execuções de compilação, migrações de dados e status do build.
* `[SEGURANCA]` — Proteção de integridade referencial, sanitização de inputs ou isolamento de transações.

---

## 📋 5. Template para Novos Registros

Para criar uma nova memória de versão, copie o modelo abaixo:

```markdown
# 📦 Registro de Versão: vX.Y.Z — [Nome Resumido da Ação]

- **Data/Hora:** YYYY-MM-DD HH:mm:ss
- **Autor / Responsável:** Matheus De Souza / Antigravity AI
- **Versão:** vX.Y.Z
- **Status:** [Em Desenvolvimento | Homologado | Em Produção]
- **Módulos Afetados:** [Contratos | Financeiro | OS | Calendário | UI]

---

### 1. 🎯 Objetivo da Ação
[Breve descrição do que motivou esta alteração e o objetivo atingido]

---

### 2. 🚀 [ADICAO] Novas Funcionalidades e Recursos
- Item 1...
- Item 2...

---

### 3. 🛠️ [FIX] Correções Realizadas
- Item 1...
- Item 2...

---

### 4. 🔄 [ATUALIZACAO] Refatorações e Melhorias
- Item 1...
- Item 2...

---

### 5. 📑 [LOG] Logs Técnicos e Registro de Compilação
- Detalhes do build, classes modificadas, scripts SQL executados...

---

### 6. 🔍 Verificação e Testes
- [ ] Compilação limpa (`mvn compile`)
- [ ] Execução da aplicação (`executar.bat`)
- [ ] Testes automatizados executados
- [ ] Commit e Push realizados no GitHub
```

---

## 🔗 6. Histórico de Versões Registradas

| Versão | Data | Principais Entregas | Arquivo |
|---|---|---|---|
| **v2.1.0** | 30/09/2026 | Módulo de Contratos de Prestação de Serviços, Minutas em PDF (iText 7), Resolução de 79 erros de compilação | [v2.1.0_modulo_contratos_fix.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.0_modulo_contratos_fix.md) |
| **v2.1.1** | 30/09/2026 | Ajustes de Fachada e Utilitários de Contrato (eliminação de colisão de métodos em ContratoDAO, alias getErros, sobrecargas de extenso) | [v2.1.1_ajustes_contrato_utilitarios.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.1_ajustes_contrato_utilitarios.md) |
| **v2.1.2** | 30/09/2026 | Padronização Visual: Eliminação de Tofu (quadrados pretos) com ícones vetoriais IconesFestalia (balança, cliente, calendário, pdf) | [v2.1.2_padronizacao_icones_vetoriais.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.2_padronizacao_icones_vetoriais.md) |
| **v2.1.3** | 30/09/2026 | Harmonização dos Dados Cadastrais do Buffet (eliminação de vazio, novos campos editáveis e sincronização com minuta/PDF) | [v2.1.3_ajuste_dados_cadastrais_buffet.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.3_ajuste_dados_cadastrais_buffet.md) |
| **v2.1.4** | 30/09/2026 | Seletor e Alocação de Ordem de Serviço (OS) na Emissão de Contratos, Resolução do Bloqueio de Validação e Busca Rápida | [v2.1.4_alocacao_ordem_servico.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.4_alocacao_ordem_servico.md) |
| **v2.1.5** | 30/09/2026 | Correção na Emissão do PDF do Contrato (Tratamento de Arquivos Bloqueados no Windows, Null-Safety em Parágrafos e Exibição de Erros) | [v2.1.5_correcao_emissao_pdf_contrato.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.5_correcao_emissao_pdf_contrato.md) |
| **v2.1.6** | 30/09/2026 | Correção de Compilação Maven: Import DateTimeFormatter em ContratoPDFGenerator | [v2.1.6_correcao_import_datetimeformatter.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.6_correcao_import_datetimeformatter.md) |
| **v2.1.7** | 30/09/2026 | Correção de Flush e Map Nulo no iText 7 (immediateFlush=false e blindagem de páginas flushed na marca d'água) | [v2.1.7_correcao_flush_itext7_marca_dagua.md](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/memoria_versoes/v2.1.7_correcao_flush_itext7_marca_dagua.md) |
