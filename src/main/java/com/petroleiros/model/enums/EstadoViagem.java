package com.petroleiros.model.enums;

public enum EstadoViagem {
    PLANEADA("Planeada"),
    EM_CURSO("Em Curso"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String descricao;

    EstadoViagem(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static EstadoViagem fromDescricao(String descricao) {
        for (EstadoViagem e : values()) {
            if (e.descricao.equalsIgnoreCase(descricao)) return e;
        }
        throw new IllegalArgumentException("Estado de viagem inválido: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
