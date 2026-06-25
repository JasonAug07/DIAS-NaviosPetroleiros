package com.petroleiros.service;

import com.petroleiros.dao.PortoDAO;
import com.petroleiros.model.Porto;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PortoService {

    private final PortoDAO portoDAO;

    public PortoService() throws SQLException {
        this.portoDAO = new PortoDAO();
    }

    public List<Porto> listarTodos() throws SQLException {
        return portoDAO.findAll();
    }

    public Optional<Porto> buscar(String id) throws SQLException {
        return portoDAO.findById(id);
    }

    /** Proximo ID de porto, gerado automaticamente (formato PRT-000). */
    public String gerarProximoId() throws SQLException {
        return portoDAO.gerarProximoId();
    }

    public void registar(Porto porto) throws BusinessException, SQLException {
        validar(porto);
        if (!portoDAO.save(porto))
            throw new BusinessException("Erro ao guardar porto.");
    }

    public void editar(Porto porto) throws BusinessException, SQLException {
        validar(porto);
        if (!portoDAO.update(porto))
            throw new BusinessException("Porto não encontrado ou sem alterações.");
    }

    public void eliminar(String id) throws BusinessException, SQLException {
        // A FK ON DELETE BLOQUEADO protege os dados históricos.
        // Se existirem viagens ou cargas associadas, a BD lança SQLException.
        try {
            if (!portoDAO.delete(id))
                throw new BusinessException("Porto não encontrado.");
        } catch (SQLException e) {
            throw new BusinessException(
                "Não é possível eliminar este porto - existem viagens ou cargas associadas.", e);
        }
    }

    private void validar(Porto porto) throws BusinessException {
        if (porto.getId() == null || porto.getId().isBlank())
            throw new BusinessException("O ID do porto é obrigatório.");
        if (porto.getNome() == null || porto.getNome().isBlank())
            throw new BusinessException("O nome do porto é obrigatório.");
        if (porto.getPais() == null || porto.getPais().isBlank())
            throw new BusinessException("O país do porto é obrigatório.");
    }
}
