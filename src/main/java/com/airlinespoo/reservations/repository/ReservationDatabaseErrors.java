
package com.airlinespoo.reservations.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;

import java.sql.SQLException;

/**
 * Traduce los errores de SQL Server a excepciones propias del módulo
 * de reservas.
 *
 * <p>Permite mantener el manejo de errores de persistencia separado
 * de la lógica de negocio del sistema.</p>
 *
 * <p>Se identifican principalmente las violaciones de restricciones
 * de unicidad y de claves foráneas.</p>
 *
 * @author Equipo Airlines
 * @version 1.0
 */
public final class ReservationDatabaseErrors {

    /** Código SQL Server para violaciones de clave primaria o restricción única. */
    private static final int UNIQUE_VIOLATION = 2627;

    /** Código SQL Server para violaciones de índices únicos. */
    private static final int UNIQUE_INDEX_VIOLATION = 2601;

    /** Código SQL Server para violaciones de claves foráneas. */
    private static final int FOREIGN_KEY_VIOLATION = 547;

    /**
     * Constructor privado para impedir la creación de instancias.
     */
    private ReservationDatabaseErrors() {
    }

    /**
     * Convierte una excepción SQL en una excepción propia del sistema.
     *
     * @param exception excepción original generada por SQL Server
     * @param entityName nombre de la entidad relacionada con la operación
     * @return excepción del sistema correspondiente al error detectado
     */
    public static AirlinesException translate(SQLException exception, String entityName) {

        int errorCode = exception.getErrorCode();

        if (errorCode == UNIQUE_VIOLATION || errorCode == UNIQUE_INDEX_VIOLATION) {
            return new DuplicateException(
                "Ya existe un registro con los mismos datos para " + entityName + "."
            );
        }

        if (errorCode == FOREIGN_KEY_VIOLATION) {
            return new BusinessRuleException(
                "No se puede realizar la operación porque existe una referencia relacionada con "
                    + entityName + "."
            );
        }

        return new AirlinesException(
            "Error de base de datos (" + entityName + "): " + exception.getMessage(),
            exception
        );
    }
}
