package com.petroleiros.service;

import com.petroleiros.dao.TripulanteDAO;
import com.petroleiros.model.Tripulante;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TripulanteService {

    private final TripulanteDAO tripulanteDAO;

    public TripulanteService() throws SQLException {
        this.tripulanteDAO = new TripulanteDAO();
    }

    public List<Tripulante> listarTodos() throws SQLException {
        return tripulanteDAO.findAll();
    }

    public List<Tripulante> listarDisponiveis() throws SQLException {
        return tripulanteDAO.findDisponiveis();
    }

    public Optional<Tripulante> buscar(String id) throws SQLException {
        return tripulanteDAO.findById(id);
    }

    /** Proximo ID de tripulante, gerado automaticamente (formato TRP-000). */
    public String gerarProximoId() throws SQLException {
        return tripulanteDAO.gerarProximoId();
    }

    /** Resumo agregado do histórico do tripulante (vista vw_TripulanteHistorico). */
    public Optional<TripulanteDAO.HistoricoTripulante> historico(String id) throws SQLException {
        return tripulanteDAO.historico(id);
    }

    /** Lista detalhada das viagens em que o tripulante participou. */
    public List<TripulanteDAO.ViagemDoTripulante> viagensDoTripulante(String id) throws SQLException {
        return tripulanteDAO.viagensDoTripulante(id);
    }

    public void registar(Tripulante tripulante) throws BusinessException, SQLException {
        validar(tripulante);
        if (!tripulanteDAO.save(tripulante))
            throw new BusinessException("Erro ao guardar tripulante.");
    }

    public void editar(Tripulante tripulante) throws BusinessException, SQLException {
        validar(tripulante);

        // Não permitir editar tripulante que está Em Viagem
        Optional<Tripulante> atual = tripulanteDAO.findById(tripulante.getId());
        if (atual.isPresent() && "Em Viagem".equals(atual.get().getEstadoDisponibilidade()))
            throw new BusinessException("Não é possível editar um tripulante que está actualmente em viagem.");

        if (!tripulanteDAO.update(tripulante))
            throw new BusinessException("Tripulante não encontrado ou sem alterações.");
    }

    public void eliminar(String id) throws BusinessException, SQLException {
        Optional<Tripulante> tripulante = tripulanteDAO.findById(id);
        if (tripulante.isEmpty())
            throw new BusinessException("Tripulante não encontrado.");
        if ("Em Viagem".equals(tripulante.get().getEstadoDisponibilidade()))
            throw new BusinessException("Não é possível eliminar um tripulante que está em viagem.");

        try {
            if (!tripulanteDAO.delete(id))
                throw new BusinessException("Erro ao eliminar tripulante.");
        } catch (SQLException e) {
            throw new BusinessException(
                "Não é possível eliminar este tripulante — tem histórico de viagens associado.", e);
        }
    }

    private void validar(Tripulante t) throws BusinessException {
        if (t.getId() == null || t.getId().isBlank())
            throw new BusinessException("O ID do tripulante é obrigatório.");
        if (t.getNome() == null || t.getNome().isBlank())
            throw new BusinessException("O nome do tripulante é obrigatório.");
        if (t.getFuncao() == null)
            throw new BusinessException("A função do tripulante é obrigatória.");
    }
}
