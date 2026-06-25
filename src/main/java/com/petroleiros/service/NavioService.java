package com.petroleiros.service;

import com.petroleiros.dao.NavioDAO;
import com.petroleiros.dao.ViagemDAO;
import com.petroleiros.model.Navio;
import com.petroleiros.model.enums.EstadoOperacional;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Optional;

public class NavioService {

    private final NavioDAO  navioDAO;
    private final ViagemDAO viagemDAO;

    public NavioService() throws SQLException {
        this.navioDAO  = new NavioDAO();
        this.viagemDAO = new ViagemDAO();
    }

    public List<Navio> listarTodos() throws SQLException {
        return navioDAO.findAll();
    }

    public List<Navio> listarDisponiveis() throws SQLException {
        return navioDAO.findDisponiveis();
    }

    public Optional<Navio> buscar(String id) throws SQLException {
        return navioDAO.findById(id);
    }

    /** Atraso medio (horas) das viagens concluidas de um navio (funcao fn_AtrasoMedioNavio). */
    public double atrasoMedio(String navioId) throws SQLException {
        return navioDAO.atrasoMedio(navioId);
    }

    /** Proximo ID de navio, gerado automaticamente pela aplicacao (formato NAV-000). */
    public String gerarProximoId() throws SQLException {
        return navioDAO.gerarProximoId();
    }

    public void registar(Navio navio) throws BusinessException, SQLException {
        validar(navio);
        if (!navioDAO.save(navio))
            throw new BusinessException("Erro ao guardar navio.");
    }

    public void editar(Navio navio) throws BusinessException, SQLException {
        validar(navio);
        if (!navioDAO.update(navio))
            throw new BusinessException("Navio não encontrado ou sem alterações.");
    }

    /**
     * Altera o estado operacional do navio.
     * Se o navio passar a Manutenção ou Inativo, o trigger trg_Navio_MudancaEstado
     * cancela automaticamente as viagens Planeadas na BD.
     */
    public void alterarEstado(String navioId, EstadoOperacional novoEstado)
            throws BusinessException, SQLException {

        Navio navio = navioDAO.findById(navioId)
                .orElseThrow(() -> new BusinessException("Navio não encontrado."));

        if (navio.getEstadoOperacional() == novoEstado)
            throw new BusinessException("O navio já se encontra nesse estado.");

        // Não permitir desactivar navio com viagem Em Curso
        if (novoEstado != EstadoOperacional.ATIVO
                && viagemDAO.findViagemAtivaDoNavio(navioId).isPresent())
            throw new BusinessException(
                "Não é possível alterar o estado - o navio tem uma viagem Em Curso. " +
                "Conclua ou cancele a viagem primeiro.");

        navio.setEstadoOperacional(novoEstado);
        navioDAO.update(navio);
        // O trigger trata do resto (cancelar Planeadas + registar evento)
    }

    public void eliminar(String id) throws BusinessException, SQLException {
        Navio navio = navioDAO.findById(id)
                .orElseThrow(() -> new BusinessException("Navio não encontrado."));

        if (viagemDAO.findViagemAtivaDoNavio(id).isPresent())
            throw new BusinessException("Não é possível eliminar um navio com viagem Em Curso.");

        try {
            if (!navioDAO.delete(id))
                throw new BusinessException("Erro ao eliminar navio.");
        } catch (SQLException e) {
            throw new BusinessException(
                "Não é possível eliminar este navio - tem viagens registadas no histórico.", e);
        }
    }

    private void validar(Navio navio) throws BusinessException {
        if (navio.getId() == null || navio.getId().isBlank())
            throw new BusinessException("O ID do navio é obrigatório.");
        if (navio.getNome() == null || navio.getNome().isBlank())
            throw new BusinessException("O nome do navio é obrigatório.");
        if (navio.getCodigoIMO() == null || navio.getCodigoIMO().isBlank())
            throw new BusinessException("O código IMO do navio é obrigatório.");
        if (navio.getTipoNavio() == null)
            throw new BusinessException("O tipo de navio é obrigatório.");
        if (navio.getCapacidadeMaxima() <= 0)
            throw new BusinessException("A capacidade máxima deve ser maior que zero.");
        if (navio.getNumTanques() <= 0)
            throw new BusinessException("O número de tanques deve ser maior que zero.");
        if (navio.getBandeira() == null || navio.getBandeira().isBlank())
            throw new BusinessException("A bandeira do navio é obrigatória.");
        if (navio.getAnoFabrico() < 1900 || navio.getAnoFabrico() > Year.now().getValue())
            throw new BusinessException("Ano de fabrico inválido.");
        if (navio.getEstadoOperacional() == null)
            throw new BusinessException("O estado operacional é obrigatório.");
    }
}
