package com.airlinespoo.app;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;

import java.util.ArrayList;
import java.util.List;

public class MainMenu {

    private final List<Menu> menus;
    private final ConsoleInput input;

    public MainMenu(List<Menu> menus, ConsoleInput input) {
        this.menus = new ArrayList<>(menus);
        this.input = input;
    }

    public void run() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== SISTEMA AIRLINES-POO ===");
            for (int i = 0; i < menus.size(); i++) {
                System.out.println((i + 1) + ". " + menus.get(i).title());
            }
            System.out.println("0. Salir");

            int option = input.readInt("Seleccione una opción", 0, menus.size());

            if (option == 0) {
                running = false;
                System.out.println("¡Hasta pronto!");
            } else {
                Menu selectedMenu = menus.get(option - 1);
                try {
                    selectedMenu.run();
                } catch (Exception e) {
                    System.out.println("Error inesperado en el módulo: " + e.getMessage());
                }
            }
        }
    }
}
