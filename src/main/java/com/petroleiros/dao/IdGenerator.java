package com.petroleiros.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Gera identificadores sequenciais no formato PREFIXO-000 para as entidades
 * cujos IDs sao geridos pela aplicacao (nao sao IDENTITY na base de dados).
 *
 * A tabela e o prefixo sao sempre constantes de codigo (nunca input do
 * utilizador), por isso a sua concatenacao na query nao constitui risco de
 * injecao; os valores variaveis continuam a passar por PreparedStatement.
 */
public final class IdGenerator {

    private IdGenerator() {}

    public static String proximo(Connection conn, String tabela, String prefixo) throws SQLException {
        int posNumero = prefixo.length() + 2; // 1o digito, logo a seguir a "PREFIXO-"
        // TRY_CAST devolve NULL (em vez de erro) se a parte numerica nao for so digitos,
        // o que torna a geracao robusta mesmo que exista um ID manual mal formado.
        String sql = "SELECT ISNULL(MAX(TRY_CAST(SUBSTRING(id, " + posNumero + ", 20) AS INT)), 0) + 1 AS prox " +
                     "FROM dbo." + tabela + " WHERE id LIKE '" + prefixo + "-[0-9]%'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return String.format("%s-%03d", prefixo, rs.getInt("prox"));
        }
        return prefixo + "-001";
    }
}
