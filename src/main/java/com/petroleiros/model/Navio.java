package com.petroleiros.model;

import com.petroleiros.model.enums.EstadoOperacional;
import java.util.Objects;

public class Navio {

    private String id;
    private String nome;
    private String codigoIMO;
    private TipoNavio tipoNavio;
    private double capacidadeMaxima;
    private int numTanques;
    private String bandeira;
    private int anoFabrico;
    private EstadoOperacional estadoOperacional;
    private Porto portoAtual; // pode ser null (em alto mar)

    public Navio() {}

    public Navio(String id, String nome, String codigoIMO, TipoNavio tipoNavio,
                 double capacidadeMaxima, int numTanques, String bandeira,
                 int anoFabrico, EstadoOperacional estadoOperacional, Porto portoAtual) {
        this.id = id;
        this.nome = nome;
        this.codigoIMO = codigoIMO;
        this.tipoNavio = tipoNavio;
        this.capacidadeMaxima = capacidadeMaxima;
        this.numTanques = numTanques;
        this.bandeira = bandeira;
        this.anoFabrico = anoFabrico;
        this.estadoOperacional = estadoOperacional;
        this.portoAtual = portoAtual;
    }

    public boolean isDisponivel() {
        return estadoOperacional == EstadoOperacional.ATIVO;
    }

    public boolean aceitaTipoCarga(TipoCarga tipoCarga) {
        return tipoNavio != null && tipoNavio.aceitaCarga(tipoCarga);
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getNome()                        { return nome; }
    public void setNome(String nome)               { this.nome = nome; }

    public String getCodigoIMO()                       { return codigoIMO; }
    public void setCodigoIMO(String codigoIMO)         { this.codigoIMO = codigoIMO; }

    public TipoNavio getTipoNavio()                    { return tipoNavio; }
    public void setTipoNavio(TipoNavio tipoNavio)      { this.tipoNavio = tipoNavio; }

    public double getCapacidadeMaxima()                        { return capacidadeMaxima; }
    public void setCapacidadeMaxima(double capacidadeMaxima)   { this.capacidadeMaxima = capacidadeMaxima; }

    public int getNumTanques()                     { return numTanques; }
    public void setNumTanques(int numTanques)       { this.numTanques = numTanques; }

    public String getBandeira()                    { return bandeira; }
    public void setBandeira(String bandeira)       { this.bandeira = bandeira; }

    public int getAnoFabrico()                     { return anoFabrico; }
    public void setAnoFabrico(int anoFabrico)      { this.anoFabrico = anoFabrico; }

    public EstadoOperacional getEstadoOperacional()                            { return estadoOperacional; }
    public void setEstadoOperacional(EstadoOperacional estadoOperacional)      { this.estadoOperacional = estadoOperacional; }

    public Porto getPortoAtual()                   { return portoAtual; }
    public void setPortoAtual(Porto portoAtual)    { this.portoAtual = portoAtual; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Navio navio)) return false;
        return Objects.equals(id, navio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return nome + " [" + codigoIMO + "]";
    }
}
