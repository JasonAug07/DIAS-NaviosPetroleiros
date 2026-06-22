package com.petroleiros.controller;
import com.petroleiros.model.*;
import com.petroleiros.model.enums.FuncaoTripulante;
import com.petroleiros.service.*;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class CargaFormularioController implements Initializable {

    @FXML private TextField              campoId;
    @FXML private TextField              campoDesignacao;
    @FXML private ComboBox<TipoCarga>    comboTipoCarga;
    @FXML private TextField              campoPeso;
    @FXML private TextField              campoVolume;
    @FXML private TextField              campoTanques;
    @FXML private ComboBox<Porto>        comboPortoCarga;
    @FXML private ComboBox<Porto>        comboPortoDescarga;
    @FXML private Label                  labelErro;

    private CargaService                 cargaService;
    private TipoCargoService tipoCargoService;
    private PortoService                 portoService;
    private Viagem                       viagem;
    private Stage                        stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargaService     = new CargaService();
            tipoCargoService = new TipoCargoService();
            portoService     = new PortoService();
            comboTipoCarga.getItems().setAll(tipoCargoService.listarTodos());
            comboPortoCarga.getItems().setAll(portoService.listarTodos());
            comboPortoDescarga.getItems().setAll(portoService.listarTodos());
        } catch (SQLException e) {
            labelErro.setText("Erro: " + e.getMessage());
        }
    }

    public void inicializar(Viagem viagem, Stage stage) {
        this.viagem = viagem;
        this.stage  = stage;
        // ID da carga gerado automaticamente (formato CRG-000)
        try {
            campoId.setText(cargaService.gerarProximoId());
        } catch (SQLException e) {
            labelErro.setText("Erro ao gerar ID: " + e.getMessage());
        }
        campoId.setDisable(true);
    }

    @FXML private void guardar() {
        try {
            Carga carga = new Carga(
                    campoId.getText().trim(),
                    campoDesignacao.getText().trim(),
                    comboTipoCarga.getValue(),
                    viagem.getId(),
                    Integer.parseInt(campoTanques.getText().trim()),
                    Double.parseDouble(campoVolume.getText().trim()),
                    Double.parseDouble(campoPeso.getText().trim()),
                    comboPortoCarga.getValue(),
                    comboPortoDescarga.getValue()
            );
            cargaService.adicionarCargaViagem(carga);
            AlertHelper.sucesso("Carga adicionada com sucesso.");
            stage.close();
        } catch (BusinessException e) { labelErro.setText("⚠ " + e.getMessage());
        } catch (NumberFormatException e) { labelErro.setText("⚠ Peso, volume e tanques devem ser numéricos.");
        } catch (SQLException e) { labelErro.setText("⚠ Erro BD: " + e.getMessage()); }
    }

    @FXML private void cancelar() { stage.close(); }
}
