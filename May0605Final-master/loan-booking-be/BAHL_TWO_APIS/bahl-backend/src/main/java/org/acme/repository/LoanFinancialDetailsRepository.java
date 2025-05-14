package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.LoanFinancialDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class LoanFinancialDetailsRepository {

    @Inject
    AgroalDataSource dataSource;

    public void create(LoanFinancialDetails financialDetails) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO LoanFinancialDetails (LoanNumber, ProfitAmount, ReceivableAmountAtMaturity) VALUES (?, ?, ?)");
        stmt.setString(1, financialDetails.getLoanNumber());
        stmt.setDouble(2, financialDetails.getProfitAmount());
        stmt.setDouble(3, financialDetails.getReceivableAmountAtMaturity());
        stmt.executeUpdate();
    }

    public void update(LoanFinancialDetails financialDetails) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE LoanFinancialDetails SET ProfitAmount = ?, ReceivableAmountAtMaturity = ? WHERE LoanNumber = ?");
        stmt.setDouble(1, financialDetails.getProfitAmount());
        stmt.setDouble(2, financialDetails.getReceivableAmountAtMaturity());
        stmt.setString(3, financialDetails.getLoanNumber());
        stmt.executeUpdate();
    }

    public LoanFinancialDetails findByLoanNumber(String loanNumber) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT LoanNumber, ProfitAmount, ReceivableAmountAtMaturity FROM LoanFinancialDetails WHERE LoanNumber = ?");
        stmt.setString(1, loanNumber);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            LoanFinancialDetails financialDetails = new LoanFinancialDetails();
            financialDetails.setLoanNumber(rs.getString("LoanNumber"));
            financialDetails.setProfitAmount(rs.getDouble("ProfitAmount"));
            financialDetails.setReceivableAmountAtMaturity(rs.getDouble("ReceivableAmountAtMaturity"));
            return financialDetails;
        }
        return null;
    }
}