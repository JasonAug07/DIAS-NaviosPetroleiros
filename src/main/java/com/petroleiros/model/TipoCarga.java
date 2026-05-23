package com.petroleiros.model;

import java.util.Objects;

public class TipoCarga {

    private String id;
    private String nome;
    private boolean inflamavel;
    private boolean corrosiva;
    private boolean toxica;

    public TipoCarga() {}

    public TipoCarga(String id, String nome, boolean inflamavel, boolean corrosiva, boolean toxica) {
        this.id = id;
        this.nome = nome;
        this.inflamavel = inflamavel;
        this.corrosiva = corrosiva;
        this.toxica = toxica;
    }

    public String getId()              { return id; }
    public void setId(String id)       { this.id = id; }

    public String getNome()            { return nome; }
    public void setNome(String nome)   { this.nome = nome; }

    public boolean isInflamavel()                  { return inflamavel; }
    public void setInflamavel(boolean inflamavel)  { this.inflamavel = inflamavel; }

    public boolean isCorrosiva()                   { return corrosiva; }
    public void setCorrosiva(boolean corrosiva)    { this.corrosiva = corrosiva; }

    public boolean isToxica()                      { return toxica; }
    public void setToxica(boolean toxica)          { this.toxica = toxica; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoCarga tc)) return false;
        return Objects.equals(id, tc.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nome;
    }
}
