package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.Markup;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MarkupRepository {

    @Inject
    AgroalDataSource dataSource;

    public List<Markup> listAll() throws SQLException {
        List<Markup> markups = new ArrayList<>();
        String sql = "SELECT id, loanNumber, baseRateId, spreadRate, applicableRate, odRate FROM MarkupTable";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                markups.add(new Markup(
                        resultSet.getLong("id"),
                        resultSet.getString("loanNumber"),
                        resultSet.getLong("baseRateId"),
                        resultSet.getBigDecimal("spreadRate"),
                        resultSet.getBigDecimal("applicableRate"),
                        resultSet.getBigDecimal("odRate")
                ));
            }
        }
        return markups;
    }

    public Optional<Markup> findById(Long id) throws SQLException {
        String sql = "SELECT id, loanNumber, baseRateId, spreadRate, applicableRate, odRate FROM MarkupTable WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Markup(
                            resultSet.getLong("id"),
                            resultSet.getString("loanNumber"),
                            resultSet.getLong("baseRateId"),
                            resultSet.getBigDecimal("spreadRate"),
                            resultSet.getBigDecimal("applicableRate"),
                            resultSet.getBigDecimal("odRate")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public void create(String loanNumber, Long baseRateId, BigDecimal spreadRate, BigDecimal applicableRate, BigDecimal odRate) throws SQLException {
        String sql = "INSERT INTO MarkupTable (loanNumber, baseRateId, spreadRate, applicableRate, odRate) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loanNumber);
            statement.setLong(2, baseRateId);
            statement.setBigDecimal(3, spreadRate);
            statement.setBigDecimal(4, applicableRate);
            statement.setBigDecimal(5, odRate);
            statement.executeUpdate();
        }
    }

    public void update(Long id, String loanNumber, BigDecimal spreadRate, BigDecimal applicableRate, BigDecimal odRate) throws SQLException {
        String sql = "UPDATE MarkupTable SET loanNumber = ?, spreadRate = ?, applicableRate = ?, odRate = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loanNumber);
            statement.setBigDecimal(2, spreadRate);
            statement.setBigDecimal(3, applicableRate);
            statement.setBigDecimal(4, odRate);
            statement.setLong(5, id);
            statement.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM MarkupTable WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    // Add the findByLoanNumber method here
    public Optional<Markup> findByLoanNumber(String loanNumber) throws SQLException {
        String sql = "SELECT id, loanNumber, baseRateId, spreadRate, applicableRate, odRate FROM MarkupTable WHERE loanNumber = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, loanNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Markup(
                            resultSet.getLong("id"),
                            resultSet.getString("loanNumber"),
                            resultSet.getLong("baseRateId"),
                            resultSet.getBigDecimal("spreadRate"),
                            resultSet.getBigDecimal("applicableRate"),
                            resultSet.getBigDecimal("odRate")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public BigDecimal getBaseRateValue(Long baseRateId) throws SQLException {
        String sql = "SELECT baseRate FROM BaseRateTable WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, baseRateId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("baseRate");
                }
            }
        }
        return null; // Or throw an exception if base rate is not found.  Important for error handling
    }

    public void updateBaseRate(Long id, BigDecimal baseRate) throws SQLException{
        String sql = "UPDATE BaseRateTable SET baseRate = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, baseRate);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }
}