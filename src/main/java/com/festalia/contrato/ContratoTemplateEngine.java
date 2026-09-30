package com.festalia.contrato;

import com.festalia.Cardapio;
import com.festalia.OrdemServico;
import com.festalia.financeiro.NumeroPorExtensoUtil;
import com.festalia.financeiro.ParcelaOS;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Motor de Templates e Geração de Minutas Contratuais do Festalia ERP.
 * Monta as 14 cláusulas jurídicas fundamentadas no Código Civil, CDC (Lei 8.078/90) e LGPD (Lei 13.709/18),
 * integrando dados da empresa, do cliente, da OS e do cronograma financeiro.
 */
public class ContratoTemplateEngine {

    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class ParametrosContrato {
        public boolean incluirCancelamento = true;
        public boolean incluirAlteracaoEscopo = true;
        public boolean incluirDanosLoucas = true;
        public boolean incluirLGPD = true;
        public boolean incluirDireitoArrependimento = false;
        public boolean incluirCronogramaDetalhado = true;
        public boolean incluirTestemunhas = true;

        public int prazoAntecedenciaCancelamentoDias = 30;
        public double percentualRetencaoDespesasAdm = 15.0; // Despesas administrativas prévias comprovadas
        public int prazoConfirmacaoFinalConvidadosDias = 7;
        public int toleranciaHorarioMinutos = 30;
        public String observacoesEspeciais = "";
    }

    /**
     * Validação rigorosa dos dados prévios antes da emissão do contrato.
     */
    public static ValidacaoContratoResultado validar(OrdemServico os, Contrato contrato, List<ParcelaOS> parcelas) {
        ValidacaoContratoResultado res = new ValidacaoContratoResultado();

        // 1. Dados do Contratante
        String nome = contrato != null && contrato.getContratanteNome() != null && !contrato.getContratanteNome().isBlank()
                ? contrato.getContratanteNome().trim() : (os != null ? os.getCliente().trim() : "");
        if (nome.isBlank()) {
            res.adicionarPendencia("Nome do Contratante / Cliente não informado.");
        }

        String doc = contrato != null ? contrato.getContratanteCpfCnpj() : "";
        if (doc == null || doc.trim().isBlank()) {
            res.adicionarPendencia("CPF ou CNPJ do Contratante não informado.");
        } else {
            String docLimpo = doc.replaceAll("[^0-9]", "");
            if (docLimpo.length() != 11 && docLimpo.length() != 14) {
                res.adicionarAviso("O documento do contratante parece incompleto (" + doc + "). Verifique a formatação do CPF (11 dígitos) ou CNPJ (14 dígitos).");
            }
        }

        if (contrato != null && (contrato.getContratanteEndereco() == null || contrato.getContratanteEndereco().isBlank())) {
            res.adicionarAviso("Endereço completo do Contratante não informado na qualificação.");
        }

        // 2. Dados da Empresa
        if (contrato != null && (contrato.getEmpresaCnpj() == null || contrato.getEmpresaCnpj().isBlank())) {
            res.adicionarAviso("CNPJ da Empresa de Buffet não cadastrado nas preferências.");
        }

        // 3. Dados do Evento e Vinculação da OS
        if (os == null && contrato != null && (contrato.getOrdemServicoId() > 0 || contrato.getNumeroOS() > 0)) {
            int buscaId = contrato.getOrdemServicoId() > 0 ? contrato.getOrdemServicoId() : contrato.getNumeroOS();
            os = new com.festalia.OrdemServicoDAO().buscarPorNumeroOS(buscaId);
            if (os == null) {
                os = new com.festalia.OrdemServicoDAO().buscarPorId(buscaId);
            }
        }

        LocalDate dataEv = os != null ? os.getDataEvento() : (contrato != null ? contrato.getDataEvento() : null);
        String localEv = os != null ? os.getLocal() : (contrato != null ? contrato.getLocalEvento() : "");
        int qtdConv = os != null ? os.getQuantidadeConvidados() : (contrato != null ? contrato.getNumeroConvidados() : 0);
        double valTot = os != null ? os.getValorTotal() : (contrato != null ? contrato.getValorTotal() : 0.0);

        if (os == null && (contrato == null || (contrato.getOrdemServicoId() <= 0 && contrato.getNumeroOS() <= 0))) {
            res.adicionarPendencia("Ordem de Serviço não alocada para este contrato. Vincule uma OS para prosseguir.");
            return res;
        }

        if (dataEv == null) {
            res.adicionarPendencia("Data do evento não informada.");
        } else if (dataEv.isBefore(LocalDate.now())) {
            res.adicionarAviso("A data do evento cadastrada é anterior à data de hoje.");
        }

        if (localEv == null || localEv.isBlank()) {
            res.adicionarAviso("Local de realização do evento não definido.");
        }

        if (qtdConv <= 0) {
            res.adicionarPendencia("Quantidade de convidados estimada deve ser maior que zero.");
        }

        if (valTot <= 0.0) {
            res.adicionarPendencia("Valor total do contrato não pode ser zero ou negativo.");
        }

        // 4. Consistência Financeira do Cronograma
        if (parcelas == null || parcelas.isEmpty()) {
            res.adicionarAviso("Nenhum cronograma financeiro gerado para a OS. O contrato utilizará as condições gerais de pagamento à vista.");
        } else {
            double totalParcelas = parcelas.stream().mapToDouble(ParcelaOS::getValor).sum();
            double diff = Math.abs(totalParcelas - os.getValorTotal());
            if (diff > 0.05) {
                res.adicionarAviso(String.format(
                        "A soma do cronograma financeiro (R$ %,.2f) diverge do valor total da OS (R$ %,.2f) em R$ %,.2f.",
                        totalParcelas, os.getValorTotal(), diff
                ));
            }

            // Verifica parcelas vencidas
            long vencidas = parcelas.stream().filter(p -> p.getDiasAtraso() > 0).count();
            if (vencidas > 0) {
                res.adicionarAviso("Existem " + vencidas + " parcela(s) com vencimento anterior à data de emissão.");
            }
        }

        return res;
    }

