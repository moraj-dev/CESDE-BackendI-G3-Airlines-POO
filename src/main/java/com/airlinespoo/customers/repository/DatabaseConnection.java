package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.AirlinesException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones con SQL Server usando JDBC.
 *
 * <p>Los datos de conexión (url, usuario y contraseña) NO están escritos en el código:
 * se leen del archivo {@code db.properties} que va en {@code src/main/resources}.
 * Ese archivo no se sube a GitHub porque tiene la contraseña; en el repositorio
 * solo está la plantilla {@code db.properties.example}.</p>
 */
public class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";

    private final String url;
    private final String user;
    private final String password;

    /**
     * Lee el archivo db.properties y guarda los datos de conexión.
     *
     * @throws AirlinesException si el archivo no existe o le falta algún dato
     */
    public DatabaseConnection() {
        Properties config = loadConfig();
        this.url = requireProperty(config, "db.url");
        this.user = requireProperty(config, "db.user");
        this.password = requireProperty(config, "db.password");
    }

    /**
     * Abre una conexión nueva. Quien la pide debe cerrarla (con try-with-resources).
     *
     * @return una conexión abierta con la base de datos
     * @throws SQLException si no se puede conectar
     */
    public Connection open() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    // Busca db.properties dentro de src/main/resources y lo carga
    private Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream file = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
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

    // Devuelve un dato del archivo; si falta, avisa cuál
    private String requireProperty(Properties config, String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new AirlinesException("Falta el dato '" + key + "' en " + CONFIG_FILE + ".");
        }
        return value.trim();
    }
}
