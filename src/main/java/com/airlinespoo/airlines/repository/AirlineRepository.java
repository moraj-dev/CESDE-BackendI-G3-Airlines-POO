package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airlines;
import com.airlinespoo.core.repository.InMemoryRepository;

import java.util.List;
import java.util.stream.Collectors;

public class AirlineRepository extends InMemoryRepository<Airlines, Integer> {

    @Override
    protected String entityName() {
        return "Airline";
    }

    // Búsqueda por nombre (coincidencia parcial, sin distinguir mayúsculas)
    public List<Airlines> findByName(String name) {
        String lowerCaseName = name.toLowerCase();
        return findAll().stream()
            .filter(a -> a.getName().toLowerCase().contains(lowerCaseName))
            .collect(Collectors.toList());
    }

    // Verificar si ya existe una aerolínea con ese nombre exacto
    public boolean existsByName(String name) {
        return findAll().stream()
            .anyMatch(a -> a.getName().equalsIgnoreCase(name.trim()));
    }
}
