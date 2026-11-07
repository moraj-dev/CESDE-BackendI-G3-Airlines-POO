package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.Country;

/**
 * Implementación EN MEMORIA del repositorio de países. Guarda los países en un mapa.
 * Hereda todo el CRUD del kit base.
 */
public class InMemoryCountryRepository extends InMemoryRepository<Country, Integer>
        implements CountryRepository {

    // Nombre que se usa en los mensajes de error
    @Override
    protected String entityName() {
        return "país";
    }
}
