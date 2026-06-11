package com.petroleiros.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
        "jdbc:sqlserver://localhost\\SQLEXPRESS;databaseName=final_DIAS_TBD;" +
        "user=petroleiros;password=Petroleiros123!;" +
        "trustServerCertificate=true;encrypt=false;" +
        "loginTimeout=6";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL);
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}
