package com.petroleiros.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface DAO<T> {
    Optional<T> findById(String id) throws SQLException;
    List<T> findAll() throws SQLException;
    boolean save(T entity) throws SQLException;
    boolean update(T entity) throws SQLException;
    boolean delete(String id) throws SQLException;
}
