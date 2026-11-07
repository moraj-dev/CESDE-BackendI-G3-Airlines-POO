package com.airlinespoo.customers.ui;

import com.airlinespoo.customers.service.CustomerService;

/**
 * Carga datos de ejemplo del módulo de clientes: 5 países, 4 tipos de documento y 3 clientes.
 * Así los otros módulos pueden probar sin escribir datos cada vez.
 */
public final class CustomerSampleData {

    private CustomerSampleData() {
        // Solo tiene un método estático: no se instancia.
    }

    /**
     * Registra los datos de ejemplo usando el servicio, para que pasen por todas las validaciones.
     * Solo carga lo que esté vacío: con base de datos los datos no se borran al cerrar el
     * programa, y cargarlos otra vez daría error de "ya existe".
     *
     * @param service servicio de clientes donde se cargan los datos
     */
    public static void load(CustomerService service) {
        if (service.listCountries().isEmpty()) {
            loadCountries(service);
        }
        if (service.listDocumentTypes().isEmpty()) {
            loadDocumentTypes(service);
        }
        if (service.countCustomers() == 0) {
            loadCustomers(service);
        }
    }

    // 5 países (el id es el indicativo telefónico de cada país)
    private static void loadCountries(CustomerService service) {
        service.createCountry(57, "Colombia");
        service.createCountry(52, "México");
        service.createCountry(51, "Perú");
        service.createCountry(54, "Argentina");
        service.createCountry(34, "España");
    }

    // 4 tipos de documento
    private static void loadDocumentTypes(CustomerService service) {
        service.createDocumentType("CC", "Cédula de ciudadanía");
        service.createDocumentType("CE", "Cédula extranjería");
        service.createDocumentType("TI", "Tarjeta de identidad");
        service.createDocumentType("PA", "Pasaporte");
    }

    // 3 clientes de muestra (documento, nombre, apellido, tipo, teléfono, correo, país, dirección)
    private static void loadCustomers(CustomerService service) {
        service.createCustomer("1037654321", "Ana", "Pérez", "CC", "3001234567",
                "ana.perez@correo.com", 57, "Calle 10 # 20-30");
        service.createCustomer("98765432", "Luis", "Gómez", "CE", "+525512345678",
                "luis.gomez@correo.com", 52, null);
        service.createCustomer("PA4455667", "Carla", "Arango", "PA", "3109876543",
                null, 57, "Carrera 45 # 12-08");
    }
}
