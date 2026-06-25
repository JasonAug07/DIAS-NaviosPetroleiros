package com.petroleiros.controller;

import com.petroleiros.controller.AlertHelper;
import com.petroleiros.controller.TripulanteFormularioController;
import com.petroleiros.dao.TripulanteDAO;
import com.petroleiros.model.Tripulante;
import com.petroleiros.model.enums.FuncaoTripulante;
import com.petroleiros.service.TripulanteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.petroleiros.service.BusinessException;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class TripulacaoController implements Initializable {

    @FXML
    private TableView<Tripulante> tabelaTripulantes;
    @FXML private TableColumn<Tripulante, String> colNome;
    @FXML private TableColumn<Tripulante, String>   colFuncao;
    @FXML private TableColumn<Tripulante, String>   colEstado;
    @FXML private ComboBox<String> filtroFuncao;
    @FXML private TextField filtroBusca;

    private TripulanteService tripulanteService;
    private ObservableList<Tripulante> dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            tripulanteService = new TripulanteService();
            colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
            colFuncao.setCellValueFactory(d ->
                    new javafx.beans.property.SimpleStringProperty(
                            d.getValue().getFuncao().getDescricao()));
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoDisponibilidade"));
            tabelaTripulantes.setItems(dados);

            filtroFuncao.getItems().add("Todos");
            for (FuncaoTripulante f : FuncaoTripulante.values())
                filtroFuncao.getItems().add(f.getDescricao());
            filtroFuncao.setValue("Todos");

            carregarDados();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    private void carregarDados() throws SQLException {
        dados.setAll(tripulanteService.listarTodos());
    }

    @FXML private void pesquisar() {
        try {
            String busca   = filtroBusca.getText().toLowerCase().trim();
            String funcao  = filtroFuncao.getValue();
            dados.setAll(tripulanteService.listarTodos().stream()
                    .filter(t -> busca.isEmpty() || t.getNome().toLowerCase().contains(busca))
                    .filter(t -> "Todos".equals(funcao) || t.getFuncao().getDescricao().equals(funcao))
                    .toList());
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao pesquisar", e.getMessage());
        }
    }

    @FXML private void limparFiltros() {
        filtroBusca.clear();
        filtroFuncao.setValue("Todos");
        try { carregarDados(); } catch (SQLException e) {
            AlertHelper.erro("Erro", e.getMessage());
        }
    }

    @FXML private void abrirFormularioNovo() { abrirFormulario(null); }

    @FXML private void editarSelecionado() {
        Tripulante t = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tripulante para editar."); return; }
        abrirFormulario(t);
    }

    @FXML private void eliminarSelecionado() {
        Tripulante t = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tripulante para eliminar."); return; }
        if (!AlertHelper.confirmar("Eliminar Tripulante",
                "Eliminar '" + t.getNome() + "'?")) return;
        try {
            tripulanteService.eliminar(t.getId());
            AlertHelper.sucesso("Tripulante eliminado.");
            carregarDados();
        } catch (BusinessException e) {
            AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.erro("Erro de base de dados", e.getMessage());
        }
    }

    /**
     * Caso de uso "Consultar Histórico de Viagens do Tripulante".
     * Mostra os totais agregados (vista vw_TripulanteHistorico) e a lista
     * detalhada das viagens em que o tripulante seleccionado participou.
     */
    @FXML private void verHistorico() {
        Tripulante t = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (t == null) { AlertHelper.aviso("Seleccione um tripulante para ver o histórico."); return; }
        try {
            Optional<TripulanteDAO.HistoricoTripulante> resumoOpt = tripulanteService.historico(t.getId());
            List<TripulanteDAO.ViagemDoTripulante> viagens = tripulanteService.viagensDoTripulante(t.getId());
            abrirDialogoHistorico(t, resumoOpt.orElse(null), viagens);
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao obter histórico", e.getMessage());
        }
    }

    private void abrirDialogoHistorico(Tripulante t,
                                       TripulanteDAO.HistoricoTripulante resumo,
                                       List<TripulanteDAO.ViagemDoTripulante> viagens) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Label titulo = new Label("Histórico de " + t.getNome() +
                "  (" + t.getFuncao().getDescricao() + ")");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        String resumoTxt;
        if (resumo == null) {
            resumoTxt = "Sem dados de histórico.";
        } else {
            String ultima = resumo.dataUltimaViagem() == null
                    ? "-" : resumo.dataUltimaViagem().format(fmt);
            resumoTxt = "Total de viagens: " + resumo.totalViagens() +
                    "      Concluídas: " + resumo.viagensConcluidas() +
                    "      Em curso: " + resumo.viagensEmCurso() +
                    "      Canceladas: " + resumo.viagensCanceladas() +
                    "\nÚltima partida: " + ultima +
                    "      Disponibilidade actual: " + resumo.estadoDisponibilidade();
        }
        Label labelResumo = new Label(resumoTxt);

        TableView<TripulanteDAO.ViagemDoTripulante> tabela = new TableView<>();
        TableColumn<TripulanteDAO.ViagemDoTripulante, String> cViagem = new TableColumn<>("Viagem");
        cViagem.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().viagemId()));
        cViagem.setPrefWidth(80);
        TableColumn<TripulanteDAO.ViagemDoTripulante, String> cNavio = new TableColumn<>("Navio");
        cNavio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().navioNome()));
        cNavio.setPrefWidth(150);
        TableColumn<TripulanteDAO.ViagemDoTripulante, String> cRota = new TableColumn<>("Rota");
        cRota.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().origem() + "  ->  " + d.getValue().destino()));
        cRota.setPrefWidth(220);
        TableColumn<TripulanteDAO.ViagemDoTripulante, String> cData = new TableColumn<>("Partida");
        cData.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().dataPartida() == null ? "-" : d.getValue().dataPartida().format(fmt)));
        cData.setPrefWidth(130);
        TableColumn<TripulanteDAO.ViagemDoTripulante, String> cEstado = new TableColumn<>("Estado");
        cEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        cEstado.setPrefWidth(110);
        tabela.getColumns().setAll(List.of(cViagem, cNavio, cRota, cData, cEstado));
        tabela.setItems(FXCollections.observableArrayList(viagens));
        tabela.setPlaceholder(new Label("Este tripulante ainda não participou em nenhuma viagem."));
        tabela.setPrefHeight(280);

        VBox conteudo = new VBox(10, titulo, labelResumo, tabela);
        conteudo.setPadding(new Insets(16));
        conteudo.setPrefWidth(720);

        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Histórico de Viagens do Tripulante");
        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.initModality(Modality.APPLICATION_MODAL);
        AlertHelper.estilizar(dialogo);
        dialogo.showAndWait();
    }

    private void abrirFormulario(Tripulante tripulante) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/tripulacao/TripulanteFormulario.fxml"));
            Stage stage = new Stage();
            stage.setTitle(tripulante == null ? "Registar Tripulante" : "Editar Tripulante");
            stage.setScene(new Scene(loader.load(), 420, 320));
            stage.initModality(Modality.APPLICATION_MODAL);
            TripulanteFormularioController ctrl = loader.getController();
            ctrl.inicializar(tripulante, stage);
            stage.showAndWait();
            carregarDados();
        } catch (Exception e) {
            AlertHelper.erro("Erro ao abrir formulário", e.getMessage());
        }
    }
}
