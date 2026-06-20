package com.petroleiros.controller;
import com.petroleiros.controller.AlertHelper;
import com.petroleiros.model.Tripulante;
import com.petroleiros.model.enums.FuncaoTripulante;
import com.petroleiros.service.TripulanteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.petroleiros.service.BusinessException;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class TripulanteFormularioController implements Initializable {

    @FXML
    private Label labelTitulo;
    @FXML private TextField campoId;
    @FXML private TextField                      campoNome;
    @FXML private ComboBox<FuncaoTripulante> comboFuncao;
    @FXML private Label                          labelErro;

    private TripulanteService tripulanteService;
    private Tripulante editando;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            tripulanteService = new TripulanteService();
            comboFuncao.getItems().setAll(FuncaoTripulante.values());
        } catch (SQLException e) {
            labelErro.setText("Erro: " + e.getMessage());
        }
    }

    public void inicializar(Tripulante t, Stage stage) {
        this.stage = stage;
        this.editando = t;
        if (t != null) {
            labelTitulo.setText("Editar Tripulante");
            campoId.setText(t.getId());
            campoId.setDisable(true);
            campoNome.setText(t.getNome());
            comboFuncao.setValue(t.getFuncao());
        } else {
            labelTitulo.setText("Registar Tripulante");
            try {
                campoId.setText(tripulanteService.gerarProximoId());
            } catch (SQLException e) {
                labelErro.setText("Erro ao gerar ID: " + e.getMessage());
            }
            campoId.setDisable(true);
        }
    }

    @FXML private void guardar() {
        try {
            Tripulante t = new Tripulante(
                    campoId.getText().trim(),
                    campoNome.getText().trim(),
                    comboFuncao.getValue(),
                    "Disponível");
            if (editando == null) tripulanteService.registar(t);
            else tripulanteService.editar(t);
            AlertHelper.sucesso("Tripulante guardado.");
            stage.close();
        } catch (BusinessException e) {
            labelErro.setText("⚠ " + e.getMessage());
        } catch (SQLException e) {
            labelErro.setText("⚠ Erro BD: " + e.getMessage());
        }
    }

    @FXML private void cancelar() { stage.close(); }
}

