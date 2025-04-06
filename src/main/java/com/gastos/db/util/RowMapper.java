package com.gastos.db.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface funcional para mapear linhas de ResultSet para objetos.
 * 
 * @param <T> Tipo do objeto que será criado a partir do ResultSet
 */
@FunctionalInterface
public interface RowMapper<T> {
    /**
     * Mapeia uma linha do ResultSet para um objeto.
     * 
     * @param rs O ResultSet posicionado na linha a ser mapeada
     * @return O objeto mapeado
     * @throws SQLException em caso de erro no acesso ao ResultSet
     */
    T mapRow(ResultSet rs) throws SQLException;
}