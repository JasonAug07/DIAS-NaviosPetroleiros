package com.petroleiros.controller;

import com.petroleiros.model.Navio;
import com.petroleiros.model.Porto;
import com.petroleiros.model.Viagem;
import com.petroleiros.model.enums.EstadoViagem;
import com.petroleiros.service.BusinessException;
import com.petroleiros.service.NavioService;
import com.petroleiros.service.PortoService;
import com.petroleiros.service.ViagemService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ViagemFormularioController implements Initializable {

    @FXML private Label                   labelTitulo;
    @FXML private TextField               campoId;
    @FXML private ComboBox<Navio>         comboNavio;
    @FXML private ComboBox<Porto>         comboOrigem;
    @FXML private ComboBox<Porto>         comboDestino;
    @FXML private DatePicker              dataPartida;
    @FXML private TextField               horaPartida;
    @FXML private DatePicker              dataChegada;
    @FXML private TextField               horaChegada;
    @FXML private Label                   labelErro;

    private ViagemService                 viagemService;
    private NavioService                  navioService;
    private PortoService                  portoService;
    private Viagem                        viagemEditando;
    private Stage                         stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            viagemService = new ViagemService();
            navioService  = new NavioService();
            portoService  = new PortoService();
            preencherCombos();
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar dados: " + e.getMessage());
        }
    }

    public void inicializar(Viagem viagem, Stage stage) {
        this.stage          = stage;
        this.viagemEditando = viagem;

        if (viagem != null) {
            labelTitulo.setText("Editar Viagem");
            campoId.setText(viagem.getId());
            campoId.setDisable(true);
            comboNavio.setValue(viagem.getNavio());
            comboOrigem.setValue(viagem.getPortoOrigem());
            comboDestino.setValue(viagem.getPortoDestino());
            if (viagem.getDataPartida() != null) {
                dataPartida.setValue(viagem.getDataPartida().toLocalDate());
                horaPartida.setText(viagem.getDataPartida().toLocalTime().toString());
            }
            if (viagem.getDataChegadaPrevista() != null) {
                dataChegada.setValue(viagem.getDataChegadaPrevista().toLocalDate());
                horaChegada.setText(viagem.getDataChegadaPrevista().toLocalTime().toString());
            }
        } else {
            labelTitulo.setText("Criar Viagem");
            try {
                campoId.setText(viagemService.gerarProximoId());
            } catch (SQLException e) {
                mostrarErro("Erro ao gerar ID: " + e.getMessage());
            }
            campoId.setDisable(true);
        }
    }

    private void preencherCombos() throws SQLException {
        comboNavio.getItems().setAll(navioService.listarDisponiveis());
        comboOrigem.getItems().setAll(portoService.listarTodos());
        comboDestino.getItems().setAll(portoService.listarTodos());
        horaPartida.setText("08:00");
        horaChegada.setText("08:00");
    }

    @FXML
    private void guardar() {
        limparErro();
        try {
            Viagem viagem = construirViagem();
            if (viagemEditando == null)
                viagemService.criarViagem(viagem);
            else
                viagemService.editarViagem(viagem);

            AlertHelper.sucesso("Viagem guardada com sucesso.");
            stage.close();
        } catch (BusinessException e) {
            mostrarErro(e.getMessage());
        } catch (Exception e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        stage.close();
    }

    private Viagem construirViagem() throws Exception {
        if (dataPartida.getValue() == null || dataChegada.getValue() == null)
            throw new BusinessException("As datas de partida e chegada são obrigatórias.");

        LocalDateTime partida  = LocalDateTime.of(dataPartida.getValue(),
            java.time.LocalTime.parse(horaPartida.getText()));
        LocalDateTime chegada  = LocalDateTime.of(dataChegada.getValue(),
            java.time.LocalTime.parse(horaChegada.getText()));

        Viagem viagem = new Viagem();
        viagem.setId(campoId.getText().trim());
        viagem.setNavio(comboNavio.getValue());
        viagem.setPortoOrigem(comboOrigem.getValue());
        viagem.setPortoDestino(comboDestino.getValue());
        viagem.setDataPartida(partida);
        viagem.setDataChegadaPrevista(chegada);
        viagem.setEstado(EstadoViagem.PLANEADA);
        return viagem;
    }

    private void mostrarErro(String msg) { labelErro.setText("⚠ " + msg); }
    private void limparErro()            { labelErro.setText(""); }
}
