package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.DocumentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de tipos de documento con JDBC sobre la tabla tblDocumentsType.
 * Cumple el mismo contrato que la versión en memoria.
 */
public class JdbcDocumentTypeRepository implements DocumentTypeRepository {

    private static final String ENTITY = "tipo de documento";

    // Sentencias SQL (los ? se llenan con PreparedStatement para evitar inyección SQL)
    private static final String INSERT = "INSERT INTO tblDocumentsType (TypeId, Description) VALUES (?, ?)";
    private static final String SELECT_BY_ID = "SELECT TypeId, Description FROM tblDocumentsType WHERE TypeId = ?";
    private static final String SELECT_ALL = "SELECT TypeId, Description FROM tblDocumentsType ORDER BY TypeId";
    private static final String UPDATE = "UPDATE tblDocumentsType SET Description = ? WHERE TypeId = ?";
    private static final String DELETE = "DELETE FROM tblDocumentsType WHERE TypeId = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM tblDocumentsType";

    private final DatabaseConnection database;

    /**
     * Recibe la conexión por el constructor (inyección de dependencias).
     *
     * @param database fábrica de conexiones a SQL Server
     */
    public JdbcDocumentTypeRepository(DatabaseConnection database) {
        this.database = database;
    }

    /** INSERT. Falla si el id ya existe. */
    @Override
    public DocumentType create(DocumentType type) {
        Validator.requireNotNull(type, ENTITY);
        if (existsById(type.getId())) {
            throw new DuplicateException(ENTITY, type.getId());
        }
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(INSERT)) {
            sql.setString(1, type.getId());
            sql.setString(2, type.getDescription());
            sql.executeUpdate();
            return type;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** SELECT ... WHERE TypeId = ? */
    @Override
    public Optional<DocumentType> findById(String id) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(SELECT_BY_ID)) {
            sql.setString(1, id);
            try (ResultSet row = sql.executeQuery()) {
                if (row.next()) {
                    return Optional.of(toDocumentType(row));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** SELECT de todos los tipos de documento, ordenados por código. */
    @Override
    public List<DocumentType> findAll() {
        List<DocumentType> tipos = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(SELECT_ALL);
             ResultSet row = sql.executeQuery()) {
            while (row.next()) {
                tipos.add(toDocumentType(row));
            }
            return tipos;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** UPDATE ... SET Description. Falla si el tipo no existe. */
    @Override
    public DocumentType update(DocumentType type) {
        Validator.requireNotNull(type, ENTITY);
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(UPDATE)) {
            sql.setString(1, type.getDescription());
            sql.setString(2, type.getId());
            if (sql.executeUpdate() == 0) {
                throw new NotFoundException(ENTITY, type.getId());
            }
            return type;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** DELETE ... WHERE TypeId = ? */
    @Override
    public boolean deleteById(String id) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(DELETE)) {
            sql.setString(1, id);
            return sql.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** Usa findById: si encuentra algo, existe. */
    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    /** SELECT COUNT(*) */
    @Override
    public long count() {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(COUNT);
             ResultSet row = sql.executeQuery()) {
            row.next();
            return row.getLong(1);
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    // Convierte la fila actual del resultado en un objeto DocumentType
    private DocumentType toDocumentType(ResultSet row) throws SQLException {
        return new DocumentType(row.getString("TypeId"), row.getString("Description"));
    }
}
