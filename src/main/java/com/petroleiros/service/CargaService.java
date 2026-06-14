package com.petroleiros.service;

import com.petroleiros.dao.CargaDAO;
import com.petroleiros.model.Carga;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CargaService {

    private final CargaDAO cargaDAO;

    public CargaService() throws SQLException {
        this.cargaDAO = new CargaDAO();
    }

    public List<Carga> listarPorViagem(String viagemId) throws SQLException {
        return cargaDAO.findByViagem(viagemId);
    }

    /** Lista todas as cargas do sistema (visao global, apenas consulta). */
    public List<Carga> listarTodas() throws SQLException {
        return cargaDAO.findAll();
    }

    /** Capacidade ainda livre numa viagem, via a funcao fn_CapacidadeDisponivel. */
    public double capacidadeDisponivel(String viagemId) throws SQLException {
        return cargaDAO.capacidadeDisponivel(viagemId);
    }

    /** Proximo ID de carga, gerado automaticamente (formato CRG-000). */
    public String gerarProximoId() throws SQLException {
        return cargaDAO.gerarProximoId();
    }

    public Optional<Carga> buscar(String id) throws SQLException {
        return cargaDAO.findById(id);
    }

    /**
     * Adiciona uma carga a uma viagem.
     * Chama sp_AdicionarCargaViagem que valida:
     *   - Viagem no estado Planeada
     *   - Compatibilidade tipo navio / tipo carga
     *   - Capacidade disponível do navio
     *   - Número máximo de cargas por viagem
     */
    public void adicionarCargaViagem(Carga carga) throws BusinessException, SQLException {
        validar(carga);
        try {
            cargaDAO.adicionarCargaViagem(carga);
        } catch (SQLException e) {
            // Traduz erros do stored procedure para mensagens legíveis
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    public void editar(Carga carga) throws BusinessException, SQLException {
        validar(carga);
        // O trigger trg_Carga_ValidaCapacidade actua como segunda linha de defesa
        try {
            if (!cargaDAO.update(carga))
                throw new BusinessException("Carga não encontrada ou sem alterações.");
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    public void eliminar(String id) throws BusinessException, SQLException {
        if (cargaDAO.findById(id).isEmpty())
            throw new BusinessException("Carga não encontrada.");
        if (!cargaDAO.delete(id))
            throw new BusinessException("Erro ao eliminar carga.");
    }

    private void validar(Carga carga) throws BusinessException {
        if (carga.getId() == null || carga.getId().isBlank())
            throw new BusinessException("O ID da carga é obrigatório.");
        if (carga.getDesignacao() == null || carga.getDesignacao().isBlank())
            throw new BusinessException("A designação da carga é obrigatória.");
        if (carga.getTipoCarga() == null)
            throw new BusinessException("O tipo de carga é obrigatório.");
        if (carga.getViagemId() == null || carga.getViagemId().isBlank())
            throw new BusinessException("A carga deve estar associada a uma viagem.");
        if (carga.getNumTanquesOcupados() <= 0)
            throw new BusinessException("O número de tanques ocupados deve ser maior que zero.");
        if (carga.getVolume() <= 0)
            throw new BusinessException("O volume deve ser maior que zero.");
        if (carga.getPeso() <= 0)
            throw new BusinessException("O peso deve ser maior que zero.");
        if (carga.getPortoCarga() == null)
            throw new BusinessException("O porto de carga é obrigatório.");
        if (carga.getPortoDescarga() == null)
            throw new BusinessException("O porto de descarga é obrigatório.");
        if (carga.getPortoCarga().equals(carga.getPortoDescarga()))
            throw new BusinessException("O porto de carga e o porto de descarga não podem ser o mesmo.");
    }

    private String traduzirErroDB(SQLException e) {
        return switch (e.getErrorCode()) {
            case 50007 -> "Só é possível adicionar cargas a viagens no estado Planeada.";
            case 50008 -> "Tipo de carga incompatível com o tipo de navio.";
            case 50009 -> "Peso da carga excede a capacidade disponível do navio.";
            case 50010 -> "Número máximo de cargas por viagem atingido.";
            case 50014 -> "Número de tanques ocupados excede os tanques disponíveis do navio.";
            case 50021 -> "Capacidade máxima do navio excedida pelo peso total das cargas.";
            case 50025 -> "Número total de tanques ocupados excede os tanques físicos do navio.";
            default    -> "Erro na base de dados: " + (e.getMessage() != null ? e.getMessage() : "");
        };
    }
}
