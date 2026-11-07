package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de clientes con JDBC sobre la tabla tblCustomers.
 *
 * <p>Para armar cada {@link Customer} con su {@link Country} y su {@link DocumentType}
 * (objetos, no códigos sueltos) las consultas hacen JOIN con tblCountries y tblDocumentsType.</p>
 *
 * <p>Ojo: en la base de datos la columna del nombre se llama FirtName (con error);
 * en Java el atributo se llama firstName.</p>
 */
public class JdbcCustomerRepository implements CustomerRepository {

    private static final String ENTITY = "cliente";

    // Consulta base: cliente + su tipo de documento + su país
    private static final String SELECT_BASE =
            "SELECT c.customerId, c.FirtName, c.Lastname, c.TelephoneNumber, c.Email, c.Address, "
            + "d.TypeId, d.Description, p.CountryId, p.Name "
            + "FROM tblCustomers c "
            + "JOIN tblDocumentsType d ON c.TypeId = d.TypeId "
            + "JOIN tblCountries p ON c.CountryId = p.CountryId ";

    private static final String SELECT_BY_ID = SELECT_BASE + "WHERE c.customerId = ?";
    private static final String SELECT_BY_EMAIL = SELECT_BASE + "WHERE LOWER(c.Email) = LOWER(?)";
    private static final String SELECT_BY_LAST_NAME = SELECT_BASE + "WHERE LOWER(c.Lastname) LIKE ?";
    private static final String SELECT_BY_COUNTRY = SELECT_BASE + "WHERE c.CountryId = ?";
    private static final String SELECT_BY_TYPE = SELECT_BASE + "WHERE c.TypeId = ?";

    private static final String INSERT =
            "INSERT INTO tblCustomers (customerId, FirtName, Lastname, TypeId, TelephoneNumber, "
            + "Email, CountryId, Address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE tblCustomers SET FirtName = ?, Lastname = ?, TypeId = ?, TelephoneNumber = ?, "
            + "Email = ?, CountryId = ?, Address = ? WHERE customerId = ?";
    private static final String DELETE = "DELETE FROM tblCustomers WHERE customerId = ?";
    private static final String COUNT = "SELECT COUNT(*) FROM tblCustomers";
    private static final String EXISTS_BY_COUNTRY = "SELECT 1 FROM tblCustomers WHERE CountryId = ?";
    private static final String EXISTS_BY_TYPE = "SELECT 1 FROM tblCustomers WHERE TypeId = ?";

    private final DatabaseConnection database;

    /**
     * Recibe la conexión por el constructor (inyección de dependencias).
     *
     * @param database fábrica de conexiones a SQL Server
     */
    public JdbcCustomerRepository(DatabaseConnection database) {
        this.database = database;
    }

    // ---------- Operaciones comunes del contrato ----------

