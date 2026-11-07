package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.util.List;
import java.util.Optional;

/**
 * Contrato del repositorio de clientes: las operaciones comunes del kit base
 * más las búsquedas propias de este módulo.
 * Lo cumplen {@link InMemoryCustomerRepository} (memoria) y
 * {@link JdbcCustomerRepository} (SQL Server).
 */
public interface CustomerRepository extends Repository<Customer, String> {

    /**
     * Busca un cliente por su correo, sin importar mayúsculas.
     * Equivale a: SELECT ... WHERE Email = ?
     *
     * @param email correo buscado
     * @return el cliente, o vacío si nadie tiene ese correo
     */
    Optional<Customer> findByEmail(String email);

    /**
     * Busca clientes cuyo apellido contenga el texto, sin importar mayúsculas.
     * Equivale a: SELECT ... WHERE Lastname LIKE ?
     *
     * @param text parte del apellido
     * @return lista de clientes que coinciden
     */
    List<Customer> findByLastName(String text);

    /**
     * Lista los clientes de un país.
     * Equivale a: SELECT ... WHERE CountryId = ?
     *
     * @param country país buscado
     * @return lista de clientes de ese país
     */
    List<Customer> findByCountry(Country country);

    /**
     * Lista los clientes que usan un tipo de documento.
     * Equivale a: SELECT ... WHERE TypeId = ?
     *
     * @param documentType tipo de documento buscado
     * @return lista de clientes con ese tipo
     */
    List<Customer> findByDocumentType(DocumentType documentType);

    /**
     * Indica si algún cliente pertenece a ese país.
     * Equivale a: SELECT 1 ... WHERE CountryId = ?
     *
     * @param country país a revisar
     * @return true si al menos un cliente es de ese país
     */
    boolean existsByCountry(Country country);

    /**
     * Indica si algún cliente usa ese tipo de documento.
     * Equivale a: SELECT 1 ... WHERE TypeId = ?
     *
     * @param documentType tipo de documento a revisar
     * @return true si al menos un cliente lo usa
     */
    boolean existsByDocumentType(DocumentType documentType);
}
