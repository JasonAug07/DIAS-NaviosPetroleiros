package com.petroleiros.dao;

import com.petroleiros.model.Tripulante;
import com.petroleiros.model.enums.FuncaoTripulante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TripulanteDAO implements DAO<Tripulante> {

    private final Connection connection;

    public TripulanteDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "TRIPULANTE", "TRP");
    }

    /**
     * Resumo do histórico de um tripulante, lido da vista vw_TripulanteHistorico
     * (totais de viagens por estado e data da última partida).
     */
    public record HistoricoTripulante(
            String nome, String funcao, String estadoDisponibilidade,
            int totalViagens, int viagensConcluidas, int viagensEmCurso,
            int viagensCanceladas, java.time.LocalDateTime dataUltimaViagem) {}

    /** Uma linha da lista de viagens em que o tripulante participou. */
    public record ViagemDoTripulante(
            String viagemId, String navioNome, String origem, String destino,
            java.time.LocalDateTime dataPartida, String estado) {}

    /** Lê os totais agregados do tripulante a partir da vista vw_TripulanteHistorico. */
    public Optional<HistoricoTripulante> historico(String tripulanteId) throws SQLException {
        String sql = "SELECT nome, funcao, estado_disponibilidade, total_viagens, " +
                     "viagens_concluidas, viagens_em_curso, viagens_canceladas, data_ultima_viagem " +
                     "FROM dbo.vw_TripulanteHistorico WHERE tripulante_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tripulanteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ultima = rs.getTimestamp("data_ultima_viagem");
                    return Optional.of(new HistoricoTripulante(
                            rs.getString("nome"),
                            rs.getString("funcao"),
                            rs.getString("estado_disponibilidade"),
                            rs.getInt("total_viagens"),
                            rs.getInt("viagens_concluidas"),
                            rs.getInt("viagens_em_curso"),
                            rs.getInt("viagens_canceladas"),
                            ultima == null ? null : ultima.toLocalDateTime()));
                }
            }
        }
        return Optional.empty();
    }

    /** Lista detalhada das viagens de um tripulante (via VIAGEM_TRIPULANTE). */
    public List<ViagemDoTripulante> viagensDoTripulante(String tripulanteId) throws SQLException {
        List<ViagemDoTripulante> list = new ArrayList<>();
        String sql = "SELECT v.id, n.nome AS navio_nome, po.nome AS origem, pd.nome AS destino, " +
                     "v.data_partida, v.estado " +
                     "FROM dbo.VIAGEM_TRIPULANTE vt " +
                     "JOIN dbo.VIAGEM v  ON v.id = vt.viagem_id " +
                     "JOIN dbo.NAVIO n   ON n.id = v.navio_id " +
                     "JOIN dbo.PORTO po  ON po.id = v.porto_origem_id " +
                     "JOIN dbo.PORTO pd  ON pd.id = v.porto_destino_id " +
                     "WHERE vt.tripulante_id = ? ORDER BY v.data_partida DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tripulanteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp dp = rs.getTimestamp("data_partida");
                    list.add(new ViagemDoTripulante(
                            rs.getString("id"),
                            rs.getString("navio_nome"),
                            rs.getString("origem"),
                            rs.getString("destino"),
                            dp == null ? null : dp.toLocalDateTime(),
                            rs.getString("estado")));
                }
            }
        }
        return list;
    }

    @Override
    public Optional<Tripulante> findById(String id) throws SQLException {
        String sql = "SELECT id, nome, funcao, estado_disponibilidade FROM dbo.TRIPULANTE WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Tripulante> findAll() throws SQLException {
        List<Tripulante> list = new ArrayList<>();
        String sql = "SELECT id, nome, funcao, estado_disponibilidade FROM dbo.TRIPULANTE ORDER BY nome";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Tripulante> findDisponiveis() throws SQLException {
        List<Tripulante> list = new ArrayList<>();
        String sql = "SELECT id, nome, funcao, estado_disponibilidade FROM dbo.TRIPULANTE " +
                     "WHERE estado_disponibilidade = 'Disponível' ORDER BY nome";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Tripulante> findByViagem(String viagemId) throws SQLException {
        List<Tripulante> list = new ArrayList<>();
        String sql = "SELECT t.id, t.nome, t.funcao, t.estado_disponibilidade " +
                     "FROM dbo.TRIPULANTE t " +
                     "JOIN dbo.VIAGEM_TRIPULANTE vt ON vt.tripulante_id = t.id " +
                     "WHERE vt.viagem_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, viagemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean save(Tripulante tripulante) throws SQLException {
        String sql = "INSERT INTO dbo.TRIPULANTE (id, nome, funcao, estado_disponibilidade) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tripulante.getId());
            ps.setString(2, tripulante.getNome());
            ps.setString(3, tripulante.getFuncao().getDescricao());
            ps.setString(4, tripulante.getEstadoDisponibilidade());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Tripulante tripulante) throws SQLException {
        String sql = "UPDATE dbo.TRIPULANTE SET nome = ?, funcao = ?, estado_disponibilidade = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tripulante.getNome());
            ps.setString(2, tripulante.getFuncao().getDescricao());
            ps.setString(3, tripulante.getEstadoDisponibilidade());
            ps.setString(4, tripulante.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM dbo.TRIPULANTE WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Tripulante mapRow(ResultSet rs) throws SQLException {
        return new Tripulante(
                rs.getString("id"),
                rs.getString("nome"),
                FuncaoTripulante.fromDescricao(rs.getString("funcao")),
                rs.getString("estado_disponibilidade")
        );
    }
}
