package com.petroleiros.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;

public class AlertHelper {

    /** Aplica a folha de estilos principal a um diálogo/alerta, para aspeto consistente. */
    public static void estilizar(Dialog<?> dialog) {
        var css = AlertHelper.class.getResource("/views/estilos.css");
        if (css != null)
            dialog.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    public static void erro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        estilizar(alert);
        alert.showAndWait();
    }

    public static void sucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        estilizar(alert);
        alert.showAndWait();
    }

    public static void aviso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        estilizar(alert);
        alert.showAndWait();
    }

    public static boolean confirmar(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);
        estilizar(alert);
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }
}
