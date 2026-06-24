package com.petroleiros.model;

import com.petroleiros.model.enums.EstadoOperacional;
import com.petroleiros.model.enums.EstadoViagem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da logica de dominio da Viagem.
 * Regras do enunciado: a capacidade do navio nao pode ser excedida pelas cargas
 * e o numero de cargas nao pode ultrapassar o maximo do tipo de navio.
 */
class ViagemTest {

    private Carga cargaComPeso(String id, double peso) {
        return new Carga(id, "Carga " + id, null, "VGM-001", 1, 100, peso, null, null);
    }

    private Navio navio(double capacidade, int maxCargas) {
        TipoNavio tipo = new TipoNavio("TN-001", "Petroleiro", maxCargas, "Crude");
        return new Navio("NAV-001", "Atlas", "IMO1", tipo,
                capacidade, 12, "Portugal", 2010, EstadoOperacional.ATIVO, null);
    }

    private Viagem viagemComNavio(Navio n) {
        return new Viagem("VGM-001", n, null, null,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), EstadoViagem.PLANEADA);
    }

    @Test
    @DisplayName("Peso total soma os pesos de todas as cargas")
    void pesoTotalSomaOsPesos() {
        Viagem v = viagemComNavio(navio(10000, 5));
        v.getCargas().add(cargaComPeso("CRG-001", 1500));
        v.getCargas().add(cargaComPeso("CRG-002", 2500));

        assertEquals(4000, v.pesoTotalCargas(), 0.001);
    }

    @Test
    @DisplayName("Sem cargas o peso total e zero")
    void semCargasPesoTotalZero() {
        Viagem v = viagemComNavio(navio(10000, 5));
        assertEquals(0, v.pesoTotalCargas(), 0.001);
    }

    @Test
    @DisplayName("Capacidade excedida quando o peso ultrapassa o maximo do navio")
    void capacidadeExcedida() {
        Viagem v = viagemComNavio(navio(3000, 5));
        v.getCargas().add(cargaComPeso("CRG-001", 2000));
        v.getCargas().add(cargaComPeso("CRG-002", 2000)); // 4000 > 3000

        assertTrue(v.capacidadeExcedida());
    }

    @Test
    @DisplayName("Capacidade nao excedida quando o peso esta dentro do limite")
    void capacidadeNaoExcedida() {
        Viagem v = viagemComNavio(navio(5000, 5));
        v.getCargas().add(cargaComPeso("CRG-001", 2000));
        v.getCargas().add(cargaComPeso("CRG-002", 2000)); // 4000 <= 5000

        assertFalse(v.capacidadeExcedida());
    }

    @Test
    @DisplayName("Limite de cargas atingido ao chegar ao maximo do tipo de navio")
    void maxCargasExcedido() {
        Viagem v = viagemComNavio(navio(10000, 2)); // maximo 2 cargas
        v.getCargas().add(cargaComPeso("CRG-001", 100));
        assertFalse(v.maxCargasExcedido());          // 1 < 2

        v.getCargas().add(cargaComPeso("CRG-002", 100));
        assertTrue(v.maxCargasExcedido());           // 2 >= 2
    }

    @Test
    @DisplayName("Viagem Em Curso esta ativa, Planeada nao")
    void viagemEmCursoEstaAtiva() {
        Viagem v = viagemComNavio(navio(10000, 5));
        v.setEstado(EstadoViagem.EM_CURSO);
        assertTrue(v.isAtiva());

        v.setEstado(EstadoViagem.PLANEADA);
        assertFalse(v.isAtiva());
    }
}
