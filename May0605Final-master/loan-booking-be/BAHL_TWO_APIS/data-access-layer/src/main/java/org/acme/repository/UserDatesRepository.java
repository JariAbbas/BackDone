package org.acme.repository;

import io.agroal.api.AgroalDataSource; // ✅ Correct Agroal DataSource
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.sql.*;
import java.time.LocalDateTime;
import org.acme.modelDAL.UserDates;

@ApplicationScoped
public class UserDatesRepository {

    @Inject
    AgroalDataSource dataSource; // ✅ Corrected Injection

    public void saveUserDates(UserDates userDates) {
        String insertQuery = "INSERT INTO UserDates (userId, branchCode, branchName, currentDate, previousDate) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            preparedStatement.setString(1, userDates.getUserId());
            preparedStatement.setInt(2, userDates.getBranchCode());
            preparedStatement.setString(3, userDates.getBranchName());
            preparedStatement.setTimestamp(4, Timestamp.valueOf(userDates.getCurrentDate()));
            preparedStatement.setTimestamp(5, userDates.getPreviousDate() != null ? Timestamp.valueOf(userDates.getPreviousDate()) : null);

            preparedStatement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LocalDateTime getPreviousDateForUser(String userId) {
        String selectQuery = "SELECT currentDate FROM UserDates WHERE userId = ? ORDER BY currentDate DESC FETCH FIRST 1 ROWS ONLY"; // ✅ DB2 ke liye LIMIT ki jagah FETCH FIRST

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {

            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getTimestamp("currentDate").toLocalDateTime();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UserDates findLatestUserDate() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM UserDates ORDER BY currentDate DESC FETCH FIRST 1 ROW ONLY")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDates userDate = new UserDates();
                    userDate.setId(rs.getInt("id"));
                    userDate.setUserId(rs.getString("userId"));
                    userDate.setBranchCode(rs.getInt("branchCode"));
                    userDate.setBranchName(rs.getString("branchName"));
                    userDate.setCurrentDate(rs.getTimestamp("currentDate").toLocalDateTime());
                    userDate.setPreviousDate(rs.getTimestamp("previousDate") != null ? rs.getTimestamp("previousDate").toLocalDateTime() : null);
                    return userDate;
                }
            }
        }
        return null;
    }

    public LocalDateTime findLatestGrantDate() throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT currentDate FROM UserDates ORDER BY id DESC FETCH FIRST 1 ROW ONLY");
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getTimestamp("currentDate").toLocalDateTime();
        }
        return null;
    }

    public UserDates findLatest() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM UserDates ORDER BY currentDate DESC FETCH FIRST 1 ROW ONLY")) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDates userDate = new UserDates();
                    userDate.setId(rs.getInt("id"));
                    userDate.setUserId(rs.getString("userId"));
                    userDate.setBranchCode(rs.getInt("branchCode"));
                    userDate.setBranchName(rs.getString("branchName"));
                    userDate.setCurrentDate(rs.getTimestamp("currentDate").toLocalDateTime());
                    userDate.setPreviousDate(rs.getTimestamp("previousDate") != null ? rs.getTimestamp("previousDate").toLocalDateTime() : null);
                    return userDate;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error appropriately
        }
        return null;
    }

    public UserDates findLatestUserDateByUserId(String userId) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM UserDates WHERE userId = ? ORDER BY currentDate DESC FETCH FIRST 1 ROW ONLY")) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDates userDate = new UserDates();
                    userDate.setId(rs.getInt("id"));
                    userDate.setUserId(rs.getString("userId"));
                    userDate.setBranchCode(rs.getInt("branchCode"));
                    userDate.setBranchName(rs.getString("branchName"));
                    userDate.setCurrentDate(rs.getTimestamp("currentDate").toLocalDateTime());
                    userDate.setPreviousDate(rs.getTimestamp("previousDate") != null ? rs.getTimestamp("previousDate").toLocalDateTime() : null);
                    return userDate;
                }
            }
        }
        return null;
    }
}
