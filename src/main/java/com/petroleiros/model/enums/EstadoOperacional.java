package com.petroleiros.model.enums;

public enum EstadoOperacional {
    ATIVO("Ativo"),
    MANUTENCAO("Manutenção"),
    INATIVO("Inativo");

    private final String descricao;

    EstadoOperacional(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static EstadoOperacional fromDescricao(String descricao) {
        for (EstadoOperacional e : values()) {
            if (e.descricao.equalsIgnoreCase(descricao)) return e;
        }
        throw new IllegalArgumentException("Estado operacional inválido: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
