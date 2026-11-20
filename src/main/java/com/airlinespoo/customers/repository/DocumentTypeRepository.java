package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.customers.model.DocumentType;

/**
 * Contrato del repositorio de tipos de documento.
 * El servicio depende de esta interfaz, no de una clase concreta. Así se puede usar
 * la versión en memoria ({@link InMemoryDocumentTypeRepository}) o la de base de datos
 * ({@link JdbcDocumentTypeRepository}) sin cambiar el servicio.
 */
public interface DocumentTypeRepository extends Repository<DocumentType, String> {
    // Solo usa las operaciones comunes del kit base (create, findById, findAll, ...)
}
