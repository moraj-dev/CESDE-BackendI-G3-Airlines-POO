package com.airlinespoo.app;

import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.airlines.repository.SqlAirlineRepository;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.ConsoleUI;

public final class Main {

    private Main() {
        // Clase de arranque: no se instancia.
    }

    public static void main(String[] args) {
        System.out.println("=== Airlines-poo ===");

        SqlAirlineRepository repository = new SqlAirlineRepository();
        AirlineService service = new AirlineService(repository);
        ConsoleUI ui = new ConsoleUI(service);
        ui.start();
    }
}
