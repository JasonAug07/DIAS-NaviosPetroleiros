package com.petroleiros.model;

import com.petroleiros.model.enums.EstadoViagem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Viagem {

    private String id;
    private Navio navio;
    private Porto portoOrigem;
    private Porto portoDestino;
    private LocalDateTime dataPartida;
    private LocalDateTime dataChegadaPrevista;
    private LocalDateTime dataChegadaReal;       // preenchida ao concluir
    private EstadoViagem estado;
    private String motivoCancelamento;            // preenchido ao cancelar
    private List<Carga> cargas = new ArrayList<>();
    private List<Tripulante> tripulacao = new ArrayList<>();
    private int numCargas;       // contagem usada nas listagens (sem carregar a lista completa)
    private int numTripulantes;  // contagem usada nas listagens

    public Viagem() {}

    public Viagem(String id, Navio navio, Porto portoOrigem, Porto portoDestino,
                  LocalDateTime dataPartida, LocalDateTime dataChegadaPrevista,
                  EstadoViagem estado) {
        this.id = id;
        this.navio = navio;
        this.portoOrigem = portoOrigem;
        this.portoDestino = portoDestino;
        this.dataPartida = dataPartida;
        this.dataChegadaPrevista = dataChegadaPrevista;
        this.estado = estado;
    }

    // --- Regras de negócio ---

    public boolean isAtiva() {
        return estado == EstadoViagem.EM_CURSO;
    }

    public double pesoTotalCargas() {
        return cargas.stream().mapToDouble(Carga::getPeso).sum();
    }

    public int tanquesOcupados() {
        return cargas.stream().mapToInt(Carga::getNumTanquesOcupados).sum();
    }

    public boolean capacidadeExcedida() {
        return navio != null && pesoTotalCargas() > navio.getCapacidadeMaxima();
    }

    public boolean maxCargasExcedido() {
        return navio != null && navio.getTipoNavio() != null
                && cargas.size() >= navio.getTipoNavio().getMaxCargasPorViagem();
    }

    // --- Getters e Setters ---

    public String getId()                      { return id; }
    public void setId(String id)               { this.id = id; }

    public Navio getNavio()                    { return navio; }
    public void setNavio(Navio navio)          { this.navio = navio; }

    public Porto getPortoOrigem()                      { return portoOrigem; }
    public void setPortoOrigem(Porto portoOrigem)      { this.portoOrigem = portoOrigem; }

    public Porto getPortoDestino()                     { return portoDestino; }
    public void setPortoDestino(Porto portoDestino)    { this.portoDestino = portoDestino; }

    public LocalDateTime getDataPartida()                          { return dataPartida; }
    public void setDataPartida(LocalDateTime dataPartida)          { this.dataPartida = dataPartida; }

    public LocalDateTime getDataChegadaPrevista()                              { return dataChegadaPrevista; }
    public void setDataChegadaPrevista(LocalDateTime dataChegadaPrevista)      { this.dataChegadaPrevista = dataChegadaPrevista; }

    public LocalDateTime getDataChegadaReal()                                  { return dataChegadaReal; }
    public void setDataChegadaReal(LocalDateTime dataChegadaReal)              { this.dataChegadaReal = dataChegadaReal; }

    public String getMotivoCancelamento()                                       { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento)                { this.motivoCancelamento = motivoCancelamento; }

    public EstadoViagem getEstado()                    { return estado; }
    public void setEstado(EstadoViagem estado)         { this.estado = estado; }

    public List<Carga> getCargas()                     { return cargas; }
    public void setCargas(List<Carga> cargas)          { this.cargas = cargas; }

    public List<Tripulante> getTripulacao()                    { return tripulacao; }
    public void setTripulacao(List<Tripulante> tripulacao)     { this.tripulacao = tripulacao; }

    public int getNumCargas()                  { return numCargas; }
    public void setNumCargas(int numCargas)    { this.numCargas = numCargas; }

    public int getNumTripulantes()                     { return numTripulantes; }
    public void setNumTripulantes(int numTripulantes)  { this.numTripulantes = numTripulantes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Viagem v)) return false;
        return Objects.equals(id, v.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id + " | " + (portoOrigem != null ? portoOrigem.getNome() : "?")
                + " -> " + (portoDestino != null ? portoDestino.getNome() : "?")
                + " [" + estado + "]";
    }
}
