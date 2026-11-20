package com.airlinespoo.airlines.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;

import java.util.List;

public class AirlineService {

    private final AirlineRepository repository;

    // Constructor para inyectar el repositorio
    public AirlineService(AirlineRepository repository) {
        this.repository = repository;
    }

    // Listar todas las aerolíneas
    public List<Airline> findAll() {
        return repository.findAll();
    }

    // Buscar aerolínea por ID
    public Airline findById(int id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Aerolínea con id " + id + " no encontrada"));
    }

    // Guardar una nueva aerolínea validando que no esté duplicada
    public Airline save(Airline airline) {
        if (repository.existsByName(airline.getName())) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre: " + airline.getName());
        }
        return repository.create(airline);
    }

    // Actualizar aerolínea
    public Airline update(int id, Airline updatedAirline) {
        Airline existing = findById(id);
        updatedAirline.setId(id);

        String normalizedName = updatedAirline.getName() == null ? "" : updatedAirline.getName().trim();

        boolean duplicateInOtherAirline = repository.findAll().stream()
            .filter(a -> a.getId() != null && !a.getId().equals(id))
            .anyMatch(a -> a.getName() != null && a.getName().trim().equalsIgnoreCase(normalizedName));

        if (duplicateInOtherAirline) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre: " + updatedAirline.getName());
        }

        return repository.update(updatedAirline);
    }

    // Eliminar aerolínea
    public void delete(int id) {
        findById(id);
        repository.deleteById(id);
    }

    // Búsqueda por nombre parcial
    public List<Airline> findByName(String name) {
        return repository.findByName(name);
    }
}
