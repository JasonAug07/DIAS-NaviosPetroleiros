package com.petroleiros.dao;

import com.petroleiros.model.Porto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PortoDAO implements DAO<Porto> {

    private final Connection connection;

    public PortoDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "PORTO", "PRT");
    }

    @Override
    public Optional<Porto> findById(String id) throws SQLException {
        String sql = "SELECT id, nome, pais FROM dbo.PORTO WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Porto> findAll() throws SQLException {
        List<Porto> list = new ArrayList<>();
        String sql = "SELECT id, nome, pais FROM dbo.PORTO ORDER BY nome";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    @Override
    public boolean save(Porto porto) throws SQLException {
        String sql = "INSERT INTO dbo.PORTO (id, nome, pais) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, porto.getId());
            ps.setString(2, porto.getNome());
            ps.setString(3, porto.getPais());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Porto porto) throws SQLException {
        String sql = "UPDATE dbo.PORTO SET nome = ?, pais = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, porto.getNome());
            ps.setString(2, porto.getPais());
            ps.setString(3, porto.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM dbo.PORTO WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Porto mapRow(ResultSet rs) throws SQLException {
        return new Porto(
                rs.getString("id"),
                rs.getString("nome"),
                rs.getString("pais")
        );
    }
}
