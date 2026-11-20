
package com.airlinespoo.locations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import java.util.Objects;


public class City implements Identifiable<Integer> {

    private final int id;
    private String name;

    public City(int id, String name) {
        Validator.requirePositive(id, "id de ciudad");
        this.id = id;
        setName(name);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Valida el nombre antes de guardarlo.
    public void setName(String name) {
        this.name = Validator.requireText(name, "nombre de ciudad", 20);
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }

    // Dos ciudades son iguales si tienen el mismo ID.
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof City)) return false;

        City other = (City) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
