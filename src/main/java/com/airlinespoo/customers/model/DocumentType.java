package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

/**
 * Representa un tipo de documento (CC, TI, PA...). Viene de la tabla tblDocumentsType.
 */
public class DocumentType implements Identifiable<String> {

    // El código del tipo de documento siempre tiene 2 caracteres
    public static final int ID_LENGTH = 2;

    // Máximo de caracteres de la descripción
    public static final int MAX_DESCRIPTION_LENGTH = 20;

    // Atributos privados
    private final String id;   // final: el id no se puede cambiar
    private String description;

    /**
     * Constructor: crea un tipo de documento y valida sus datos.
     *
     * @param id          código de 2 caracteres, por ejemplo "CC"
     * @param description descripción, obligatoria y máximo 20 caracteres
     */
    public DocumentType(String id, String description) {
        // Primero se valida que el código no esté vacío (también le quita los espacios)
        String codigo = Validator.requireText(id, "código del tipo de documento");

        // Se pasa a mayúsculas para que "cc" y "CC" sean lo mismo
        codigo = codigo.toUpperCase();

        // El código debe tener exactamente 2 caracteres
        if (codigo.length() != ID_LENGTH) {
            throw new ValidationException("El tipo de documento debe tener exactamente 2 caracteres.");
        }
        this.id = codigo;

        // La descripción no puede estar vacía ni pasar de 20 caracteres
        this.description = Validator.requireText(description, "descripción del tipo de documento",
            MAX_DESCRIPTION_LENGTH);
    }

    /**
     * Devuelve el id. No tiene setter porque el id no cambia.
     *
     * @return el identificador
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Devuelve la descripción del tipo de documento.
     *
     * @return la descripción
     */
    public String getDescription() {
        return description;
    }

    /**
     * Cambia la descripción. Valida igual que el constructor.
     *
     * @param description nueva descripción
     */
    public void setDescription(String description) {
        this.description = Validator.requireText(description, "descripción del tipo de documento",
            MAX_DESCRIPTION_LENGTH);
    }

    /**
     * Dos tipos de documento son iguales si tienen el mismo código.
     *
     * @param obj objeto a comparar
     * @return true si es un tipo de documento con el mismo código
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DocumentType otro = (DocumentType) obj;
        return this.id.equals(otro.id);   // con textos se usa equals, no ==
    }

    /**
     * Se calcula con el id, igual que equals.
     *
     * @return código hash del tipo de documento
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Texto que se muestra al imprimir el tipo de documento.
     *
     * @return código y descripción, por ejemplo "CC - Cédula"
     */
    @Override
    public String toString() {
        return id + " - " + description;
    }
}
