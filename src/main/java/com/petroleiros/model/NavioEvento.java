package com.petroleiros.model;

import java.time.LocalDateTime;

/**
 * Representa um registo da tabela de log NAVIO_EVENTO (apenas leitura).
 * Os nomes do navio e do porto vem ja resolvidos por JOIN, para visualizacao.
 */
public class NavioEvento {

    private final int id;
    private final String navioId;
    private final String navioNome;
    private final String viagemId;
    private final String tipoEvento;
    private final String portoNome;
    private final LocalDateTime dataEvento;
    private final String descricao;

    public NavioEvento(int id, String navioId, String navioNome, String viagemId,
                       String tipoEvento, String portoNome, LocalDateTime dataEvento, String descricao) {
        this.id = id;
        this.navioId = navioId;
        this.navioNome = navioNome;
        this.viagemId = viagemId;
        this.tipoEvento = tipoEvento;
        this.portoNome = portoNome;
        this.dataEvento = dataEvento;
        this.descricao = descricao;
    }

    public int getId()                  { return id; }
    public String getNavioId()          { return navioId; }
    public String getNavioNome()        { return navioNome; }
    public String getViagemId()         { return viagemId; }
    public String getTipoEvento()       { return tipoEvento; }
    public String getPortoNome()        { return portoNome; }
    public LocalDateTime getDataEvento(){ return dataEvento; }
    public String getDescricao()        { return descricao; }
}
