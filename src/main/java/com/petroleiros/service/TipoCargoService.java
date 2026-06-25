package com.petroleiros.service;
import com.petroleiros.model.*;

import java.sql.SQLException;
import java.util.*;


public class TipoCargoService {

    private final com.petroleiros.dao.TipoCargoDAO dao;

    public TipoCargoService() throws SQLException {
        this.dao = new com.petroleiros.dao.TipoCargoDAO();
    }

    public List<TipoCarga> listarTodos() throws SQLException { return dao.findAll(); }

    Optional<TipoCarga> buscar(String id) throws SQLException { return dao.findById(id); }

    /** Proximo ID de tipo de carga, gerado automaticamente (formato TC-000). */
    public String gerarProximoId() throws SQLException { return dao.gerarProximoId(); }

    public void registar(TipoCarga t) throws BusinessException, SQLException {
        validar(t);
        if (!dao.save(t)) throw new BusinessException("Erro ao guardar tipo de carga.");
    }

    public void editar(TipoCarga t) throws BusinessException, SQLException {
        validar(t);
        if (!dao.update(t)) throw new BusinessException("Tipo de carga não encontrado.");
    }

    public void eliminar(String id) throws BusinessException, SQLException {
        try {
            if (!dao.delete(id)) throw new BusinessException("Tipo de carga não encontrado.");
        } catch (SQLException e) {
            throw new BusinessException(
                    "Não é possível eliminar - existem cargas com este tipo.", e);
        }
    }

    private void validar(TipoCarga t) throws BusinessException {
        if (t.getId() == null || t.getId().isBlank())
            throw new BusinessException("O ID do tipo de carga é obrigatório.");
        if (t.getNome() == null || t.getNome().isBlank())
            throw new BusinessException("O nome do tipo de carga é obrigatório.");
    }
}
