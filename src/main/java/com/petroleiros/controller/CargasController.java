package com.petroleiros.controller;

import com.petroleiros.model.Carga;
import com.petroleiros.service.CargaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Listagem global de todas as cargas do sistema (apenas consulta).
 * A criacao/edicao de cargas e feita dentro de cada viagem (ViagemCargas),
 * porque uma carga nao existe sem viagem.
 */
public class CargasController implements Initializable {

    @FXML private TableView<Carga> tabelaCargas;
    @FXML private TableColumn<Carga, String> colDesignacao;
    @FXML private TableColumn<Carga, String> colTipo;
    @FXML private TableColumn<Carga, String> colViagem;
    @FXML private TableColumn<Carga, String> colTanques;
    @FXML private TableColumn<Carga, String> colVolume;
    @FXML private TableColumn<Carga, String> colPeso;
    @FXML private TableColumn<Carga, String> colPortoCarga;
    @FXML private TableColumn<Carga, String> colPortoDescarga;
    @FXML private TextField filtroBusca;

    private CargaService cargaService;
    private final ObservableList<Carga> dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargaService = new CargaService();

            colDesignacao.setCellValueFactory(new PropertyValueFactory<>("designacao"));
            colViagem.setCellValueFactory(new PropertyValueFactory<>("viagemId"));

            colTipo.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getTipoCarga() != null ? c.getValue().getTipoCarga().getNome() : ""));
            colTanques.setCellValueFactory(c -> new SimpleStringProperty(
                    String.valueOf(c.getValue().getNumTanquesOcupados())));
            colVolume.setCellValueFactory(c -> new SimpleStringProperty(
                    String.format("%.0f", c.getValue().getVolume())));
            colPeso.setCellValueFactory(c -> new SimpleStringProperty(
                    String.format("%.0f", c.getValue().getPeso())));
            colPortoCarga.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getPortoCarga() != null ? c.getValue().getPortoCarga().getNome() : ""));
            colPortoDescarga.setCellValueFactory(c -> new SimpleStringProperty(
                    c.getValue().getPortoDescarga() != null ? c.getValue().getPortoDescarga().getNome() : ""));

            tabelaCargas.setItems(dados);
            carregarDados();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    private void carregarDados() throws SQLException {
        dados.setAll(cargaService.listarTodas());
    }

    @FXML
    private void pesquisar() {
        try {
            String busca = filtroBusca.getText() == null ? "" : filtroBusca.getText().toLowerCase();
            dados.setAll(cargaService.listarTodas().stream()
                    .filter(c -> busca.isEmpty()
                            || (c.getDesignacao() != null && c.getDesignacao().toLowerCase().contains(busca))
                            || (c.getTipoCarga() != null && c.getTipoCarga().getNome().toLowerCase().contains(busca))
                            || (c.getViagemId() != null && c.getViagemId().toLowerCase().contains(busca)))
                    .toList());
        } catch (SQLException e) {
            AlertHelper.erro("Erro", e.getMessage());
        }
    }

    @FXML
    private void atualizar() {
        try {
            filtroBusca.clear();
            carregarDados();
        } catch (SQLException e) {
            AlertHelper.erro("Erro", e.getMessage());
        }
    }
}
