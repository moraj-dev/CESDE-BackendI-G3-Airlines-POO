package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.customers.model.Country;

/**
 * Contrato del repositorio de países.
 * El servicio depende de esta interfaz, no de una clase concreta. Así se puede usar
 * la versión en memoria ({@link InMemoryCountryRepository}) o la de base de datos
 * ({@link JdbcCountryRepository}) sin cambiar el servicio.
 */
public interface CountryRepository extends Repository<Country, Integer> {
    // Solo usa las operaciones comunes del kit base (create, findById, findAll, ...)
}
