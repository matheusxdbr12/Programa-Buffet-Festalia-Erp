# Relatório de Diagnóstico e Correção de Erros: Falha de Flush e Map Nulo no iText 7

**Data:** 30/09/2026  
**Versão:** 2.1.7  
**Módulo:** Geração de Contratos em PDF (`ContratoPDFGenerator.java`) e OS (`PDFGenerator.java`)  
**Status:** Resolvido com Sucesso  

---

## 1. Descrição do Problema Relatado
Ao clicar em **"Salvar & Gerar PDF do Contrato"** na tela de revisão de minuta, o sistema exibia a caixa de erro detalhada:

```text
Erro ao gerar o documento PDF do contrato.

Detalhes:
NullPointerException: Cannot invoke "java.util.Map.get(Object)" because "this.map" is null
```

---

## 2. Diagnóstico Técnico e Causa Raiz
1. **Comportamento Padrão de `immediateFlush` no iText 7:**
   - O construtor padrão `new Document(pdfDoc)` habilita internamente `immediateFlush = true`.
   - Essa funcionalidade do iText 7 existe para economia de memória, descarregando (flushing) automaticamente as páginas anteriores para o disco/stream assim que uma nova página é iniciada.
   - Como um Contrato de Buffet possui entre 3 e 4 páginas, as páginas 1 e 2 eram descarregadas e finalizadas pelo motor antes que o fluxo de renderização terminasse.

2. **Acesso aos Recursos de Página Já Descarregada (`page.isFlushed()`):**
   - Ao final da montagem do conteúdo, o método `aplicarMarcaDaguaNoFundo(pdfDoc, logoWatermark)` percorria todas as páginas (`for (int p = 1; p <= totalPaginas; p++)`).
   - Ao tentar invocar `page.newContentStreamBefore()` e `page.getResources()` em uma página já descarregada, o dicionário interno de objetos do iText (`PdfDictionary` / `PdfResources`) encontrava o mapa de objetos limpo (`this.map == null`), disparando:
     > `NullPointerException: Cannot invoke "java.util.Map.get(Object)" because "this.map" is null`

---

## 3. Ações Corretivas Implementadas

### A. Desativação do `immediateFlush` nos Construtores de `Document`
- No [ContratoPDFGenerator.java](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/src/main/java/com/festalia/contrato/ContratoPDFGenerator.java) e no [PDFGenerator.java](file:///c:/Users/matheus.souza/Documents/Festalia%20ERP%20V2.0/src/main/java/com/festalia/PDFGenerator.java), a instanciação do documento foi ajustada para:
  ```java
  Document document = new Document(pdfDoc, PageSize.A4, false);
  ```
- O parâmetro `false` impede o descarte prematuro das páginas da memória até que `document.close()` seja expressamente chamado no final do processo, mantendo o mapa de recursos íntegro em todas as páginas.

### B. Checagem Preventiva de `page.isFlushed()`
- Tanto em `aplicarMarcaDaguaNoFundo` quanto no manipulador de rodapé `NumeracaoPaginaHandler`, foi inserida a guarda:
  ```java
  if (page == null || page.isFlushed()) continue;
  ```
- Ambos os blocos foram envolvidos em tratamento de exceções com fallback seguro para garantir que eventuais peculiaridades gráficas nunca abortem a emissão do contrato.
