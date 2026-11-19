package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqlAirlineRepository extends AirlineRepository {

    public SqlAirlineRepository() {
        super();
    }

    @Override
    public Airline create(Airline airline) {
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
    public List<Airline> findAll() {
        List<Airline> airlinesList = new ArrayList<>();
        String sql = "SELECT id, name FROM airlines";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Airline airline = new Airline(id, name);
                airlinesList.add(airline);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar las aerolíneas en SQL: " + e.getMessage());
        }

        return airlinesList;
    }

    @Override
    public boolean deleteById(Integer id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM airlines WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error al eliminar la aerolínea en SQL: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Airline> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, name FROM airlines WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int foundId = rs.getInt("id");
                    String name = rs.getString("name");
                    return Optional.of(new Airline(foundId, name));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error al buscar la aerolínea por ID en SQL: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Airline update(Airline airline) {
        Validator.requireNotNull(airline, this.entityName());
        Integer id = Validator.requireNotNull(airline.getId(), "id de " + this.entityName());

        String sql = "UPDATE airlines SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, airline.getName());
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new NotFoundException(this.entityName(), id);
            }

            return airline;

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la aerolínea en SQL: " + e.getMessage(), e);
        }
    }

}
