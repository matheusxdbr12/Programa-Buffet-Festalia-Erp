package com.festalia;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerenciador de conexão e inicialização do banco de dados SQLite para o Festalia ERP.
 * Cria automaticamente as tabelas e dados iniciais se não existirem.
 */
public class DatabaseManager {

    private static final String NOME_BANCO = "festalia_erp.db";
    private static final String URL_CONEXAO = "jdbc:sqlite:" + NOME_BANCO;

    static {
        try {
            // Garante o carregamento do driver JDBC do SQLite
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver SQLite JDBC não encontrado no classpath: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL_CONEXAO);
    }

    /**
     * Inicializa a estrutura de tabelas e insere os cardápios padrão caso a tabela esteja vazia.
     */
    public static void inicializarBanco() {
        String sqlTabelaCardapios = """
            CREATE TABLE IF NOT EXISTS cardapios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL UNIQUE,
                descricao TEXT,
                preco_por_pessoa REAL NOT NULL DEFAULT 0.0,
                itens TEXT NOT NULL
            );
        """;

        String sqlTabelaOrdensServico = """
            CREATE TABLE IF NOT EXISTS ordens_servico (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_os INTEGER NOT NULL UNIQUE,
                nome_buffet TEXT DEFAULT 'Festalia Buffet & Eventos',
                subtitulo_buffet TEXT DEFAULT 'Gestão Profissional de Buffets e Eventos',
                caminho_logo_buffet TEXT,
                cliente TEXT NOT NULL,
                telefone TEXT,
                data_evento TEXT NOT NULL,
                local_evento TEXT,
                qtd_convidados INTEGER NOT NULL DEFAULT 0,
                preco_por_pessoa REAL NOT NULL DEFAULT 0.0,
                taxa_servico REAL NOT NULL DEFAULT 0.0,
                taxa_transporte REAL NOT NULL DEFAULT 0.0,
                valor_total REAL NOT NULL DEFAULT 0.0,
                status TEXT NOT NULL DEFAULT 'Orçamento',
                cardapio_nome TEXT NOT NULL,
                cardapio_descricao TEXT,
                cardapio_itens TEXT NOT NULL,
                horario_inicio TEXT DEFAULT '18:00',
                horario_fim TEXT DEFAULT '22:00',
                tipo_evento TEXT DEFAULT 'Casamento',
                conflito_confirmado INTEGER DEFAULT 0,
                observacoes TEXT,
                data_criacao TEXT NOT NULL
            );
        """;

        String sqlTabelaConfiguracoes = """
            CREATE TABLE IF NOT EXISTS configuracoes (
                chave TEXT PRIMARY KEY,
                valor TEXT NOT NULL
            );
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(sqlTabelaCardapios);
            stmt.execute(sqlTabelaOrdensServico);
            stmt.execute(sqlTabelaConfiguracoes);

            // Módulo de Gestão Financeira Avançada: Tabelas de Parcelamento, Pagamentos e Auditoria
            String sqlTabelaParcelas = """
                CREATE TABLE IF NOT EXISTS parcelas_os (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ordem_servico_id INTEGER NOT NULL,
                    numero_parcela INTEGER NOT NULL,
                    descricao TEXT NOT NULL,
                    valor REAL NOT NULL,
                    data_vencimento TEXT NOT NULL,
                    forma_pagamento_sugerida TEXT DEFAULT 'PIX',
                    status TEXT NOT NULL DEFAULT 'PENDENTE',
                    observacoes TEXT,
                    data_criacao TEXT NOT NULL,
                    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(sqlTabelaParcelas);

            String sqlTabelaPagamentos = """
                CREATE TABLE IF NOT EXISTS pagamentos_parcela (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    parcela_id INTEGER NOT NULL,
                    ordem_servico_id INTEGER NOT NULL,
                    valor_pago REAL NOT NULL,
                    data_pagamento TEXT NOT NULL,
                    forma_pagamento TEXT NOT NULL,
                    identificacao_transacao TEXT,
                    observacoes TEXT,
                    usuario_responsavel TEXT DEFAULT 'Operador',
                    data_registro TEXT NOT NULL,
                    FOREIGN KEY (parcela_id) REFERENCES parcelas_os(id) ON DELETE CASCADE,
                    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(sqlTabelaPagamentos);

            String sqlTabelaHistoricoFinanceiro = """
                CREATE TABLE IF NOT EXISTS historico_financeiro_os (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ordem_servico_id INTEGER NOT NULL,
                    tipo_operacao TEXT NOT NULL,
                    descricao TEXT NOT NULL,
                    valor REAL DEFAULT 0.0,
                    usuario TEXT DEFAULT 'Operador',
                    data_hora TEXT NOT NULL,
                    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(sqlTabelaHistoricoFinanceiro);

            // =========================================================================
            // MÓDULO DE CONTRATOS DE PRESTAÇÃO DE SERVIÇOS: Tabelas, Versões e Auditoria
            // =========================================================================
            String sqlTabelaContratos = """
                CREATE TABLE IF NOT EXISTS contratos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    ordem_servico_id INTEGER NOT NULL,
                    numero_contrato TEXT NOT NULL UNIQUE,
                    modelo_tipo TEXT DEFAULT 'BUFFET',
                    versao_atual TEXT DEFAULT '1.0',
                    status TEXT NOT NULL DEFAULT 'RASCUNHO',
                    contratante_nome TEXT NOT NULL,
                    contratante_cpf_cnpj TEXT,
                    contratante_rg TEXT,
                    contratante_endereco TEXT,
                    contratante_telefone TEXT,
                    contratante_email TEXT,
                    empresa_razao_social TEXT,
                    empresa_nome_fantasia TEXT,
                    empresa_cnpj TEXT,
                    empresa_endereco TEXT,
                    empresa_telefone TEXT,
                    empresa_email TEXT,
                    empresa_representante_nome TEXT,
                    empresa_representante_cpf TEXT,
                    empresa_cidade_foro TEXT,
                    valor_total REAL NOT NULL DEFAULT 0.0,
                    data_evento TEXT,
                    data_emissao TEXT,
                    data_assinatura TEXT,
                    data_cancelamento TEXT,
                    observacoes_especiais TEXT,
                    caminho_pdf TEXT,
                    data_criacao TEXT NOT NULL,
                    data_atualizacao TEXT NOT NULL,
                    FOREIGN KEY (ordem_servico_id) REFERENCES ordens_servico(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(sqlTabelaContratos);

            String sqlTabelaContratosVersoes = """
                CREATE TABLE IF NOT EXISTS contratos_versoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    contrato_id INTEGER NOT NULL,
                    versao TEXT NOT NULL,
                    status TEXT NOT NULL,
                    conteudo_minuta TEXT NOT NULL,
                    parametros_config TEXT,
                    motivo_alteracao TEXT,
                    usuario_responsavel TEXT DEFAULT 'Operador',
                    data_registro TEXT NOT NULL,
                    caminho_pdf TEXT,
                    FOREIGN KEY (contrato_id) REFERENCES contratos(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(sqlTabelaContratosVersoes);

            String sqlTabelaContratosAuditoria = """
                CREATE TABLE IF NOT EXISTS contratos_auditoria (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    contrato_id INTEGER NOT NULL,
                    tipo_operacao TEXT NOT NULL,
                    descricao TEXT NOT NULL,
                    versao TEXT,
                    usuario TEXT DEFAULT 'Operador',
                    data_hora TEXT NOT NULL,
                    FOREIGN KEY (contrato_id) REFERENCES contratos(id) ON DELETE CASCADE
                );
            """;
            stmt.execute(sqlTabelaContratosAuditoria);

            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_parcelas_os ON parcelas_os(ordem_servico_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_pagamentos_parcela ON pagamentos_parcela(parcela_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_pagamentos_os ON pagamentos_parcela(ordem_servico_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_hist_fin_os ON historico_financeiro_os(ordem_servico_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_contratos_os ON contratos(ordem_servico_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_contratos_versoes ON contratos_versoes(contrato_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_contratos_auditoria ON contratos_auditoria(contrato_id);");
            } catch (SQLException ignored) {}

            // Migração de colunas caso o banco já existisse
            migrarColunasTaxasSeNecessario(stmt);

            // Popula as configurações empresariais padrão caso não existam
            inicializarConfiguracoesEmpresaSeVazio(conn);

            // Popula os cardápios iniciais com tabela de preços caso seja a primeira execução
            popularCardapiosIniciaisSeVazio(conn);

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar o banco de dados Festalia ERP: " + e.getMessage());
        }
    }

    private static void migrarColunasTaxasSeNecessario(Statement stmt) {
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN taxa_servico REAL NOT NULL DEFAULT 0.0");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN taxa_transporte REAL NOT NULL DEFAULT 0.0");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN nome_buffet TEXT DEFAULT 'Festalia Buffet & Eventos'");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN subtitulo_buffet TEXT DEFAULT 'Gestão Profissional de Buffets e Eventos'");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN caminho_logo_buffet TEXT");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN horario_inicio TEXT DEFAULT '18:00'");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN horario_fim TEXT DEFAULT '22:00'");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN tipo_evento TEXT DEFAULT 'Casamento'");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN conflito_confirmado INTEGER DEFAULT 0");
        } catch (SQLException ignored) {
            // Coluna já existe
        }
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN cliente_cpf_cnpj TEXT");
        } catch (SQLException ignored) {}
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN cliente_rg TEXT");
        } catch (SQLException ignored) {}
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN cliente_endereco TEXT");
        } catch (SQLException ignored) {}
        try {
            stmt.execute("ALTER TABLE ordens_servico ADD COLUMN cliente_email TEXT");
        } catch (SQLException ignored) {}
    }

    private static void inicializarConfiguracoesEmpresaSeVazio(Connection conn) {
        configurarSeNaoExistir(conn, "empresa.razao_social", "Festalia Buffet & Eventos Ltda");
        configurarSeNaoExistir(conn, "empresa.nome_fantasia", "Festalia Buffet & Eventos");
        configurarSeNaoExistir(conn, "empresa.cnpj", "12.345.678/0001-90");
        configurarSeNaoExistir(conn, "empresa.endereco", "Av. das Festas, 1000 - Jardins, São Paulo - SP");
        configurarSeNaoExistir(conn, "empresa.telefone", "(11) 3456-7890 / (11) 98765-4321");
        configurarSeNaoExistir(conn, "empresa.email", "contato@festaliabuffet.com.br");
        configurarSeNaoExistir(conn, "empresa.representante_nome", "Matheus De Souza");
        configurarSeNaoExistir(conn, "empresa.representante_cpf", "123.456.789-00");
        configurarSeNaoExistir(conn, "empresa.cidade_foro", "São Paulo - SP");
    }

    private static void configurarSeNaoExistir(Connection conn, String chave, String valorPadrao) {
        String sql = "INSERT OR IGNORE INTO configuracoes (chave, valor) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            ps.setString(2, valorPadrao);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static synchronized String gerarProximoNumeroContrato() {
        int ano = java.time.LocalDate.now().getYear();
        String sql = "SELECT COUNT(*) FROM contratos";
        int count = 1;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            count = (int) (System.currentTimeMillis() % 10000);
        }
        return String.format("CTR-%d-%04d", ano, count);
    }

    public static void salvarConfiguracao(String chave, String valor) {
        String sql = "INSERT INTO configuracoes (chave, valor) VALUES (?, ?) ON CONFLICT(chave) DO UPDATE SET valor = excluded.valor";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            ps.setString(2, valor);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao salvar configuração [" + chave + "]: " + e.getMessage());
        }
    }

    public static String obterConfiguracao(String chave, String valorPadrao) {
        String sql = "SELECT valor FROM configuracoes WHERE chave = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chave);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valor");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao obter configuração [" + chave + "]: " + e.getMessage());
        }
        return valorPadrao;
    }

    private static void popularCardapiosIniciaisSeVazio(Connection conn) throws SQLException {
        String sqlCount = "SELECT COUNT(*) FROM cardapios";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlCount)) {
            if (rs.next() && rs.getInt(1) == 0) {
                inserirCardapioPadrao(conn,
                        "Casamento Real & Nobre",
                        "Buffet sofisticado para grandes celebrações noturnas",
                        185.00,
                        String.join("\n",
                                "Entradas finas: Canapés de salmão defumado, bruschettas caprese e folhados de brie",
                                "Prato Principal 1: Tournedor de Filet Mignon ao molho de vinho do Porto e risoto trufado",
                                "Prato Principal 2: Salmão grelhado ao molho de maracujá e mousseline de mandioquinha",
                                "Sobremesas: Mini pâtisserie francesa, verrines e Petit Gâteau artesanal",
                                "Bebidas: Espumante Brut, Vinhos tintos selecionados, cervejas artesanais e refrigerantes"
                        ));

                inserirCardapioPadrao(conn,
                        "Churrasco Prime & Chopp",
                        "Almoço corporativo ou confraternizações descontraídas",
                        125.00,
                        String.join("\n",
                                "Entradas: Pão de alho especial, queijo coalho com melaço e linguiça artesanal cuiabana",
                                "Carnes Nobres: Picanha Black Angus, Fraldinha na brasa e Costela fogo de chão",
                                "Acompanhamentos: Arroz carreteiro, farofa crocante de bacon e vinagrete clássico",
                                "Sobremesas: Abacaxi assado com canela e raspas de limão e Sorvete caseiro",
                                "Bebidas: Chopp artesanal gelado, caipirinhas de frutas, sucos e refrigerantes"
                        ));

                inserirCardapioPadrao(conn,
                        "Festa Infantil Alegria Total",
                        "Cardápio temático completo para crianças e famílias",
                        78.00,
                        String.join("\n",
                                "Salgadinhos tradicionais: Coxinha de frango, bolinha de queijo, empadinha e kibe",
                                "Lanchinhos quentes: Mini hambúrguer gourmet, mini hot dog e mini pizza de mussarela",
                                "Mesa Doce: Mesa de docinhos tradicionais, algodão doce e bolo temático de chocolate",
                                "Bebidas: Sucos naturais de frutas da estação, refrigerantes e água mineral"
                        ));

                inserirCardapioPadrao(conn,
                        "Coquetel Corporativo Finger Food",
                        "Ideal para lançamentos, conferências e workshops",
                        95.00,
                        String.join("\n",
                                "Finger Foods Frios: Carpaccio em crocante de parmesão e ceviche de tilápia",
                                "Finger Foods Quentes: Mini quiches lorraine e dadinhos de tapioca com geleia de pimenta",
                                "Doces e Café: Petit fours, macarons franceses e estação de café expresso",
                                "Bebidas: Água aromatizada, coquetéis sem álcool, vinhos e espumantes"
                        ));
            }
        }
    }

    private static void inserirCardapioPadrao(Connection conn, String nome, String desc, double preco, String itens) throws SQLException {
        String sql = "INSERT INTO cardapios (nome, descricao, preco_por_pessoa, itens) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nome);
            ps.setString(2, desc);
            ps.setDouble(3, preco);
            ps.setString(4, itens);
            ps.executeUpdate();
        }
    }

    /**
     * Retorna o próximo número de OS sequencial persistido.
     */
    public static int gerarProximoNumeroOS() {
        String sql = "SELECT COALESCE(MAX(numero_os), 1000) + 1 FROM ordens_servico";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar próximo número de OS: " + e.getMessage());
        }
        return 1001;
    }

    public static String getCaminhoBanco() {
        return new File(NOME_BANCO).getAbsolutePath();
    }
}
