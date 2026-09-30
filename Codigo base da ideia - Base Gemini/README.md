# Festalia ERP v2.0 - Sistema Integrado de Gestão para Buffets e Eventos

O **Festalia ERP v2.0** é uma plataforma desktop em Java completa e otimizada para o setor de festas, buffets e eventos corporativos. O sistema conta com persistência local em banco de dados **SQLite**, portfólio de cardápios com precificação dinâmica, histórico de eventos com métricas financeiras e emissão de Ordens de Serviço (OS) profissionais em formato **PDF** via iText 7.

---

> ### ⚠️ NOTA OBRIGATÓRIA DE CONTROLE DE VERSÃO
> **Aviso aos mantenedores e colaboradores:** Toda e qualquer atualização ou alteração realizada no projeto **DEVE ser obrigatoriamente registrada com um `git commit`** claro e descritivo antes de novas modificações ou envio ao repositório remoto.

---

---

## 🛠️ Tecnologias e Infraestrutura

- **Linguagem**: Java 17+
- **Banco de Dados**: SQLite embarcado (`sqlite-jdbc` 3.45.1.0)
  - Zero configuração / Zero servidor: os dados são salvos localmente em `festalia_erp.db`.
- **Motor de PDF**: [iText 7 Core](https://itextpdf.com/) (versão `7.2.5`)
- **Interface Gráfica**: Java Swing com suporte a Look and Feel nativo e acessibilidade por atalhos (`Alt+N`, `Alt+H`, `Alt+P`).
- **Gerenciador de Build**: Apache Maven

---

## 📁 Arquitetura do Projeto

```text
FestaliaERP/
├── pom.xml                                      # Gerenciador de dependências (iText 7, SQLite JDBC)
├── README.md                                    # Documentação do sistema
├── festalia_erp.db                              # Arquivo do banco de dados SQLite (criado automaticamente)
└── src/
    └── main/
        └── java/
            └── com/
                └── festalia/
                    ├── DatabaseManager.java     # Inicialização e conexões SQLite
                    ├── Cardapio.java            # Modelo com dados, preços e itens
                    ├── OrdemServico.java        # Modelo completo de OS com status e valores
                    ├── CardapioDAO.java         # Acesso a dados dos cardápios e repertório
                    ├── OrdemServicoDAO.java     # Acesso a dados do histórico de OS e métricas
                    ├── PDFGenerator.java        # Serviço de estilização e geração do PDF
                    └── BuffetErpApp.java        # Interface em abas (Nova OS, Histórico, Portfólio)
```

---

## ✨ Funcionalidades da Versão 2.0

### 1. 📋 Nova Ordem de Serviço & Emissão PDF (`Alt + N`)
- **Numeração Automática de OS**: Sequencial persistida no banco SQLite (`FEST-1001`, `FEST-1002`, ...).
- **Cálculo Financeiro em Tempo Real**: Multiplica automaticamente a quantidade de convidados pelo valor por pessoa.
- **Cardápio Personalizável**: Permite alterar pratos e descrições mantendo os modelos originais intactos e preservando as alterações no PDF emitido.
- **Status do Evento**: Definição entre *Orçamento*, *Confirmado*, *Realizado* e *Cancelado*.

### 2. 📊 Histórico de Eventos & Faturamento (`Alt + H`)
- **Busca em Tempo Real**: Filtragem instantânea por nome do cliente, local, status ou número da OS.
- **Métricas Financeiras**: Exibição do total de eventos ativos e faturamento consolidado.
- **Reemissão de PDF**: Reemita ou visualize a Ordem de Serviço de qualquer evento passado com 1 clique.
- **Gerenciamento de Status**: Atualização ágil de status da reserva.

### 3. 🍽️ Portfólio de Cardápios & Tabela de Preços (`Alt + P`)
- **Gestão de Repertório**: Cadastro e edição de cardápios com descrição e lista de itens.
- **Precificação por Pessoa**: Defina o preço base por convidado para cada opção gastronômica.
- **Sincronização Imediata**: Novos cardápios ficam disponíveis instantaneamente para novas ordens de serviço.

---

## 🚀 Como Executar

### 1. Gerar o pacote executável com Maven:
Na pasta raiz do projeto:
```bash
mvn clean package
```

### 2. Executar a aplicação:
```bash
java -jar target/festalia-erp-1.0.0-jar-with-dependencies.jar
```
