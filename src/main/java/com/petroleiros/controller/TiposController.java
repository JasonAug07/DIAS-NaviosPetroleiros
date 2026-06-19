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
import com.petroleiros.service.*;

public class TiposController implements Initializable {

    // Tipos de Navio
    @FXML private TableView<TipoNavio>             tabelaTiposNavio;
    @FXML private TableColumn<TipoNavio,String>    colTNNome;
    @FXML private TableColumn<TipoNavio,Integer>   colTNMaxCargas;
    @FXML private TableColumn<TipoNavio,String>    colTNDiscriminator;
    @FXML private TableColumn<TipoNavio,String>    colTNCompativeis;
    // Tipos de Carga
    @FXML private TableView<TipoCarga>             tabelaTiposCarga;
    @FXML private TableColumn<TipoCarga,String>    colTCNome;
    @FXML private TableColumn<TipoCarga,Boolean>   colTCInflamavel;
    @FXML private TableColumn<TipoCarga,Boolean>   colTCCorrosiva;
    @FXML private TableColumn<TipoCarga,Boolean>   colTCToxica;

    private TipoNavioService tipoNavioService;
    private TipoCargoService tipoCargoService;
    private ObservableList<TipoNavio> dadosNavio = FXCollections.observableArrayList();
    private ObservableList<TipoCarga> dadosCarga = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            tipoNavioService = new TipoNavioService();
            tipoCargoService = new TipoCargoService();
            configurarTiposNavio();
            configurarTiposCarga();
            carregarTodos();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    private void configurarTiposNavio() {
        colTNNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTNMaxCargas.setCellValueFactory(new PropertyValueFactory<>("maxCargasPorViagem"));
        colTNDiscriminator.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colTNCompativeis.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCargasCompativeis() == null ? "-" :
                        d.getValue().getCargasCompativeis().stream()
                                .map(TipoCarga::getNome).reduce("", (a,b) -> a.isEmpty() ? b : a+", "+b)));
        tabelaTiposNavio.setItems(dadosNavio);
    }

    private void configurarTiposCarga() {
        colTCNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTCInflamavel.setCellValueFactory(new PropertyValueFactory<>("inflamavel"));
        colTCCorrosiva.setCellValueFactory(new PropertyValueFactory<>("corrosiva"));
        colTCToxica.setCellValueFactory(new PropertyValueFactory<>("toxica"));
        tabelaTiposCarga.setItems(dadosCarga);
    }

    private void carregarTodos() throws SQLException {
        dadosNavio.setAll(tipoNavioService.listarTodos());
        dadosCarga.setAll(tipoCargoService.listarTodos());
    }

    @FXML private void novoTipoNavio()    { abrirDialogTipoNavio(null); }
    @FXML private void editarTipoNavio()  {
        TipoNavio t = tabelaTiposNavio.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tipo de navio."); return; }
        abrirDialogTipoNavio(t);
    }
    @FXML private void eliminarTipoNavio() {
        TipoNavio t = tabelaTiposNavio.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tipo de navio."); return; }
        if (!AlertHelper.confirmar("Eliminar", "Eliminar tipo '" + t.getNome() + "'?")) return;
        try {
            tipoNavioService.eliminar(t.getId());
            carregarTodos();
        } catch (BusinessException e) { AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
    }

    @FXML private void novoTipoCarga()   { abrirDialogTipoCarga(null); }
    @FXML private void editarTipoCarga() {
        TipoCarga t = tabelaTiposCarga.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tipo de carga."); return; }
        abrirDialogTipoCarga(t);
    }
    @FXML private void eliminarTipoCarga() {
        TipoCarga t = tabelaTiposCarga.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tipo de carga."); return; }
        if (!AlertHelper.confirmar("Eliminar", "Eliminar tipo '" + t.getNome() + "'?")) return;
        try {
            tipoCargoService.eliminar(t.getId());
            carregarTodos();
        } catch (BusinessException e) { AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
    }

    private void abrirDialogTipoNavio(TipoNavio tn) {
        Dialog<TipoNavio> d = new Dialog<>();
        d.setTitle(tn == null ? "Novo Tipo de Navio" : "Editar Tipo de Navio");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(10); g.setVgap(10);
        TextField campoId   = new TextField(tn != null ? tn.getId() : "");
        TextField campoNome = new TextField(tn != null ? tn.getNome() : "");
        TextField campoMax  = new TextField(tn != null ? String.valueOf(tn.getMaxCargasPorViagem()) : "");
        ComboBox<String> comboDisc = new ComboBox<>();
        comboDisc.getItems().addAll("Crude", "Produto", "Químico", "Químico/Produto");
        if (tn != null) { comboDisc.setValue(tn.getCategoria()); campoId.setDisable(true); }
        else {
            try { campoId.setText(tipoNavioService.gerarProximoId()); }
            catch (SQLException e) { AlertHelper.erro("Erro", e.getMessage()); }
            campoId.setDisable(true);
        }
        g.add(new Label("ID:"),          0,0); g.add(campoId,   1,0);
        g.add(new Label("Nome:"),        0,1); g.add(campoNome, 1,1);
        g.add(new Label("Máx. Cargas:"), 0,2); g.add(campoMax,  1,2);
        g.add(new Label("Categoria:"),   0,3); g.add(comboDisc, 1,3);
        d.getDialogPane().setContent(g);
        d.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            TipoNavio r = new TipoNavio(campoId.getText().trim(), campoNome.getText().trim(),
                    Integer.parseInt(campoMax.getText().trim()), comboDisc.getValue());
            if (tn != null) r.setCargasCompativeis(tn.getCargasCompativeis());
            return r;
        });
        AlertHelper.estilizar(d);
        d.showAndWait().ifPresent(t -> {
            try {
                if (tn == null) tipoNavioService.registar(t);
                else            tipoNavioService.editar(t);
                carregarTodos();
            } catch (BusinessException e) { AlertHelper.erro("Erro", e.getMessage());
            } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
        });
    }

    private void abrirDialogTipoCarga(TipoCarga tc) {
        Dialog<TipoCarga> d = new Dialog<>();
        d.setTitle(tc == null ? "Novo Tipo de Carga" : "Editar Tipo de Carga");
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        javafx.scene.layout.GridPane g = new javafx.scene.layout.GridPane();
        g.setHgap(10); g.setVgap(10);
        TextField campoId   = new TextField(tc != null ? tc.getId() : "");
        TextField campoNome = new TextField(tc != null ? tc.getNome() : "");
        CheckBox  cbInf     = new CheckBox(); if (tc != null) cbInf.setSelected(tc.isInflamavel());
        CheckBox  cbCor     = new CheckBox(); if (tc != null) cbCor.setSelected(tc.isCorrosiva());
        CheckBox  cbTox     = new CheckBox(); if (tc != null) cbTox.setSelected(tc.isToxica());
        if (tc != null) {
            campoId.setDisable(true);
        } else {
            try { campoId.setText(tipoCargoService.gerarProximoId()); }
            catch (SQLException e) { AlertHelper.erro("Erro", e.getMessage()); }
            campoId.setDisable(true);
        }
        g.add(new Label("ID:"),         0,0); g.add(campoId,  1,0);
        g.add(new Label("Nome:"),       0,1); g.add(campoNome,1,1);
        g.add(new Label("Inflamável:"), 0,2); g.add(cbInf,   1,2);
        g.add(new Label("Corrosiva:"),  0,3); g.add(cbCor,   1,3);
        g.add(new Label("Tóxica:"),     0,4); g.add(cbTox,   1,4);
        d.getDialogPane().setContent(g);
        d.setResultConverter(btn -> btn == ButtonType.OK
                ? new TipoCarga(campoId.getText().trim(), campoNome.getText().trim(),
                cbInf.isSelected(), cbCor.isSelected(), cbTox.isSelected()) : null);
        AlertHelper.estilizar(d);
        d.showAndWait().ifPresent(t -> {
            try {
                if (tc == null) tipoCargoService.registar(t);
                else            tipoCargoService.editar(t);
                carregarTodos();
            } catch (BusinessException e) { AlertHelper.erro("Erro", e.getMessage());
            } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
        });
    }
}
