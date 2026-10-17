package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda los clientes en memoria. Hereda el CRUD del kit base y agrega
 * búsquedas por correo, apellido, país y tipo de documento.
 */
public class CustomerRepository extends InMemoryRepository<Customer, String> {

    // Nombre que se usa en los mensajes de error
    @Override
    protected String entityName() {
        return "cliente";
    }

    /**
     * Busca un cliente por su correo, sin importar mayúsculas.
     * Equivale a: SELECT ... WHERE Email = ?
     *
     * @param email correo buscado
     * @return el cliente, o vacío si nadie tiene ese correo
     */
    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        for (Customer cliente : values()) {
            if (cliente.getEmail() != null && cliente.getEmail().equalsIgnoreCase(email)) {
                return Optional.of(cliente);
            }
        }
        return Optional.empty();
    }

    /**
     * Busca clientes cuyo apellido contenga el texto, sin importar mayúsculas.
     * Equivale a: SELECT ... WHERE Lastname LIKE ?
     *
     * @param text parte del apellido
     * @return lista de clientes que coinciden
     */
    public List<Customer> findByLastName(String text) {
        List<Customer> resultado = new ArrayList<>();
        String buscado = text.toLowerCase();
        for (Customer cliente : values()) {
            if (cliente.getLastName().toLowerCase().contains(buscado)) {
                resultado.add(cliente);
            }
        }
        return resultado;
    }

    /**
     * Lista los clientes de un país.
     * Equivale a: SELECT ... WHERE CountryId = ?
     *
     * @param country país buscado
     * @return lista de clientes de ese país
     */
    public List<Customer> findByCountry(Country country) {
        List<Customer> resultado = new ArrayList<>();
        for (Customer cliente : values()) {
            if (cliente.getCountry().equals(country)) {
                resultado.add(cliente);
            }
        }
        return resultado;
    }

    /**
     * Lista los clientes que usan un tipo de documento.
     * Equivale a: SELECT ... WHERE TypeId = ?
     *
     * @param documentType tipo de documento buscado
     * @return lista de clientes con ese tipo
     */
    public List<Customer> findByDocumentType(DocumentType documentType) {
        List<Customer> resultado = new ArrayList<>();
        for (Customer cliente : values()) {
            if (cliente.getDocumentType().equals(documentType)) {
                resultado.add(cliente);
            }
        }
        return resultado;
    }

    /**
     * Indica si algún cliente pertenece a ese país.
     * Sirve para no dejar eliminar un país que está en uso.
     *
     * @param country país a revisar
     * @return true si al menos un cliente es de ese país
     */
    public boolean existsByCountry(Country country) {
        return !findByCountry(country).isEmpty();
    }

    /**
     * Indica si algún cliente usa ese tipo de documento.
     * Sirve para no dejar eliminar un tipo de documento que está en uso.
     *
     * @param documentType tipo de documento a revisar
     * @return true si al menos un cliente lo usa
     */
    public boolean existsByDocumentType(DocumentType documentType) {
        return !findByDocumentType(documentType).isEmpty();
    }
}