    /**
     * Monta a lista completa de cláusulas ativas com conteúdo dinâmico.
     */
    public static List<ClausulaContrato> gerarClausulas(OrdemServico os, Contrato c, List<ParcelaOS> parcelas, ParametrosContrato params) {
        List<ClausulaContrato> lista = new ArrayList<>();
        int num = 1;

        // CLÁUSULA 1ª — DO OBJETO
        String objTexto = String.format(
                "O presente instrumento tem por objeto a prestação de serviços especializados de buffet gastronômico, " +
                "bebidas e infraestrutura de atendimento para a realização do evento denominado \"%s\", sob coordenação " +
                "da CONTRATADA, em conformidade com as especificações estipuladas na Ordem de Serviço Nº %d vinculada.",
                c.getTipoEvento(), os.getNumeroOS()
        );
        lista.add(new ClausulaContrato(num++, "OBJETO", "Do Objeto", objTexto, true, true));

        // CLÁUSULA 2ª — DO EVENTO E LOGÍSTICA
        String evTexto = String.format(
                "O evento será realizado no dia %s, com horário de início previsto para as %s e término às %s.\n" +
                "Parágrafo Primeiro: O local de execução será no endereço: %s.\n" +
                "Parágrafo Segundo: A estimativa contratada é de %d (%s) convidados pagantes, com tolerância técnica " +
                "operacional de até %d minutos para início do serviço gastronômico.",
                os.getDataEventoFormatada(),
                os.getHorarioInicioFormatado(),
                os.getHorarioFimFormatado(),
                (os.getLocal() != null && !os.getLocal().isBlank() ? os.getLocal() : "Espaço Festalia / Salão indicado pelo Contratante"),
                os.getQuantidadeConvidados(),
                NumeroPorExtensoUtil.converter(os.getQuantidadeConvidados()).replace("reais", "pessoas").replace("real", "pessoa"),
                params.toleranciaHorarioMinutos
        );
        lista.add(new ClausulaContrato(num++, "EVENTO", "Do Evento e Logística", evTexto, true, true));

        // CLÁUSULA 3ª — DOS SERVIÇOS CONTRATADOS E CARDÁPIO
        StringBuilder sbCardapio = new StringBuilder();
        Cardapio card = os.getCardapioSelecionado();
        if (card != null) {
            sbCardapio.append("Cardápio Contratado: ").append(card.getNome()).append(".\n");
            if (card.getDescricao() != null && !card.getDescricao().isBlank()) {
                sbCardapio.append(card.getDescricao()).append("\n");
            }
            if (card.getItens() != null && !card.getItens().isEmpty()) {
                sbCardapio.append("Composição gastronômica inclusa:\n");
                for (String item : card.getItens()) {
                    sbCardapio.append(" • ").append(item).append("\n");
                }
            }
        } else {
            sbCardapio.append("Serviço de buffet completo conforme alinhamento prévio e memorial da Ordem de Serviço Nº ").append(os.getNumeroOS()).append(".\n");
        }
        sbCardapio.append("Parágrafo Único: Estão inclusos os serviços de copeiros, garçons, montagem de mise en place " +
                "e suporte de salão durante o período contratado.");
        lista.add(new ClausulaContrato(num++, "SERVICOS", "Dos Serviços Contratados e Cardápio", sbCardapio.toString().trim(), true, true));

        // CLÁUSULA 4ª — DO PREÇO E CONDIÇÕES DE PAGAMENTO
        StringBuilder sbFin = new StringBuilder();
        sbFin.append(String.format("Pela prestação dos serviços ora pactuados, o(a) CONTRATANTE pagará à CONTRATADA o valor total de %s (%s).\n",
                os.getValorTotalFormatado(), NumeroPorExtensoUtil.converter(os.getValorTotal())));

        if (parcelas != null && !parcelas.isEmpty() && params.incluirCronogramaDetalhado) {
            sbFin.append("O pagamento dar-se-á conforme o seguinte cronograma financeiro ajustado:\n");
            for (ParcelaOS p : parcelas) {
                sbFin.append(String.format(" • Parcela %02d: %s (%s) — Vencimento: %s — Forma: %s;\n",
                        p.getNumeroParcela(), p.getDescricao(), p.getValorFormatado(),
                        p.getDataVencimentoFormatada(), p.getFormaPagamentoSugerida()));
            }
            sbFin.append("Parágrafo Primeiro: O atraso injustificado no pagamento de qualquer parcela sujeitará o Contratante a " +
                    "atualização monetária e juros moratórios legais de 1% ao mês, calculados pro rata die.\n");
            sbFin.append("Parágrafo Segundo: A quitação integral do evento deverá ocorrer obrigatoriamente antes do início da " +
                    "montagem estrutural, ressalvado eventual acordo prévio formalizado por aditivo.");
        } else {
            sbFin.append("O pagamento será realizado em moeda corrente nacional, mediante as condições operacionais " +
                    "registradas na Ordem de Serviço, com quitação prévia ao evento.");
        }
        lista.add(new ClausulaContrato(num++, "VALOR_PAGAMENTO", "Do Preço e Condições de Pagamento", sbFin.toString().trim(), true, true));

        // CLÁUSULA 5ª — DAS OBRIGAÇÕES DA CONTRATADA
        String obgContratada = """
            São obrigações da CONTRATADA:
            a) Fornecer gêneros alimentícios e insumos de primeira qualidade, higienizados e manipulados sob estrito cumprimento das normas sanitárias vigentes;
            b) Disponibilizar equipe de atendimento treinada, uniformizada e devidamente capacitada para o porte do evento;
            c) Cumprir os horários pactuados para início de montagem, serviço de buffet e encerramento;
            d) Manter a cordialidade, ordem e zelo com os ambientes e equipamentos disponibilizados;
            e) Comunicar imediatamente ao CONTRATANTE qualquer intercorrência de força maior que possa afetar o cronograma.
            """.trim();
        lista.add(new ClausulaContrato(num++, "OBRIGACOES_CONTRATADA", "Das Obrigações da Contratada", obgContratada, true, true));

        // CLÁUSULA 6ª — DAS OBRIGAÇÕES DO CONTRATANTE
        String obgContratante = String.format("""
            São obrigações do(a) CONTRATANTE:
            a) Efetuar pontualmente os pagamentos nos termos e prazos estipulados na Cláusula 4ª;
            b) Assegurar à equipe da CONTRATADA livre acesso ao local com antecedência mínima de 3 (três) horas para descarga e montagem;
            c) Garantir que o local do evento conte com infraestrutura básica de energia elétrica estável, água encanada potável e espaço adequado para apoio térmico/higienização;
            d) Informar com antecedência mínima de %d (%s) dias a quantidade final e definitiva de convidados pagantes;
            e) Responder por comportamentos abusivos ou danos deliberados causados aos funcionários da CONTRATADA por convidados presentes.
            """.trim(), params.prazoConfirmacaoFinalConvidadosDias, NumeroPorExtensoUtil.converter(params.prazoConfirmacaoFinalConvidadosDias).replace("reais", "dias").replace("real", "dia"));
        lista.add(new ClausulaContrato(num++, "OBRIGACOES_CONTRATANTE", "Das Obrigações do Contratante", obgContratante, true, true));

        // CLÁUSULA 7ª — DO CANCELAMENTO E DA RESCISÃO CONTRATUAL
        if (params.incluirCancelamento) {
            String cancTexto = String.format(
                    "Em caso de resilição unilateral (cancelamento) por iniciativa de qualquer das partes, aplicam-se as seguintes regras fundamentadas na boa-fé e equilíbrio contratual:\n" +
                    "I - Se o cancelamento ocorrer com antecedência igual ou superior a %d dias da data do evento, a CONTRATADA reterá " +
                    "o percentual de %.1f%% do valor total do contrato, exclusivamente para cobertura de despesas administrativas e reserva da data, " +
                    "restituindo o saldo remanescente devidamente atualizado no prazo de até 15 dias úteis;\n" +
                    "II - Se o cancelamento ocorrer com prazo inferior a %d dias, serão deduzidos, além da taxa administrativa, os custos com " +
                    "ingredientes perecíveis e insumos já comprovadamente encomendados ou manipulados especificamente para a celebração;\n" +
                    "III - Em caso de rescisão motivada por inadimplemento culposo comprovado da CONTRATADA, os valores pagos serão restituídos integralmente " +
                    "ao CONTRATANTE, sem prejuízo da apuração de eventuais perdas e danos nos termos do Código Civil.",
                    params.prazoAntecedenciaCancelamentoDias, params.percentualRetencaoDespesasAdm, params.prazoAntecedenciaCancelamentoDias
            );
            lista.add(new ClausulaContrato(num++, "CANCELAMENTO_RESCISAO", "Do Cancelamento e da Rescisão Contratual", cancTexto, false, true));
        }

        // CLÁUSULA 8ª — DO DIREITO DE ARREPENDIMENTO (CDC)
        if (params.incluirDireitoArrependimento) {
            String cdcTexto = """
                Na hipótese desta contratação ter ocorrido fora do estabelecimento comercial (via telefone, internet ou domicílio), é assegurado ao(à) CONTRATANTE o direito de arrependimento no prazo improrrogável de até 7 (sete) dias corridos a contar da assinatura deste contrato, nos exatos termos do Artigo 49 da Lei Federal nº 8.078/1990 (Código de Defesa do Consumidor), ensejando a devolução integral de eventuais quantias pagas a título de sinal.
                """.trim();
            lista.add(new ClausulaContrato(num++, "ARREPENDIMENTO_CDC", "Do Direito de Arrependimento", cdcTexto, false, true));
        }

        // CLÁUSULA 9ª — DOS DANOS A BENS, LOUÇAS E UTENSÍLIOS
        if (params.incluirDanosLoucas) {
            String danosTexto = """
                A CONTRATADA disponibilizará durante o evento taças, copos, pratos, talheres, rechauds e mobiliário de apoio em perfeito estado de uso e conservação.
                Parágrafo Primeiro: Quebras de louças e copos decorrentes de acidentes ordinários dentro do limite de tolerância usual de até 3% (três por cento) do acervo serão absorvidas pela CONTRATADA sem custo ao cliente.
                Parágrafo Segundo: Danos deliberados, extravios e quebras extraordinárias comprovadas que excedam o limite técnico serão indenizados pelo CONTRATANTE a preço de reposição de atacado comprovado por nota fiscal de fornecedor, vedada a cobrança arbitrária ou abusiva.
                """.trim();
            lista.add(new ClausulaContrato(num++, "DANOS_LOUCAS", "Dos Bens, Louças e Utensílios", danosTexto, false, true));
        }

        // CLÁUSULA 10ª — DA ALTERAÇÃO DO ESCOPO DO EVENTO
        if (params.incluirAlteracaoEscopo) {
            String escopoTexto = String.format(
                    "Qualquer acréscimo de convidados, extensão de horário ou modificação de itens do cardápio solicitada após a assinatura deste contrato " +
                    "estará sujeita à disponibilidade operacional da CONTRATADA e deverá ser formalizada por aditivo.\n" +
                    "Parágrafo Primeiro: O acréscimo de convidados será cobrado com base no valor unitário por pessoa de R$ %,.2f, informado até %d dias antes do evento.\n" +
                    "Parágrafo Segundo: Caso no dia do evento o número de presentes ultrapasse em até 10%% o contratado, o excedente será calculado e " +
                    "faturado imediatamente após o término das celebrações.",
                    os.getPrecoPorPessoa(), params.prazoConfirmacaoFinalConvidadosDias
            );
            lista.add(new ClausulaContrato(num++, "ALTERACAO_ESCOPO", "Da Alteração do Escopo do Evento", escopoTexto, false, true));
        }

        // CLÁUSULA 11ª — DA EXECUÇÃO DOS SERVIÇOS E SEGURANÇA ALIMENTAR
        String execTexto = """
            A CONTRATADA executará os serviços com estrita observância das normas da ANVISA e boas práticas de manipulação de alimentos.
            Parágrafo Primeiro: Em observância às normas de segurança alimentar e saúde pública, não é recomendado o consumo ou reaproveitamento posterior de alimentos perecíveis que permaneceram expostos em temperatura ambiente além do período técnico recomendado.
            Parágrafo Segundo: Alimentos perecíveis levados pelos anfitriões após o término do serviço passam à sua exclusiva responsabilidade de refrigeração e acondicionamento.
            """.trim();
        lista.add(new ClausulaContrato(num++, "EXECUCAO_SERVICOS", "Da Execução dos Serviços e Segurança Alimentar", execTexto, true, true));

        // CLÁUSULA 12ª — DA PROTEÇÃO DE DADOS PESSOAIS (LGPD)
        if (params.incluirLGPD) {
            String lgpdTexto = """
                As partes declaram cumprir as disposições da Lei Geral de Proteção de Dados (Lei Federal nº 13.709/2018).
                Os dados pessoais fornecidos pelo CONTRATANTE (nome, documento, telefone, e-mail e endereço) serão utilizados exclusivamente para a execução deste contrato, emissão de ordens de serviço, notas fiscais e obrigações tributárias legais, sendo vedado seu compartilhamento não autorizado com terceiros para fins publicitários ou comerciais.
                """.trim();
            lista.add(new ClausulaContrato(num++, "PROTECAO_DADOS_LGPD", "Da Proteção de Dados Pessoais (LGPD)", lgpdTexto, false, true));
        }

        // CLÁUSULA 13ª — DAS DISPOSIÇÕES GERAIS E CASO FORTUITO
        String dispGerais = """
            Nenhuma das partes será responsabilizada por falhas de execução decorrentes de caso fortuito ou força maior, tais como temporais extremos, interrupção pública prolongada de fornecimento elétrico municipal ou decretos de calamidade, nos termos do Artigo 393 do Código Civil Brasileiro.
            Parágrafo Único: O presente contrato obriga as partes e seus sucessores a qualquer título, constituindo o acordo integral entre contratante e buffet para este evento.
            """.trim();
        if (params.observacoesEspeciais != null && !params.observacoesEspeciais.isBlank()) {
            dispGerais += "\nObservações Especiais Ajustadas: " + params.observacoesEspeciais.trim();
        }
        lista.add(new ClausulaContrato(num++, "DISPOSICOES_GERAIS", "Das Disposições Gerais", dispGerais, true, true));

        // CLÁUSULA 14ª — DO FORO
        String foroTexto = String.format(
                "Para dirimir quaisquer controvérsias oriundas da interpretação ou execução deste contrato, as partes elegem preferencialmente o Foro da Comarca de %s, " +
                "assegurando-se, nos termos do Artigo 101, I, do Código de Defesa do Consumidor, a prerrogativa do consumidor de ajuizar demanda no foro de seu domicílio caso aplicável.",
                (c.getEmpresaCidadeForo() != null && !c.getEmpresaCidadeForo().isBlank() ? c.getEmpresaCidadeForo() : "São Paulo - SP")
        );
        lista.add(new ClausulaContrato(num++, "FORO", "Do Foro", foroTexto, true, true));

        return lista;
    }

