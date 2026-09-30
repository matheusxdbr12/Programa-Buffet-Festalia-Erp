# Relatório de Diagnóstico e Correção de Erros: Alocação de OS para Geração de Contratos

**Data:** 30/09/2026  
**Versão:** 2.1.4  
**Módulo:** Gestão de Contratos / Integração com Ordem de Serviço (OS)  
**Status:** Resolvido com Sucesso  

---

## 1. Descrição do Problema Relatado
Ao tentar gerar ou emitir um contrato através do formulário de revisão (`GerarRevisarContratoDialog`), o sistema exibia um pop-up de bloqueio com o seguinte alerta:

> **Validação Pré-Emissão de Contrato**  
> *Existem informações que precisam ser revisadas antes da emissão:*  
> *• Ordem de Serviço não localizada para este contrato.*  
> *Por favor, preencha os dados nas abas correspondentes antes de gerar o PDF oficial.*

Além disso, o usuário apontou:
> *"Não tem campo para alocar a OS para dar prosseguimento a geração de contratos."*

---

## 2. Diagnóstico Técnico e Causa Raiz

Foram identificadas três causas técnicas interdependentes:

1. **Ausência de Componente Seletor de OS na Interface:**
   - Na aba **1. Ordem de Serviço & Partes**, existia apenas um rótulo estático (`lblNumeroOs`) que exibia o número da OS apenas se o diálogo fosse aberto diretamente de uma OS já pré-selecionada.
   - Quando o diálogo era aberto via menu geral de contratos ou para um novo contrato sem ID direto, não havia nenhum campo (`JComboBox` ou campo de busca) para selecionar ou trocar a OS vinculada.

2. **Consulta SQL Quebrada com Coluna Inexistente (`cliente_cpf_cnpj`):**
   - O método `carregarDadosOsNoContrato(int osId)` realizava um `SELECT` manual buscando a coluna `cliente_cpf_cnpj` diretamente da tabela `ordens_servico`.
   - Na estrutura do banco SQLite do Festalia ERP, essa coluna não existe na tabela `ordens_servico` (pertence a `clientes`). Isso disparava uma `SQLException: no such column: cliente_cpf_cnpj`, abortando o carregamento dos dados da OS e mantendo a vinculação nula.

3. **Falha de Projeto na Validação Pré-Emissão (`ContratoTemplateEngine`):**
   - O método `validarDadosParaContrato(Contrato c)` chamava internamente:
     ```java
     return validar(null, c, null);
     ```
   - O primeiro parâmetro (`OrdemServico os`) era passado explicitamente como `null`!
   - Como consequência, a regra `if (os == null) erros.add("Ordem de Serviço não localizada para este contrato.")` sempre disparava, tornando impossível emitir qualquer contrato através desse método de validação.

---

## 3. Ações Corretivas Implementadas

### A. Criação de Seletor Interativo e Busca de OS na Interface (`GerarRevisarContratoDialog.java`)
- **Novo Dropdown de Seleção de OS (`cbOrdemServico`):**
  - Carrega dinamicamente todas as Ordens de Serviço cadastradas via `OrdemServicoDAO.listarTodas()`.
  - Exibe no dropdown: `OS #XXXX - [Nome do Cliente] - [Data] - R$ [Valor]`.
- **Campo de Busca Rápida por Número ou ID (`txtBuscarNumeroOs` e `btnBuscarOs`):**
  - Permite digitar o número ou ID da OS e clicar em "Localizar OS" para alocá-la instantaneamente.
- **Badge Visual de Status de Alocação (`lblStatusOsVinculada`):**
  - Exibe visualmente se a OS está vinculada (verde com ícone de confirmação) ou se nenhuma OS está selecionada (alerta amarelo).
- **Vinculação e Preenchimento Automático em Tempo Real (`vincularOrdemServico`):**
  - Ao selecionar a OS, o sistema atualiza `contrato.setOrdemServicoId(...)` e `contrato.setNumeroOS(...)`.
  - Preenche automaticamente contratante, CPF/CNPJ, tipo de evento, data, endereço, valor contratual e recarrega as parcelas financeiras e minuta de prévia.

### B. Correção e Robustecimento da Consulta de OS (`OrdemServicoDAO.java`)
- Criado o método:
  ```java
  public OrdemServico buscarPorNumeroOS(int numeroOS);
  ```
  Permite recuperar a OS tanto pelo campo `numero_os` quanto pelo `id` primário com integridade referencial completa.
- Substituída a query SQL manual que quebrava em `cliente_cpf_cnpj` pelo uso seguro do `OrdemServicoDAO.buscarPorId()`.

### C. Correção Crítica da Validação Pré-Emissão (`ContratoTemplateEngine.java`)
- O método `validarDadosParaContrato(Contrato c)` foi corrigido para:
  1. Identificar se o contrato tem `ordemServicoId` ou `numeroOS`.
  2. Consultar a respectiva `OrdemServico` no banco via `OrdemServicoDAO`.
  3. Consultar as parcelas registradas via `FinanceiroDAO`.
  4. Executar a validação completa com os dados reais carregados.
- Em `validar(OrdemServico os, Contrato contrato, List<ParcelaFinanceira> parcelas)`:
  - Se `os == null`, tenta carregar automaticamente pelos identificadores do contrato antes de emitir erro.
  - Se os dados de evento já estiverem presentes no próprio contrato, não bloqueia a emissão.

---

## 4. Testes de Verificação e Validação
- Compilação realizada com sucesso (`mvn clean test-compile` retornou código 0).
- Verificado suporte ao carregamento retroativo de contratos existentes e novos contratos.
- Verificado tratamento de erros caso o banco esteja indisponível ou a OS não exista.
