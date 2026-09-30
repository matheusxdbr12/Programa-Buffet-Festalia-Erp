package com.festalia;

import com.festalia.calendario.ReservaConflitoValidator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Testes automatizados para validação de conflitos e regras do Calendário de Reservas.
 */
public class TesteCalendarioConflitos {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Iniciando bateria de testes do Calendário e Conflitos");
        System.out.println("==================================================");

        try {
            testarRegrasSobreposicaoHorario();
            testarVerificacaoListaConflitos();
            testarEventosMesmoDiaHorariosDiferentes();
            testarPersistenciaDAO();
            System.out.println("\n>>> TODOS OS TESTES PASSARAM COM SUCESSO! <<<");
        } catch (Throwable t) {
            System.err.println("\nFALHA NO TESTE: " + t.getMessage());
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void testarRegrasSobreposicaoHorario() {
        System.out.print("Testando regras de sobreposição de horário... ");

        LocalDate data = LocalDate.of(2026, 10, 15);

        OrdemServico os1 = criarOSExemplo(1, 101, data, LocalTime.of(18, 0), LocalTime.of(22, 0), "Confirmado");
        OrdemServico os2Sobreposta = criarOSExemplo(2, 102, data, LocalTime.of(20, 0), LocalTime.of(23, 0), "Confirmado");
        OrdemServico os3Identica = criarOSExemplo(3, 103, data, LocalTime.of(18, 0), LocalTime.of(22, 0), "Confirmado");
        OrdemServico os4Contida = criarOSExemplo(4, 104, data, LocalTime.of(19, 0), LocalTime.of(21, 0), "Confirmado");
        OrdemServico os5Adjacente = criarOSExemplo(5, 105, data, LocalTime.of(22, 0), LocalTime.of(23, 30), "Confirmado");
        OrdemServico os6OutroDia = criarOSExemplo(6, 106, data.plusDays(1), LocalTime.of(18, 0), LocalTime.of(22, 0), "Confirmado");
        OrdemServico os7Cancelada = criarOSExemplo(7, 107, data, LocalTime.of(18, 0), LocalTime.of(22, 0), "Cancelado");

        // Casos com conflito
        validar(ReservaConflitoValidator.temConflitoHorario(os1, os2Sobreposta), "Deveria haver conflito entre 18-22 e 20-23");
        validar(ReservaConflitoValidator.temConflitoHorario(os1, os3Identica), "Deveria haver conflito para horários idênticos 18-22 e 18-22");
        validar(ReservaConflitoValidator.temConflitoHorario(os1, os4Contida), "Deveria haver conflito para horário contido 19-21 em 18-22");

        // Casos sem conflito
        validar(!ReservaConflitoValidator.temConflitoHorario(os1, os5Adjacente), "Adjacente (término 22:00 vs início 22:00) NÃO deve ser conflito");
        validar(!ReservaConflitoValidator.temConflitoHorario(os1, os6OutroDia), "Datas diferentes NÃO devem gerar conflito");
        validar(!ReservaConflitoValidator.temConflitoHorario(os1, os7Cancelada), "Eventos cancelados NÃO devem gerar conflito");
        validar(!ReservaConflitoValidator.temConflitoHorario(os1, os1), "Mesmo evento (mesmo ID) NÃO deve gerar conflito consigo mesmo");

        System.out.println("OK!");
    }

    private static void testarEventosMesmoDiaHorariosDiferentes() {
        System.out.print("Testando eventos no mesmo dia em horários diferentes (Almoço vs Jantar)... ");

        LocalDate data = LocalDate.of(2026, 10, 20);
        OrdemServico almoco = criarOSExemplo(10, 201, data, LocalTime.of(11, 30), LocalTime.of(15, 30), "Confirmado");
        OrdemServico jantar = criarOSExemplo(11, 202, data, LocalTime.of(18, 0), LocalTime.of(23, 0), "Confirmado");

        validar(!ReservaConflitoValidator.temConflitoHorario(almoco, jantar),
                "Eventos no mesmo dia em faixas distintas NÃO devem ser tratados como conflito");

        System.out.println("OK!");
    }

    private static void testarVerificacaoListaConflitos() {
        System.out.print("Testando busca de conflitos em lista... ");

        LocalDate data = LocalDate.of(2026, 11, 5);
        List<OrdemServico> existentes = new ArrayList<>();
        existentes.add(criarOSExemplo(20, 301, data, LocalTime.of(16, 0), LocalTime.of(20, 0), "Confirmado"));
        existentes.add(criarOSExemplo(21, 302, data, LocalTime.of(19, 0), LocalTime.of(23, 0), "Confirmado"));
        existentes.add(criarOSExemplo(22, 303, data.plusDays(1), LocalTime.of(18, 0), LocalTime.of(22, 0), "Confirmado"));

        OrdemServico novaSobreposta = criarOSExemplo(0, 304, data, LocalTime.of(18, 30), LocalTime.of(21, 30), "Confirmado");
        List<OrdemServico> conflitos = ReservaConflitoValidator.verificarConflitos(novaSobreposta, existentes);

        validar(conflitos.size() == 2, "Deveria encontrar exatamente 2 conflitos (OS 301 e OS 302)");
        validar(conflitos.get(0).getNumeroOS() == 301 || conflitos.get(0).getNumeroOS() == 302, "Conflito identificado incorreto");

        System.out.println("OK! (" + conflitos.size() + " conflitos detectados)");
    }

    private static void testarPersistenciaDAO() throws Exception {
        System.out.print("Testando migrações de banco e persistência DAO com novos campos... ");

        OrdemServicoDAO dao = new OrdemServicoDAO();
        LocalDate dataTeste = LocalDate.of(2026, 12, 25);
        LocalTime inicio = LocalTime.of(19, 30);
        LocalTime fim = LocalTime.of(23, 30);
        String tipo = "Corporativo";

        OrdemServico os = criarOSExemplo(0, 9999, dataTeste, inicio, fim, "Confirmado");
        os.setTipoEvento(tipo);
        os.setConflitoConfirmado(true);

        dao.salvar(os);
        validar(os.getId() > 0, "DAO deveria ter atribuído ID gerado");

        // Recuperar e validar
        List<OrdemServico> todas = dao.listarTodas();
        OrdemServico recuperada = todas.stream().filter(o -> o.getId() == os.getId()).findFirst().orElse(null);
        validar(recuperada != null, "OS deveria existir no banco");
        validar(recuperada.getHorarioInicio().equals(inicio), "Horário de início incorreto: " + recuperada.getHorarioInicio());
        validar(recuperada.getHorarioFim().equals(fim), "Horário de término incorreto: " + recuperada.getHorarioFim());
        validar(tipo.equals(recuperada.getTipoEvento()), "Tipo de evento incorreto: " + recuperada.getTipoEvento());
        validar(recuperada.isConflitoConfirmado(), "Flag de conflito confirmado deveria ser true");

        // Atualizar horário
        recuperada.setHorarioInicio(LocalTime.of(20, 0));
        dao.salvar(recuperada);

        List<OrdemServico> atualizadas = dao.listarTodas();
        OrdemServico osAtualizada = atualizadas.stream().filter(o -> o.getId() == os.getId()).findFirst().orElse(null);
        validar(osAtualizada.getHorarioInicio().equals(LocalTime.of(20, 0)), "Horário atualizado incorreto");

        // Limpeza do registro de teste
        dao.excluir(os.getId());
        System.out.println("OK!");
    }

    private static OrdemServico criarOSExemplo(int id, int numeroOS, LocalDate data, LocalTime inicio, LocalTime fim, String status) {
        Cardapio c = new Cardapio("Menu Teste", "Descricao");
        return new OrdemServico(
                id,
                numeroOS,
                "Festalia Buffet",
                "Gestão",
                null,
                "Cliente Teste #" + numeroOS,
                "(11) 99999-9999",
                data,
                "Salão de Festas",
                120,
                150.0,
                10.0,
                300.0,
                19800.0,
                status,
                c,
                "Observação de teste",
                null,
                inicio,
                fim,
                "Casamento",
                false
        );
    }

    private static void validar(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError("VALIDAÇÃO FALHOU: " + mensagem);
        }
    }
}
