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

public class ViagemTripulacaoController implements Initializable, ViagemAwareController {

    @FXML private Label                         labelTitulo;
    @FXML private TableView<Tripulante>         tabelaTripulacao;
    @FXML private TableColumn<Tripulante,String>colNome;
    @FXML private TableColumn<Tripulante,String>colFuncao;
    @FXML private TableColumn<Tripulante,String>colEstado;

    private ViagemService                       viagemService;
    private TripulanteService                   tripulanteService;
    private Viagem                              viagem;
    private Stage                               stage;
    private ObservableList<Tripulante>          dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            viagemService    = new ViagemService();
            tripulanteService = new TripulanteService();
            colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
            colFuncao.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getFuncao().getDescricao()));
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoDisponibilidade"));
            tabelaTripulacao.setItems(dados);
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    @Override
    public void setViagem(Viagem viagem, Stage stage) {
        this.viagem = viagem;
        this.stage  = stage;
        labelTitulo.setText("Tripulação — Viagem " + viagem.getId());
        carregarDados();
    }

    private void carregarDados() {
        dados.setAll(viagem.getTripulacao());
    }

    @FXML private void associarTripulante() {
        try {
            List<Tripulante> disponiveis = tripulanteService.listarDisponiveis();
            if (disponiveis.isEmpty()) {
                AlertHelper.aviso("Não há tripulantes disponíveis."); return;
            }
            ChoiceDialog<Tripulante> dialog =
                    new ChoiceDialog<>(disponiveis.get(0), disponiveis);
            dialog.setTitle("Associar Tripulante");
            dialog.setHeaderText("Seleccione o tripulante a associar à viagem:");
            dialog.setContentText("Tripulante:");
            AlertHelper.estilizar(dialog);
            dialog.showAndWait().ifPresent(t -> {
                try {
                    viagemService.associarTripulante(viagem.getId(), t.getId());
                    viagem.getTripulacao().add(t);
                    carregarDados();
                } catch (BusinessException e) { AlertHelper.erro("Operação inválida", e.getMessage());
                } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
            });
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao carregar tripulantes", e.getMessage());
        }
    }

    @FXML private void removerTripulante() {
        Tripulante t = tabelaTripulacao.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tripulante para remover."); return; }
        if (!AlertHelper.confirmar("Remover Tripulante",
                "Remover '" + t.getNome() + "' da viagem?")) return;
        try {
            viagemService.removerTripulante(viagem.getId(), t.getId());
            viagem.getTripulacao().remove(t);
            carregarDados();
        } catch (BusinessException e) { AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
    }
}
