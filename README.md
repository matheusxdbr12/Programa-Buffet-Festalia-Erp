# 🍽️ Festalia ERP — Sistema de Gestão Integrada para Buffets e Eventos

<p align="center">
  <img src="src/main/resources/festalia_logo.jpg" alt="Festalia ERP Logo" width="220" style="border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);" />
</p>

<p align="center">
  <strong>Solução Desktop profissional para orçamentos, coordenação de eventos, emissão de Ordens de Serviço em PDF e controle financeiro de buffets.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17+" />
  <img src="https://img.shields.io/badge/SQLite-3.45-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite" />
  <img src="https://img.shields.io/badge/iText-7.2.5-006699?style=for-the-badge" alt="iText 7" />
  <img src="https://img.shields.io/badge/FlatLaf-3.4.1-2C3E50?style=for-the-badge" alt="FlatLaf" />
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white" alt="Maven" />
</p>

---

## 📌 Apresentação do Projeto

O **Festalia ERP** é uma aplicação desktop desenvolvida em **Java** com interface moderna (**FlatLaf**), banco de dados local embutido (**SQLite**) e motor de geração de documentos em alta resolução (**iText 7**).

Projetado especialmente para empresas de buffet, salões de festas, assessores e coordenadores de eventos, o sistema resolve as principais dores operacionais:
1. **Padronização na emissão de Ordens de Serviço (OS):** Cria documentos PDF com identidade visual customizável do próprio buffet emissor e marca d'água de proteção.
2. **Cálculos orçamentários precisos:** Automatiza a soma de convidados $\times$ valor/pessoa, cálculo proporcional de **taxa de serviço percentual (`%`)** e **taxa de transporte/logística**.
3. **Controle de histórico e faturamento:** Centraliza orçamentos e reservas em banco de dados SQLite sem necessidade de servidores externos.
4. **Portfólio gastronômico dinâmico:** Permite gerenciar cardápios pré-definidos e customizá-los sob medida para cada cliente.

---

> ### ⚠️ NOTA OBRIGATÓRIA DE DESENVOLVIMENTO & ATUALIZAÇÕES
> **Aviso aos mantenedores e colaboradores:** Toda e qualquer atualização, correção ou evolução realizada neste projeto **DEVE ser obrigatoriamente acompanhada de um `git commit`** claro e descritivo antes de novas modificações ou envio ao repositório. Mantenha o histórico de versões sempre rastreável e documentado.

---

## ✨ Funcionalidades Principais

### 1. 📋 Emissão de Ordens de Serviço (OS) em Tempo Real
- **Identificação Customizável do Buffet:** Campos editáveis para *Nome do Buffet* e *Slogan/Subtítulo*, persistidos no banco de dados para lembrar a marca do emissor em todas as ordens.
- **Numeração Sequencial Automática:** Identificador exclusivo por evento gerenciado pelo SQLite (`FEST-1001`, `FEST-1002`, ...).
- **Taxa de Serviço Percentual (`%`):** Calculada automaticamente sobre o valor por pessoa multiplicado pelo número de convidados, com conversão em reais exibida em tempo real na tela.
- **Taxa de Transporte / Logística:** Campo dedicado para custos de deslocamento somados ao valor final.
- **Cardápio Flexível:** Selecione uma opção do repertório e ajuste itens específicos para o cliente sem alterar o modelo original cadastrado.

### 2. 📄 Geração de PDF Profissional com Marca D'Água Customizável
- **Identidade Visual Completa do Buffet:** Campos editáveis para *Nome do Buffet*, *Slogan/Subtítulo* e seleção do arquivo de *Logomarca* do buffet (PNG, JPG, etc.).
- **Logomarca como Marca D'Água Dinâmica:** A imagem selecionada da empresa (a exemplo da *Mendez Buffet*) é aplicada automaticamente como **Marca D'Água translúcida** no fundo de todas as páginas da Ordem de Serviço, com preview em tempo real no sistema e persistência em banco local.
- **Layout Limpo e Timbrado Executivo:** Discriminação completa de contratante, evento, tabela detalhada do menu gastronômico, demonstrativo financeiro e eliminação de áreas manuais desnecessárias de assinatura.

### 3. 📊 Histórico de Eventos & Faturamento
- **Busca em Tempo Real:** Filtro instantâneo por cliente, local do evento, status ou número da OS.
- **Indicadores Financeiros:** Dashboard com contagem de eventos ativos e faturamento bruto consolidado.
- **Reemissão Instantânea:** Reemita ou visualize a qualquer momento o PDF original de qualquer OS histórica.
- **Controle de Status:** Atualização ágil entre *Orçamento*, *Confirmado*, *Realizado* e *Cancelado*.

### 4. 🍽️ Portfólio de Cardápios & Tabela de Preços
- **Repertório Gastronômico:** Cadastro de opções de cardápios com descrição detalhada e itens inclusos.
- **Pré-populado com Cardápios Clássicos:**
  - *Casamento Real & Nobre*
  - *Churrasco Prime & Chopp*
  - *Festa Infantil Alegria Total*
  - *Coquetel Corporativo Finger Food*
- **Tabela de Preços por Pessoa:** Precificação base atualizada automaticamente ao selecionar o cardápio na nova OS.

---

## 🏗️ Arquitetura do Projeto

```
festalia-erp/
├── pom.xml                                  # Configurações do Maven e dependências
├── .gitignore                               # Regras de exclusão do controle de versão
├── README.md                                # Documentação oficial do projeto
├── festalia_logo.jpg                        # Logotipo oficial do Festalia ERP
└── src/
    ├── main/
    │   ├── java/com/festalia/
    │   │   ├── BuffetErpApp.java            # Interface gráfica em abas (Java Swing + FlatLaf)
    │   │   ├── DatabaseManager.java         # Gestão de conexão e migrações SQLite
    │   │   ├── OrdemServico.java            # Modelo de dados da Ordem de Serviço
    │   │   ├── OrdemServicoDAO.java         # Persistência e consultas de OS no SQLite
    │   │   ├── Cardapio.java                # Modelo de Cardápio e itens gastronômicos
    │   │   ├── CardapioDAO.java             # Persistência do portfólio gastronômico
    │   │   └── PDFGenerator.java            # Motor de estilização e geração do PDF (iText 7)
    │   └── resources/
    │       └── festalia_logo.jpg            # Asset da logo empacotado no JAR
    └── test/
        └── java/com/festalia/               # Testes de integração e validação de PDF
```

---

## 🚀 Como Executar

### Pré-requisitos
- **Java JDK 17** ou superior instalado.
- **Apache Maven 3.8+** configurado no PATH do sistema.

### 1. Clonar o repositório:
```bash
git clone https://github.com/<seu-usuario>/festalia-erp.git
cd festalia-erp
```

### 2. Compilar e empacotar:
```bash
mvn clean package
```
*O comando gerará o executável completo na pasta `target/festalia-erp-1.0.0-jar-with-dependencies.jar`.*

### 3. Executar o sistema:
```bash
java -jar target/festalia-erp-1.0.0-jar-with-dependencies.jar
```

---

## 📄 Licença
Distribuído sob licença proprietária para uso em gestão de eventos e buffets. Todos os direitos reservados.
