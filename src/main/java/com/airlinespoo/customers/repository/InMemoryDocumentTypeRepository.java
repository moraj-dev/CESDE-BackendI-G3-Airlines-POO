package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.DocumentType;

/**
 * Implementación EN MEMORIA del repositorio de tipos de documento. Guarda los tipos de documento en un mapa.
 * Hereda todo el CRUD del kit base.
 */
public class InMemoryDocumentTypeRepository extends InMemoryRepository<DocumentType, String>
        implements DocumentTypeRepository {

    // Nombre que se usa en los mensajes de error
    @Override
    protected String entityName() {
        return "tipo de documento";
    }
}
