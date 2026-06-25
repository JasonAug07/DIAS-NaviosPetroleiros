package com.petroleiros.service;

import com.petroleiros.dao.TipoNavioDAO;
import com.petroleiros.model.TipoNavio;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TipoNavioService {

    private final TipoNavioDAO tipoNavioDAO;

    public TipoNavioService() throws SQLException {
        this.tipoNavioDAO = new TipoNavioDAO();
    }

    public List<TipoNavio> listarTodos() throws SQLException {
        return tipoNavioDAO.findAll();
    }

    public Optional<TipoNavio> buscar(String id) throws SQLException {
        return tipoNavioDAO.findById(id);
    }

    /** Proximo ID de tipo de navio, gerado automaticamente (formato TN-000). */
    public String gerarProximoId() throws SQLException {
        return tipoNavioDAO.gerarProximoId();
    }

    public void registar(TipoNavio tipoNavio) throws BusinessException, SQLException {
        validar(tipoNavio);
        if (!tipoNavioDAO.save(tipoNavio))
            throw new BusinessException("Erro ao guardar tipo de navio.");
    }

    public void editar(TipoNavio tipoNavio) throws BusinessException, SQLException {
        validar(tipoNavio);
        if (!tipoNavioDAO.update(tipoNavio))
            throw new BusinessException("Tipo de navio não encontrado ou sem alterações.");
    }

    public void eliminar(String id) throws BusinessException, SQLException {
        try {
            if (!tipoNavioDAO.delete(id))
                throw new BusinessException("Tipo de navio não encontrado.");
        } catch (SQLException e) {
            throw new BusinessException(
                "Não é possível eliminar este tipo de navio - existem navios associados.", e);
        }
    }

    private void validar(TipoNavio tn) throws BusinessException {
        if (tn.getId() == null || tn.getId().isBlank())
            throw new BusinessException("O ID do tipo de navio é obrigatório.");
        if (tn.getNome() == null || tn.getNome().isBlank())
            throw new BusinessException("O nome do tipo de navio é obrigatório.");
        if (tn.getMaxCargasPorViagem() <= 0)
            throw new BusinessException("O número máximo de cargas por viagem deve ser maior que zero.");
        if (tn.getCategoria() == null || tn.getCategoria().isBlank())
            throw new BusinessException("A categoria do tipo de navio é obrigatória.");
        if (tn.getCargasCompativeis() == null || tn.getCargasCompativeis().isEmpty())
            throw new BusinessException("O tipo de navio deve ter pelo menos um tipo de carga compatível.");
    }
}
