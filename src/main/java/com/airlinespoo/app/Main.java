package com.airlinespoo.app;

import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.airlines.repository.SqlAirlineRepository;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.ConsoleUI;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;
import com.airlinespoo.customers.repository.JdbcCountryRepository;
import com.airlinespoo.customers.repository.JdbcCustomerRepository;
import com.airlinespoo.customers.repository.JdbcDocumentTypeRepository;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.ui.CustomerMenu;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.flights.repository.JdbcFlightRepository;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.flights.ui.FlightMenu;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;
import com.airlinespoo.locations.repository.JdbcAirportRepository;
import com.airlinespoo.locations.repository.JdbcCityRepository;
import com.airlinespoo.locations.service.LocationService;
import com.airlinespoo.locations.ui.LocationMenu;
import com.airlinespoo.reservations.repository.JdbcReservationRepository;
import com.airlinespoo.reservations.repository.ReservationRepository;
import com.airlinespoo.reservations.service.ReservationService;
import com.airlinespoo.reservations.ui.ReservationMenu;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput();

        var locationDb = new com.airlinespoo.locations.repository.DatabaseConnection();
        var customerDb = new com.airlinespoo.customers.repository.DatabaseConnection();
        var flightDb = new com.airlinespoo.flights.repository.DatabaseConnection();

        IdGenerator locationIdGenerator = new IdGenerator(1, 1);
        IdGenerator reservationIdGenerator = new IdGenerator(1, 1);
        IdGenerator detailIdGenerator = new IdGenerator(1, 1);

        CityRepository cityRepository = new JdbcCityRepository(locationDb);
        AirportRepository airportRepository = new JdbcAirportRepository(locationDb);
        LocationService locationService = new LocationService(
            cityRepository,
            airportRepository,
            locationIdGenerator
        );

        AirlineRepository airlineRepository = new SqlAirlineRepository();
        AirlineService airlineService = new AirlineService(airlineRepository);

        FlightRepository flightRepository = new JdbcFlightRepository(flightDb);
        FlightService flightService = new FlightService(
            flightRepository,
            locationService,
            airlineRepository
        );

        CountryRepository countryRepository = new JdbcCountryRepository(customerDb);
        DocumentTypeRepository documentTypeRepository = new JdbcDocumentTypeRepository(customerDb);
        CustomerRepository customerRepository = new JdbcCustomerRepository(customerDb);
        CustomerService customerService = new CustomerService(
            countryRepository,
            documentTypeRepository,
            customerRepository
        );

        ReservationRepository reservationRepository = new JdbcReservationRepository(
            flightDb::open,
            customerRepository,
            flightRepository
        );
        ReservationService reservationService = new ReservationService(
            reservationRepository,
            customerService,
            flightService,
            reservationIdGenerator,
            detailIdGenerator
        );

        List<Menu> menus = List.of(
            new LocationMenu(locationService, input),
            new ConsoleUI(airlineService),
            new FlightMenu(flightService, locationService, airlineService, input),
            new CustomerMenu(customerService, input),
            new ReservationMenu(reservationService, customerService, flightService, input)
        );

        MainMenu mainMenu = new MainMenu(menus, input);
        mainMenu.run();
    }
}
