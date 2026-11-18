package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airlines;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqlAirlineRepository extends AirlineRepository {

    public SqlAirlineRepository() {
        super();
    }

    @Override
    public Airlines create(Airlines airline) {
        String sql = "INSERT INTO airlines (id, name) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, airline.getId());
            pstmt.setString(2, airline.getName());

            pstmt.executeUpdate();
            System.out.println("¡Guardado exitoso en SQL Server!");

        } catch (SQLException e) {
            System.out.println("ERROR SQL: " + e.getMessage());
        }

        return airline;
    }

    @Override
    public List<Airlines> findAll() {
        List<Airlines> airlinesList = new ArrayList<>();
        String sql = "SELECT id, name FROM airlines";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Airlines airline = new Airlines(id, name);
                airlinesList.add(airline);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar las aerolíneas en SQL: " + e.getMessage());
        }

        return airlinesList;
    }
}
