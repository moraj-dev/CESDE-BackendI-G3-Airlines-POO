
package com.airlinespoo.locations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.locations.model.Airport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AirportRepository extends InMemoryRepository<Airport, UUID> {

    @Override
    protected String entityName() {
        return "Aeropuerto";
    }

    // Busca aeropuertos pertenecientes a una ciudad.
    public List<Airport> findByCity(int cityId) {
        List<Airport> results = new ArrayList<>();

        for (Airport airport : findAll()) {
            if (airport.getCity().getId() == cityId) {
                results.add(airport);
            }
        }

        return results;
    }

    // Busca aeropuertos por coincidencia parcial del nombre.
    public List<Airport> findByName(String name) {
        List<Airport> results = new ArrayList<>();

        if (name == null) {
            return results;
        }

        String search = name.trim().toLowerCase();

        for (Airport airport : findAll()) {
            if (airport.getName().toLowerCase().contains(search)) {
                results.add(airport);
            }
        }

        return results;
    }
}
