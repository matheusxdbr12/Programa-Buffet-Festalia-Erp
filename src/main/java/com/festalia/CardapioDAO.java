package com.festalia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Objeto de Acesso a Dados (DAO) para o modelo Cardapio.
 * Gerencia a persistência no banco SQLite do portfólio e repertório de preços de cardápios.
 */
public class CardapioDAO {

    public List<Cardapio> listarTodos() {
        List<Cardapio> lista = new ArrayList<>();
        String sql = "SELECT id, nome, descricao, preco_por_pessoa, itens FROM cardapios ORDER BY nome ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(extrairCardapio(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cardápios: " + e.getMessage());
        }
        return lista;
    }

    public Cardapio buscarPorId(int id) {
        String sql = "SELECT id, nome, descricao, preco_por_pessoa, itens FROM cardapios WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extrairCardapio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cardápio por ID: " + e.getMessage());
        }
        return null;
    }

    public Cardapio buscarPorNome(String nome) {
        String sql = "SELECT id, nome, descricao, preco_por_pessoa, itens FROM cardapios WHERE nome = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extrairCardapio(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar cardápio por nome: " + e.getMessage());
        }
        return null;
    }

    public void salvar(Cardapio cardapio) throws SQLException {
        if (cardapio.getId() > 0) {
            atualizar(cardapio);
        } else {
            inserir(cardapio);
        }
    }

    private void inserir(Cardapio cardapio) throws SQLException {
        String sql = "INSERT INTO cardapios (nome, descricao, preco_por_pessoa, itens) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cardapio.getNome());
            ps.setString(2, cardapio.getDescricao());
            ps.setDouble(3, cardapio.getPrecoPorPessoa());
            ps.setString(4, cardapio.getItensComoTexto());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cardapio.setId(rs.getInt(1));
                }
            }
        }
    }

    private void atualizar(Cardapio cardapio) throws SQLException {
        String sql = "UPDATE cardapios SET nome = ?, descricao = ?, preco_por_pessoa = ?, itens = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cardapio.getNome());
            ps.setString(2, cardapio.getDescricao());
            ps.setDouble(3, cardapio.getPrecoPorPessoa());
            ps.setString(4, cardapio.getItensComoTexto());
            ps.setInt(5, cardapio.getId());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        String sql = "DELETE FROM cardapios WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Cardapio extrairCardapio(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nome = rs.getString("nome");
        String descricao = rs.getString("descricao");
        double preco = rs.getDouble("preco_por_pessoa");
        String itensTexto = rs.getString("itens");

        List<String> itens = new ArrayList<>();
        if (itensTexto != null && !itensTexto.isBlank()) {
            itens.addAll(Arrays.asList(itensTexto.split("\\r?\\n")));
        }

        return new Cardapio(id, nome, descricao, preco, itens);
    }
}
