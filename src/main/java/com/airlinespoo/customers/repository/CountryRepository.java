package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.Country;

/**
 * Guarda los países en memoria. Hereda todo el CRUD del kit base.
 */
public class CountryRepository extends InMemoryRepository<Country, Integer> {

    // Nombre que se usa en los mensajes de error
    @Override
    protected String entityName() {
        return "país";
    }
}
