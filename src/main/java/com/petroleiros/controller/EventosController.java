package com.petroleiros.controller;

import com.petroleiros.dao.NavioEventoDAO;
import com.petroleiros.model.Navio;
import com.petroleiros.model.NavioEvento;
import com.petroleiros.service.NavioService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Ecra de consulta do log de eventos (tabela NAVIO_EVENTO).
 * Os eventos sao gerados automaticamente pelos procedimentos e triggers da BD;
 * este ecra apenas os mostra. Permite filtrar por navio e ve o atraso medio
 * desse navio atraves da funcao fn_AtrasoMedioNavio.
 */
public class EventosController implements Initializable {

    @FXML private TableView<NavioEvento> tabelaEventos;
    @FXML private TableColumn<NavioEvento, String> colData;
    @FXML private TableColumn<NavioEvento, String> colNavio;
    @FXML private TableColumn<NavioEvento, String> colTipo;
    @FXML private TableColumn<NavioEvento, String> colViagem;
    @FXML private TableColumn<NavioEvento, String> colPorto;
    @FXML private TableColumn<NavioEvento, String> colDescricao;
    @FXML private ComboBox<Navio> comboNavio;
    @FXML private Label labelInfo;

    private NavioEventoDAO eventoDAO;
    private NavioService navioService;
    private final ObservableList<NavioEvento> dados = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            eventoDAO = new NavioEventoDAO();
            navioService = new NavioService();

            colData.setCellValueFactory(e -> new SimpleStringProperty(
                    e.getValue().getDataEvento() != null ? FMT.format(e.getValue().getDataEvento()) : ""));
            colNavio.setCellValueFactory(new PropertyValueFactory<>("navioNome"));
            colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoEvento"));
            colViagem.setCellValueFactory(e -> new SimpleStringProperty(
                    e.getValue().getViagemId() != null ? e.getValue().getViagemId() : "-"));
            colPorto.setCellValueFactory(e -> new SimpleStringProperty(
                    e.getValue().getPortoNome() != null ? e.getValue().getPortoNome() : "-"));
            colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));

            tabelaEventos.setItems(dados);

            comboNavio.setConverter(new StringConverter<>() {
                @Override public String toString(Navio n) { return n == null ? "" : n.getNome(); }
                @Override public Navio fromString(String s) { return null; }
            });
            comboNavio.getItems().setAll(navioService.listarTodos());

            verTodos();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    @FXML
    private void filtrarPorNavio() {
        Navio navio = comboNavio.getValue();
        if (navio == null) { AlertHelper.aviso("Seleccione um navio para filtrar."); return; }
        try {
            dados.setAll(eventoDAO.listarPorNavio(navio.getId()));
            double atraso = navioService.atrasoMedio(navio.getId());
            labelInfo.setText(String.format(
                    "%s - %d evento(s).  Atraso médio das viagens concluídas: %.1f h",
                    navio.getNome(), dados.size(), atraso));
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao carregar eventos", e.getMessage());
        }
    }

    @FXML
    private void verTodos() {
        try {
            comboNavio.getSelectionModel().clearSelection();
            dados.setAll(eventoDAO.listarTodos());
            labelInfo.setText("Todos os eventos - " + dados.size() + " registo(s).");
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao carregar eventos", e.getMessage());
        }
    }
}
