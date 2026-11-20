
package com.airlinespoo.locations.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.service.LocationService;

import java.util.List;
import java.util.UUID;

public class LocationMenu implements Menu {

    private final LocationService service;
    private final ConsoleInput input;

    public LocationMenu(LocationService service, ConsoleInput input) {
        this.service = service;
        this.input = input;
    }

    @Override
    public String title() {
        return "Ciudades y aeropuertos";
    }

    @Override
    public void run() {
        int option;

        do {
            System.out.println("\n===== UBICACIONES =====");
            System.out.println("1. Registrar ciudad");
            System.out.println("2. Listar ciudades");
            System.out.println("3. Buscar ciudad por ID");
            System.out.println("4. Renombrar ciudad");
            System.out.println("5. Eliminar ciudad");
            System.out.println("6. Buscar ciudades por nombre");
            System.out.println("7. Registrar aeropuerto");
            System.out.println("8. Listar aeropuertos");
            System.out.println("9. Buscar aeropuerto por ID");
            System.out.println("10. Actualizar aeropuerto");
            System.out.println("11. Eliminar aeropuerto");
            System.out.println("12. Aeropuertos por ciudad");
            System.out.println("13. Buscar aeropuertos por nombre");
            System.out.println("0. Volver");

            option = input.readInt("Seleccione una opción: ", 0, 13);

            try {
                switch (option) {
                    case 1 -> createCity();
                    case 2 -> showCities(service.listCities());
                    case 3 -> getCity();
                    case 4 -> updateCity();
                    case 5 -> deleteCity();
                    case 6 -> searchCities();
                    case 7 -> createAirport();
                    case 8 -> showAirports(service.listAirports());
                    case 9 -> getAirport();
                    case 10 -> updateAirport();
                    case 11 -> deleteAirport();
                    case 12 -> listAirportsByCity();
                    case 13 -> searchAirports();
                    case 0 -> System.out.println("Volviendo al menú principal...");
                }
            } catch (AirlinesException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } while (option != 0);
    }

    private void createCity() {
        String name = input.readRequiredText("Nombre de la ciudad: ");
        City city = service.createCity(name);
        System.out.println("Ciudad registrada: " + city);
    }

    private void getCity() {
        int id = input.readInt("ID de ciudad: ", 1, Integer.MAX_VALUE);
        System.out.println(service.getCity(id));
    }

    private void updateCity() {
        int id = input.readInt("ID de ciudad: ", 1, Integer.MAX_VALUE);
        String name = input.readRequiredText("Nuevo nombre: ");
        System.out.println("Actualizada: " + service.updateCity(id, name));
    }

    private void deleteCity() {
        int id = input.readInt("ID de ciudad: ", 1, Integer.MAX_VALUE);

        if (input.confirm("¿Desea eliminar esta ciudad?")) {
            service.deleteCity(id);
            System.out.println("Ciudad eliminada.");
        }
    }

    private void searchCities() {
        String name = input.readRequiredText("Nombre a buscar: ");
        showCities(service.searchCitiesByName(name));
    }

    private void createAirport() {
        String name = input.readRequiredText("Nombre del aeropuerto: ");
        int cityId = input.readInt("ID de ciudad: ", 1, Integer.MAX_VALUE);

        Airport airport = service.createAirport(name, cityId);
        System.out.println("Aeropuerto registrado: " + airport);
    }

    private UUID readAirportId() {
        String text = input.readRequiredText("UUID del aeropuerto: ");
        return UUID.fromString(text);
    }

    private void getAirport() {
        System.out.println(service.getAirport(readAirportId()));
    }

    private void updateAirport() {
        UUID id = readAirportId();
        String name = input.readRequiredText("Nuevo nombre: ");
        int cityId = input.readInt("ID de ciudad: ", 1, Integer.MAX_VALUE);

        System.out.println("Actualizado: " + service.updateAirport(id, name, cityId));
    }

    private void deleteAirport() {
        UUID id = readAirportId();

        if (input.confirm("¿Desea eliminar este aeropuerto?")) {
            service.deleteAirport(id);
            System.out.println("Aeropuerto eliminado.");
        }
    }

    private void listAirportsByCity() {
        int cityId = input.readInt("ID de ciudad: ", 1, Integer.MAX_VALUE);
        showAirports(service.listAirportsByCity(cityId));
    }

    private void searchAirports() {
        String name = input.readRequiredText("Nombre a buscar: ");
        showAirports(service.searchAirportsByName(name));
    }

    // Imprime las ciudades obtenidas del servicio.
    private void showCities(List<City> cities) {
        if (cities.isEmpty()) {
            System.out.println("No se encontraron ciudades.");
            return;
        }

        for (City city : cities) {
            System.out.println(city);
        }
    }

    // Imprime los aeropuertos obtenidos del servicio.
    private void showAirports(List<Airport> airports) {
        if (airports.isEmpty()) {
            System.out.println("No se encontraron aeropuertos.");
            return;
        }

        for (Airport airport : airports) {
            System.out.println(airport);
        }
    }
}
