/**
 * Módulo 3 (Aerolíneas) · Capa de modelo.
 * Clase del modelo: {@code Airline}.
 */
package com.airlinespoo.airlines.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.exception.ValidationException;

public class Airlines implements Identifiable<Integer> {
    private int id;
    private String name;

    // Constructor que valida y deja el objeto en un estado válido
    public Airlines(int id, String name) {
        if (id <= 0) {
            throw new ValidationException("El id debe ser positivo");
        }
        this.id = id;
        setName(name);
    }

    public String getName() {
        return name;
    }

    // Setter con validaciones obligatorias y de longitud máxima
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("El nombre de la aerolínea es obligatorio");
        }
        if (name.length() > 50) {
            throw new ValidationException("El nombre de la aerolínea no puede superar los 50 caracteres");
        }
        this.name = name.trim();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Airlines{id=" + id + ", name='" + name + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airlines airlines = (Airlines) o;
        return id == airlines.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
