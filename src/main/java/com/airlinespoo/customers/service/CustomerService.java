package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio del módulo de clientes: países, tipos de documento y clientes.
 * No imprime nada ni lee del teclado; si algo falla, lanza una excepción.
 */
public class CustomerService {

    private final CountryRepository countryRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final CustomerRepository customerRepository;

    /**
     * Recibe los repositorios por el constructor (inyección de dependencias manual).
     */
    public CustomerService(CountryRepository countryRepository,
                           DocumentTypeRepository documentTypeRepository,
                           CustomerRepository customerRepository) {
        this.countryRepository = countryRepository;
        this.documentTypeRepository = documentTypeRepository;
        this.customerRepository = customerRepository;
    }

    // =====================================================
    //                       PAÍSES
    // =====================================================

    /** Registra un país nuevo. El id no puede repetirse. */
    public Country createCountry(int id, String name) {
        Country pais = new Country(id, name); // el constructor valida los datos
        if (countryRepository.existsById(pais.getId())) {
            throw new DuplicateException("país", id);
        }
        return countryRepository.create(pais);
    }

    /** Busca un país por id. Si no existe, lanza "no encontrado". */
    public Country getCountry(int id) {
        Optional<Country> pais = countryRepository.findById(id);
        if (pais.isEmpty()) {
            throw new NotFoundException("país", id);
        }
        return pais.get();
    }

    /** Lista todos los países ordenados por nombre. */
    public List<Country> listCountries() {
        List<Country> paises = countryRepository.findAll();
        paises.sort(Comparator.comparing(Country::getName));
        return paises;
    }

    /** Cambia el nombre de un país existente. */
    public Country updateCountry(int id, String name) {
        getCountry(id); // si no existe, lanza la excepción
        Country actualizado = new Country(id, name);
        return countryRepository.update(actualizado);
    }

    /** Elimina un país, solo si ningún cliente pertenece a él. */
    public void deleteCountry(int id) {
        Country pais = getCountry(id);
        if (customerRepository.existsByCountry(pais)) {
            throw new BusinessRuleException("No se puede eliminar el país porque tiene clientes.");
        }
        countryRepository.deleteById(id);
    }

    // =====================================================
    //                  TIPOS DE DOCUMENTO
    // =====================================================

    /** Registra un tipo de documento nuevo. El código no puede repetirse. */
    public DocumentType createDocumentType(String id, String description) {
        DocumentType tipo = new DocumentType(id, description); // aquí "cc" pasa a "CC"
        if (documentTypeRepository.existsById(tipo.getId())) {
            throw new DuplicateException("tipo de documento", tipo.getId());
        }
        return documentTypeRepository.create(tipo);
    }

    /** Busca un tipo de documento por código. Si no existe, lanza "no encontrado". */
    public DocumentType getDocumentType(String id) {
        String codigo = normalizeDocumentTypeId(id);
        Optional<DocumentType> tipo = documentTypeRepository.findById(codigo);
        if (tipo.isEmpty()) {
            throw new NotFoundException("tipo de documento", codigo);
        }
        return tipo.get();
    }

    /** Lista todos los tipos de documento ordenados por código. */
    public List<DocumentType> listDocumentTypes() {
        List<DocumentType> tipos = documentTypeRepository.findAll();
        tipos.sort(Comparator.comparing(DocumentType::getId));
        return tipos;
    }

    /** Cambia la descripción de un tipo de documento existente. */
    public DocumentType updateDocumentType(String id, String description) {
        DocumentType existente = getDocumentType(id);
        DocumentType actualizado = new DocumentType(existente.getId(), description);
        return documentTypeRepository.update(actualizado);
    }

    /** Elimina un tipo de documento, solo si ningún cliente lo usa. */
    public void deleteDocumentType(String id) {
        DocumentType tipo = getDocumentType(id);
        if (customerRepository.existsByDocumentType(tipo)) {
            throw new BusinessRuleException("No se puede eliminar el tipo de documento porque hay clientes que lo usan.");
        }
        documentTypeRepository.deleteById(tipo.getId());
    }

    // Quita espacios y pasa a mayúsculas, igual que hace la clase DocumentType
    private String normalizeDocumentTypeId(String id) {
        return Validator.requireText(id, "código del tipo de documento").toUpperCase();
    }

    // =====================================================
    //                       CLIENTES
    // =====================================================