    /**
     * Compila o texto integral da minuta contratual em formato textual corrido para revisão na interface.
     */
    public static String compilarTextoMinuta(Contrato c, List<ClausulaContrato> clausulas) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append(c.getModeloTipo().getTitulo().toUpperCase()).append("\n");
        sb.append("INSTRUMENTO PARTICULAR DE PRESTAÇÃO DE SERVIÇOS DE BUFFET E EVENTOS\n");
        sb.append("Contrato Nº: ").append(c.getNumeroContrato()).append("  •  Versão: ").append(c.getVersaoAtual()).append("\n");
        sb.append("================================================================================\n\n");

        sb.append("DAS PARTES CONTRATANTES:\n\n");
        sb.append("CONTRATADA (EMPRESA PRESTADORA):\n");
        sb.append(c.getEmpresaRazaoSocial()).append(" (Nome Fantasia: ").append(c.getEmpresaNomeFantasia()).append("), ");
        sb.append("inscrita no CNPJ sob o nº ").append(c.getEmpresaCnpj()).append(", com sede em ").append(c.getEmpresaEndereco());
        if (c.getEmpresaTelefone() != null && !c.getEmpresaTelefone().isBlank()) {
            sb.append(", Telefone: ").append(c.getEmpresaTelefone());
        }
        if (c.getEmpresaEmail() != null && !c.getEmpresaEmail().isBlank()) {
            sb.append(", E-mail: ").append(c.getEmpresaEmail());
        }
        if (c.getEmpresaRepresentanteNome() != null && !c.getEmpresaRepresentanteNome().isBlank()) {
            sb.append(", neste ato representada por seu administrador legal ").append(c.getEmpresaRepresentanteNome());
            if (c.getEmpresaRepresentanteCpf() != null && !c.getEmpresaRepresentanteCpf().isBlank()) {
                sb.append(", CPF nº ").append(c.getEmpresaRepresentanteCpf());
            }
        }
        sb.append(".\n\n");

