package com.petroleiros;

import com.petroleiros.dao.DatabaseConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainApp extends Application {

    private static final int MAX_TENTATIVAS  = 5;
    private static final int INTERVALO_SEG   = 5;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        primaryStage.setTitle("Sistema de Gestão de Navios Petroleiros");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.getIcons().add(new Image(
            MainApp.class.getResourceAsStream("/images/logo.png")));

        if (!ligarComRetry()) return;

        mostrarMenuPrincipal();
        primaryStage.show();
    }

    /**
     * Tenta ligar à BD até MAX_TENTATIVAS vezes, com INTERVALO_SEG segundos entre cada.
     * Mostra uma janela de progresso cancelável durante o processo.
     * Retorna true se ligou com sucesso, false se cancelou ou esgotou tentativas.
     */
    private boolean ligarComRetry() {
        // Janela de progresso
        Stage dialogStage = new Stage(StageStyle.TRANSPARENT);
        dialogStage.setResizable(false);

        ImageView logoSplash = new ImageView(new Image(
            MainApp.class.getResourceAsStream("/images/logo.png")));
        logoSplash.setFitWidth(48);
        logoSplash.setFitHeight(48);
        logoSplash.setPreserveRatio(true);

        Label lblTitulo  = new Label("A ligar à base de dados");
        lblTitulo.getStyleClass().add("conexao-titulo");

        Label lblEstado  = new Label("A iniciar ligação...");
        lblEstado.getStyleClass().add("conexao-estado");

        Label lblDetalhe = new Label("");
        lblDetalhe.getStyleClass().add("conexao-detalhe");
        lblDetalhe.setWrapText(true);
        lblDetalhe.setMaxWidth(300);

        ProgressBar pb = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        pb.getStyleClass().add("conexao-progress");

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("conexao-btn-cancelar");

        VBox card = new VBox(12, logoSplash, lblTitulo, lblEstado, pb, lblDetalhe, btnCancelar);
        card.getStyleClass().add("conexao-card");

        VBox root = new VBox(card);
        root.getStyleClass().add("conexao-root");
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, 400, 210);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        scene.getStylesheets().add(
            getClass().getResource("/views/conexao.css").toExternalForm()
        );
        dialogStage.setScene(scene);

        AtomicBoolean cancelado = new AtomicBoolean(false);
        AtomicBoolean sucesso   = new AtomicBoolean(false);

        btnCancelar.setOnAction(e -> {
            cancelado.set(true);
            dialogStage.close();
        });

        // Thread de ligação em background
        Thread thread = new Thread(() -> {
            for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
                if (cancelado.get()) break;

                final int t = tentativa;
                Platform.runLater(() -> {
                    lblEstado.setText("A tentar ligar... (tentativa " + t + "/" + MAX_TENTATIVAS + ")");
                    lblDetalhe.setText("A aguardar resposta do servidor...");
                });

                // Timer a correr enquanto tenta ligar
                AtomicBoolean timerAtivo = new AtomicBoolean(true);
                Thread timerThread = new Thread(() -> {
                    for (int seg = 1; timerAtivo.get() && !cancelado.get(); seg++) {
                        final int s = seg;
                        Platform.runLater(() ->
                            lblDetalhe.setText("A aguardar resposta... " + s + "s")
                        );
                        try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
                    }
                });
                timerThread.setDaemon(true);
                timerThread.start();

                try {
                    DatabaseConnection.getInstance();
                    sucesso.set(true);
                    timerAtivo.set(false);
                    Platform.runLater(dialogStage::close);
                    break;

                } catch (SQLException e) {
                    timerAtivo.set(false);
                    final String msg = e.getMessage() != null
                        ? e.getMessage().lines().findFirst().orElse(e.getMessage())
                        : "Erro desconhecido";

                    if (tentativa == MAX_TENTATIVAS) {
                        // Esgotou tentativas
                        Platform.runLater(() -> {
                            dialogStage.close();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Sem ligação");
                            alert.setHeaderText("Não foi possível ligar à base de dados após "
                                + MAX_TENTATIVAS + " tentativas.");
                            alert.setContentText(
                                "Verifique se o SQL Server está em execução.\n\nDetalhe: " + msg);
                            alert.showAndWait();
                        });
                        break;
                    }

                    // Contagem regressiva antes da próxima tentativa
                    for (int seg = INTERVALO_SEG; seg > 0; seg--) {
                        if (cancelado.get()) break;
                        final int s = seg;
                        Platform.runLater(() ->
                            lblDetalhe.setText("Próxima tentativa em " + s + "s  (" + msg + ")")
                        );
                        try { Thread.sleep(1000); } catch (InterruptedException ie) { break; }
                    }
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
        dialogStage.showAndWait();

        return sucesso.get();
    }

    public static void mostrarMenuPrincipal() throws Exception {
        FXMLLoader loader = new FXMLLoader(
            MainApp.class.getResource("/views/MenuPrincipal.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(
            MainApp.class.getResource("/views/estilos.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
