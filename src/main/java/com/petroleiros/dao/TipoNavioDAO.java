package com.petroleiros.dao;

import com.petroleiros.model.TipoCarga;
import com.petroleiros.model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipoNavioDAO implements DAO<TipoNavio> {

    private final Connection connection;
    private final TipoCargoDAO tipoCargoDAO;

    public TipoNavioDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.tipoCargoDAO = new TipoCargoDAO();
    }

    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "TIPO_NAVIO", "TN");
    }

    @Override
    public Optional<TipoNavio> findById(String id) throws SQLException {
        String sql = "SELECT id, nome, max_cargas_por_viagem, categoria FROM dbo.TIPO_NAVIO WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TipoNavio tipoNavio = mapRow(rs);
                    tipoNavio.setCargasCompativeis(findCargasCompativeis(id));
                    return Optional.of(tipoNavio);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<TipoNavio> findAll() throws SQLException {
        List<TipoNavio> list = new ArrayList<>();
        String sql = "SELECT id, nome, max_cargas_por_viagem, categoria FROM dbo.TIPO_NAVIO ORDER BY nome";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                TipoNavio tn = mapRow(rs);
                tn.setCargasCompativeis(findCargasCompativeis(tn.getId()));
                list.add(tn);
            }
        }
        return list;
    }

    @Override
    public boolean save(TipoNavio tipoNavio) throws SQLException {
        String sql = "INSERT INTO dbo.TIPO_NAVIO (id, nome, max_cargas_por_viagem, categoria) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoNavio.getId());
            ps.setString(2, tipoNavio.getNome());
            ps.setInt(3, tipoNavio.getMaxCargasPorViagem());
            ps.setString(4, tipoNavio.getCategoria());
            boolean saved = ps.executeUpdate() > 0;
            if (saved) saveCompatibilidades(tipoNavio);
            return saved;
        }
    }

    @Override
    public boolean update(TipoNavio tipoNavio) throws SQLException {
        String sql = "UPDATE dbo.TIPO_NAVIO SET nome = ?, max_cargas_por_viagem = ?, categoria = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoNavio.getNome());
            ps.setInt(2, tipoNavio.getMaxCargasPorViagem());
            ps.setString(3, tipoNavio.getCategoria());
            ps.setString(4, tipoNavio.getId());
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                deleteCompatibilidades(tipoNavio.getId());
                saveCompatibilidades(tipoNavio);
            }
            return updated;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        deleteCompatibilidades(id);
        String sql = "DELETE FROM dbo.TIPO_NAVIO WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // --- Compatibilidades (TIPO_NAVIO_TIPO_CARGA) ---

    public List<TipoCarga> findCargasCompativeis(String tipoNavioId) throws SQLException {
        List<TipoCarga> list = new ArrayList<>();
        String sql = "SELECT tc.id, tc.nome, tc.inflamavel, tc.corrosiva, tc.toxica " +
                     "FROM dbo.TIPO_NAVIO_TIPO_CARGA ntc " +
                     "JOIN dbo.TIPO_CARGA tc ON tc.id = ntc.tipo_carga_id " +
                     "WHERE ntc.tipo_navio_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoNavioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TipoCarga(
                            rs.getString("id"), rs.getString("nome"),
                            rs.getBoolean("inflamavel"), rs.getBoolean("corrosiva"),
                            rs.getBoolean("toxica")));
                }
            }
        }
        return list;
    }

    private void saveCompatibilidades(TipoNavio tipoNavio) throws SQLException {
        if (tipoNavio.getCargasCompativeis() == null) return;
        String sql = "INSERT INTO dbo.TIPO_NAVIO_TIPO_CARGA (tipo_navio_id, tipo_carga_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (TipoCarga tc : tipoNavio.getCargasCompativeis()) {
                ps.setString(1, tipoNavio.getId());
                ps.setString(2, tc.getId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void deleteCompatibilidades(String tipoNavioId) throws SQLException {
        String sql = "DELETE FROM dbo.TIPO_NAVIO_TIPO_CARGA WHERE tipo_navio_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoNavioId);
            ps.executeUpdate();
        }
    }

    private TipoNavio mapRow(ResultSet rs) throws SQLException {
        return new TipoNavio(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getInt("max_cargas_por_viagem"),
                rs.getString("categoria")
        );
    }
}
