package com.petroleiros.model.enums;

public enum FuncaoTripulante {
    CAPITAO("Capitão"),
    OFICIAL("Oficial"),
    ENGENHEIRO("Engenheiro"),
    OPERADOR("Operador");

    private final String descricao;

    FuncaoTripulante(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static FuncaoTripulante fromDescricao(String descricao) {
        for (FuncaoTripulante f : values()) {
            if (f.descricao.equalsIgnoreCase(descricao)) return f;
        }
        throw new IllegalArgumentException("Função inválida: " + descricao);
    }

    @Override
    public String toString() {
        return descricao;
    }
}
