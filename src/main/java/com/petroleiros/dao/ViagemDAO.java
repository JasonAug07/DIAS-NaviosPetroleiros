package com.petroleiros.dao;

import com.petroleiros.model.Navio;
import com.petroleiros.model.Porto;
import com.petroleiros.model.Tripulante;
import com.petroleiros.model.Viagem;
import com.petroleiros.model.enums.EstadoViagem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViagemDAO implements DAO<Viagem> {

    private final Connection connection;
    private final NavioDAO navioDAO;
    private final PortoDAO portoDAO;
    private final CargaDAO cargaDAO;
    private final TripulanteDAO tripulanteDAO;

    public ViagemDAO() throws SQLException {
        this.connection     = DatabaseConnection.getInstance().getConnection();
        this.navioDAO       = new NavioDAO();
        this.portoDAO       = new PortoDAO();
        this.cargaDAO       = new CargaDAO();
        this.tripulanteDAO  = new TripulanteDAO();
    }

    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "VIAGEM", "VGM");
    }

    // Query de listagem: inclui as contagens de cargas e tripulantes por subquery,
    // evitando carregar as listas completas (elimina as consultas N+1 aninhadas).
    private static final String RESUMO_SQL =
        "SELECT v.id, v.navio_id, v.porto_origem_id, v.porto_destino_id, " +
        "v.data_partida, v.data_chegada_prevista, v.data_chegada_real, v.estado, v.motivo_cancelamento, " +
        "(SELECT COUNT(*) FROM dbo.CARGA c WHERE c.viagem_id = v.id) AS num_cargas, " +
        "(SELECT COUNT(*) FROM dbo.VIAGEM_TRIPULANTE vt WHERE vt.viagem_id = v.id) AS num_tripulantes " +
        "FROM dbo.VIAGEM v ";

    @Override
    public Optional<Viagem> findById(String id) throws SQLException {
        String sql = "SELECT id, navio_id, porto_origem_id, porto_destino_id, " +
                     "data_partida, data_chegada_prevista, data_chegada_real, estado, motivo_cancelamento " +
                     "FROM dbo.VIAGEM WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Viagem> findAll() throws SQLException {
        List<Viagem> list = new ArrayList<>();
        String sql = RESUMO_SQL + "ORDER BY v.data_partida DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRowResumo(rs));
        }
        return list;
    }

    public List<Viagem> findByEstado(EstadoViagem estado) throws SQLException {
        List<Viagem> list = new ArrayList<>();
        String sql = RESUMO_SQL + "WHERE v.estado = ? ORDER BY v.data_partida DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, estado.getDescricao());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowResumo(rs));
            }
        }
        return list;
    }

    public Optional<Viagem> findViagemAtivaDoNavio(String navioId) throws SQLException {
        String sql = RESUMO_SQL + "WHERE v.navio_id = ? AND v.estado = 'Em Curso'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, navioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRowResumo(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean save(Viagem viagem) throws SQLException {
        // INSERT da viagem + tripulacao numa unica transacao (atomico)
        String sql = "INSERT INTO dbo.VIAGEM (id, navio_id, porto_origem_id, porto_destino_id, " +
                     "data_partida, data_chegada_prevista, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            boolean saved;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, viagem.getId());
                ps.setString(2, viagem.getNavio().getId());
                ps.setString(3, viagem.getPortoOrigem().getId());
                ps.setString(4, viagem.getPortoDestino().getId());
                ps.setTimestamp(5, Timestamp.valueOf(viagem.getDataPartida()));
                ps.setTimestamp(6, Timestamp.valueOf(viagem.getDataChegadaPrevista()));
                ps.setString(7, viagem.getEstado().getDescricao());
                saved = ps.executeUpdate() > 0;
            }
            if (saved) saveTripulacao(viagem);
            connection.commit();
            return saved;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public boolean update(Viagem viagem) throws SQLException {
        // UPDATE da viagem + substituicao da tripulacao numa unica transacao
        String sql = "UPDATE dbo.VIAGEM SET navio_id = ?, porto_origem_id = ?, porto_destino_id = ?, " +
                     "data_partida = ?, data_chegada_prevista = ?, estado = ? WHERE id = ?";
        try {
            connection.setAutoCommit(false);
            boolean updated;
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, viagem.getNavio().getId());
                ps.setString(2, viagem.getPortoOrigem().getId());
                ps.setString(3, viagem.getPortoDestino().getId());
                ps.setTimestamp(4, Timestamp.valueOf(viagem.getDataPartida()));
                ps.setTimestamp(5, Timestamp.valueOf(viagem.getDataChegadaPrevista()));
                ps.setString(6, viagem.getEstado().getDescricao());
                ps.setString(7, viagem.getId());
                updated = ps.executeUpdate() > 0;
            }
            if (updated) {
                deleteTripulacao(viagem.getId());
                saveTripulacao(viagem);
            }
            connection.commit();
            return updated;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        // Remocao da tripulacao + viagem numa unica transacao
        try {
            connection.setAutoCommit(false);
            deleteTripulacao(id);
            boolean deleted;
            String sql = "DELETE FROM dbo.VIAGEM WHERE id = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, id);
                deleted = ps.executeUpdate() > 0;
            }
            connection.commit();
            return deleted;
        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    // --- Tripulação (VIAGEM_TRIPULANTE) ---

    private void saveTripulacao(Viagem viagem) throws SQLException {
        if (viagem.getTripulacao() == null || viagem.getTripulacao().isEmpty()) return;
        String sql = "INSERT INTO dbo.VIAGEM_TRIPULANTE (viagem_id, tripulante_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (Tripulante t : viagem.getTripulacao()) {
                ps.setString(1, viagem.getId());
                ps.setString(2, t.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteTripulacao(String viagemId) throws SQLException {
        String sql = "DELETE FROM dbo.VIAGEM_TRIPULANTE WHERE viagem_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, viagemId);
            ps.executeUpdate();
        }
    }

    // --- mapRow ---

    private Viagem mapRow(ResultSet rs) throws SQLException {
        Navio navio         = navioDAO.findById(rs.getString("navio_id")).orElse(null);
        Porto origem        = portoDAO.findById(rs.getString("porto_origem_id")).orElse(null);
        Porto destino       = portoDAO.findById(rs.getString("porto_destino_id")).orElse(null);
        LocalDateTime partida  = rs.getTimestamp("data_partida").toLocalDateTime();
        LocalDateTime chegada  = rs.getTimestamp("data_chegada_prevista").toLocalDateTime();
        EstadoViagem estado = EstadoViagem.fromDescricao(rs.getString("estado"));

        Viagem viagem = new Viagem(rs.getString("id"), navio, origem, destino, partida, chegada, estado);

        Timestamp chegadaReal = rs.getTimestamp("data_chegada_real");
        if (chegadaReal != null) viagem.setDataChegadaReal(chegadaReal.toLocalDateTime());
        viagem.setMotivoCancelamento(rs.getString("motivo_cancelamento"));

        viagem.setCargas(cargaDAO.findByViagem(viagem.getId()));
        viagem.setTripulacao(tripulanteDAO.findByViagem(viagem.getId()));
        viagem.setNumCargas(viagem.getCargas().size());
        viagem.setNumTripulantes(viagem.getTripulacao().size());
        return viagem;
    }

    /**
     * Mapeia uma viagem para LISTAGEM: carrega navio e portos e le as contagens
     * de cargas e tripulantes (das subqueries), sem carregar as listas completas.
     */
    private Viagem mapRowResumo(ResultSet rs) throws SQLException {
        Navio navio   = navioDAO.findById(rs.getString("navio_id")).orElse(null);
        Porto origem  = portoDAO.findById(rs.getString("porto_origem_id")).orElse(null);
        Porto destino = portoDAO.findById(rs.getString("porto_destino_id")).orElse(null);
        LocalDateTime partida = rs.getTimestamp("data_partida").toLocalDateTime();
        LocalDateTime chegada = rs.getTimestamp("data_chegada_prevista").toLocalDateTime();
        EstadoViagem estado   = EstadoViagem.fromDescricao(rs.getString("estado"));

        Viagem viagem = new Viagem(rs.getString("id"), navio, origem, destino, partida, chegada, estado);
        Timestamp chegadaReal = rs.getTimestamp("data_chegada_real");
        if (chegadaReal != null) viagem.setDataChegadaReal(chegadaReal.toLocalDateTime());
        viagem.setMotivoCancelamento(rs.getString("motivo_cancelamento"));
        viagem.setNumCargas(rs.getInt("num_cargas"));
        viagem.setNumTripulantes(rs.getInt("num_tripulantes"));
        return viagem;
    }

    public void iniciarViagem(String viagemId) throws SQLException {
        String sql = "{call dbo.sp_IniciarViagem(?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, viagemId);
            cs.execute();
        }
    }

    public void concluirViagem(String viagemId) throws SQLException {
        String sql = "{call dbo.sp_ConcluirViagem(?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, viagemId);
            cs.execute();
        }
    }

    public void cancelarViagem(String viagemId, String motivo) throws SQLException {
        String sql = "{call dbo.sp_CancelarViagem(?,?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, viagemId);
            cs.setString(2, motivo);
            cs.execute();
        }
    }

    /**
     * Associa um tripulante a uma viagem (insere em VIAGEM_TRIPULANTE).
     * O trigger trg_ViagemTripulante_ValidaDisponibilidade actua automaticamente.
     */
    public void adicionarTripulante(String viagemId, String tripulanteId) throws SQLException {
        String sql = "INSERT INTO dbo.VIAGEM_TRIPULANTE (viagem_id, tripulante_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, viagemId);
            ps.setString(2, tripulanteId);
            ps.executeUpdate();
        }
    }

    /**
     * Remove um tripulante de uma viagem atraves do stored procedure
     * sp_RemoverTripulanteViagem, que valida que a viagem ainda esta Planeada.
     */
    public void removerTripulante(String viagemId, String tripulanteId) throws SQLException {
        String sql = "{call dbo.sp_RemoverTripulanteViagem(?,?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, viagemId);
            cs.setString(2, tripulanteId);
            cs.execute();
        }
    }

    /** Indica se o navio esta apto a iniciar viagem, via a funcao fn_NavioDisponivel. */
    public boolean navioDisponivel(String navioId) throws SQLException {
        String sql = "SELECT dbo.fn_NavioDisponivel(?) AS disp";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, navioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("disp");
            }
        }
        return false;
    }

    /**
     * Resumo de uma viagem lido da vista vw_ViagemDetalhada: numero de cargas,
     * peso e volume totais, percentagem de capacidade usada e nº de tripulantes.
     * Apenas leitura (para ecras de consulta), evitando consultas N+1.
     */
    public ResumoViagem resumoViagem(String viagemId) throws SQLException {
        String sql = "SELECT num_cargas, peso_total_cargas, volume_total_cargas, " +
                     "pct_capacidade_usada, num_tripulantes, capacidade_maxima " +
                     "FROM dbo.vw_ViagemDetalhada WHERE viagem_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, viagemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ResumoViagem(
                            rs.getInt("num_cargas"),
                            rs.getDouble("peso_total_cargas"),
                            rs.getDouble("volume_total_cargas"),
                            rs.getDouble("pct_capacidade_usada"),
                            rs.getInt("num_tripulantes"),
                            rs.getDouble("capacidade_maxima"));
                }
            }
        }
        return null;
    }

    /** Dados agregados de uma viagem (provenientes da vista vw_ViagemDetalhada). */
    public record ResumoViagem(int numCargas, double pesoTotal, double volumeTotal,
                               double pctCapacidade, int numTripulantes, double capacidadeMaxima) {}
}
