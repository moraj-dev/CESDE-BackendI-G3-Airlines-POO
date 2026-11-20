package com.airlinespoo.airlines.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=airlines_db;encrypt=true;trustServerCertificate=true;loginTimeout=5";

    // Prueba con tu usuario habitual o 'sa' y tu contraseña
    private static final String USER = "anavelasquez12";
    private static final String PASSWORD = "Junior1025654049._";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
