# Relatório de Diagnóstico e Correção de Erros: Ausência de Import DateTimeFormatter

**Data:** 30/09/2026  
**Versão:** 2.1.6  
**Módulo:** Gestão de Contratos / Geração de PDF (`ContratoPDFGenerator.java`)  
**Status:** Resolvido com Sucesso  

---

## 1. Descrição do Erro de Compilação
Durante o build com Maven (`mvn compile`), ocorreu a seguinte falha:

```text
[ERROR] COMPILATION ERROR : 
[ERROR] /C:/Users/matheus.souza/Documents/Festalia ERP V2.0/src/main/java/com/festalia/contrato/ContratoPDFGenerator.java:[412,90] cannot find symbol
  symbol:   variable DateTimeFormatter
  location: class com.festalia.contrato.ContratoPDFGenerator
[INFO] 1 error
[INFO] BUILD FAILURE
```

---

## 2. Diagnóstico Técnico e Causa Raiz
- Na versão anterior (v2.1.5), foi implementada uma blindagem de data de contingência no bloco de assinaturas (`adicionarBlocoAssinaturas`):
  ```java
  String dataEmissao = strSafe(c.getDataEmissaoFormatada(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
  ```
- No entanto, a classe `java.time.format.DateTimeFormatter` não constava na seção de imports do cabeçalho de `ContratoPDFGenerator.java` (havia apenas `import java.time.LocalDate;`), fazendo com que o compilador `javac` emitisse o erro `cannot find symbol`.

---

## 3. Ação Corretiva Implementada
- Adicionado o import explícito no cabeçalho de `ContratoPDFGenerator.java`:
  ```java
  import java.time.format.DateTimeFormatter;
  ```
- Verificada toda a classe para certificar que nenhum outro símbolo de `java.time.*` ou `itextpdf` esteja desprovido de import.
