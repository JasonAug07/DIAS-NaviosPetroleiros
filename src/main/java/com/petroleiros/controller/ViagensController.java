package com.petroleiros.controller;

import com.petroleiros.model.Viagem;
import com.petroleiros.model.enums.EstadoViagem;
import com.petroleiros.service.BusinessException;
import com.petroleiros.service.ViagemService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ViagensController implements Initializable {

    @FXML private TableView<Viagem>             tabelaViagens;
    @FXML private TableColumn<Viagem,String>    colId;
    @FXML private TableColumn<Viagem,String>    colNavio;
    @FXML private TableColumn<Viagem,String>    colOrigem;
    @FXML private TableColumn<Viagem,String>    colDestino;
    @FXML private TableColumn<Viagem,String>    colPartida;
    @FXML private TableColumn<Viagem,String>    colChegada;
    @FXML private TableColumn<Viagem,String>    colEstado;
    @FXML private TableColumn<Viagem,Integer>   colCargas;
    @FXML private TableColumn<Viagem,Integer>   colTripulacao;
    @FXML private ComboBox<String>              filtroEstado;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ViagemService                       viagemService;
    private ObservableList<Viagem>              dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            viagemService = new ViagemService();
            configurarColunas();
            configurarFiltro();
            carregarDados();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    private void configurarColunas() {
        colId.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getId()));
        colNavio.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getNavio() != null
                ? d.getValue().getNavio().getNome() : "-"));
        colOrigem.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getPortoOrigem() != null
                ? d.getValue().getPortoOrigem().getNome() : "-"));
        colDestino.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getPortoDestino() != null
                ? d.getValue().getPortoDestino().getNome() : "-"));
        colPartida.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDataPartida() != null
                ? d.getValue().getDataPartida().format(FMT) : "-"));
        colChegada.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getDataChegadaPrevista() != null
                ? d.getValue().getDataChegadaPrevista().format(FMT) : "-"));
        colEstado.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getEstado().getDescricao()));
        colCargas.setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().getNumCargas()).asObject());
        colTripulacao.setCellValueFactory(d ->
            new SimpleIntegerProperty(d.getValue().getNumTripulantes()).asObject());
        tabelaViagens.setItems(dados);
    }

    private void configurarFiltro() {
        filtroEstado.getItems().add("Todos");
        for (EstadoViagem e : EstadoViagem.values())
            filtroEstado.getItems().add(e.getDescricao());
        filtroEstado.setValue("Todos");
    }

    private void carregarDados() throws SQLException {
        dados.setAll(viagemService.listarTodas());
    }

    @FXML
    private void pesquisar() {
        try {
            String filtro = filtroEstado.getValue();
            if ("Todos".equals(filtro) || filtro == null) {
                dados.setAll(viagemService.listarTodas());
            } else {
                dados.setAll(viagemService.listarPorEstado(
                    EstadoViagem.fromDescricao(filtro)));
            }
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao filtrar", e.getMessage());
        }
    }

    @FXML
    private void abrirFormularioNovo() {
        abrirFormulario(null);
    }

    @FXML
    private void iniciarViagem() {
        Viagem v = getSelecionada("iniciar");
        if (v == null) return;
        if (!AlertHelper.confirmar("Iniciar Viagem",
            "Iniciar a viagem " + v.getId() + "?\n" +
            "O navio partirá de " + v.getPortoOrigem().getNome() + "."))
            return;
        try {
            viagemService.iniciarViagem(v.getId());
            AlertHelper.sucesso("Viagem iniciada com sucesso.");
            carregarDados();
        } catch (BusinessException e) {
            AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.erro("Erro de base de dados", e.getMessage());
        }
    }

    @FXML
    private void concluirViagem() {
        Viagem v = getSelecionada("concluir");
        if (v == null) return;
        if (!AlertHelper.confirmar("Concluir Viagem",
            "Concluir a viagem " + v.getId() + "?\n" +
            "O navio chegará a " + v.getPortoDestino().getNome() + "."))
            return;
        try {
            viagemService.concluirViagem(v.getId());
            AlertHelper.sucesso("Viagem concluída com sucesso.");
            carregarDados();
        } catch (BusinessException e) {
            AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.erro("Erro de base de dados", e.getMessage());
        }
    }

    @FXML
    private void cancelarViagem() {
        Viagem v = getSelecionada("cancelar");
        if (v == null) return;
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Viagem");
        dialog.setHeaderText("Motivo do cancelamento (opcional):");
        dialog.setContentText("Motivo:");
        dialog.showAndWait().ifPresent(motivo -> {
            try {
                viagemService.cancelarViagem(v.getId(),
                    motivo.isBlank() ? "Sem motivo especificado" : motivo);
                AlertHelper.sucesso("Viagem cancelada.");
                carregarDados();
            } catch (BusinessException e) {
                AlertHelper.erro("Operação inválida", e.getMessage());
            } catch (SQLException e) {
                AlertHelper.erro("Erro de base de dados", e.getMessage());
            }
        });
    }

    @FXML
    private void gerirCargas() {
        Viagem v = getSelecionada("gerir cargas de");
        if (v == null) return;
        abrirSubModulo("/views/viagens/ViagemCargas.fxml", v,
            "Cargas da Viagem - " + v.getId());
    }

    @FXML
    private void gerirTripulacao() {
        Viagem v = getSelecionada("gerir tripulação de");
        if (v == null) return;
        abrirSubModulo("/views/viagens/ViagemTripulacao.fxml", v,
            "Tripulação da Viagem - " + v.getId());
    }

    @FXML
    private void verDetalhe() {
        Viagem v = getSelecionada("ver detalhe de");
        if (v == null) return;
        abrirSubModulo("/views/viagens/ViagemDetalhe.fxml", v,
            "Detalhe da Viagem - " + v.getId());
    }

    // -- Helpers --------------------------------------------------------------

    private Viagem getSelecionada(String accao) {
        Viagem v = tabelaViagens.getSelectionModel().getSelectedItem();
        if (v == null) AlertHelper.aviso("Seleccione uma viagem para " + accao + ".");
        return v;
    }

    private void abrirFormulario(Viagem viagem) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/viagens/ViagemFormulario.fxml"));
            Stage stage = new Stage();
            stage.setTitle(viagem == null ? "Criar Viagem" : "Editar Viagem");
            stage.setScene(new Scene(loader.load(), 520, 480));
            stage.initModality(Modality.APPLICATION_MODAL);
            ViagemFormularioController ctrl = loader.getController();
            ctrl.inicializar(viagem, stage);
            stage.showAndWait();
            carregarDados();
        } catch (Exception e) {
            AlertHelper.erro("Erro ao abrir formulário", e.getMessage());
        }
    }

    private void abrirSubModulo(String fxmlPath, Viagem viagem, String titulo) {
        try {
            // A listagem traz apenas contagens; recarrega a viagem completa (com
            // cargas e tripulacao) pelo ID antes de a passar ao sub-modulo.
            Viagem completa = viagemService.buscar(viagem.getId()).orElse(viagem);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.setScene(new Scene(loader.load(), 600, 450));
            stage.initModality(Modality.APPLICATION_MODAL);
            // Passa a viagem ao controller via interface comum
            Object ctrl = loader.getController();
            if (ctrl instanceof ViagemAwareController vac)
                vac.setViagem(completa, stage);
            stage.showAndWait();
            carregarDados();
        } catch (Exception e) {
            AlertHelper.erro("Erro ao abrir sub-módulo", e.getMessage());
        }
    }
}
