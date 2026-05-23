package com.petroleiros.model;

import com.petroleiros.model.enums.FuncaoTripulante;
import java.util.Objects;

public class Tripulante {

    private String id;
    private String nome;
    private FuncaoTripulante funcao;
    private String estadoDisponibilidade; // Disponível | Em Viagem | Licença

    public Tripulante() {}

    public Tripulante(String id, String nome, FuncaoTripulante funcao, String estadoDisponibilidade) {
        this.id = id;
        this.nome = nome;
        this.funcao = funcao;
        this.estadoDisponibilidade = estadoDisponibilidade;
    }

    public boolean isDisponivel() {
        return "Disponível".equalsIgnoreCase(estadoDisponibilidade);
    }

    public String getId()                      { return id; }
    public void setId(String id)               { this.id = id; }

    public String getNome()                    { return nome; }
    public void setNome(String nome)           { this.nome = nome; }

    public FuncaoTripulante getFuncao()                    { return funcao; }
    public void setFuncao(FuncaoTripulante funcao)         { this.funcao = funcao; }

    public String getEstadoDisponibilidade()                           { return estadoDisponibilidade; }
    public void setEstadoDisponibilidade(String estadoDisponibilidade) { this.estadoDisponibilidade = estadoDisponibilidade; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tripulante t)) return false;
        return Objects.equals(id, t.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nome + " (" + funcao + ")";
    }
}
