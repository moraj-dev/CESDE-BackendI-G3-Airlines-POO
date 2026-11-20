package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;

import java.sql.SQLException;

/**
 * Convierte los errores de SQL Server ({@link SQLException}) en las excepciones
 * del proyecto, para que el menú muestre un mensaje claro en español.
 */
public final class DatabaseErrors {

    // Códigos de error de SQL Server que nos interesan
    private static final int UNIQUE_VIOLATION = 2627;       // PRIMARY KEY o UNIQUE repetido
    private static final int UNIQUE_INDEX_VIOLATION = 2601; // índice único repetido
    private static final int FOREIGN_KEY_VIOLATION = 547;   // clave foránea en uso

    private DatabaseErrors() {
        // Solo tiene un método estático: no se instancia.
    }

    /**
     * Traduce un error de la base de datos a una excepción del proyecto.
     *
     * @param e          error que lanzó JDBC
     * @param entityName nombre de la entidad en español (por ejemplo, "cliente")
     * @return la excepción del proyecto que corresponde; quien llama la lanza con throw
     */
    public static AirlinesException translate(SQLException e, String entityName) {
        int code = e.getErrorCode();
        if (code == UNIQUE_VIOLATION || code == UNIQUE_INDEX_VIOLATION) {
            return new DuplicateException("La base de datos rechazó un valor repetido en "
                    + entityName + ". Revisa el id o el correo.");
        }
        if (code == FOREIGN_KEY_VIOLATION) {
            return new BusinessRuleException("No se puede completar la operación: el registro de "
                    + entityName + " está relacionado con otros datos.");
        }
        return new AirlinesException("Error de base de datos (" + entityName + "): "
                + e.getMessage(), e);
    }
}
