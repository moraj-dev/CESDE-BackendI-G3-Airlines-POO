
package com.airlinespoo.locations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import java.util.UUID;
import java.util.Objects;

public class Airport implements Identifiable<UUID> {

    private final UUID id;
    private String name;
    private City city;

    // Constructor para un aeropuerto nuevo.
    public Airport(String name, City city) {
        this(UUID.randomUUID(), name, city);
    }

    // Constructor para un aeropuerto con ID existente.
    public Airport(UUID id, String name, City city) {
        this.id = Validator.requireNotNull(id, "id de aeropuerto");
        setName(name);
        setCity(city);
    }

    @Override
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return city;
    }

    public void setName(String name) {
        this.name = Validator.requireText(name, "nombre de aeropuerto", 50);
    }

    public void setCity(City city) {
        this.city = Validator.requireNotNull(city, "ciudad");
    }

    @Override
    public String toString() {
        return name + " - " + city.getName() + " (" + id + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Airport)) return false;

        Airport other = (Airport) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
