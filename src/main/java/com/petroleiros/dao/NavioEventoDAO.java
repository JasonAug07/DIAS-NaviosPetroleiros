package com.petroleiros.dao;

import com.petroleiros.model.NavioEvento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso a tabela de log NAVIO_EVENTO (apenas leitura).
 * Os eventos sao gerados pelos procedimentos e triggers da base de dados;
 * a aplicacao apenas os consulta.
 */
public class NavioEventoDAO {

    private final Connection connection;

    public NavioEventoDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    private static final String BASE_SQL =
            "SELECT e.id, e.navio_id, n.nome AS navio_nome, e.viagem_id, e.tipo_evento, " +
            "p.nome AS porto_nome, e.data_evento, e.descricao " +
            "FROM dbo.NAVIO_EVENTO e " +
            "JOIN dbo.NAVIO n ON n.id = e.navio_id " +
            "LEFT JOIN dbo.PORTO p ON p.id = e.porto_id ";

    public List<NavioEvento> listarTodos() throws SQLException {
        List<NavioEvento> list = new ArrayList<>();
        String sql = BASE_SQL + "ORDER BY e.data_evento DESC, e.id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    /**
     * Eventos de um navio especifico. O filtro por navio_id aproveita o
     * indice IX_NAVIO_EVENTO_NAVIO (navio_id, data_evento).
     */
    public List<NavioEvento> listarPorNavio(String navioId) throws SQLException {
        List<NavioEvento> list = new ArrayList<>();
        String sql = BASE_SQL + "WHERE e.navio_id = ? ORDER BY e.data_evento DESC, e.id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, navioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    private NavioEvento mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("data_evento");
        return new NavioEvento(
                rs.getInt("id"),
                rs.getString("navio_id"),
                rs.getString("navio_nome"),
                rs.getString("viagem_id"),
                rs.getString("tipo_evento"),
                rs.getString("porto_nome"),
                ts != null ? ts.toLocalDateTime() : null,
                rs.getString("descricao")
        );
    }
}
