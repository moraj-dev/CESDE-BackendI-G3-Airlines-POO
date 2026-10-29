
package com.airlinespoo.locations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.locations.model.City;
import java.util.ArrayList;
import java.util.List;

public class CityRepository extends InMemoryRepository<City, Integer> {

    @Override
    protected String entityName() {
        return "Ciudad";
    }

    // Busca ciudades cuyo nombre contiene el texto indicado.
    public List<City> findByName(String name) {
        List<City> results = new ArrayList<>();

        if (name == null) {
            return results;
        }

        String search = name.trim().toLowerCase();

        for (City city : findAll()) {
            if (city.getName().toLowerCase().contains(search)) {
                results.add(city);
            }
        }

        return results;
    }
}
