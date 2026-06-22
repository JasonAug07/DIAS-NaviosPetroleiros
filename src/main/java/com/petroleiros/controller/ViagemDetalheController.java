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


public class ViagemDetalheController implements Initializable, ViagemAwareController {

    @FXML private Label       lblId;
    @FXML private Label       lblEstado;
    @FXML private Label       lblNavio;
    @FXML private Label       lblOrigem;
    @FXML private Label       lblDestino;
    @FXML private Label       lblPartida;
    @FXML private Label       lblChegada;
    @FXML private Label       lblPesoTotal;
    @FXML private ProgressBar barraCapacidade;
    @FXML private Label       lblNumCargas;
    @FXML private Label       lblTripulacao;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @Override
    public void setViagem(Viagem v, Stage stage) {
        this.stage = stage;
        lblId.setText(v.getId());
        lblEstado.setText(v.getEstado().getDescricao());
        lblNavio.setText(v.getNavio() != null
                ? v.getNavio().getNome() + " [" + v.getNavio().getCodigoIMO() + "]" : "-");
        lblOrigem.setText(v.getPortoOrigem() != null
                ? v.getPortoOrigem().getNome() + " (" + v.getPortoOrigem().getPais() + ")" : "-");
        lblDestino.setText(v.getPortoDestino() != null
                ? v.getPortoDestino().getNome() + " (" + v.getPortoDestino().getPais() + ")" : "-");
        lblPartida.setText(v.getDataPartida() != null
                ? v.getDataPartida().format(FMT) : "-");
        lblChegada.setText(v.getDataChegadaPrevista() != null
                ? v.getDataChegadaPrevista().format(FMT) : "-");

        double pesoTotal  = v.pesoTotalCargas();
        double capacidade = v.getNavio() != null ? v.getNavio().getCapacidadeMaxima() : 1;
        lblPesoTotal.setText(String.format("%.2f t", pesoTotal));
        barraCapacidade.setProgress(capacidade > 0 ? pesoTotal / capacidade : 0);

        lblNumCargas.setText(String.valueOf(v.getCargas().size()));
        lblTripulacao.setText(v.getTripulacao().size() + " tripulante(s): " +
                v.getTripulacao().stream().map(Tripulante::getNome)
                        .reduce("", (a,b) -> a.isEmpty() ? b : a + ", " + b));
    }

    @FXML private void fechar() { stage.close(); }
}
