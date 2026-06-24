package com.petroleiros.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes de conversao dos enums de dominio (descricao <-> constante), usados
 * na ponte entre os valores guardados na base de dados e o modelo Java.
 */
class EnumsTest {

    @Test
    @DisplayName("EstadoOperacional converte descricao em constante e vice-versa")
    void estadoOperacionalRoundTrip() {
        assertEquals(EstadoOperacional.ATIVO, EstadoOperacional.fromDescricao("Ativo"));
        assertEquals(EstadoOperacional.MANUTENCAO, EstadoOperacional.fromDescricao("Manutenção"));
        assertEquals("Inativo", EstadoOperacional.INATIVO.getDescricao());
    }

    @Test
    @DisplayName("EstadoOperacional invalido lanca excecao")
    void estadoOperacionalInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> EstadoOperacional.fromDescricao("Desconhecido"));
    }

    @Test
    @DisplayName("EstadoViagem converte descricao em constante")
    void estadoViagemRoundTrip() {
        assertEquals(EstadoViagem.EM_CURSO, EstadoViagem.fromDescricao("Em Curso"));
        assertEquals(EstadoViagem.CONCLUIDA, EstadoViagem.fromDescricao("Concluída"));
        assertEquals("Cancelada", EstadoViagem.CANCELADA.getDescricao());
    }

    @Test
    @DisplayName("FuncaoTripulante converte descricao (ignorando caixa)")
    void funcaoTripulanteRoundTrip() {
        assertEquals(FuncaoTripulante.CAPITAO, FuncaoTripulante.fromDescricao("Capitão"));
        assertEquals(FuncaoTripulante.CAPITAO, FuncaoTripulante.fromDescricao("capitão"));
        assertEquals("Operador", FuncaoTripulante.OPERADOR.getDescricao());
    }

    @Test
    @DisplayName("Funcao invalida lanca excecao")
    void funcaoInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> FuncaoTripulante.fromDescricao("Grumete"));
    }
}
