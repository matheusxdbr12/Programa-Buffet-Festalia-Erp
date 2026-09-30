package com.festalia;

import com.festalia.contrato.*;
import com.festalia.financeiro.ParcelaOS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes Unitários e Integrados do Módulo de Contratos de Prestação de Serviços.
 */
public class TesteModuloContratos {

    @BeforeAll
    public static void setUp() {
        DatabaseManager.inicializarBanco();
    }

    @Test
    @DisplayName("Garante que as tabelas de contratos, versões e auditoria existem")
    public void testEstruturaTabelasContratos() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Verifica se a tabela contratos existe
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM contratos")) {
                assertTrue(rs.next(), "Tabela contratos deve ser consultável");
            }

            // Verifica se a tabela contratos_versoes existe
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM contratos_versoes")) {
                assertTrue(rs.next(), "Tabela contratos_versoes deve ser consultável");
            }

            // Verifica se a tabela contratos_auditoria existe
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM contratos_auditoria")) {
                assertTrue(rs.next(), "Tabela contratos_auditoria deve ser consultável");
            }

            // Verifica se as configurações da empresa padrão foram inicializadas
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM configuracoes_empresa")) {
                assertTrue(rs.next());
                assertTrue(rs.getInt(1) >= 5, "Devem existir ao menos 5 configurações de empresa cadastradas");
            }
        }
    }

    @Test
    @DisplayName("Validação pré-emissão de contrato aponta pendências e aceita dados válidos")
    public void testValidacaoPreEmissaoContrato() {
        Contrato c = new Contrato();

        // 1. Contrato vazio deve ser inválido
        ValidacaoContratoResultado r1 = ContratoTemplateEngine.validarDadosParaContrato(c);
        assertFalse(r1.isValido(), "Contrato vazio não pode ser considerado válido");
        assertTrue(r1.getErros().size() >= 3, "Deve listar múltiplos erros de validação");

        // 2. Preenche dados obrigatórios
        c.setClienteNome("Ana Carolina Silveira");
        c.setClienteCpfCnpj("123.456.789-00");
        c.setClienteEndereco("Rua das Palmeiras, 100 - São Paulo/SP");
        c.setDataEvento("15/12/2026");
        c.setLocalEvento("Mansão Festalia Jardins");
        c.setValorTotal(18500.00);
        c.setNumeroConvidados(150);
        c.setEmpresaRazaoSocial("Festalia Buffet & Eventos Ltda");
        c.setEmpresaCnpj("12.345.678/0001-90");

        ValidacaoContratoResultado r2 = ContratoTemplateEngine.validarDadosParaContrato(c);
        assertTrue(r2.isValido(), "Contrato com todos os dados preenchidos deve ser válido: " + r2.getErros());
    }

    @Test
    @DisplayName("Compilação de minuta contratual com todas as cláusulas jurídicas e parcelamento")
    public void testCompilacaoMinutaContrato() {
        Contrato c = new Contrato();
        c.setNumeroContrato("CT-2026-TESTE");
        c.setVersaoAtual("1.0");
        c.setModelo(ModeloContrato.CASAMENTO);
        c.setClienteNome("Mariana Fernandes de Souza");
        c.setClienteCpfCnpj("234.567.890-11");
        c.setClienteRg("12.345.678-9");
        c.setClienteEndereco("Av. Paulista, 1500, Apto 82 - Bela Vista, São Paulo/SP");
        c.setClienteEmail("mariana@email.com");
        c.setClienteTelefone("(11) 98765-4321");

        c.setTipoEvento("Casamento");
        c.setDataEvento("20/11/2026");
        c.setHorarioEvento("19:00 às 03:00");
        c.setLocalEvento("Espaço Festalia Grand Ballroom");
        c.setNumeroConvidados(180);
        c.setValorTotal(28000.00);
        c.setCardapioServicos("Buffet Completo Premium com Mesa de Antepastos, Jantar Franco-Americano, Sobremesas Finas e Bar de Drinks.");
        c.setObservacoesEspecificas("Montagem liberada a partir das 12:00.");

        c.setEmpresaRazaoSocial("Festalia Eventos e Gastronomia Ltda");
        c.setEmpresaNomeFantasia("Festalia Buffet");
        c.setEmpresaCnpj("12.345.678/0001-90");
        c.setEmpresaEndereco("Rua das Flores, 500 - Jardins, São Paulo/SP");
        c.setEmpresaRepresentante("Diretoria Operacional Festalia");
        c.setEmpresaForoComarca("São Paulo/SP");

        c.setClausulas(ContratoTemplateEngine.criarClausulasPadrao(c.getModelo()));

        List<ParcelaOS> parcelas = new ArrayList<>();
        parcelas.add(new ParcelaOS(1, 1, 1, "Entrada", 8000.00, LocalDate.now(), "PIX", "PAGO", "", java.time.LocalDateTime.now()));
        parcelas.add(new ParcelaOS(2, 1, 2, "Parcela 2", 10000.00, LocalDate.now().plusMonths(1), "Boleto", "PENDENTE", "", java.time.LocalDateTime.now()));
        parcelas.add(new ParcelaOS(3, 1, 3, "Parcela Final", 10000.00, LocalDate.now().plusMonths(2), "Boleto", "PENDENTE", "", java.time.LocalDateTime.now()));

        String textoCompilado = ContratoTemplateEngine.compilarTextoCompleto(c, parcelas);

        assertNotNull(textoCompilado);
        assertTrue(textoCompilado.contains("CONTRATO DE PRESTAÇÃO DE SERVIÇOS PARA EVENTO"));
        assertTrue(textoCompilado.contains("Mariana Fernandes de Souza"));
        assertTrue(textoCompilado.contains("234.567.890-11"));
        assertTrue(textoCompilado.contains("CLÁUSULA 1ª — DO OBJETO"));
        assertTrue(textoCompilado.contains("CLÁUSULA 4ª — DO VALOR E DAS CONDIÇÕES DE PAGAMENTO"));
        assertTrue(textoCompilado.contains("CLÁUSULA 7ª — DO CANCELAMENTO E DA RESCISÃO"));
        assertTrue(textoCompilado.contains("CLÁUSULA 8ª — DO DIREITO DE ARREPENDIMENTO"));
        assertTrue(textoCompilado.contains("CLÁUSULA 9ª — DOS DANOS A BENS"));
        assertTrue(textoCompilado.contains("CLÁUSULA 12ª — DA PROTEÇÃO DE DADOS (LGPD)"));
        assertTrue(textoCompilado.contains("CLÁUSULA 14ª — DO FORO"));
        assertTrue(textoCompilado.contains("TESTEMUNHAS:"));
    }

    @Test
    @DisplayName("DAO e Versionamento: Salvar, auditar e consultar métricas de cartões")
    public void testDaoContratosEVersionamento() {
        Contrato c = new Contrato();
        c.setOsId(9999);
        c.setNumeroContrato("CT-9999-TEST");
        c.setClienteNome("Empresa Alpha Tecnologia");
        c.setClienteCpfCnpj("00.111.222/0001-33");
        c.setTipoEvento("Corporativo");
        c.setDataEvento("10/10/2026");
        c.setValorTotal(15000.00);
        c.setVersaoAtual("1.0");
        c.setStatus(StatusContrato.GERADO);
        c.setEmpresaRazaoSocial("Festalia Buffet");

        boolean salvo = ContratoDAO.salvarOuAtualizarContrato(c, "Criação teste DAO", "JUnit");
        assertTrue(salvo, "Deve salvar o contrato no banco");
        assertTrue(c.getId() > 0, "ID do contrato deve ser gerado");

        // Registra versão 1.0
        boolean v1 = ContratoDAO.registrarNovaVersao(c.getId(), "1.0", "Minuta V1.0", "contratos_pdf/teste_v1.pdf", "Versão inicial", "JUnit");
        assertTrue(v1, "Deve registrar a versão no banco");

        // Atualiza status para ASSINADO
        boolean statusOk = ContratoDAO.atualizarStatus(c.getId(), StatusContrato.ASSINADO, "Assinado pelo cliente", "JUnit");
        assertTrue(statusOk, "Deve atualizar o status para ASSINADO");

        // Verifica recuperação por OS ID
        Contrato recuperado = ContratoDAO.obterPorOsId(9999);
        assertNotNull(recuperado, "Deve recuperar contrato pelo OS ID");
        assertEquals(StatusContrato.ASSINADO, recuperado.getStatus());

        // Verifica contadores de cards
        Map<String, Integer> contadores = ContratoDAO.obterContadoresCardsEstatico();
        assertNotNull(contadores);
        assertTrue(contadores.getOrDefault("ASSINADO", 0) >= 1);

        // Verifica histórico de versões e auditoria
        List<VersaoContrato> versoes = ContratoDAO.listarVersoes(c.getId());
        assertFalse(versoes.isEmpty(), "Deve listar a versão registrada");

        List<HistoricoContrato> auditoria = ContratoDAO.listarAuditoria(c.getId());
        assertTrue(auditoria.size() >= 2, "Devem existir ao menos 2 registros de auditoria");
    }

    @Test
    @DisplayName("Geração do PDF de contrato oficial com iText 7")
    public void testGeracaoPdfContrato() {
        Contrato c = new Contrato();
        c.setNumeroContrato("CT-PDF-TESTE");
        c.setOsId(8888);
        c.setVersaoAtual("1.0");
        c.setModelo(ModeloContrato.BUFFET_COMPLETO);
        c.setClienteNome("Rodrigo Mendes da Costa");
        c.setClienteCpfCnpj("333.444.555-66");
        c.setClienteRg("22.333.444-5");
        c.setClienteEndereco("Rua Augusta, 1200 - Consolação, São Paulo/SP");
        c.setTipoEvento("Aniversário 40 Anos");
        c.setDataEvento("12/11/2026");
        c.setHorarioEvento("20:00 às 02:00");
        c.setLocalEvento("Espaço Festalia Gourmet");
        c.setNumeroConvidados(120);
        c.setValorTotal(16800.00);
        c.setCardapioServicos("Menu Degustação Premium com Ilhas Gastronômicas e Bar de Caipirinhas.");
        c.setEmpresaRazaoSocial("Festalia Buffet & Eventos Ltda");
        c.setEmpresaCnpj("12.345.678/0001-90");
        c.setEmpresaEndereco("Rua das Flores, 500 - Jardins, São Paulo/SP");
        c.setEmpresaRepresentante("Matheus Souza");
        c.setEmpresaForoComarca("São Paulo/SP");
        c.setClausulas(ContratoTemplateEngine.criarClausulasPadrao(c.getModelo()));

        List<ParcelaOS> parcelas = new ArrayList<>();
        parcelas.add(new ParcelaOS(1, 8888, 1, "Sinal", 5000.00, LocalDate.now(), "PIX", "PAGO", "", java.time.LocalDateTime.now()));
        parcelas.add(new ParcelaOS(2, 8888, 2, "Parcela 2", 5900.00, LocalDate.now().plusMonths(1), "Boleto", "PENDENTE", "", java.time.LocalDateTime.now()));
        parcelas.add(new ParcelaOS(3, 8888, 3, "Parcela 3", 5900.00, LocalDate.now().plusMonths(2), "Boleto", "PENDENTE", "", java.time.LocalDateTime.now()));

        String pdfPath = ContratoPDFGenerator.gerarPdfContrato(c, parcelas, true);
        assertNotNull(pdfPath, "Caminho do PDF gerado não deve ser nulo");

        File arquivoPdf = new File(pdfPath);
        assertTrue(arquivoPdf.exists(), "Arquivo PDF deve existir no disco");
        assertTrue(arquivoPdf.length() > 1024, "Arquivo PDF deve ter tamanho válido (> 1KB)");
    }
}
