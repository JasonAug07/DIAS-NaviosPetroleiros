package com.petroleiros.controller;

import com.petroleiros.model.Navio;
import com.petroleiros.model.Porto;
import com.petroleiros.model.TipoNavio;
import com.petroleiros.model.enums.EstadoOperacional;
import com.petroleiros.service.BusinessException;
import com.petroleiros.service.NavioService;
import com.petroleiros.service.PortoService;
import com.petroleiros.service.TipoNavioService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class NavioFormularioController implements Initializable {

    @FXML private Label      labelTitulo;
    @FXML private TextField  campoId;
    @FXML private TextField  campoNome;
    @FXML private TextField  campoIMO;
    @FXML private ComboBox<TipoNavio>        comboTipoNavio;
    @FXML private TextField  campoCapacidade;
    @FXML private TextField  campoTanques;
    @FXML private TextField  campoBandeira;
    @FXML private TextField  campoAno;
    @FXML private ComboBox<EstadoOperacional> comboEstado;
    @FXML private ComboBox<Porto>            comboPorto;
    @FXML private Label      labelErro;

    private NavioService     navioService;
    private TipoNavioService tipoNavioService;
    private PortoService     portoService;
    private Navio            navioEditando;
    private Stage            stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            navioService     = new NavioService();
            tipoNavioService = new TipoNavioService();
            portoService     = new PortoService();
            preencherCombos();
        } catch (SQLException e) {
            mostrarErro("Erro ao carregar dados: " + e.getMessage());
        }
    }

    public void inicializar(Navio navio, Stage stage) {
        this.stage = stage;
        this.navioEditando = navio;

        if (navio != null) {
            labelTitulo.setText("Editar Navio");
            campoId.setText(navio.getId());
            campoId.setDisable(true);
            campoNome.setText(navio.getNome());
            campoIMO.setText(navio.getCodigoIMO());
            comboTipoNavio.setValue(navio.getTipoNavio());
            campoCapacidade.setText(String.valueOf(navio.getCapacidadeMaxima()));
            campoTanques.setText(String.valueOf(navio.getNumTanques()));
            campoBandeira.setText(navio.getBandeira());
            campoAno.setText(String.valueOf(navio.getAnoFabrico()));
            comboEstado.setValue(navio.getEstadoOperacional());
            comboPorto.setValue(navio.getPortoAtual());
        } else {
            // Registo: o ID e gerado automaticamente pela aplicacao (formato NAV-000)
            labelTitulo.setText("Registar Navio");
            try {
                campoId.setText(navioService.gerarProximoId());
            } catch (SQLException e) {
                mostrarErro("Erro ao gerar ID: " + e.getMessage());
            }
            campoId.setDisable(true);
        }
    }

    private void preencherCombos() throws SQLException {
        List<TipoNavio> tipos = tipoNavioService.listarTodos();
        comboTipoNavio.getItems().setAll(tipos);

        comboEstado.getItems().setAll(EstadoOperacional.values());
        comboEstado.setValue(EstadoOperacional.ATIVO);

        List<Porto> portos = portoService.listarTodos();
        comboPorto.getItems().add(null); // representa "em alto mar"
        comboPorto.getItems().addAll(portos);
    }

    @FXML
    private void guardar() {
        limparErro();
        try {
            Navio navio = construirNavio();
            if (navioEditando == null)
                navioService.registar(navio);
            else
                navioService.editar(navio);

            AlertHelper.sucesso("Navio guardado com sucesso.");
            stage.close();
        } catch (BusinessException e) {
            mostrarErro(e.getMessage());
        } catch (NumberFormatException e) {
            mostrarErro("Capacidade, nº de tanques e ano devem ser valores numéricos.");
        } catch (SQLException e) {
            mostrarErro("Erro de base de dados: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        stage.close();
    }

    private Navio construirNavio() {
        Navio navio = new Navio();
        navio.setId(campoId.getText().trim());
        navio.setNome(campoNome.getText().trim());
        navio.setCodigoIMO(campoIMO.getText().trim());
        navio.setTipoNavio(comboTipoNavio.getValue());
        navio.setCapacidadeMaxima(Double.parseDouble(campoCapacidade.getText().trim()));
        navio.setNumTanques(Integer.parseInt(campoTanques.getText().trim()));
        navio.setBandeira(campoBandeira.getText().trim());
        navio.setAnoFabrico(Integer.parseInt(campoAno.getText().trim()));
        navio.setEstadoOperacional(comboEstado.getValue());
        navio.setPortoAtual(comboPorto.getValue());
        return navio;
    }

    private void mostrarErro(String msg) {
        labelErro.setText(msg);
    }

    private void limparErro() {
        labelErro.setText("");
    }
}
