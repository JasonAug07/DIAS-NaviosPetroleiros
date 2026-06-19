package com.petroleiros.controller;

import com.petroleiros.controller.AlertHelper;
import com.petroleiros.model.Porto;
import com.petroleiros.service.BusinessException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import com.petroleiros.service.PortoService;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Label;

public class PortosController implements Initializable {

    @FXML
    private TableView<Porto> tabelaPortos;
    @FXML private TableColumn<Porto, String> colNome;
    @FXML private TableColumn<Porto, String>   colPais;
    @FXML private TextField filtroBusca;

    private PortoService                       portoService;
    private ObservableList<Porto> dados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            portoService = new PortoService();
            colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
            colPais.setCellValueFactory(new PropertyValueFactory<>("pais"));
            tabelaPortos.setItems(dados);
            carregarDados();
        } catch (SQLException e) {
            AlertHelper.erro("Erro de ligação", e.getMessage());
        }
    }

    private void carregarDados() throws SQLException {
        dados.setAll(portoService.listarTodos());
    }

    @FXML private void pesquisar() {
        try {
            String busca = filtroBusca.getText().toLowerCase();
            dados.setAll(portoService.listarTodos().stream()
                    .filter(p -> busca.isEmpty()
                            || p.getNome().toLowerCase().contains(busca)
                            || p.getPais().toLowerCase().contains(busca))
                    .toList());
        } catch (SQLException e) {
            AlertHelper.erro("Erro", e.getMessage());
        }
    }

    @FXML private void abrirFormularioNovo() { abrirFormulario(null); }

    @FXML private void editarSelecionado() {
        Porto p = tabelaPortos.getSelectionModel().getSelectedItem();
        if (p == null) { AlertHelper.aviso("Seleccione um porto para editar."); return; }
        abrirFormulario(p);
    }

    @FXML private void eliminarSelecionado() {
        Porto p = tabelaPortos.getSelectionModel().getSelectedItem();
        if (p == null) { AlertHelper.aviso("Seleccione um porto para eliminar."); return; }
        if (!AlertHelper.confirmar("Eliminar Porto", "Eliminar '" + p.getNome() + "'?")) return;
        try {
            portoService.eliminar(p.getId());
            AlertHelper.sucesso("Porto eliminado.");
            carregarDados();
        } catch (BusinessException e) {
            AlertHelper.erro("Operação inválida", e.getMessage());
        } catch (SQLException e) {
            AlertHelper.erro("Erro de base de dados", e.getMessage());
        }
    }

    private void abrirFormulario(Porto porto) {
        // Formulário inline simples via Dialog
        Dialog<Porto> dialog = new Dialog<>();
        dialog.setTitle(porto == null ? "Registar Porto" : "Editar Porto");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField campoId   = new TextField(porto != null ? porto.getId() : "");
        TextField campoNome = new TextField(porto != null ? porto.getNome() : "");
        TextField campoPais = new TextField(porto != null ? porto.getPais() : "");
        if (porto != null) {
            campoId.setDisable(true);
        } else {
            try { campoId.setText(portoService.gerarProximoId()); }
            catch (SQLException e) { AlertHelper.erro("Erro", e.getMessage()); }
            campoId.setDisable(true);
        }

        grid.add(new Label("ID:"),   0, 0); grid.add(campoId,   1, 0);
        grid.add(new Label("Nome:"), 0, 1); grid.add(campoNome, 1, 1);
        grid.add(new Label("País:"), 0, 2); grid.add(campoPais, 1, 2);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK)
                return new Porto(campoId.getText().trim(),
                        campoNome.getText().trim(), campoPais.getText().trim());
            return null;
        });

        AlertHelper.estilizar(dialog);
        dialog.showAndWait().ifPresent(p -> {
            try {
                if (porto == null) portoService.registar(p);
                else               portoService.editar(p);
                AlertHelper.sucesso("Porto guardado.");
                carregarDados();
            } catch (BusinessException e) {
                AlertHelper.erro("Operação inválida", e.getMessage());
            } catch (SQLException e) {
                AlertHelper.erro("Erro de base de dados", e.getMessage());
            }
        });
    }
}