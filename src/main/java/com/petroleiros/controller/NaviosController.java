package com.petroleiros.controller;

import com.petroleiros.model.Navio;
import com.petroleiros.model.enums.EstadoOperacional;
import com.petroleiros.service.BusinessException;
import com.petroleiros.service.NavioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class NaviosController implements Initializable {

    @FXML private TableView<Navio>          tabelaNavios;
    @FXML private TableColumn<Navio,String> colNome;
    @FXML private TableColumn<Navio,String> colIMO;
    @FXML private TableColumn<Navio,String> colTipo;
    @FXML private TableColumn<Navio,Double> colCapacidade;
    @FXML private TableColumn<Navio,String> colBandeira;
    @FXML private TableColumn<Navio,String> colEstado;
    @FXML private TableColumn<Navio,String> colPorto;
    @FXML private ComboBox<String>          filtroEstado;
    @FXML private TextField                 filtroBusca;

    private NavioService navioService;
    private ObservableList<Navio> dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            navioService = new NavioService();
            configurarColunas();
            configurarFiltroEstado();
            carregarDados();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colIMO.setCellValueFactory(new PropertyValueFactory<>("codigoIMO"));
        colTipo.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                d.getValue().getTipoNavio() != null
                    ? d.getValue().getTipoNavio().getNome() : "-"));
        colCapacidade.setCellValueFactory(new PropertyValueFactory<>("capacidadeMaxima"));
        colBandeira.setCellValueFactory(new PropertyValueFactory<>("bandeira"));
        colEstado.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                d.getValue().getEstadoOperacional().getDescricao()));
        colPorto.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(
                d.getValue().getPortoAtual() != null
                    ? d.getValue().getPortoAtual().getNome() : "Em alto mar"));
        tabelaNavios.setItems(dados);
    }

    private void configurarFiltroEstado() {
        filtroEstado.getItems().add("Todos");
        for (EstadoOperacional e : EstadoOperacional.values())
            filtroEstado.getItems().add(e.getDescricao());
        filtroEstado.setValue("Todos");
    }

    private void carregarDados() throws SQLException {
        List<Navio> lista = navioService.listarTodos();
        dados.setAll(lista);
    }

    @FXML
    private void pesquisar() {
        try {
            List<Navio> lista = navioService.listarTodos();
            String busca  = filtroBusca.getText().toLowerCase().trim();
            String estado = filtroEstado.getValue();

            dados.setAll(lista.stream()
                .filter(n -> busca.isEmpty()
                    || n.getNome().toLowerCase().contains(busca)
                    || n.getCodigoIMO().toLowerCase().contains(busca))
                .filter(n -> "Todos".equals(estado)
                    || n.getEstadoOperacional().getDescricao().equals(estado))
                .toList());
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao pesquisar", e.getMessage());
        }
    }

    @FXML
    private void limparFiltros() {
        filtroBusca.clear();
        filtroEstado.setValue("Todos");
        try { carregarDados(); } catch (SQLException e) {
            AlertHelper.erro("Erro", e.getMessage());
        }
    }

    @FXML
    private void abrirFormularioNovo() {
        abrirFormulario(null);
    }

    @FXML
    private void editarSelecionado() {
        Navio selecionado = tabelaNavios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertHelper.aviso("Seleccione um navio para editar.");
            return;
        }
        abrirFormulario(selecionado);
    }

    @FXML
    private void alterarEstado() {
        Navio selecionado = tabelaNavios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertHelper.aviso("Seleccione um navio para alterar o estado.");
            return;
        }
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
            selecionado.getEstadoOperacional().getDescricao(),
            "Ativo", "Manutenção", "Inativo");
        dialog.setTitle("Alterar Estado Operacional");
        dialog.setHeaderText("Navio: " + selecionado.getNome());
        dialog.setContentText("Novo estado:");

        AlertHelper.estilizar(dialog);
        dialog.showAndWait().ifPresent(novoEstado -> {
            try {
                navioService.alterarEstado(
                    selecionado.getId(),
                    EstadoOperacional.fromDescricao(novoEstado));
                AlertHelper.sucesso("Estado alterado para " + novoEstado + ".");
                carregarDados();
            } catch (BusinessException e) {
                AlertHelper.erro("Operação inválida", e.getMessage());
            } catch (SQLException e) {
                AlertHelper.erro("Erro de base de dados", e.getMessage());
            }
        });
    }

    @FXML
    private void eliminarSelecionado() {
        Navio selecionado = tabelaNavios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            AlertHelper.aviso("Seleccione um navio para eliminar.");
            return;
        }
        if (!AlertHelper.confirmar("Eliminar Navio",
                "Tem a certeza que deseja eliminar o navio '" + selecionado.getNome() + "'?"))
            return;
        try {
            navioService.eliminar(selecionado.getId());
            AlertHelper.sucesso("Navio eliminado com sucesso.");
            carregarDados();
        } catch (BusinessException e) {
            AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.erro("Erro de base de dados", e.getMessage());
        }
    }

    private void abrirFormulario(Navio navio) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/navios/NavioFormulario.fxml"));
            Stage stage = new Stage();
            stage.setTitle(navio == null ? "Registar Navio" : "Editar Navio");
            stage.setScene(new Scene(loader.load(), 500, 550));
            stage.initModality(Modality.APPLICATION_MODAL);

            NavioFormularioController ctrl = loader.getController();
            ctrl.inicializar(navio, stage);
            stage.showAndWait();
            carregarDados();
        } catch (Exception e) {
            AlertHelper.erro("Erro ao abrir formulário", e.getMessage());
        }
    }
}
