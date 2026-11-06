package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.service.CustomerService;

import java.util.List;

/**
 * Menú de consola del módulo de clientes.
 * Pide los datos por teclado, llama al servicio y muestra los resultados.
 * Si el servicio lanza un error, muestra el mensaje y vuelve al menú sin cerrar el programa.
 */
public class CustomerMenu implements Menu {

    // Opciones del menú
    private static final int REGISTRAR = 1;
    private static final int CONSULTAR = 2;
    private static final int ACTUALIZAR_CONTACTO = 3;
    private static final int LISTAR = 4;
    private static final int BUSCAR = 5;
    private static final int ELIMINAR = 6;
    private static final int REGISTRAR_PAIS = 7;
    private static final int REGISTRAR_TIPO = 8;
    private static final int VOLVER = 0;

    // Rango del id de país (SMALLINT en la base de datos)
    private static final int MIN_COUNTRY_ID = 1;
    private static final int MAX_COUNTRY_ID = Short.MAX_VALUE;

    private final CustomerService service;
    private final ConsoleInput input;

    /**
     * Recibe el servicio y el lector de consola por el constructor.
     *
     * @param service servicio de clientes
     * @param input   lector de consola (una sola instancia en toda la aplicación)
     */
    public CustomerMenu(CustomerService service, ConsoleInput input) {
        this.service = service;
        this.input = input;
    }

    /**
     * Título que aparece en el menú principal.
     *
     * @return el título del menú
     */
    @Override
    public String title() {
        return "Clientes";
    }

