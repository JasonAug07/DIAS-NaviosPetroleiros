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
public class ViagemCargasController implements Initializable, ViagemAwareController {

    @FXML private Label                         labelTitulo;
    @FXML private Label                         labelCapacidade;
    @FXML private TableView<Carga>              tabelaCargas;
    @FXML private TableColumn<Carga,String>     colDesignacao;
    @FXML private TableColumn<Carga,String>     colTipoCarga;
    @FXML private TableColumn<Carga,Double>     colPeso;
    @FXML private TableColumn<Carga,Double>     colVolume;
    @FXML private TableColumn<Carga,Integer>    colTanques;
    @FXML private TableColumn<Carga,String>     colPortoCarga;
    @FXML private TableColumn<Carga,String>     colPortoDescarga;

    private CargaService                        cargaService;
    private NavioService                        navioService;
    private ViagemService                       viagemService;
    private Viagem                              viagem;
    private Stage                               stage;
    private ObservableList<Carga>               dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            cargaService = new CargaService();
            navioService = new NavioService();
            viagemService = new ViagemService();
            colDesignacao.setCellValueFactory(new PropertyValueFactory<>("designacao"));
            colTipoCarga.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getTipoCarga() != null ? d.getValue().getTipoCarga().getNome() : "-"));
            colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
            colVolume.setCellValueFactory(new PropertyValueFactory<>("volume"));
            colTanques.setCellValueFactory(new PropertyValueFactory<>("numTanquesOcupados"));
            colPortoCarga.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getPortoCarga() != null ? d.getValue().getPortoCarga().getNome() : "-"));
            colPortoDescarga.setCellValueFactory(d -> new SimpleStringProperty(
                    d.getValue().getPortoDescarga() != null ? d.getValue().getPortoDescarga().getNome() : "-"));
            tabelaCargas.setItems(dados);
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    @Override
    public void setViagem(Viagem viagem, Stage stage) {
        this.viagem = viagem;
        this.stage  = stage;
        labelTitulo.setText("Cargas - Viagem " + viagem.getId());
        carregarDados();
    }

    private void carregarDados() {
        try {
            dados.setAll(cargaService.listarPorViagem(viagem.getId()));

            // Capacidade ainda livre calculada pela funcao fn_CapacidadeDisponivel
            double disponivel = cargaService.capacidadeDisponivel(viagem.getId());
            // Totais e percentagem de utilizacao lidos da vista vw_ViagemDetalhada
            var resumo = viagemService.resumoViagem(viagem.getId());

            double capacidade = resumo != null ? resumo.capacidadeMaxima()
                    : (viagem.getNavio() != null ? viagem.getNavio().getCapacidadeMaxima() : 0);
            double pesoTotal  = resumo != null ? resumo.pesoTotal() : (capacidade - disponivel);
            double pct        = resumo != null ? resumo.pctCapacidade() : 0;

            labelCapacidade.setText(String.format(
                    "Peso total: %.2f t  |  Capacidade: %.2f t  |  Disponível: %.2f t  |  Utilização: %.1f%%",
                    pesoTotal, capacidade, disponivel, pct));
        } catch (SQLException e) {
            AlertHelper.erro("Erro ao carregar cargas", e.getMessage());
        }
    }

    @FXML private void abrirFormularioCarga() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/viagens/CargaFormulario.fxml"));
            Stage s = new Stage();
            s.setTitle("Adicionar Carga");
            s.setScene(new Scene(loader.load(), 500, 500));
            s.initModality(Modality.APPLICATION_MODAL);
            CargaFormularioController ctrl = loader.getController();
            ctrl.inicializar(viagem, s);
            s.showAndWait();
            carregarDados();
        } catch (Exception e) {
            AlertHelper.erro("Erro ao abrir formulário", e.getMessage());
        }
    }

    @FXML private void removerCarga() {
        Carga c = tabelaCargas.getSelectionModel().getSelectedItem();
        if (c == null) { AlertHelper.aviso("Seleccione uma carga para remover."); return; }
        if (!AlertHelper.confirmar("Remover Carga",
                "Remover carga '" + c.getDesignacao() + "' da viagem?")) return;
        try {
            cargaService.eliminar(c.getId());
            carregarDados();
        } catch (BusinessException e) { AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e)      { AlertHelper.erro("Erro BD", e.getMessage()); }
    }
}


