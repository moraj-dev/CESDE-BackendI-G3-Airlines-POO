
package com.airlinespoo.locations.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Administra las ciudades y los aeropuertos.
 */
public class LocationService {

    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final IdGenerator cityIds;

    public LocationService(CityRepository cityRepository,
                           AirportRepository airportRepository,
                           IdGenerator cityIds) {
        this.cityRepository = Validator.requireNotNull(cityRepository, "repositorio de ciudades");
        this.airportRepository = Validator.requireNotNull(airportRepository, "repositorio de aeropuertos");
        this.cityIds = Validator.requireNotNull(cityIds, "generador de ciudades");
    }

    // Comprueba si ya existe una ciudad con el mismo nombre.
    private boolean cityNameExists(String name, int excludedId) {
        for (City city : cityRepository.findAll()) {
            if (city.getId() != excludedId &&
                city.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // Comprueba nombres duplicados dentro de una misma ciudad.
    private boolean airportNameExists(String name, int cityId, UUID excludedId) {
        for (Airport airport : airportRepository.findAll()) {
            if (!airport.getId().equals(excludedId) &&
                airport.getCity().getId() == cityId &&
                airport.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /** Registra una ciudad nueva. */
    public City createCity(String name) {
        name = Validator.requireText(name, "nombre de ciudad", 20);

        if (cityNameExists(name, -1)) {
            throw new DuplicateException("Ya existe una ciudad con ese nombre.");
        }

        City city = new City(cityIds.next(), name);
        return cityRepository.create(city);
    }

    /** Consulta una ciudad por su identificador. */
    public City getCity(int id) {
        return cityRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Ciudad no encontrada."));
    }

    /** Devuelve todas las ciudades ordenadas por nombre. */
    public List<City> listCities() {
        List<City> cities = new ArrayList<>(cityRepository.findAll());
        cities.sort(Comparator.comparing(City::getName, String.CASE_INSENSITIVE_ORDER));
        return cities;
    }

    /** Cambia el nombre de una ciudad. */
    public City updateCity(int id, String name) {
        City city = getCity(id);
        name = Validator.requireText(name, "nombre de ciudad", 20);

        if (cityNameExists(name, id)) {
            throw new DuplicateException("Ya existe otra ciudad con ese nombre.");
        }

        city.setName(name);
        return cityRepository.update(city);
    }

    /** Elimina una ciudad si no tiene aeropuertos. */
    public void deleteCity(int id) {
        getCity(id);

        if (!airportRepository.findByCity(id).isEmpty()) {
            throw new BusinessRuleException(
                "No se puede eliminar una ciudad que tiene aeropuertos.");
        }

        cityRepository.deleteById(id);
    }

    /** Busca ciudades que contienen parte de un nombre. */
    public List<City> searchCitiesByName(String name) {
        List<City> cities = cityRepository.findByName(name);
        cities.sort(Comparator.comparing(City::getName, String.CASE_INSENSITIVE_ORDER));
        return cities;
    }

    /** Registra un aeropuerto en una ciudad existente. */
    public Airport createAirport(String name, int cityId) {
        City city = getCity(cityId);
        name = Validator.requireText(name, "nombre de aeropuerto", 50);

        if (airportNameExists(name, cityId, null)) {
            throw new DuplicateException(
                "Ya existe un aeropuerto con ese nombre en esta ciudad.");
        }

        Airport airport = new Airport(name, city);
        return airportRepository.create(airport);
    }

    /** Consulta un aeropuerto por su UUID. */
    public Airport getAirport(UUID id) {
        return airportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Aeropuerto no encontrado."));
    }

    /** Devuelve todos los aeropuertos ordenados por nombre. */
    public List<Airport> listAirports() {
        List<Airport> airports = new ArrayList<>(airportRepository.findAll());
        airports.sort(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER));
        return airports;
    }

    /** Actualiza el nombre y la ciudad de un aeropuerto. */
    public Airport updateAirport(UUID id, String name, int cityId) {
        Airport airport = getAirport(id);
        City city = getCity(cityId);
        name = Validator.requireText(name, "nombre de aeropuerto", 50);

        if (airportNameExists(name, cityId, id)) {
            throw new DuplicateException(
                "Ya existe otro aeropuerto con ese nombre en esta ciudad.");
        }

        airport.setName(name);
        airport.setCity(city);
        return airportRepository.update(airport);
    }

    /** Elimina un aeropuerto. */
    public void deleteAirport(UUID id) {
        getAirport(id);
        airportRepository.deleteById(id);
    }

    /** Lista los aeropuertos de una ciudad existente. */
    public List<Airport> listAirportsByCity(int cityId) {
        getCity(cityId);

        List<Airport> airports = airportRepository.findByCity(cityId);
        airports.sort(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER));
        return airports;
    }

    /** Busca aeropuertos por coincidencia parcial del nombre. */
    public List<Airport> searchAirportsByName(String name) {
        List<Airport> airports = airportRepository.findByName(name);
        airports.sort(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER));
        return airports;
    }
}