        sb.append("CONTRATANTE (CLIENTE):\n");
        sb.append(c.getContratanteNome()).append(", inscrito(a) no CPF/CNPJ sob o nº ").append(c.getContratanteCpfCnpj());
        if (c.getContratanteRg() != null && !c.getContratanteRg().isBlank()) {
            sb.append(", RG nº ").append(c.getContratanteRg());
        }
        if (c.getContratanteEndereco() != null && !c.getContratanteEndereco().isBlank()) {
            sb.append(", residente e domiciliado(a) em ").append(c.getContratanteEndereco());
        }
        if (c.getContratanteTelefone() != null && !c.getContratanteTelefone().isBlank()) {
            sb.append(", Telefone: ").append(c.getContratanteTelefone());
        }
        if (c.getContratanteEmail() != null && !c.getContratanteEmail().isBlank()) {
            sb.append(", E-mail: ").append(c.getContratanteEmail());
        }
        sb.append(".\n\n");

        sb.append("As partes acima identificadas têm, entre si, justo e acertado o presente Contrato de Prestação de Serviços, mediante as seguintes cláusulas:\n\n");

        int idx = 1;
        for (ClausulaContrato cl : clausulas) {
            if (!cl.isAtiva()) continue;
            sb.append("CLÁUSULA ").append(idx++).append("ª — ").append(cl.getTitulo().toUpperCase()).append("\n");
            sb.append(cl.getConteudo()).append("\n\n");
        }

