package com.festalia;

import com.festalia.financeiro.*;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Bateria de testes automatizados para o Módulo de Gestão Financeira Avançada.
 * Testa:
 * 1. Conversor de moeda para extenso em Português Brasileiro (NumeroPorExtensoUtil)
 * 2. Cálculo dinâmico de status das parcelas (PAGO, PAGAMENTO_PARCIAL, A_VENCER, VENCIDO)
 * 3. Geração e persistência do cronograma financeiro no SQLite
 * 4. Pagamentos parciais e abatimento de saldo
 * 5. Indicadores consolidados da OS (ResumoFinanceiroOS e inadimplência)
 * 6. Emissão de Recibo de Pagamento em PDF
 * 7. Regra de quitação integral e Emissão do Termo de Quitação em PDF
 */
public class TesteModuloFinanceiro {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("Iniciando Testes do Módulo de Gestão Financeira Festalia");
        System.out.println("==========================================================");

        try {
            DatabaseManager.inicializarBanco();

            testarNumeroPorExtenso();
            testarCalculosStatusParcela();
            testarFluxoCompletoFinanceiro();

            System.out.println("\n>>> TODOS OS TESTES FINANCEIROS PASSARAM COM SUCESSO! <<<");
        } catch (Throwable t) {
            System.err.println("\nFALHA NO TESTE FINANCEIRO: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void testarNumeroPorExtenso() {
        System.out.print("Testando conversor de valores por extenso... ");

        validar(NumeroPorExtensoUtil.converter(0.0).equals("zero real"), "0.0 deve ser 'zero real'");
        validar(NumeroPorExtensoUtil.converter(1.0).equals("um real"), "1.0 deve ser 'um real'");
        validar(NumeroPorExtensoUtil.converter(2.50).equals("dois reais e cinquenta centavos"), "2.50 falhou: " + NumeroPorExtensoUtil.converter(2.50));
        validar(NumeroPorExtensoUtil.converter(18500.0).equals("dezoito mil e quinhentos reais"), "18500.0 falhou: " + NumeroPorExtensoUtil.converter(18500.0));
        validar(NumeroPorExtensoUtil.converter(4500.75).equals("quatro mil e quinhentos reais e setenta e cinco centavos"), "4500.75 falhou: " + NumeroPorExtensoUtil.converter(4500.75));
        validar(NumeroPorExtensoUtil.converter(100.01).equals("cem reais e um centavo"), "100.01 falhou: " + NumeroPorExtensoUtil.converter(100.01));

        System.out.println("OK!");
    }

    private static void testarCalculosStatusParcela() {
        System.out.print("Testando cálculos e status dinâmico de parcelas... ");

        LocalDate hoje = LocalDate.now();

        // Parcela quitada
        ParcelaOS p1 = new ParcelaOS(1, 10, 1, "Entrada", 5000.0, hoje.minusDays(5), "Pix", "Entrada paga", 5000.0, "Pendente");
        validar(p1.getStatusCalculado().equals("PAGO"), "P1 com saldo zero deve estar PAGO");
        validar(p1.getSaldoRestante() == 0.0, "Saldo restante de P1 deve ser 0");

        // Parcela a vencer
        ParcelaOS p2 = new ParcelaOS(2, 10, 2, "Parcela 01", 4500.0, hoje.plusDays(10), "Boleto", "", 0.0, "Pendente");
        validar(p2.getStatusCalculado().equals("A VENCER"), "P2 no futuro sem pagamento deve ser A VENCER");

        // Parcela vencida
        ParcelaOS p3 = new ParcelaOS(3, 10, 3, "Parcela 02", 4500.0, hoje.minusDays(2), "Boleto", "", 0.0, "Pendente");
        validar(p3.getStatusCalculado().equals("VENCIDO"), "P3 no passado sem pagamento deve ser VENCIDO");
        validar(p3.getDiasEmAtraso() == 2, "P3 deve ter 2 dias de atraso");

        // Parcela parcial
        ParcelaOS p4 = new ParcelaOS(4, 10, 4, "Parcela 03", 4500.0, hoje.plusDays(20), "Cartão", "", 2000.0, "Pendente");
        validar(p4.getStatusCalculado().equals("PAGAMENTO PARCIAL"), "P4 com pagamento parcial deve ser PAGAMENTO PARCIAL");
        validar(p4.getSaldoRestante() == 2500.0, "Saldo restante de P4 deve ser 2500.0");

        System.out.println("OK!");
    }

    private static void testarFluxoCompletoFinanceiro() throws SQLException {
        System.out.print("Testando fluxo completo no banco de dados SQLite e PDFs... ");

        OrdemServicoDAO osDAO = new OrdemServicoDAO();
        FinanceiroDAO finDAO = new FinanceiroDAO();

        int numOS = 99000 + (int)(Math.random() * 900);
        LocalDate dataEvento = LocalDate.now().plusMonths(3);

        OrdemServico os = new OrdemServico(
                0, numOS, "Festalia Buffet", "Alta Gastronomia", null,
                "Cliente Teste Financeiro", "(11) 98888-7777", dataEvento, "Salão Principal",
                100, 185.0, 0.0, 0.0, 18500.0, "Confirmado",
                new Cardapio("Menu Ouro", 185.0, "Completo", List.of("Entrada", "Principal", "Sobremesa")),
                "Teste Automatizado Financeiro", null,
                LocalTime.of(19, 0), LocalTime.of(23, 0), "Casamento", false
        );
        osDAO.salvar(os);
        validar(os.getId() > 0, "OS deve ter sido inserida com ID gerado");

        // 1. Criação do cronograma financeiro (Entrada + 3 parcelas)
        ParcelaOS pEntrada = new ParcelaOS(0, os.getId(), 1, "Entrada / Sinal", 5000.0, LocalDate.now().plusDays(2), "Pix", "Sinal de reserva", 0.0, "Pendente");
        ParcelaOS p1 = new ParcelaOS(0, os.getId(), 2, "Parcela 01/03", 4500.0, LocalDate.now().plusMonths(1), "Boleto", "Parcela 1", 0.0, "Pendente");
        ParcelaOS p2 = new ParcelaOS(0, os.getId(), 3, "Parcela 02/03", 4500.0, LocalDate.now().plusMonths(2), "Boleto", "Parcela 2", 0.0, "Pendente");
        ParcelaOS p3 = new ParcelaOS(0, os.getId(), 4, "Parcela 03/03", 4500.0, dataEvento.minusDays(7), "Transferência", "Quitação pré-evento", 0.0, "Pendente");

        finDAO.salvarParcela(pEntrada);
        finDAO.salvarParcela(p1);
        finDAO.salvarParcela(p2);
        finDAO.salvarParcela(p3);

        List<ParcelaOS> parcelas = finDAO.listarParcelasPorOS(os.getId());
        validar(parcelas.size() == 4, "Devem existir 4 parcelas salvas");

        // 2. Verifica métricas iniciais
        ResumoFinanceiroOS resumo = finDAO.obterResumoOS(os.getId(), os.getValorTotal());
        validar(resumo.getTotalPrevisto() == 18500.0, "Total previsto deve ser 18500.0");
        validar(resumo.getTotalPago() == 0.0, "Total pago deve iniciar em 0.0");
        validar(resumo.getSaldoPendente() == 18500.0, "Saldo pendente deve ser 18500.0");
        validar(!resumo.isTotalmenteQuitada(), "OS não pode estar quitada ainda");

        // 3. Registrar Pagamento Total da Entrada
        PagamentoParcela pagEntrada = new PagamentoParcela(
                0, pEntrada.getId(), 5000.0, LocalDate.now(), "Pix", "PIX-123456", "Pagamento de entrada confirmado", "Administrador"
        );
        finDAO.registrarPagamento(pagEntrada);

        // Recarrega parcela e confere abatimento
        ParcelaOS pEntradaAtualizada = finDAO.buscarParcelaPorId(pEntrada.getId());
        validar(pEntradaAtualizada.getValorPago() == 5000.0, "Valor pago da entrada deve ser 5000.0");
        validar(pEntradaAtualizada.getStatusCalculado().equals("PAGO"), "Entrada deve estar com status PAGO");

        // 4. Testar Emissão de Recibo de Pagamento em PDF
        File pdfRecibo = File.createTempFile("recibo_teste_", ".pdf");
        FinanceiroPDFGenerator.gerarReciboPagamentoPDF(os, pEntradaAtualizada, pagEntrada, pdfRecibo.getAbsolutePath());
        validar(pdfRecibo.exists() && pdfRecibo.length() > 500, "Arquivo PDF do Recibo deve existir e ter tamanho válido");

        // 5. Registrar Pagamento Parcial na Parcela 01 (R$ 2.000 de R$ 4.500)
        PagamentoParcela pagParcial = new PagamentoParcela(
                0, p1.getId(), 2000.0, LocalDate.now(), "Boleto", "BOL-9988", "Primeira parte da parcela 1", "Operador"
        );
        finDAO.registrarPagamento(pagParcial);

        ParcelaOS p1Atualizada = finDAO.buscarParcelaPorId(p1.getId());
        validar(p1Atualizada.getValorPago() == 2000.0, "Parcela 1 deve registrar R$ 2.000 pagos");
        validar(p1Atualizada.getSaldoRestante() == 2500.0, "Saldo restante da parcela 1 deve ser R$ 2.500");
        validar(p1Atualizada.getStatusCalculado().equals("PAGAMENTO PARCIAL"), "Status deve ser PAGAMENTO PARCIAL");

        // 6. Quitar o restante de todas as parcelas
        PagamentoParcela pag1Restante = new PagamentoParcela(0, p1.getId(), 2500.0, LocalDate.now(), "Boleto", "BOL-9989", "Quitação parcela 1", "Operador");
        PagamentoParcela pag2 = new PagamentoParcela(0, p2.getId(), 4500.0, LocalDate.now(), "Boleto", "BOL-7766", "Quitação parcela 2", "Operador");
        PagamentoParcela pag3 = new PagamentoParcela(0, p3.getId(), 4500.0, LocalDate.now(), "Transferência", "TED-3322", "Quitação parcela 3", "Operador");

        finDAO.registrarPagamento(pag1Restante);
        finDAO.registrarPagamento(pag2);
        finDAO.registrarPagamento(pag3);

        // 7. Validação de Quitação Integral
        ResumoFinanceiroOS resumoFinal = finDAO.obterResumoOS(os.getId(), os.getValorTotal());
        validar(resumoFinal.getTotalPago() == 18500.0, "Total pago final deve ser 18500.0");
        validar(resumoFinal.getSaldoPendente() == 0.0, "Saldo pendente final deve ser 0.0");
        validar(resumoFinal.getPercentualPago() >= 99.99, "Percentual pago deve ser 100%");
        validar(resumoFinal.isTotalmenteQuitada(), "OS deve estar Totalmente Quitada");

        // 8. Testar Emissão do Termo de Quitação em PDF
        File pdfQuitacao = File.createTempFile("termo_quitacao_teste_", ".pdf");
        FinanceiroPDFGenerator.gerarTermoQuitacaoPDF(os, resumoFinal, parcelas, pdfQuitacao.getAbsolutePath());
        validar(pdfQuitacao.exists() && pdfQuitacao.length() > 500, "Arquivo PDF do Termo de Quitação deve existir e ter tamanho válido");

        // 9. Verificar Trilha de Histórico/Auditoria
        List<HistoricoFinanceiro> historico = finDAO.listarHistoricoPorOS(os.getId());
        validar(historico.size() >= 5, "Devem haver pelo menos 5 movimentações registradas na auditoria");

        // Limpeza dos arquivos temporários de teste
        pdfRecibo.delete();
        pdfQuitacao.delete();

        System.out.println("OK!");
    }

    private static void validar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError("VALIDAÇÃO FALHOU: " + mensagem);
        }
    }
}
