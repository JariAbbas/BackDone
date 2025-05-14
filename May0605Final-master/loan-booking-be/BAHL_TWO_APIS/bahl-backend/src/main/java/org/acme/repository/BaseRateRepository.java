package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.BaseRate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BaseRateRepository {

    @Inject
    AgroalDataSource dataSource;

    public List<BaseRate> listAll() throws SQLException {
        List<BaseRate> baseRates = new ArrayList<>();
        String sql = "SELECT id, baseName, baseRate FROM BaseRateTable";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                baseRates.add(new BaseRate(
                        resultSet.getLong("id"),
                        resultSet.getString("baseName"),
                        resultSet.getBigDecimal("baseRate")
                ));
            }
        }
        return baseRates;
    }

    public Optional<BaseRate> findByBaseName(String baseName) throws SQLException {
        String sql = "SELECT id, baseName, baseRate FROM BaseRateTable WHERE baseName = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, baseName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new BaseRate(
                            resultSet.getLong("id"),
                            resultSet.getString("baseName"),
                            resultSet.getBigDecimal("baseRate")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<BaseRate> findById(Long id) throws SQLException {
        String sql = "SELECT id, baseName, baseRate FROM BaseRateTable WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new BaseRate(
                            resultSet.getLong("id"),
                            resultSet.getString("baseName"),
                            resultSet.getBigDecimal("baseRate")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public void create(String baseName, BigDecimal baseRate) throws SQLException {
        String sql = "INSERT INTO BaseRateTable (baseName, baseRate) VALUES (?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, baseName);
            statement.setBigDecimal(2, baseRate);
            statement.executeUpdate();
        }
    }

    public void update(String baseName, BigDecimal baseRate) throws SQLException {
        String sql = "UPDATE BaseRateTable SET baseRate = ? WHERE baseName = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, baseRate);
            statement.setString(2, baseName);
            statement.executeUpdate();
        }
    }

    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM BaseRateTable WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public BigDecimal getBaseRateValue(Long id) throws SQLException {
        String sql = "SELECT baseRate FROM BaseRateTable WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getBigDecimal("baseRate");
                }
            }
        }
        return null;
    }

    public void updateBaseRate(Long id, BigDecimal baseRate) throws SQLException {
        String sql = "UPDATE BaseRateTable SET baseRate = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, baseRate);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }
}