package com.airlinespoo.customers.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

/**
 * Representa un país. Viene de la tabla tblCountries.
 */
public class Country implements Identifiable<Integer> {

    // Máximo de caracteres que puede tener el nombre del país
    public static final int MAX_NAME_LENGTH = 20;

    // Atributos privados (encapsulamiento)
    private final int id;   // final: el id no se puede cambiar
    private String name;

    /**
     * Constructor: crea un país y valida sus datos.
     *
     * @param id   número del país, debe ser mayor que cero
     * @param name nombre del país, obligatorio y máximo 20 caracteres
     */
    public Country(int id, String name) {
        // El id debe ser positivo
        Validator.requirePositive(id, "id del país");
        this.id = id;

        // El nombre no puede estar vacío ni pasar de 20 caracteres
        this.name = Validator.requireText(name, "nombre del país", MAX_NAME_LENGTH);
    }

    /**
     * Devuelve el id. No tiene setter porque el id no cambia.
     *
     * @return el identificador
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Devuelve el nombre del país.
     *
     * @return el nombre
     */
    public String getName() {
        return name;
    }

    /**
     * Cambia el nombre del país. Valida igual que el constructor.
     *
     * @param name nuevo nombre
     */
    public void setName(String name) {
        this.name = Validator.requireText(name, "nombre del país", MAX_NAME_LENGTH);
    }

    /**
     * Dos países son iguales si tienen el mismo id.
     *
     * @param obj objeto a comparar
     * @return true si es un país con el mismo id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Country otro = (Country) obj;
        return this.id == otro.id;
    }

    /**
     * Debe ir de la mano con equals: se calcula con el id.
     *
     * @return código hash del país
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    /**
     * Texto que se muestra al imprimir el país.
     *
     * @return id y nombre, por ejemplo "57 - Colombia"
     */
    @Override
    public String toString() {
        return id + " - " + name;
    }
}
