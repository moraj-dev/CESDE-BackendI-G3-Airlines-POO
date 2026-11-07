package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de países con JDBC sobre la tabla tblCountries.
 * Cumple el mismo contrato que la versión en memoria.
 */
public class JdbcCountryRepository implements CountryRepository {

    private static final String ENTITY = "país";

    // Sentencias SQL (los ? se llenan con PreparedStatement para evitar inyección SQL)
    private static final String INSERT = "INSERT INTO tblCountries (CountryId, Name) VALUES (?, ?)";
    private static final String SELECT_BY_ID = "SELECT CountryId, Name FROM tblCountries WHERE CountryId = ?";
    private static final String SELECT_ALL = "SELECT CountryId, Name FROM tblCountries ORDER BY Name";
    private static final String UPDATE = "UPDATE tblCountries SET Name = ? WHERE CountryId = ?";
    private static final String DELETE = "DELETE FROM tblCountries WHERE CountryId = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM tblCountries";

    private final DatabaseConnection database;

    /**
     * Recibe la conexión por el constructor (inyección de dependencias).
     *
     * @param database fábrica de conexiones a SQL Server
     */
    public JdbcCountryRepository(DatabaseConnection database) {
        this.database = database;
    }

    /** INSERT. Falla si el id ya existe. */
    @Override
    public Country create(Country country) {
        Validator.requireNotNull(country, ENTITY);
        if (existsById(country.getId())) {
            throw new DuplicateException(ENTITY, country.getId());
        }
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(INSERT)) {
            sql.setInt(1, country.getId());
            sql.setString(2, country.getName());
            sql.executeUpdate();
            return country;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** SELECT ... WHERE CountryId = ? */
    @Override
    public Optional<Country> findById(Integer id) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(SELECT_BY_ID)) {
            sql.setInt(1, id);
            try (ResultSet row = sql.executeQuery()) {
                if (row.next()) {
                    return Optional.of(toCountry(row));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** SELECT de todos los países, ordenados por nombre. */
    @Override
    public List<Country> findAll() {
        List<Country> paises = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(SELECT_ALL);
             ResultSet row = sql.executeQuery()) {
            while (row.next()) {
                paises.add(toCountry(row));
            }
            return paises;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** UPDATE ... SET Name. Falla si el país no existe. */
    @Override
    public Country update(Country country) {
        Validator.requireNotNull(country, ENTITY);
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(UPDATE)) {
            sql.setString(1, country.getName());
            sql.setInt(2, country.getId());
            if (sql.executeUpdate() == 0) {
                throw new NotFoundException(ENTITY, country.getId());
            }
            return country;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** DELETE ... WHERE CountryId = ? */
    @Override
    public boolean deleteById(Integer id) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(DELETE)) {
            sql.setInt(1, id);
            return sql.executeUpdate() > 0;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** Usa findById: si encuentra algo, existe. */
    @Override
    public boolean existsById(Integer id) {
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

    // Convierte la fila actual del resultado en un objeto Country
    private Country toCountry(ResultSet row) throws SQLException {
        return new Country(row.getInt("CountryId"), row.getString("Name"));
    }
}
