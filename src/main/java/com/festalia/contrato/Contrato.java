package com.festalia.contrato;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade mestre que representa um Contrato de Prestação de Serviços formal vinculado à Ordem de Serviço (OS).
 */
public class Contrato {

    public static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private int ordemServicoId;
    private int numeroOS; // Informação para exibição e joins
    private String numeroContrato;
    private ModeloContrato modeloTipo = ModeloContrato.BUFFET;
    private String versaoAtual = "1.0";
    private StatusContrato status = StatusContrato.RASCUNHO;

    // Dados do Contratante (Cliente)
    private String contratanteNome = "";
    private String contratanteCpfCnpj = "";
    private String contratanteRg = "";
    private String contratanteEndereco = "";
    private String contratanteTelefone = "";
    private String contratanteEmail = "";

    // Dados da Contratada (Empresa de Buffet)
    private String empresaRazaoSocial = "Festalia Buffet & Eventos Ltda";
    private String empresaNomeFantasia = "Festalia Buffet";
    private String empresaCnpj = "12.345.678/0001-90";
    private String empresaEndereco = "Rua das Flores, 500 - Jardins, São Paulo/SP";
    private String empresaTelefone = "(11) 3456-7890";
    private String empresaEmail = "contato@festalia.com.br";
    private String empresaRepresentanteNome = "Diretoria Festalia";
    private String empresaRepresentanteCpf = "";
    private String empresaCidadeForo = "São Paulo/SP";

    // Dados do Evento e Financeiro
    private String tipoEvento = "Casamento";
    private double valorTotal = 0.0;
    private LocalDate dataEvento;
    private String horarioEvento = "18:00 às 22:00";
    private String localEvento = "";
    private int numeroConvidados = 100;
    private String cardapioServicos = "";
    private String textoMinuta = "";
    private List<ClausulaContrato> clausulas = new ArrayList<>();

    private LocalDate dataEmissao;
    private LocalDate dataAssinatura;
    private LocalDate dataCancelamento;
    private String observacoesEspeciais = "";
    private String caminhoPdf;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    private List<VersaoContrato> versoes = new ArrayList<>();

