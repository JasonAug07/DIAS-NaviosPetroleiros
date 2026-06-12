package com.petroleiros.dao;

import com.petroleiros.model.Carga;
import com.petroleiros.model.Porto;
import com.petroleiros.model.TipoCarga;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CargaDAO implements DAO<Carga> {

    private final Connection connection;
    private final TipoCargoDAO tipoCargoDAO;
    private final PortoDAO portoDAO;

    public CargaDAO() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.tipoCargoDAO = new TipoCargoDAO();
        this.portoDAO = new PortoDAO();
    }

    public String gerarProximoId() throws SQLException {
        return IdGenerator.proximo(connection, "CARGA", "CRG");
    }

    /**
     * Adiciona uma carga a uma viagem atraves do stored procedure
     * sp_AdicionarCargaViagem, que valida (numa transacao) a compatibilidade,
     * a capacidade, os tanques e o limite de cargas do navio.
     */
    public Boolean adicionarCargaViagem(Carga carga) throws SQLException {
        String sql = "{call dbo.sp_AdicionarCargaViagem(?,?,?,?,?,?,?,?,?)}";
        try (CallableStatement cs = connection.prepareCall(sql)) {
            cs.setString(1, carga.getId());
            cs.setString(2, carga.getDesignacao());
            cs.setString(3, carga.getTipoCarga().getId());
            cs.setString(4, carga.getViagemId());
            cs.setInt(5, carga.getNumTanquesOcupados());
            cs.setDouble(6, carga.getVolume());
            cs.setDouble(7, carga.getPeso());
            cs.setString(8, carga.getPortoCarga().getId());
            cs.setString(9, carga.getPortoDescarga().getId());
            cs.execute();
            return true;
        }
    }

    /** Capacidade ainda livre numa viagem, via a funcao fn_CapacidadeDisponivel. */
    public double capacidadeDisponivel(String viagemId) throws SQLException {
        String sql = "SELECT dbo.fn_CapacidadeDisponivel(?) AS cap";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, viagemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("cap");
            }
        }
        return 0;
    }



    @Override
    public Optional<Carga> findById(String id) throws SQLException {
        String sql = "SELECT id, designacao, tipo_carga_id, viagem_id, num_tanques_ocupados, " +
                     "volume, peso, porto_carga_id, porto_descarga_id FROM dbo.CARGA WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Carga> findAll() throws SQLException {
        List<Carga> list = new ArrayList<>();
        String sql = "SELECT id, designacao, tipo_carga_id, viagem_id, num_tanques_ocupados, " +
                     "volume, peso, porto_carga_id, porto_descarga_id FROM dbo.CARGA ORDER BY designacao";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Carga> findByViagem(String viagemId) throws SQLException {
        List<Carga> list = new ArrayList<>();
        String sql = "SELECT id, designacao, tipo_carga_id, viagem_id, num_tanques_ocupados, " +
                     "volume, peso, porto_carga_id, porto_descarga_id " +
                     "FROM dbo.CARGA WHERE viagem_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, viagemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override
    public boolean save(Carga carga) throws SQLException {
        String sql = "INSERT INTO dbo.CARGA (id, designacao, tipo_carga_id, viagem_id, " +
                     "num_tanques_ocupados, volume, peso, porto_carga_id, porto_descarga_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, carga.getId());
            ps.setString(2, carga.getDesignacao());
            ps.setString(3, carga.getTipoCarga().getId());
            ps.setString(4, carga.getViagemId());
            ps.setInt(5, carga.getNumTanquesOcupados());
            ps.setDouble(6, carga.getVolume());
            ps.setDouble(7, carga.getPeso());
            ps.setString(8, carga.getPortoCarga().getId());
            ps.setString(9, carga.getPortoDescarga().getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(Carga carga) throws SQLException {
        String sql = "UPDATE dbo.CARGA SET designacao = ?, tipo_carga_id = ?, viagem_id = ?, " +
                     "num_tanques_ocupados = ?, volume = ?, peso = ?, " +
                     "porto_carga_id = ?, porto_descarga_id = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, carga.getDesignacao());
            ps.setString(2, carga.getTipoCarga().getId());
            ps.setString(3, carga.getViagemId());
            ps.setInt(4, carga.getNumTanquesOcupados());
            ps.setDouble(5, carga.getVolume());
            ps.setDouble(6, carga.getPeso());
            ps.setString(7, carga.getPortoCarga().getId());
            ps.setString(8, carga.getPortoDescarga().getId());
            ps.setString(9, carga.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(String id) throws SQLException {
        String sql = "DELETE FROM dbo.CARGA WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Carga mapRow(ResultSet rs) throws SQLException {
        TipoCarga tipoCarga = tipoCargoDAO.findById(rs.getString("tipo_carga_id")).orElse(null);
        Porto portoCarga    = portoDAO.findById(rs.getString("porto_carga_id")).orElse(null);
        Porto portoDescarga = portoDAO.findById(rs.getString("porto_descarga_id")).orElse(null);

        return new Carga(
                rs.getString("id"),
                rs.getString("designacao"),
                tipoCarga,
                rs.getString("viagem_id"),
                rs.getInt("num_tanques_ocupados"),
                rs.getDouble("volume"),
                rs.getDouble("peso"),
                portoCarga,
                portoDescarga
        );
    }
}