    /**
     * Muestra el menú y atiende al usuario hasta que elija volver.
     */
    @Override
    public void run() {
        int opcion;
        do {
            showOptions();
            opcion = input.readInt("Elige una opción", VOLVER, REGISTRAR_TIPO);
            try {
                runOption(opcion);
            } catch (AirlinesException e) {
                // Cualquier error del sistema: se muestra y el menú sigue
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != VOLVER);
    }

    // Imprime las opciones disponibles
    private void showOptions() {
        System.out.println();
        System.out.println("===== CLIENTES =====");
        System.out.println(REGISTRAR + ". Registrar cliente");
        System.out.println(CONSULTAR + ". Consultar cliente por documento");
        System.out.println(ACTUALIZAR_CONTACTO + ". Actualizar datos de contacto");
        System.out.println(LISTAR + ". Listar clientes");
        System.out.println(BUSCAR + ". Buscar por apellido");
        System.out.println(ELIMINAR + ". Eliminar cliente");
        System.out.println(REGISTRAR_PAIS + ". Registrar país");
        System.out.println(REGISTRAR_TIPO + ". Registrar tipo de documento");
        System.out.println(VOLVER + ". Volver");
    }

    // Llama al método que corresponde a la opción elegida
    private void runOption(int opcion) {
        switch (opcion) {
            case REGISTRAR -> registerCustomer();
            case CONSULTAR -> showCustomer();
            case ACTUALIZAR_CONTACTO -> updateContact();
            case LISTAR -> listCustomers();
            case BUSCAR -> searchByLastName();
            case ELIMINAR -> deleteCustomer();
            case REGISTRAR_PAIS -> registerCountry();
            case REGISTRAR_TIPO -> registerDocumentType();
            default -> System.out.println("Volviendo al menú principal...");
        }
    }

    // ---------- Opciones ----------

    private void registerCustomer() {
        showDocumentTypes();
        String tipo = input.readRequiredText("Tipo de documento (ej. CC)");
        String documento = input.readRequiredText("Número de documento");
        String nombre = input.readRequiredText("Nombre");
        String apellido = input.readRequiredText("Apellido");
        String telefono = input.readRequiredText("Teléfono");
        String correo = input.readOptionalText("Correo");
        showCountries();
        int pais = input.readInt("Id del país", MIN_COUNTRY_ID, MAX_COUNTRY_ID);
        String direccion = input.readOptionalText("Dirección");

        Customer cliente = service.createCustomer(documento, nombre, apellido, tipo,
                telefono, correo, pais, direccion);
        System.out.println("Cliente registrado: " + cliente);
    }

    private void showCustomer() {
        String documento = input.readRequiredText("Número de documento");
        Customer cliente = service.getCustomer(documento);
        printDetail(cliente);
    }

    /*
     * Actualiza solo el teléfono, el correo y la dirección.
     * Los demás datos se toman del cliente que ya existe.
     */
    private void updateContact() {
        String documento = input.readRequiredText("Número de documento");
        Customer actual = service.getCustomer(documento);
        printDetail(actual);

        System.out.println("Escribe los datos nuevos.");
        String telefono = input.readRequiredText("Teléfono");
        String correo = input.readOptionalText("Correo");
        String direccion = input.readOptionalText("Dirección");

        Customer actualizado = service.updateCustomer(actual.getId(), actual.getFirstName(),
                actual.getLastName(), actual.getDocumentType().getId(), telefono, correo,
                actual.getCountry().getId(), direccion);
        System.out.println("Datos de contacto actualizados: " + actualizado);
    }

    private void listCustomers() {
        List<Customer> clientes = service.listCustomers();
        System.out.println("Clientes registrados: " + clientes.size());
        printList(clientes);
    }

    private void searchByLastName() {
        String texto = input.readRequiredText("Apellido o parte del apellido");
        List<Customer> clientes = service.searchCustomersByLastName(texto);
        printList(clientes);
    }

    private void deleteCustomer() {
        String documento = input.readRequiredText("Número de documento");
        Customer cliente = service.getCustomer(documento);
        if (input.confirm("¿Seguro que quieres eliminar a " + cliente.getFullName() + "?")) {
            service.deleteCustomer(cliente.getId());
            System.out.println("Cliente eliminado.");
        } else {
            System.out.println("No se eliminó nada.");
        }
    }

    private void registerCountry() {
        int id = input.readInt("Id del país", MIN_COUNTRY_ID, MAX_COUNTRY_ID);
        String nombre = input.readRequiredText("Nombre del país");
        Country pais = service.createCountry(id, nombre);
        System.out.println("País registrado: " + pais);
    }

    private void registerDocumentType() {
        String codigo = input.readRequiredText("Código (2 letras, ej. CC)");
        String descripcion = input.readRequiredText("Descripción");
        DocumentType tipo = service.createDocumentType(codigo, descripcion);
        System.out.println("Tipo de documento registrado: " + tipo);
    }

    // ---------- Ayudas para mostrar datos ----------

    private void printList(List<Customer> clientes) {
        if (clientes.isEmpty()) {
            System.out.println("No se encontraron clientes.");
            return;
        }
        for (Customer cliente : clientes) {
            System.out.println("- " + cliente);
        }
    }

    // Muestra todos los datos de un cliente (aquí sí se ven los datos de contacto)
    private void printDetail(Customer cliente) {
        System.out.println("Documento: " + cliente.getDocumentType().getId() + " " + cliente.getId());
        System.out.println("Nombre:    " + cliente.getFullName());
        System.out.println("Teléfono:  " + cliente.getTelephoneNumber());
        System.out.println("Correo:    " + textOrDash(cliente.getEmail()));
        System.out.println("País:      " + cliente.getCountry().getName());
        System.out.println("Dirección: " + textOrDash(cliente.getAddress()));
    }

    // Si el dato opcional no existe, muestra un guion
    private String textOrDash(String texto) {
        if (texto == null) {
            return "-";
        }
        return texto;
    }

    private void showCountries() {
        System.out.println("Países disponibles:");
        for (Country pais : service.listCountries()) {
            System.out.println("  " + pais);
        }
    }

    private void showDocumentTypes() {
        System.out.println("Tipos de documento disponibles:");
        for (DocumentType tipo : service.listDocumentTypes()) {
            System.out.println("  " + tipo);
        }
    }
}
