package com.petroleiros.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MenuPrincipalController {

    @FXML private StackPane conteudoCentral;

    @FXML
    private void abrirNavios() {
        carregarModulo("/views/navios/Navios.fxml");
    }

    @FXML
    private void abrirPortos() {
        carregarModulo("/views/portos/Portos.fxml");
    }

    @FXML
    private void abrirTipos() {
        carregarModulo("/views/tipos/Tipos.fxml");
    }

    @FXML
    private void abrirCargas() {
        carregarModulo("/views/cargas/Cargas.fxml");
    }

    @FXML
    private void abrirViagens() {
        carregarModulo("/views/viagens/Viagens.fxml");
    }

    @FXML
    private void abrirTripulacao() {
        carregarModulo("/views/tripulacao/Tripulacao.fxml");
    }

    @FXML
    private void abrirEventos() {
        carregarModulo("/views/eventos/Eventos.fxml");
    }

    @FXML
    private void sair() {
        Stage stage = (Stage) conteudoCentral.getScene().getWindow();
        stage.close();
    }

    private void carregarModulo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Pane modulo = loader.load();
            conteudoCentral.getChildren().setAll(modulo);
        } catch (Exception e) {
            AlertHelper.erro("Erro ao carregar módulo", e.getMessage());
        }
    }
}
