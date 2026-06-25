package com.petroleiros.service;

import com.petroleiros.dao.NavioDAO;
import com.petroleiros.dao.TripulanteDAO;
import com.petroleiros.dao.ViagemDAO;
import com.petroleiros.model.Navio;
import com.petroleiros.model.Tripulante;
import com.petroleiros.model.Viagem;
import com.petroleiros.model.enums.EstadoViagem;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ViagemService {

    private final ViagemDAO    viagemDAO;
    private final NavioDAO     navioDAO;
    private final TripulanteDAO tripulanteDAO;

    public ViagemService() throws SQLException {
        this.viagemDAO     = new ViagemDAO();
        this.navioDAO      = new NavioDAO();
        this.tripulanteDAO = new TripulanteDAO();
    }

    // -- Consultas -------------------------------------------------------------

    public List<Viagem> listarTodas() throws SQLException {
        return viagemDAO.findAll();
    }

    public List<Viagem> listarPorEstado(EstadoViagem estado) throws SQLException {
        return viagemDAO.findByEstado(estado);
    }

    public Optional<Viagem> buscar(String id) throws SQLException {
        return viagemDAO.findById(id);
    }

    /** Indica se um navio esta apto a iniciar viagem (funcao fn_NavioDisponivel). */
    public boolean navioDisponivel(String navioId) throws SQLException {
        return viagemDAO.navioDisponivel(navioId);
    }

    /** Resumo agregado de uma viagem, lido da vista vw_ViagemDetalhada. */
    public ViagemDAO.ResumoViagem resumoViagem(String viagemId) throws SQLException {
        return viagemDAO.resumoViagem(viagemId);
    }

    /** Proximo ID de viagem, gerado automaticamente (formato VGM-000). */
    public String gerarProximoId() throws SQLException {
        return viagemDAO.gerarProximoId();
    }

    // -- Ciclo de vida da viagem -----------------------------------------------

    /**
     * Cria uma nova viagem no estado Planeada.
     * Valida: navio disponível, portos distintos, datas coerentes.
     */
    public void criarViagem(Viagem viagem) throws BusinessException, SQLException {
        validarDadosViagem(viagem);

        Navio navio = navioDAO.findById(viagem.getNavio().getId())
                .orElseThrow(() -> new BusinessException("Navio não encontrado."));

        // Disponibilidade verificada pela funcao fn_NavioDisponivel (navio ativo e sem viagem em curso)
        if (!viagemDAO.navioDisponivel(navio.getId()))
            throw new BusinessException(
                "O navio '" + navio.getNome() + "' não está disponível para nova viagem " +
                "(inativo, em manutenção ou já em viagem).");

        viagem.setEstado(EstadoViagem.PLANEADA);

        try {
            if (!viagemDAO.save(viagem))
                throw new BusinessException("Erro ao guardar viagem.");
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    /**
     * Inicia uma viagem Planeada -> Em Curso.
     * Delega no sp_IniciarViagem que valida e actualiza tudo atomicamente.
     */
    public void iniciarViagem(String viagemId) throws BusinessException, SQLException {
        garantirExiste(viagemId);
        try {
            viagemDAO.iniciarViagem(viagemId);
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    /**
     * Conclui uma viagem Em Curso -> Concluída.
     * Delega no sp_ConcluirViagem.
     */
    public void concluirViagem(String viagemId) throws BusinessException, SQLException {
        garantirExiste(viagemId);
        try {
            viagemDAO.concluirViagem(viagemId);
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    /**
     * Cancela uma viagem Planeada ou Em Curso.
     * Delega no sp_CancelarViagem que trata da libertação de recursos.
     */
    public void cancelarViagem(String viagemId, String motivo)
            throws BusinessException, SQLException {
        garantirExiste(viagemId);
        try {
            viagemDAO.cancelarViagem(viagemId, motivo);
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    /**
     * Edita os dados de uma viagem Planeada (portos, datas).
     * Não é possível editar viagens Em Curso, Concluídas ou Canceladas.
     */
    public void editarViagem(Viagem viagem) throws BusinessException, SQLException {
        validarDadosViagem(viagem);

        Viagem atual = viagemDAO.findById(viagem.getId())
                .orElseThrow(() -> new BusinessException("Viagem não encontrada."));

        if (atual.getEstado() != EstadoViagem.PLANEADA)
            throw new BusinessException(
                "Só é possível editar viagens no estado Planeada. " +
                "Estado actual: " + atual.getEstado() + ".");

        if (!viagemDAO.update(viagem))
            throw new BusinessException("Erro ao actualizar viagem.");
    }

    // -- Tripulação ------------------------------------------------------------

    /**
     * Associa um tripulante a uma viagem Planeada.
     * O trigger trg_ViagemTripulante_ValidaDisponibilidade garante que
     * o tripulante não está já Em Viagem.
     */
    public void associarTripulante(String viagemId, String tripulanteId)
            throws BusinessException, SQLException {

        Viagem viagem = viagemDAO.findById(viagemId)
                .orElseThrow(() -> new BusinessException("Viagem não encontrada."));

        if (viagem.getEstado() != EstadoViagem.PLANEADA)
            throw new BusinessException("Só é possível associar tripulação a viagens Planeadas.");

        Tripulante tripulante = tripulanteDAO.findById(tripulanteId)
                .orElseThrow(() -> new BusinessException("Tripulante não encontrado."));

        if (!tripulante.isDisponivel())
            throw new BusinessException(
                "O tripulante '" + tripulante.getNome() + "' não está disponível " +
                "(estado: " + tripulante.getEstadoDisponibilidade() + ").");

        // Verificar se já está associado a esta viagem
        boolean jaAssociado = viagem.getTripulacao().stream()
                .anyMatch(t -> t.getId().equals(tripulanteId));
        if (jaAssociado)
            throw new BusinessException("Este tripulante já está associado à viagem.");

        try {
            viagemDAO.adicionarTripulante(viagemId, tripulanteId);
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    public void removerTripulante(String viagemId, String tripulanteId)
            throws BusinessException, SQLException {

        Viagem viagem = viagemDAO.findById(viagemId)
                .orElseThrow(() -> new BusinessException("Viagem não encontrada."));

        if (viagem.getEstado() != EstadoViagem.PLANEADA)
            throw new BusinessException("Só é possível remover tripulação de viagens Planeadas.");

        try {
            viagemDAO.removerTripulante(viagemId, tripulanteId);
        } catch (SQLException e) {
            throw new BusinessException(traduzirErroDB(e), e);
        }
    }

    // -- Helpers privados ------------------------------------------------------

    private void garantirExiste(String viagemId) throws BusinessException, SQLException {
        if (viagemDAO.findById(viagemId).isEmpty())
            throw new BusinessException("Viagem não encontrada: " + viagemId);
    }

    private void validarDadosViagem(Viagem v) throws BusinessException {
        if (v.getId() == null || v.getId().isBlank())
            throw new BusinessException("O ID da viagem é obrigatório.");
        if (v.getNavio() == null)
            throw new BusinessException("O navio é obrigatório.");
        if (v.getPortoOrigem() == null)
            throw new BusinessException("O porto de origem é obrigatório.");
        if (v.getPortoDestino() == null)
            throw new BusinessException("O porto de destino é obrigatório.");
        if (v.getPortoOrigem().getId().equals(v.getPortoDestino().getId()))
            throw new BusinessException("O porto de origem e o porto de destino não podem ser o mesmo.");
        if (v.getDataPartida() == null)
            throw new BusinessException("A data de partida é obrigatória.");
        if (v.getDataChegadaPrevista() == null)
            throw new BusinessException("A data prevista de chegada é obrigatória.");
        if (!v.getDataChegadaPrevista().isAfter(v.getDataPartida()))
            throw new BusinessException("A data de chegada deve ser posterior à data de partida.");
        if (v.getDataPartida().isBefore(LocalDateTime.now().minusMinutes(5)))
            throw new BusinessException("A data de partida não pode ser no passado.");
    }

    private String traduzirErroDB(SQLException e) {
        return switch (e.getErrorCode()) {
            case 50001 -> "Viagem não encontrada.";
            case 50002 -> "Só é possível iniciar viagens no estado Planeada.";
            case 50003 -> "Navio indisponível: inativo, em manutenção ou já em viagem.";
            case 50004 -> "A viagem não tem tripulação associada.";
            case 50005 -> "A viagem não tem cargas associadas.";
            case 50006 -> "Só é possível concluir viagens Em Curso.";
            case 50011 -> "Não é possível cancelar uma viagem já concluída ou cancelada.";
            case 50012 -> "O navio não se encontra no porto de origem da viagem.";
            case 50013 -> "A viagem não tem nenhum Capitão na tripulação.";
            case 50015 -> "Um ou mais tripulantes não estão disponíveis (em licença ou já em viagem).";
            case 50020 -> "Transição de estado inválida.";
            case 50022 -> "Tripulante já está em viagem.";
            case 50023 -> "Tripulante já associado a outra viagem ativa.";
            case 50024 -> "O navio já tem uma viagem ativa (Planeada ou Em Curso).";
            case 50030 -> "Só é possível remover tripulação de viagens no estado Planeada.";
            case 50031 -> "O tripulante não está associado a esta viagem.";
            default    -> "Erro na base de dados: " + (e.getMessage() != null ? e.getMessage() : "");
        };
    }
}
