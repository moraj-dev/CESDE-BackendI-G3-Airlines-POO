package com.airlinespoo.airlines.repository;

import com.airlinespoo.core.exception.AirlinesException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Properties config = loadConfig();
        String url = requireProperty(config, "db.url");
        String user = requireProperty(config, "db.user");
        String password = requireProperty(config, "db.password");
        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream file = DatabaseConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (file == null) {
                throw new AirlinesException("No se encontró el archivo " + CONFIG_FILE
                    + " en src/main/resources. Cópialo desde db.properties.example.");
            }
            config.load(file);
        } catch (IOException e) {
            throw new AirlinesException("No se pudo leer el archivo " + CONFIG_FILE + ".", e);
        }
        return config;
    }

    private static String requireProperty(Properties config, String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new AirlinesException("Falta el dato '" + key + "' en " + CONFIG_FILE + ".");
        }
        return value.trim();
    }
}
