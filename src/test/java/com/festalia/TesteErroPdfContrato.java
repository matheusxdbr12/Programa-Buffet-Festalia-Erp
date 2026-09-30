package com.festalia;

import com.festalia.contrato.Contrato;
import com.festalia.contrato.ContratoDAO;
import com.festalia.contrato.ContratoPDFGenerator;
import com.festalia.financeiro.FinanceiroDAO;
import com.festalia.financeiro.ParcelaOS;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TesteErroPdfContrato {
    @Test
    public void testDiagnostico() {
        StringBuilder sb = new StringBuilder();
        try {
            DatabaseManager.inicializarBanco();
            sb.append("--- DIAGNOSTICO ERRO PDF CONTRATO ---\n");

            OrdemServicoDAO osDAO = new OrdemServicoDAO();
            List<OrdemServico> todasOS = osDAO.listarTodas();
            sb.append("Total de OSs cadastradas: ").append(todasOS.size()).append("\n");
            for (OrdemServico o : todasOS) {
                sb.append(" -> OS ID=").append(o.getId()).append(", Num=").append(o.getNumeroOS())
                  .append(", Cliente=").append(o.getNomeCliente()).append(", Tipo=").append(o.getTipoEvento())
                  .append(", HorarioInicio=").append(o.getHorarioInicio()).append(", HorarioFim=").append(o.getHorarioFim())
                  .append(", Logo=").append(o.getCaminhoLogoBuffet()).append("\n");
            }

            OrdemServico os3 = osDAO.buscarPorNumeroOS(3);
            if (os3 == null) {
                os3 = osDAO.buscarPorId(3);
            }
            sb.append("OS 3 encontrada: ").append(os3 != null ? ("ID=" + os3.getId() + ", num=" + os3.getNumeroOS()) : "NULL").append("\n");

            Contrato contrato = ContratoDAO.obterPorNumeroContrato("CTR-2026-0001");
            if (contrato == null && os3 != null) {
                contrato = ContratoDAO.obterPorOsId(os3.getId());
            }

            if (contrato != null) {
                sb.append("Contrato do banco: ID=").append(contrato.getId()).append(", Num=").append(contrato.getNumeroContrato()).append("\n");
            } else {
                sb.append("Simulando contrato conforme tela do usuario...\n");
                contrato = new Contrato();
                contrato.setNumeroContrato("CTR-2026-0001");
                contrato.setOrdemServicoId(os3 != null ? os3.getId() : 3);
                contrato.setNumeroOS(3);
                contrato.setContratanteNome("Washitu");
                contrato.setContratanteCpfCnpj("11111111");
                contrato.setContratanteEndereco("Rua teste");
                contrato.setContratanteTelefone("81999555555");
                contrato.setEmpresaRazaoSocial("Festalia Buffet & Eventos Ltda");
                contrato.setEmpresaNomeFantasia("Festalia Buffet");
                contrato.setEmpresaCnpj("12.345.678/0001-90");
                contrato.setEmpresaEndereco("Rua das Flores, 500");
                contrato.setEmpresaRepresentante("Diretoria Festalia");
                contrato.setEmpresaCidadeForo("São Paulo/SP");
                contrato.setEmpresaTelefone("(11) 3456-7890");
            }

            List<ParcelaOS> parcelas = new FinanceiroDAO().listarParcelasPorOS(contrato.getOrdemServicoId());
            sb.append("Parcelas encontradas: ").append(parcelas.size()).append("\n");

            sb.append("Chamando ContratoPDFGenerator.gerarPdfContrato...\n");
            String pdfPath = ContratoPDFGenerator.gerarPdfContrato(contrato, parcelas, false);
            sb.append("Resultado pdfPath: ").append(pdfPath).append("\n");

        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            sb.append("EXCEPTION CAPTURADA:\n").append(sw.toString()).append("\n");
        } finally {
            try {
                Files.writeString(Path.of("resultado_diagnostico.txt"), sb.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
