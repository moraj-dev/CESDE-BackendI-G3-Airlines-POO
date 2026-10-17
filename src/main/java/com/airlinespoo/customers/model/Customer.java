package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

/**
 * Representa un cliente de la aerolínea. Viene de la tabla tblCustomers.
 */
public class Customer implements Identifiable<String> {

    // Máximos de caracteres de cada campo
    public static final int MAX_ID_LENGTH = 20;
    public static final int MAX_NAME_LENGTH = 30;
    public static final int MAX_PHONE_LENGTH = 15;
    public static final int MAX_EMAIL_LENGTH = 80;
    public static final int MAX_ADDRESS_LENGTH = 40;

    // Teléfono: solo dígitos, con un + opcional al inicio
    private static final String PHONE_FORMAT = "\\+?\\d+";

    // Correo: algo@algo.algo (formato básico)
    private static final String EMAIL_FORMAT = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";

    // Atributos privados
    private final String id;          // es el número de documento, no cambia
    private String firstName;
    private String lastName;
    private DocumentType documentType; // relación con otro objeto, no un código suelto
    private String telephoneNumber;
    private String email;              // opcional
    private Country country;           // relación con otro objeto
    private String address;            // opcional

    /**
     * Constructor con los datos obligatorios.
     * El correo y la dirección son opcionales y se asignan con sus setters.
     *
     * @param id              número de documento, máximo 20 caracteres
     * @param firstName       nombre, máximo 30 caracteres
     * @param lastName        apellido, máximo 30 caracteres
     * @param documentType    tipo de documento del cliente
     * @param telephoneNumber teléfono, solo dígitos y máximo 15
     * @param country         país del cliente
     */
    public Customer(String id, String firstName, String lastName, DocumentType documentType,
                    String telephoneNumber, Country country) {
        this.id = Validator.requireText(id, "documento del cliente", MAX_ID_LENGTH);
        setFirstName(firstName);
        setLastName(lastName);
        setDocumentType(documentType);
        setTelephoneNumber(telephoneNumber);
        setCountry(country);
    }

    // ---------- Getters ----------

    @Override
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Country getCountry() {
        return country;
    }

    public String getAddress() {
        return address;
    }

    /**
     * Devuelve el nombre y el apellido juntos.
     *
     * @return nombre completo del cliente
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ---------- Setters (todos validan) ----------

    public void setFirstName(String firstName) {
        this.firstName = Validator.requireText(firstName, "nombre del cliente", MAX_NAME_LENGTH);
    }

    public void setLastName(String lastName) {
        this.lastName = Validator.requireText(lastName, "apellido del cliente", MAX_NAME_LENGTH);
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = Validator.requireNotNull(documentType, "tipo de documento");
    }

    public void setCountry(Country country) {
        this.country = Validator.requireNotNull(country, "país del cliente");
    }

    /**
     * Cambia el teléfono. Es obligatorio, máximo 15 caracteres y solo dígitos
     * (puede empezar con +).
     *
     * @param telephoneNumber nuevo teléfono
     */
    public void setTelephoneNumber(String telephoneNumber) {
        String telefono = Validator.requireText(telephoneNumber, "teléfono del cliente", MAX_PHONE_LENGTH);
        if (!telefono.matches(PHONE_FORMAT)) {
            throw new ValidationException("El teléfono solo puede tener dígitos (y un + al inicio).");
        }
        this.telephoneNumber = telefono;
    }

    /**
     * Cambia el correo. Es opcional: si viene vacío se guarda como null.
     * Si viene con datos, debe tener formato válido y máximo 80 caracteres.
     *
     * @param email nuevo correo, puede ser null o vacío
     */
    public void setEmail(String email) {
        String correo = emptyToNull(email);
        if (correo != null) {
            Validator.requireMaxLength(correo, MAX_EMAIL_LENGTH, "correo del cliente");
            if (!correo.matches(EMAIL_FORMAT)) {
                throw new ValidationException("El correo no tiene un formato válido.");
            }
            correo = correo.toLowerCase(); // para que no se repitan correos por mayúsculas
        }
        this.email = correo;
    }

    /**
     * Cambia la dirección. Es opcional: si viene vacía se guarda como null.
     *
     * @param address nueva dirección, puede ser null o vacía
     */
    public void setAddress(String address) {
        String direccion = emptyToNull(address);
        Validator.requireMaxLength(direccion, MAX_ADDRESS_LENGTH, "dirección del cliente");
        this.address = direccion;
    }

    // Quita espacios sobrantes; si el texto queda vacío, devuelve null
    private String emptyToNull(String texto) {
        String limpio = Validator.normalize(texto);
        if (limpio == null || limpio.isEmpty()) {
            return null;
        }
        return limpio;
    }

    // ---------- equals, hashCode y toString ----------

    // Dos clientes son iguales si tienen el mismo documento
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Customer otro = (Customer) obj;
        return this.id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    // No muestra teléfono, correo ni dirección (datos sensibles)
    @Override
    public String toString() {
        return documentType.getId() + " " + id + " - " + getFullName() + " (" + country.getName() + ")";
    }
}