    /**
     * Registra un cliente (lo que hacía el procedimiento uspCrearClientes).
     * El país y el tipo de documento deben existir; el documento y el correo no pueden repetirse.
     */
    public Customer createCustomer(String id, String firstName, String lastName, String documentTypeId,
                                   String telephoneNumber, String email, int countryId, String address) {
        // 1. Las referencias deben existir
        DocumentType tipo = getDocumentType(documentTypeId);
        Country pais = getCountry(countryId);

        // 2. Se arma el cliente (aquí se validan todos sus datos)
        Customer cliente = new Customer(id, firstName, lastName, tipo, telephoneNumber, pais);
        cliente.setEmail(email);
        cliente.setAddress(address);

        // 3. Reglas de unicidad
        if (customerRepository.existsById(cliente.getId())) {
            throw new DuplicateException("cliente", cliente.getId());
        }
        validateEmailNotUsed(cliente.getEmail(), cliente.getId());

        // 4. Se guarda
        return customerRepository.create(cliente);
    }

    /** Busca un cliente por documento. Si no existe, lanza "no encontrado". */
    public Customer getCustomer(String id) {
        Optional<Customer> cliente = customerRepository.findById(Validator.normalize(id));
        if (cliente.isEmpty()) {
            throw new NotFoundException("cliente", id);
        }
        return cliente.get();
    }

    /** Busca un cliente por correo. Devuelve vacío si no existe. */
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(Validator.normalize(email));
    }

    /** Lista todos los clientes ordenados por apellido y luego por nombre. */
    public List<Customer> listCustomers() {
        List<Customer> clientes = customerRepository.findAll();
        sortByName(clientes);
        return clientes;
    }

    /**
     * Actualiza todos los datos del cliente menos el documento.
     * El correo nuevo no puede pertenecer a otro cliente.
     */
    public Customer updateCustomer(String id, String firstName, String lastName, String documentTypeId,
                                   String telephoneNumber, String email, int countryId, String address) {
        Customer existente = getCustomer(id);
        DocumentType tipo = getDocumentType(documentTypeId);
        Country pais = getCountry(countryId);

        // Se arma un cliente nuevo con los datos actualizados.
        // Así, si un dato es inválido, el cliente guardado no queda modificado a medias.
        Customer actualizado = new Customer(existente.getId(), firstName, lastName, tipo, telephoneNumber, pais);
        actualizado.setEmail(email);
        actualizado.setAddress(address);

        validateEmailNotUsed(actualizado.getEmail(), actualizado.getId());
        return customerRepository.update(actualizado);
    }

    /** Elimina un cliente por documento. */
    public void deleteCustomer(String id) {
        Customer cliente = getCustomer(id);
        customerRepository.deleteById(cliente.getId());
    }

    /** Busca clientes por parte del apellido, sin importar mayúsculas. */
    public List<Customer> searchCustomersByLastName(String text) {
        String buscado = Validator.requireText(text, "apellido a buscar");
        List<Customer> clientes = customerRepository.findByLastName(buscado);
        sortByName(clientes);
        return clientes;
    }

    /** Lista los clientes de un país. El país debe existir. */
    public List<Customer> listCustomersByCountry(int countryId) {
        Country pais = getCountry(countryId);
        List<Customer> clientes = customerRepository.findByCountry(pais);
        sortByName(clientes);
        return clientes;
    }

    /** Lista los clientes que usan un tipo de documento. El tipo debe existir. */
    public List<Customer> listCustomersByDocumentType(String documentTypeId) {
        DocumentType tipo = getDocumentType(documentTypeId);
        List<Customer> clientes = customerRepository.findByDocumentType(tipo);
        sortByName(clientes);
        return clientes;
    }

    /** Devuelve cuántos clientes hay registrados. */
    public long countCustomers() {
        return customerRepository.count();
    }

    // El correo no puede pertenecer a OTRO cliente (al mismo cliente sí, para poder dejarlo igual)
    private void validateEmailNotUsed(String email, String customerId) {
        if (email == null) {
            return; // el correo es opcional
        }
        Optional<Customer> dueño = customerRepository.findByEmail(email);
        if (dueño.isPresent() && !dueño.get().getId().equals(customerId)) {
            throw new DuplicateException("El correo '" + email + "' ya pertenece a otro cliente.");
        }
    }

    // Ordena por apellido y, si se repite, por nombre
    private void sortByName(List<Customer> clientes) {
        clientes.sort(Comparator.comparing(Customer::getLastName)
            .thenComparing(Customer::getFirstName));
    }
}