    public Contrato() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        this.dataEmissao = LocalDate.now();
        this.status = StatusContrato.RASCUNHO;
    }

    public Contrato(int id, int ordemServicoId, String numeroContrato, ModeloContrato modeloTipo,
                    String versaoAtual, StatusContrato status, String contratanteNome, String contratanteCpfCnpj,
                    String contratanteRg, String contratanteEndereco, String contratanteTelefone,
                    String contratanteEmail, String empresaRazaoSocial, String empresaNomeFantasia,
                    String empresaCnpj, String empresaEndereco, String empresaTelefone, String empresaEmail,
                    String empresaRepresentanteNome, String empresaRepresentanteCpf, String empresaCidadeForo,
                    double valorTotal, LocalDate dataEvento, LocalDate dataEmissao, LocalDate dataAssinatura,
                    LocalDate dataCancelamento, String observacoesEspeciais, String caminhoPdf,
                    LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
        this.id = id;
        this.ordemServicoId = ordemServicoId;
        this.numeroContrato = numeroContrato;
        this.modeloTipo = modeloTipo != null ? modeloTipo : ModeloContrato.BUFFET;
        this.versaoAtual = versaoAtual != null ? versaoAtual : "1.0";
        this.status = status != null ? status : StatusContrato.RASCUNHO;
        this.contratanteNome = contratanteNome != null ? contratanteNome : "";
        this.contratanteCpfCnpj = contratanteCpfCnpj != null ? contratanteCpfCnpj : "";
        this.contratanteRg = contratanteRg != null ? contratanteRg : "";
        this.contratanteEndereco = contratanteEndereco != null ? contratanteEndereco : "";
        this.contratanteTelefone = contratanteTelefone != null ? contratanteTelefone : "";
        this.contratanteEmail = contratanteEmail != null ? contratanteEmail : "";
        this.empresaRazaoSocial = empresaRazaoSocial;
        this.empresaNomeFantasia = empresaNomeFantasia;
        this.empresaCnpj = empresaCnpj;
        this.empresaEndereco = empresaEndereco;
        this.empresaTelefone = empresaTelefone;
        this.empresaEmail = empresaEmail;
        this.empresaRepresentanteNome = empresaRepresentanteNome;
        this.empresaRepresentanteCpf = empresaRepresentanteCpf;
        this.empresaCidadeForo = empresaCidadeForo;
        this.valorTotal = Math.max(0.0, valorTotal);
        this.dataEvento = dataEvento;
        this.dataEmissao = dataEmissao != null ? dataEmissao : LocalDate.now();
        this.dataAssinatura = dataAssinatura;
        this.dataCancelamento = dataCancelamento;
        this.observacoesEspeciais = observacoesEspeciais != null ? observacoesEspeciais : "";
        this.caminhoPdf = caminhoPdf;
        this.dataCriacao = dataCriacao != null ? dataCriacao : LocalDateTime.now();
        this.dataAtualizacao = dataAtualizacao != null ? dataAtualizacao : LocalDateTime.now();
    }

    // Getters and Setters com aliases para compatibilidade
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrdemServicoId() { return ordemServicoId; }
    public void setOrdemServicoId(int ordemServicoId) { this.ordemServicoId = ordemServicoId; }

    public int getOsId() { return ordemServicoId; }
    public void setOsId(int osId) { this.ordemServicoId = osId; }

    public int getNumeroOS() { return numeroOS; }
    public void setNumeroOS(int numeroOS) { this.numeroOS = numeroOS; }

    public String getNumeroContrato() { return numeroContrato; }
    public void setNumeroContrato(String numeroContrato) { this.numeroContrato = numeroContrato; }

    public ModeloContrato getModeloTipo() { return modeloTipo; }
    public void setModeloTipo(ModeloContrato modeloTipo) { this.modeloTipo = modeloTipo; }

    public ModeloContrato getModelo() { return modeloTipo; }
    public void setModelo(ModeloContrato modelo) { this.modeloTipo = modelo; }

    public String getVersaoAtual() { return versaoAtual; }
    public void setVersaoAtual(String versaoAtual) { this.versaoAtual = versaoAtual; }

    public StatusContrato getStatus() { return status; }
    public void setStatus(StatusContrato status) { this.status = status; }

    public String getContratanteNome() { return contratanteNome; }
    public void setContratanteNome(String contratanteNome) { this.contratanteNome = contratanteNome != null ? contratanteNome : ""; }

    public String getClienteNome() { return contratanteNome; }
    public void setClienteNome(String clienteNome) { this.contratanteNome = clienteNome != null ? clienteNome : ""; }

    public String getContratanteCpfCnpj() { return contratanteCpfCnpj; }
    public void setContratanteCpfCnpj(String contratanteCpfCnpj) { this.contratanteCpfCnpj = contratanteCpfCnpj != null ? contratanteCpfCnpj : ""; }

    public String getClienteCpfCnpj() { return contratanteCpfCnpj; }
    public void setClienteCpfCnpj(String cpfCnpj) { this.contratanteCpfCnpj = cpfCnpj != null ? cpfCnpj : ""; }

    public String getContratanteRg() { return contratanteRg; }
    public void setContratanteRg(String contratanteRg) { this.contratanteRg = contratanteRg != null ? contratanteRg : ""; }

    public String getClienteRg() { return contratanteRg; }
    public void setClienteRg(String rg) { this.contratanteRg = rg != null ? rg : ""; }

    public String getContratanteEndereco() { return contratanteEndereco; }
    public void setContratanteEndereco(String contratanteEndereco) { this.contratanteEndereco = contratanteEndereco != null ? contratanteEndereco : ""; }

    public String getClienteEndereco() { return contratanteEndereco; }
    public void setClienteEndereco(String end) { this.contratanteEndereco = end != null ? end : ""; }

    public String getContratanteTelefone() { return contratanteTelefone; }
    public void setContratanteTelefone(String contratanteTelefone) { this.contratanteTelefone = contratanteTelefone != null ? contratanteTelefone : ""; }

    public String getClienteTelefone() { return contratanteTelefone; }
    public void setClienteTelefone(String tel) { this.contratanteTelefone = tel != null ? tel : ""; }

    public String getContratanteEmail() { return contratanteEmail; }
    public void setContratanteEmail(String contratanteEmail) { this.contratanteEmail = contratanteEmail != null ? contratanteEmail : ""; }

    public String getClienteEmail() { return contratanteEmail; }
    public void setClienteEmail(String email) { this.contratanteEmail = email != null ? email : ""; }

    public String getEmpresaRazaoSocial() { return empresaRazaoSocial; }
    public void setEmpresaRazaoSocial(String empresaRazaoSocial) { this.empresaRazaoSocial = empresaRazaoSocial; }

    public String getEmpresaNomeFantasia() { return empresaNomeFantasia; }
    public void setEmpresaNomeFantasia(String empresaNomeFantasia) { this.empresaNomeFantasia = empresaNomeFantasia; }

    public String getEmpresaCnpj() { return empresaCnpj; }
    public void setEmpresaCnpj(String empresaCnpj) { this.empresaCnpj = empresaCnpj; }

    public String getEmpresaEndereco() { return empresaEndereco; }
    public void setEmpresaEndereco(String empresaEndereco) { this.empresaEndereco = empresaEndereco; }

    public String getEmpresaTelefone() { return empresaTelefone; }
    public void setEmpresaTelefone(String empresaTelefone) { this.empresaTelefone = empresaTelefone; }

    public String getEmpresaEmail() { return empresaEmail; }
    public void setEmpresaEmail(String empresaEmail) { this.empresaEmail = empresaEmail; }

    public String getEmpresaRepresentanteNome() { return empresaRepresentanteNome; }
    public void setEmpresaRepresentanteNome(String empresaRepresentanteNome) { this.empresaRepresentanteNome = empresaRepresentanteNome; }

    public String getEmpresaRepresentante() { return empresaRepresentanteNome; }
    public void setEmpresaRepresentante(String rep) { this.empresaRepresentanteNome = rep; }

    public String getEmpresaRepresentanteCpf() { return empresaRepresentanteCpf; }
    public void setEmpresaRepresentanteCpf(String empresaRepresentanteCpf) { this.empresaRepresentanteCpf = empresaRepresentanteCpf; }

    public String getEmpresaCidadeForo() { return empresaCidadeForo; }
    public void setEmpresaCidadeForo(String empresaCidadeForo) { this.empresaCidadeForo = empresaCidadeForo; }

    public String getEmpresaForoComarca() { return empresaCidadeForo; }
    public void setEmpresaForoComarca(String foro) { this.empresaCidadeForo = foro; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public LocalDate getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDate dataEvento) { this.dataEvento = dataEvento; }

    public void setDataEvento(String dataStr) {
        if (dataStr == null || dataStr.trim().isEmpty()) {
            this.dataEvento = null;
            return;
        }
        try {
            if (dataStr.contains("/")) {
                this.dataEvento = LocalDate.parse(dataStr.trim(), FORMATO_DATA);
            } else {
                this.dataEvento = LocalDate.parse(dataStr.trim());
            }
        } catch (Exception e) {
            this.dataEvento = null;
        }
    }

    public String getHorarioEvento() { return horarioEvento; }
    public void setHorarioEvento(String horarioEvento) { this.horarioEvento = horarioEvento; }

    public String getLocalEvento() { return localEvento; }
    public void setLocalEvento(String localEvento) { this.localEvento = localEvento; }

    public int getNumeroConvidados() { return numeroConvidados; }
    public void setNumeroConvidados(int numeroConvidados) { this.numeroConvidados = numeroConvidados; }

    public String getCardapioServicos() { return cardapioServicos; }
    public void setCardapioServicos(String cardapioServicos) { this.cardapioServicos = cardapioServicos; }

    public String getTextoMinuta() { return textoMinuta; }
    public void setTextoMinuta(String textoMinuta) { this.textoMinuta = textoMinuta; }

    public List<ClausulaContrato> getClausulas() { return clausulas; }
    public void setClausulas(List<ClausulaContrato> clausulas) { this.clausulas = clausulas != null ? clausulas : new ArrayList<>(); }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataAssinatura() { return dataAssinatura; }
    public void setDataAssinatura(LocalDate dataAssinatura) { this.dataAssinatura = dataAssinatura; }

    public LocalDate getDataCancelamento() { return dataCancelamento; }
    public void setDataCancelamento(LocalDate dataCancelamento) { this.dataCancelamento = dataCancelamento; }

    public String getObservacoesEspeciais() { return observacoesEspeciais; }
    public void setObservacoesEspeciais(String observacoesEspeciais) { this.observacoesEspeciais = observacoesEspeciais; }

    public String getObservacoesEspecificas() { return observacoesEspeciais; }
    public void setObservacoesEspecificas(String obs) { this.observacoesEspeciais = obs; }

    public String getCaminhoPdf() { return caminhoPdf; }
    public void setCaminhoPdf(String caminhoPdf) { this.caminhoPdf = caminhoPdf; }

    public String getArquivoPdfPath() { return caminhoPdf; }
    public void setArquivoPdfPath(String caminho) { this.caminhoPdf = caminho; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public List<VersaoContrato> getVersoes() { return versoes; }
    public void setVersoes(List<VersaoContrato> versoes) { this.versoes = versoes != null ? versoes : new ArrayList<>(); }

    public String getValorTotalFormatado() {
        return String.format("R$ %,.2f", valorTotal);
    }

    public String getDataEventoFormatada() {
        return dataEvento != null ? dataEvento.format(FORMATO_DATA) : "—";
    }

    public String getDataEmissaoFormatada() {
        return dataEmissao != null ? dataEmissao.format(FORMATO_DATA) : "—";
    }

    public String getDataAssinaturaFormatada() {
        return dataAssinatura != null ? dataAssinatura.format(FORMATO_DATA) : "—";
    }
}
