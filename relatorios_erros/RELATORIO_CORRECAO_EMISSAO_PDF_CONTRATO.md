# Relatório de Diagnóstico e Correção de Erros: Emissão do PDF do Contrato

**Data:** 30/09/2026  
**Versão:** 2.1.5  
**Módulo:** Gestão de Contratos / Geração de Documentos em PDF (iText 7)  
**Status:** Resolvido com Sucesso  

---

## 1. Descrição do Problema Relatado
Ao clicar no botão **"Salvar & Gerar PDF do Contrato"** na tela de minuta de contrato (`GerarRevisarContratoDialog`), o sistema exibia a caixa de diálogo de erro:

> **Erro**  
> ❌ *Erro ao gerar o documento PDF do contrato.*

A validação dos dados de formulário passava normalmente e o contrato era gravado no banco de dados, mas o arquivo PDF oficial falhava em ser gerado ou sobrescrito.

---

## 2. Diagnóstico Técnico e Causas Raiz

1. **Bloqueio de Arquivo em Execução no Windows (File Lock):**
   - Ao emitir o contrato pela primeira vez, o Festalia ERP abre automaticamente o arquivo gerado via `Desktop.getDesktop().open(new File(pdfPath))`.
   - No sistema operacional Windows, visualizadores de PDF (como Adobe Acrobat Reader, Google Chrome ou Microsoft Edge) mantêm um bloqueio exclusivo de leitura/leitura compartilhada no arquivo (`Contrato_CTR-2026-0001_Washitu_v1.0.pdf`).
   - Quando o usuário realizava ajustes na tela e tentava clicar novamente em **"Salvar & Gerar PDF do Contrato"**, o comando `new FileOutputStream(arquivoDestino)` disparava uma `java.io.FileNotFoundException` (*"O arquivo já está sendo usado por outro processo"* ou *"Acesso negado"*).
   - O `ContratoPDFGenerator` capturava a exceção silenciosamente em seu `catch (Exception ex)` e retornava `null`.
   - A interface exibia apenas uma mensagem genérica sem informar o real motivo ao usuário.

2. **Vulnerabilidade a Valores Nulos (Null-Safety) no iText 7:**
   - No motor iText 7, qualquer chamada a `new Paragraph((String) null)` lança imediatamente `java.lang.NullPointerException: Text cannot be null`.
   - Campos opcionais (como RG, e-mail de contato, observações da OS ou forma de pagamento sugerida na parcela) poderiam conter valores nulos em determinadas ordens de serviço ou contratos em rascunho, interrompendo a renderização antes do encerramento do documento.

3. **Fallback Inexistente na Localização da Ordem de Serviço:**
   - A busca da OS no `ContratoPDFGenerator` utilizava estritamente `osDAO.buscarPorId(contrato.getOrdemServicoId())`. Caso a OS estivesse vinculada pelo número público (`numeroOS`) em vez da chave primária interna (`id`), ocorria inconsistência na recuperação do cabeçalho.

---

## 3. Ações Corretivas Implementadas

### A. Algoritmo Inteligente de Fallback para Arquivos Bloqueados (`ContratoPDFGenerator.java`)
- Antes de tentar a escrita, o sistema verifica se o arquivo PDF de destino já existe e tenta abri-lo em modo append para testar sua disponibilidade.
- Caso o arquivo esteja bloqueado pelo leitor de PDF do Windows, o sistema automaticamente gera versões sequenciais numeradas (ex: `Contrato_CTR-2026-0001_Washitu_v1.0_(1).pdf`, `_(2).pdf`), garantindo que o PDF seja sempre gerado e aberto sem travar o operador.

### B. Proteção Completa contra Nulos (Null-Safety) em Todos os Parágrafos e Células
- Implementado o método utilitário `strSafe(String s, String fallback)` que garante que nenhum texto nulo chegue aos construtores `Paragraph(...)`, `Cell(...)` ou tabelas do iText 7.
- Campos como razão social, nome fantasia, CNPJ, comarca de foro, e-mail, telefone e observações agora possuem valores de contingência padronizados e seguros.

### C. Registro e Exposição de Erros Detalhados na Interface (`GerarRevisarContratoDialog.java`)
- Adicionada a propriedade `ultimoErro` e o método `getUltimoErro()` em `ContratoPDFGenerator`.
- Se ocorrer qualquer exceção em disco ou permissão, a mensagem na tela exibe com clareza a causa do problema, incluindo a orientação caso o arquivo precise ser fechado no visualizador externo.

### D. Garantia da Data de Emissão
- Caso o contrato esteja em rascunho sem data de emissão preenchida, o gerador de PDF agora atribui automaticamente a data atual (`LocalDate.now()`).
