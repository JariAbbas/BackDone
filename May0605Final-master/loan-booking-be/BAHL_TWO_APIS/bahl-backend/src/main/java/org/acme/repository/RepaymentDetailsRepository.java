package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.RepaymentDetails;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@ApplicationScoped
public class RepaymentDetailsRepository {

    @Inject
    AgroalDataSource dataSource;

    public RepaymentDetails findByLoanNumber(String loanNumber) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM REPAYMENTDETAILS WHERE LoanNumber = ?");
        stmt.setString(1, loanNumber);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            RepaymentDetails repayment = new RepaymentDetails();
            repayment.setLoanNumber(rs.getString("LoanNumber"));
            repayment.setNoOfDays(rs.getInt("NoOfDays"));
            repayment.setMaturityDate(rs.getDate("MaturityDate"));
            return repayment;
        }
        return null;
    }

    public void create(RepaymentDetails repayment) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO REPAYMENTDETAILS (LoanNumber, NoOfDays, MaturityDate) VALUES (?, ?, ?)");
        stmt.setString(1, repayment.getLoanNumber());
        stmt.setInt(2, repayment.getNoOfDays());
        stmt.setDate(3, repayment.getMaturityDate());
        stmt.executeUpdate();
    }

    // New method to update only NoOfDays and MaturityDate
    public boolean updateNoOfDays(String loanNumber, Integer noOfDays, Date maturityDate) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE REPAYMENTDETAILS SET NoOfDays = ?, MaturityDate = ? WHERE LoanNumber = ?");
        stmt.setInt(1, noOfDays);
        stmt.setDate(2, maturityDate);
        stmt.setString(3, loanNumber);
        int rowsUpdated = stmt.executeUpdate();
        return rowsUpdated > 0;
    }

    public void update(String loanNumber, RepaymentDetails repayment) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("UPDATE REPAYMENTDETAILS SET NoOfDays = ?, MaturityDate = ? WHERE LoanNumber = ?");
        stmt.setInt(1, repayment.getNoOfDays());
        stmt.setDate(2, repayment.getMaturityDate());
        stmt.setString(3, loanNumber);
        stmt.executeUpdate();
    }

    public void delete(String loanNumber) throws SQLException {
        Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM REPAYMENTDETAILS WHERE LoanNumber = ?");
        stmt.setString(1, loanNumber);
        stmt.executeUpdate();
    }
}