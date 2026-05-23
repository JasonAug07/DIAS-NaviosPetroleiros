package com.petroleiros.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TipoNavio {

    private String id;
    private String nome;
    private int maxCargasPorViagem;
    private String discriminator; // CRUDE | REFINADOS | QUIMICO | HIBRIDO
    private List<TipoCarga> cargasCompativeis = new ArrayList<>();

    public TipoNavio() {}

    public TipoNavio(String id, String nome, int maxCargasPorViagem, String discriminator) {
        this.id = id;
        this.nome = nome;
        this.maxCargasPorViagem = maxCargasPorViagem;
        this.discriminator = discriminator;
    }

    public boolean aceitaCarga(TipoCarga tipoCarga) {
        return cargasCompativeis.contains(tipoCarga);
    }

    public String getId()                      { return id; }
    public void setId(String id)               { this.id = id; }

    public String getNome()                    { return nome; }
    public void setNome(String nome)           { this.nome = nome; }

    public int getMaxCargasPorViagem()                         { return maxCargasPorViagem; }
    public void setMaxCargasPorViagem(int maxCargasPorViagem)  { this.maxCargasPorViagem = maxCargasPorViagem; }

    public String getDiscriminator()                   { return discriminator; }
    public void setDiscriminator(String discriminator) { this.discriminator = discriminator; }

    public List<TipoCarga> getCargasCompativeis()                          { return cargasCompativeis; }
    public void setCargasCompativeis(List<TipoCarga> cargasCompativeis)    { this.cargasCompativeis = cargasCompativeis; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TipoNavio tn)) return false;
        return Objects.equals(id, tn.id);
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
