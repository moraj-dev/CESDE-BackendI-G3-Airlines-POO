package com.airlinespoo.reservations.repository;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contrato para obtener conexiones con la base de datos.
 *
 * <p>Permite que el repositorio JDBC reciba la conexión mediante
 * inyección de dependencias, sin depender de una implementación
 * específica de DatabaseConnection.</p>
 *
 * @author Equipo Airlines
 * @version 1.0
 */
@FunctionalInterface
public interface SqlConnectionProvider {

    /**
     * Abre una conexión con la base de datos.
     *
     * @return conexión JDBC disponible
     * @throws SQLException si ocurre un error al establecer la conexión
     */
    Connection open() throws SQLException;
}
