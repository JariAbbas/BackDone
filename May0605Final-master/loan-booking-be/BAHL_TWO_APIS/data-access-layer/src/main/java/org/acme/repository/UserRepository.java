package org.acme.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.agroal.api.AgroalDataSource;
import org.acme.modelDAL.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@ApplicationScoped
public class UserRepository {

    @Inject
    AgroalDataSource dataSource;

    public User validateUser(String userId, int branchCode, String password) {
        String query = "SELECT u.userId, u.branchCode, b.branchName, u.userIPAddress, u.userRole, u.module, u.lastSignOnDate "
                + "FROM users u " +
                "JOIN branches b ON u.branchCode = b.branchCode " +
                "WHERE u.userId = ? AND u.branchCode = ? AND u.password = ? AND u.status = 'A'";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userId);
            preparedStatement.setInt(2, branchCode);
            preparedStatement.setString(3, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(
                            resultSet.getString("userId"),
                            resultSet.getInt("branchCode"),
                            resultSet.getString("userIPAddress"),
                            resultSet.getString("userRole"),
                            resultSet.getString("module"),
                            resultSet.getString("branchName")
                    );
                    user.setLastSignOnDate(resultSet.getTimestamp("lastSignOnDate") != null
                            ? resultSet.getTimestamp("lastSignOnDate").toLocalDateTime()
                            : null);
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public LocalDateTime getPreviousLastSignOnDate(String userId, int branchCode) {
        String query = "SELECT lastSignOnDate FROM users WHERE userId = ? AND branchCode = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setInt(2, branchCode);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp timestamp = resultSet.getTimestamp("lastSignOnDate");
                    return timestamp != null ? timestamp.toLocalDateTime() : null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateLastSignOnDate(String userId, int branchCode) {
        String updateQuery = "UPDATE users SET lastSignOnDate = ? WHERE userId = ? AND branchCode = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {

            preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            preparedStatement.setString(2, userId);
            preparedStatement.setInt(3, branchCode);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}