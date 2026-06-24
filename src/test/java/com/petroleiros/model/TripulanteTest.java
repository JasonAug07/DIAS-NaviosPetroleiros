package com.petroleiros.model;

import com.petroleiros.model.enums.FuncaoTripulante;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da disponibilidade do Tripulante.
 * Regra do enunciado: so tripulantes Disponiveis podem ser associados a viagens.
 */
class TripulanteTest {

    private Tripulante comEstado(String estado) {
        return new Tripulante("TRP-001", "Joao", FuncaoTripulante.CAPITAO, estado);
    }

    @Test
    @DisplayName("Tripulante Disponivel esta disponivel")
    void disponivel() {
        assertTrue(comEstado("Disponível").isDisponivel());
    }

    @Test
    @DisplayName("Tripulante Em Viagem nao esta disponivel")
    void emViagem() {
        assertFalse(comEstado("Em Viagem").isDisponivel());
    }

    @Test
    @DisplayName("Tripulante de Licenca nao esta disponivel")
    void licenca() {
        assertFalse(comEstado("Licença").isDisponivel());
    }

    @Test
    @DisplayName("Disponibilidade ignora maiusculas/minusculas")
    void ignoraCaixa() {
        assertTrue(comEstado("disponível").isDisponivel());
    }
}
