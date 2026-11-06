package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.DocumentType;

/**
 * Guarda los tipos de documento en memoria. Hereda todo el CRUD del kit base.
 */
public class DocumentTypeRepository extends InMemoryRepository<DocumentType, String> {

    // Nombre que se usa en los mensajes de error
    @Override
    protected String entityName() {
        return "tipo de documento";
    }
}
