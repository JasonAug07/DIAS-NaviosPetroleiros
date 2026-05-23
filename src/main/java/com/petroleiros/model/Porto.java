package com.petroleiros.model;

import java.util.Objects;

public class Porto {

    private String id;
    private String nome;
    private String pais;

    public Porto() {}

    public Porto(String id, String nome, String pais) {
        this.id = id;
        this.nome = nome;
        this.pais = pais;
    }

    public String getId()          { return id; }
    public void setId(String id)   { this.id = id; }

    public String getNome()            { return nome; }
    public void setNome(String nome)   { this.nome = nome; }

    public String getPais()            { return pais; }
    public void setPais(String pais)   { this.pais = pais; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Porto porto)) return false;
        return Objects.equals(id, porto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nome + " (" + pais + ")";
    }
}
