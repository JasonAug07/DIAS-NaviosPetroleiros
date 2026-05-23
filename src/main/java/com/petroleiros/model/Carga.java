package com.petroleiros.model;

import java.util.Objects;

public class Carga {

    private String id;
    private String designacao;
    private TipoCarga tipoCarga;
    private String viagemId; // FK — carregado lazy para evitar ciclos
    private int numTanquesOcupados;
    private double volume;
    private double peso;
    private Porto portoCarga;
    private Porto portoDescarga;

    public Carga() {}

    public Carga(String id, String designacao, TipoCarga tipoCarga, String viagemId,
                 int numTanquesOcupados, double volume, double peso,
                 Porto portoCarga, Porto portoDescarga) {
        this.id = id;
        this.designacao = designacao;
        this.tipoCarga = tipoCarga;
        this.viagemId = viagemId;
        this.numTanquesOcupados = numTanquesOcupados;
        this.volume = volume;
        this.peso = peso;
        this.portoCarga = portoCarga;
        this.portoDescarga = portoDescarga;
    }

    public String getId()                      { return id; }
    public void setId(String id)               { this.id = id; }

    public String getDesignacao()                  { return designacao; }
    public void setDesignacao(String designacao)   { this.designacao = designacao; }

    public TipoCarga getTipoCarga()                    { return tipoCarga; }
    public void setTipoCarga(TipoCarga tipoCarga)      { this.tipoCarga = tipoCarga; }

    public String getViagemId()                    { return viagemId; }
    public void setViagemId(String viagemId)       { this.viagemId = viagemId; }

    public int getNumTanquesOcupados()                         { return numTanquesOcupados; }
    public void setNumTanquesOcupados(int numTanquesOcupados)  { this.numTanquesOcupados = numTanquesOcupados; }

    public double getVolume()                  { return volume; }
    public void setVolume(double volume)       { this.volume = volume; }

    public double getPeso()                    { return peso; }
    public void setPeso(double peso)           { this.peso = peso; }

    public Porto getPortoCarga()                   { return portoCarga; }
    public void setPortoCarga(Porto portoCarga)    { this.portoCarga = portoCarga; }

    public Porto getPortoDescarga()                    { return portoDescarga; }
    public void setPortoDescarga(Porto portoDescarga)  { this.portoDescarga = portoDescarga; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carga c)) return false;
        return Objects.equals(id, c.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return designacao + " (" + (tipoCarga != null ? tipoCarga.getNome() : "?") + ")";
    }
}
