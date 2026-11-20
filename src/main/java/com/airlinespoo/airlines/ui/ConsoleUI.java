package com.airlinespoo.airlines.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;


import java.awt.*;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI implements Menu {

    private final AirlineService service;
    private final Scanner scanner;

    public ConsoleUI(AirlineService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String title() {
        return "Aerolíneas";
    }

    @Override
    public void run() {
        start();
    }

    public void start() {
        int option = -1;
        do {
            System.out.println("\n========================================");
            System.out.println("       GESTIÓN DE AEROLÍNEAS - POO      ");
            System.out.println("========================================");
            System.out.println("1. Listar todas las aerolíneas");
            System.out.println("2. Buscar aerolínea por ID");
            System.out.println("3. Buscar aerolínea por nombre");
            System.out.println("4. Registrar nueva aerolínea");
            System.out.println("5. Actualizar aerolínea");
            System.out.println("6. Eliminar aerolínea");
            System.out.println("0. Salir");
            System.out.print("Selecciona una opción: ");

            try {
                option = Integer.parseInt(scanner.nextLine());

                switch (option) {
                    case 1 -> listAll();
                    case 2 -> findById();
                    case 3 -> findByName();
                    case 4 -> createAirline();
                    case 5 -> updateAirline();
                    case 6 -> deleteAirline();
                    case 0 -> System.out.println("¡Saliendo del sistema de aerolíneas. Hasta pronto!");
                    default -> System.out.println("Opción inválida. Intenta de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Por favor ingresa un número válido.");
            } catch (Exception e) {
                System.out.println("Error inesperado: " + e.getMessage());
            }

        } while (option != 0);
    }

    private void listAll() {
        System.out.println("\n--- LISTA DE AEROLÍNEAS ---");
        List<Airline> airlines = service.findAll();
        if (airlines.isEmpty()) {
            System.out.println("No hay aerolíneas registradas.");
        } else {
            airlines.forEach(System.out::println); // <-- Corrección aquí (::)
        }
    }

    private void findById() {
        System.out.print("\nIngresa el ID de la aerolínea: ");
        int id = Integer.parseInt(scanner.nextLine());
        try {
            Airline airline = service.findById(id);
            System.out.println("Encontrada: " + airline);
        } catch (NotFoundException e) {
            System.out.println("Aviso: " + e.getMessage());
        }
    }

    private void findByName() {
        System.out.print("\nIngresa el nombre o parte del nombre a buscar: ");
        String name = scanner.nextLine();
        List<Airline> results = service.findByName(name);
        if (results.isEmpty()) {
            System.out.println("No se encontraron aerolíneas con ese criterio.");
        } else {
            results.forEach(System.out::println); // <-- Corrección aquí (::)
        }
    }

    private void createAirline() {
        System.out.println("\n--- REGISTRAR NUEVA AEROLÍNEA ---");
        try {
            System.out.print("Ingresa el ID (número entero): ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Ingresa el nombre de la aerolínea: ");
            String name = scanner.nextLine();

            Airline newAirline = new Airline(id, name);
            service.save(newAirline);
            System.out.println("¡Aerolínea registrada con éxito!");

        } catch (ValidationException | DuplicateException e) {
            System.out.println("No se pudo registrar: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error: El ID debe ser un número entero.");
        }
    }

    private void updateAirline() {
        System.out.println("\n--- ACTUALIZAR AEROLÍNEA ---");
        try {
            System.out.print("Ingresa el ID de la aerolínea a actualizar: ");
            int id = Integer.parseInt(scanner.nextLine());

            System.out.print("Ingresa el nuevo nombre: ");
            String name = scanner.nextLine();

            Airline updatedData = new Airline(id, name);
            service.update(id, updatedData);
            System.out.println("¡Aerolínea actualizada con éxito!");

        } catch (NotFoundException | ValidationException e) {
            System.out.println("Error al actualizar: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error: El ID debe ser un número entero.");
        }
    }

    private void deleteAirline() {
        System.out.println("\n--- ELIMINAR AEROLÍNEA ---");
        try {
            System.out.print("Ingresa el ID de la aerolínea a eliminar: ");
            int id = Integer.parseInt(scanner.nextLine());

            service.delete(id);
            System.out.println("¡Aerolínea eliminada correctamente!");

        } catch (NotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Error: El ID debe ser un número entero.");
        }
    }

}
