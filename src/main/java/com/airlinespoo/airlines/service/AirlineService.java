package com.airlinespoo.airlines.service;

import com.airlinespoo.airlines.model.Airlines;
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
    public List<Airlines> findAll() {
        return repository.findAll();
    }

    // Buscar aerolínea por ID
    public Airlines findById(int id) {
        return repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Aerolínea con id " + id + " no encontrada"));
    }

    // Guardar una nueva aerolínea validando que no esté duplicada
    public Airlines save(Airlines airline) {
        if (repository.existsByName(airline.getName())) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre: " + airline.getName());
        }
        return repository.save(airline);
    }

    // Actualizar aerolínea (recibe 1 solo argumento según el repositorio base)
    public Airlines update(int id, Airlines updatedAirline) {
        findById(id); // Verifica que exista antes de actualizar
        return repository.update(updatedAirline);
    }

    // Eliminar aerolínea
    public void delete(int id) {
        Airlines airline = findById(id);
        repository.delete(airline);
    }

    // Búsqueda por nombre parcial
    public List<Airlines> findByName(String name) {
        return repository.findByName(name);
    }
}
