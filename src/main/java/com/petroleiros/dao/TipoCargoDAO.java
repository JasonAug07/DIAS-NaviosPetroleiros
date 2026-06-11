package com.petroleiros.dao;

import com.petroleiros.model.TipoCarga;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipoCargoDAO implements DAO<TipoCarga> {

    private final Connection connection;

    public TipoCargoDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "TIPO_CARGA", "TC");
    }

    @Override
    public Optional<TipoCarga> findById(String id) throws SQLException {
        String sql = "SELECT id, nome, inflamavel, corrosiva, toxica FROM dbo.TIPO_CARGA WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<TipoCarga> findAll() throws SQLException {
        List<TipoCarga> list = new ArrayList<>();
        String sql = "SELECT id, nome, inflamavel, corrosiva, toxica FROM dbo.TIPO_CARGA ORDER BY nome";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean save(TipoCarga tipoCarga) throws SQLException {
        String sql = "INSERT INTO dbo.TIPO_CARGA (id, nome, inflamavel, corrosiva, toxica) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoCarga.getId());
            ps.setString(2, tipoCarga.getNome());
            ps.setBoolean(3, tipoCarga.isInflamavel());
            ps.setBoolean(4, tipoCarga.isCorrosiva());
            ps.setBoolean(5, tipoCarga.isToxica());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(TipoCarga tipoCarga) throws SQLException {
        String sql = "UPDATE dbo.TIPO_CARGA SET nome = ?, inflamavel = ?, corrosiva = ?, toxica = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tipoCarga.getNome());
            ps.setBoolean(2, tipoCarga.isInflamavel());
            ps.setBoolean(3, tipoCarga.isCorrosiva());
            ps.setBoolean(4, tipoCarga.isToxica());
            ps.setString(5, tipoCarga.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM dbo.TIPO_CARGA WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private TipoCarga mapRow(ResultSet rs) throws SQLException {
        return new TipoCarga(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getBoolean("inflamavel"),
                rs.getBoolean("corrosiva"),
                rs.getBoolean("toxica")
        );
    }
}
