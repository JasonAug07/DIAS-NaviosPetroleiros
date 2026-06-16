package com.petroleiros.controller;

import com.petroleiros.model.Viagem;
import javafx.stage.Stage;

/**
 * Interface implementada por controllers que recebem
 * uma Viagem como contexto (cargas, tripulação, detalhe).
 */
public interface ViagemAwareController {
    void setViagem(Viagem viagem, Stage stage);
}