        sb.append("E, por estarem assim justas e contratadas, as partes assinam o presente instrumento em 2 (duas) vias de igual teor e forma, na presença de 2 (duas) testemunhas instrumentárias.\n\n");
        sb.append(c.getEmpresaCidadeForo()).append(", ").append(c.getDataEmissaoFormatada()).append(".\n\n");

        sb.append("_____________________________________________         _____________________________________________\n");
        sb.append("CONTRATADA: ").append(c.getEmpresaNomeFantasia()).append("          CONTRATANTE: ").append(c.getContratanteNome()).append("\n");
        sb.append("CNPJ: ").append(c.getEmpresaCnpj()).append("                    CPF/CNPJ: ").append(c.getContratanteCpfCnpj()).append("\n\n");

        sb.append("TESTEMUNHAS:\n\n");
        sb.append("1. __________________________________________         2. __________________________________________\n");
        sb.append("Nome:                                                 Nome:\n");
        sb.append("CPF:                                                  CPF:\n");

        return sb.toString();
    }

    public static ValidacaoContratoResultado validarDadosParaContrato(Contrato c) {
        com.festalia.OrdemServico os = null;
        if (c != null && (c.getOrdemServicoId() > 0 || c.getNumeroOS() > 0)) {
            int buscaId = c.getOrdemServicoId() > 0 ? c.getOrdemServicoId() : c.getNumeroOS();
            os = new com.festalia.OrdemServicoDAO().buscarPorNumeroOS(buscaId);
            if (os == null) {
                os = new com.festalia.OrdemServicoDAO().buscarPorId(buscaId);
            }
        }
        List<ParcelaOS> parcelas = null;
        if (c != null && c.getOrdemServicoId() > 0) {
            try {
                parcelas = new com.festalia.financeiro.FinanceiroDAO().listarParcelasPorOS(c.getOrdemServicoId());
            } catch (Exception ignored) {}
        }
        return validar(os, c, parcelas);
    }

    public static List<ClausulaContrato> criarClausulasPadrao(ModeloContrato modelo) {
        ParametrosContrato params = new ParametrosContrato();
        Contrato c = new Contrato();
        c.setModeloTipo(modelo != null ? modelo : ModeloContrato.BUFFET);
        OrdemServico osMock = new OrdemServico(0, 1, "Festalia Buffet", "Gestão de Buffets", null,
                "Cliente", "", LocalDate.now(), "Espaço Contratado", 100, 150.0, 0.0, 0.0, 15000.0,
                "Confirmado", null, "", null, java.time.LocalTime.of(18, 0), java.time.LocalTime.of(22, 0), "Casamento", false);
        return gerarClausulas(osMock, c, new ArrayList<>(), params);
    }

    public static String compilarTextoCompleto(Contrato c, List<?> parcelasObj) {
        List<ClausulaContrato> clausulas = c.getClausulas();
        if (clausulas == null || clausulas.isEmpty()) {
            clausulas = criarClausulasPadrao(c.getModeloTipo());
        }
        return compilarTextoMinuta(c, clausulas);
    }
}
