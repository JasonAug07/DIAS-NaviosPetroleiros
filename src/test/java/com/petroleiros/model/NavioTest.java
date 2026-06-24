package com.petroleiros.model;

import com.petroleiros.model.enums.EstadoOperacional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes da logica de dominio do Navio.
 * Regras do enunciado: disponibilidade (so navios Ativos podem viajar) e
 * compatibilidade entre tipo de navio e tipo de carga.
 */
class NavioTest {

    private Navio navioComEstado(EstadoOperacional estado) {
        return new Navio("NAV-001", "Atlas", "IMO1", null,
                50000, 12, "Portugal", 2010, estado, null);
    }

    @Test
    @DisplayName("Navio Ativo esta disponivel")
    void navioAtivoEstaDisponivel() {
        assertTrue(navioComEstado(EstadoOperacional.ATIVO).isDisponivel());
    }

    @Test
    @DisplayName("Navio em Manutencao nao esta disponivel")
    void navioEmManutencaoNaoEstaDisponivel() {
        assertFalse(navioComEstado(EstadoOperacional.MANUTENCAO).isDisponivel());
    }

    @Test
    @DisplayName("Navio Inativo nao esta disponivel")
    void navioInativoNaoEstaDisponivel() {
        assertFalse(navioComEstado(EstadoOperacional.INATIVO).isDisponivel());
    }

    @Test
    @DisplayName("Aceita carga compativel com o tipo de navio")
    void aceitaCargaCompativel() {
        TipoCarga crude = new TipoCarga("TC-001", "Petroleo bruto", true, false, false);
        TipoNavio petroleiro = new TipoNavio("TN-001", "Petroleiro de crude", 5, "Crude");
        petroleiro.setCargasCompativeis(List.of(crude));
        Navio navio = new Navio("NAV-001", "Atlas", "IMO1", petroleiro,
                50000, 12, "Portugal", 2010, EstadoOperacional.ATIVO, null);

        assertTrue(navio.aceitaTipoCarga(crude));
    }

    @Test
    @DisplayName("Rejeita carga incompativel com o tipo de navio")
    void rejeitaCargaIncompativel() {
        TipoCarga crude   = new TipoCarga("TC-001", "Petroleo bruto", true, false, false);
        TipoCarga quimica = new TipoCarga("TC-002", "Produto quimico", false, true, true);
        TipoNavio petroleiro = new TipoNavio("TN-001", "Petroleiro de crude", 5, "Crude");
        petroleiro.setCargasCompativeis(List.of(crude));
        Navio navio = new Navio("NAV-001", "Atlas", "IMO1", petroleiro,
                50000, 12, "Portugal", 2010, EstadoOperacional.ATIVO, null);

        assertFalse(navio.aceitaTipoCarga(quimica));
    }

    @Test
    @DisplayName("Navio sem tipo definido nao aceita qualquer carga")
    void semTipoNavioNaoAceitaCarga() {
        TipoCarga crude = new TipoCarga("TC-001", "Petroleo bruto", true, false, false);
        Navio navio = navioComEstado(EstadoOperacional.ATIVO); // tipoNavio == null

        assertFalse(navio.aceitaTipoCarga(crude));
    }
}
