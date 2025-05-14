// org.acme.repository.GLAccountRepository.java
package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.GLAccountCustomerNumbers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GLAccountRepository {

    @Inject
    AgroalDataSource dataSource;

    public GLAccountCustomerNumbers getCustomerNumbers() {
        String sql = "SELECT DISTINCT CustomerNumber FROM GLAccountsTable LIMIT 2";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            String customerNumber1 = null;
            String customerNumber2 = null;

            if (rs.next()) {
                customerNumber1 = rs.getString("CustomerNumber");
            }
            if (rs.next()) {
                customerNumber2 = rs.getString("CustomerNumber");
            }

            return new GLAccountCustomerNumbers(customerNumber1, customerNumber2);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}