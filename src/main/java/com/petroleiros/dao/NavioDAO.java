package com.petroleiros.dao;

import com.petroleiros.model.Navio;
import com.petroleiros.model.Porto;
import com.petroleiros.model.TipoNavio;
import com.petroleiros.model.enums.EstadoOperacional;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NavioDAO implements DAO<Navio> {

    private final Connection connection;
    private final TipoNavioDAO tipoNavioDAO;
    private final PortoDAO portoDAO;

    public NavioDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.tipoNavioDAO = new TipoNavioDAO();
        this.portoDAO = new PortoDAO();
    }

    @Override
    public Optional<Navio> findById(String id) throws SQLException {
        String sql = "SELECT n.id, n.nome, n.codigo_imo, n.tipo_navio_id, n.capacidade_maxima, " +
                     "n.num_tanques, n.bandeira, n.ano_fabrico, n.estado_operacional, n.porto_atual_id " +
                     "FROM dbo.NAVIO n WHERE n.id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /** Atraso medio (em horas) das viagens concluidas do navio, via fn_AtrasoMedioNavio. */
    public double atrasoMedio(String navioId) throws SQLException {
        String sql = "SELECT dbo.fn_AtrasoMedioNavio(?) AS atraso";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, navioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("atraso");
            }
        }
        return 0;
    }

    /**
     * Gera o proximo identificador de navio no formato NAV-000, com base no
     * maior numero ja existente. Os IDs sao gerados pela aplicacao (nao sao
     * IDENTITY), por isso a app calcula o proximo automaticamente.
     */
    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "NAVIO", "NAV");
    }

    @Override
    public List<Navio> findAll() throws SQLException {
        List<Navio> list = new ArrayList<>();
        String sql = "SELECT id, nome, codigo_imo, tipo_navio_id, capacidade_maxima, " +
                     "num_tanques, bandeira, ano_fabrico, estado_operacional, porto_atual_id " +
                     "FROM dbo.NAVIO ORDER BY nome";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Navio> findDisponiveis() throws SQLException {
        List<Navio> list = new ArrayList<>();
        String sql = "SELECT id, nome, codigo_imo, tipo_navio_id, capacidade_maxima, " +
                     "num_tanques, bandeira, ano_fabrico, estado_operacional, porto_atual_id " +
                     "FROM dbo.NAVIO WHERE estado_operacional = 'Ativo' " +
                     "AND id NOT IN (SELECT navio_id FROM dbo.VIAGEM WHERE estado = 'Em Curso')";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean save(Navio navio) throws SQLException {
        String sql = "INSERT INTO dbo.NAVIO (id, nome, codigo_imo, tipo_navio_id, capacidade_maxima, " +
                     "num_tanques, bandeira, ano_fabrico, estado_operacional, porto_atual_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, navio.getId());
            ps.setString(2, navio.getNome());
            ps.setString(3, navio.getCodigoIMO());
            ps.setString(4, navio.getTipoNavio().getId());
            ps.setDouble(5, navio.getCapacidadeMaxima());
            ps.setInt(6, navio.getNumTanques());
            ps.setString(7, navio.getBandeira());
            ps.setInt(8, navio.getAnoFabrico());
            ps.setString(9, navio.getEstadoOperacional().getDescricao());
            if (navio.getPortoAtual() != null)
                ps.setString(10, navio.getPortoAtual().getId());
            else
                ps.setNull(10, Types.VARCHAR);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Navio navio) throws SQLException {
        String sql = "UPDATE dbo.NAVIO SET nome = ?, codigo_imo = ?, tipo_navio_id = ?, " +
                     "capacidade_maxima = ?, num_tanques = ?, bandeira = ?, ano_fabrico = ?, " +
                     "estado_operacional = ?, porto_atual_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, navio.getNome());
            ps.setString(2, navio.getCodigoIMO());
            ps.setString(3, navio.getTipoNavio().getId());
            ps.setDouble(4, navio.getCapacidadeMaxima());
            ps.setInt(5, navio.getNumTanques());
            ps.setString(6, navio.getBandeira());
            ps.setInt(7, navio.getAnoFabrico());
            ps.setString(8, navio.getEstadoOperacional().getDescricao());
            if (navio.getPortoAtual() != null)
                ps.setString(9, navio.getPortoAtual().getId());
            else
                ps.setNull(9, Types.VARCHAR);
            ps.setString(10, navio.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM dbo.NAVIO WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Navio mapRow(ResultSet rs) throws SQLException {
        TipoNavio tipoNavio = tipoNavioDAO.findById(rs.getString("tipo_navio_id")).orElse(null);
        Porto portoAtual = null;
        String portoId = rs.getString("porto_atual_id");
        if (portoId != null) portoAtual = portoDAO.findById(portoId).orElse(null);

        return new Navio(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("codigo_imo"),
                tipoNavio,
                rs.getDouble("capacidade_maxima"),
                rs.getInt("num_tanques"),
                rs.getString("bandeira"),
                rs.getInt("ano_fabrico"),
                EstadoOperacional.fromDescricao(rs.getString("estado_operacional")),
                portoAtual
        );
    }
}