    /** INSERT en tblCustomers (lo que hacía uspCrearClientes). Falla si el documento ya existe. */
    @Override
    public Customer create(Customer customer) {
        Validator.requireNotNull(customer, ENTITY);
        if (existsById(customer.getId())) {
            throw new DuplicateException(ENTITY, customer.getId());
        }
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(INSERT)) {
            sql.setString(1, customer.getId());
            sql.setString(2, customer.getFirstName());
            sql.setString(3, customer.getLastName());
            sql.setString(4, customer.getDocumentType().getId());
            sql.setString(5, customer.getTelephoneNumber());
            setOptionalText(sql, 6, customer.getEmail());
            sql.setInt(7, customer.getCountry().getId());
            setOptionalText(sql, 8, customer.getAddress());
            sql.executeUpdate();
            return customer;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** SELECT ... WHERE customerId = ? */
    @Override
    public Optional<Customer> findById(String id) {
        List<Customer> encontrados = queryList(SELECT_BY_ID, id);
        if (encontrados.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(encontrados.get(0));
    }

    /** SELECT de todos los clientes (el servicio se encarga de ordenarlos). */
    @Override
    public List<Customer> findAll() {
        return queryList(SELECT_BASE);
    }

    /** UPDATE tblCustomers SET ... (todo menos el documento). Falla si no existe. */
    @Override
    public Customer update(Customer customer) {
        Validator.requireNotNull(customer, ENTITY);
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(UPDATE)) {
            sql.setString(1, customer.getFirstName());
            sql.setString(2, customer.getLastName());
            sql.setString(3, customer.getDocumentType().getId());
            sql.setString(4, customer.getTelephoneNumber());
            setOptionalText(sql, 5, customer.getEmail());
            sql.setInt(6, customer.getCountry().getId());
            setOptionalText(sql, 7, customer.getAddress());
            sql.setString(8, customer.getId());
            if (sql.executeUpdate() == 0) {
                throw new NotFoundException(ENTITY, customer.getId());
            }
            return customer;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    /** DELETE ... WHERE customerId = ? */
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

    // ---------- Búsquedas propias del módulo ----------

    /** SELECT ... WHERE Email = ? (sin importar mayúsculas) */
    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        List<Customer> encontrados = queryList(SELECT_BY_EMAIL, email);
        if (encontrados.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(encontrados.get(0));
    }

    /** SELECT ... WHERE Lastname LIKE ? (coincidencia parcial, sin importar mayúsculas) */
    @Override
    public List<Customer> findByLastName(String text) {
        return queryList(SELECT_BY_LAST_NAME, "%" + text.toLowerCase() + "%");
    }

    /** SELECT ... WHERE CountryId = ? */
    @Override
    public List<Customer> findByCountry(Country country) {
        return queryList(SELECT_BY_COUNTRY, country.getId());
    }

    /** SELECT ... WHERE TypeId = ? */
    @Override
    public List<Customer> findByDocumentType(DocumentType documentType) {
        return queryList(SELECT_BY_TYPE, documentType.getId());
    }

    /** SELECT 1 ... WHERE CountryId = ? */
    @Override
    public boolean existsByCountry(Country country) {
        return queryExists(EXISTS_BY_COUNTRY, country.getId());
    }

    /** SELECT 1 ... WHERE TypeId = ? */
    @Override
    public boolean existsByDocumentType(DocumentType documentType) {
        return queryExists(EXISTS_BY_TYPE, documentType.getId());
    }

    // ---------- Métodos de apoyo (para no repetir código) ----------

    // Ejecuta un SELECT con 0 o 1 parámetro y convierte cada fila en un Customer
    private List<Customer> queryList(String query, Object... params) {
        List<Customer> clientes = new ArrayList<>();
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {
            setParams(sql, params);
            try (ResultSet row = sql.executeQuery()) {
                while (row.next()) {
                    clientes.add(toCustomer(row));
                }
            }
            return clientes;
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    // Ejecuta un SELECT 1 y responde si devolvió al menos una fila
    private boolean queryExists(String query, Object param) {
        try (Connection con = database.open();
             PreparedStatement sql = con.prepareStatement(query)) {
            setParams(sql, param);
            try (ResultSet row = sql.executeQuery()) {
                return row.next();
            }
        } catch (SQLException e) {
            throw DatabaseErrors.translate(e, ENTITY);
        }
    }

    // Llena los ? de la consulta en orden (setObject sirve para texto y números)
    private void setParams(PreparedStatement sql, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            sql.setObject(i + 1, params[i]);
        }
    }

    // Los datos opcionales (correo, dirección) se guardan como NULL si no existen
    private void setOptionalText(PreparedStatement sql, int position, String value) throws SQLException {
        if (value == null) {
            sql.setNull(position, Types.NVARCHAR);
        } else {
            sql.setString(position, value);
        }
    }

    // Convierte la fila actual en un Customer con su tipo de documento y su país
    private Customer toCustomer(ResultSet row) throws SQLException {
        DocumentType tipo = new DocumentType(row.getString("TypeId"), row.getString("Description"));
        Country pais = new Country(row.getInt("CountryId"), row.getString("Name"));
        Customer cliente = new Customer(row.getString("customerId"), row.getString("FirtName"),
                row.getString("Lastname"), tipo, row.getString("TelephoneNumber"), pais);
        cliente.setEmail(row.getString("Email"));
        cliente.setAddress(row.getString("Address"));
        return cliente;
    }
}
