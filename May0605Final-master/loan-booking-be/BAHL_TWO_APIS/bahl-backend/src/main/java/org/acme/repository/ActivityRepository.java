package org.acme.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.agroal.api.AgroalDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ActivityRepository {

    @Inject
    AgroalDataSource dataSource;

    public List<String> getActivitiesByUserId(String userId) {
        List<String> activities = new ArrayList<>();
        String query = "SELECT activity FROM activities WHERE userId = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(resultSet.getString("activity"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activities;
    }
}