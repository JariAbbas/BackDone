package org.acme.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.LoanApplication;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LoanApplicationRepository {

    @Inject
    AgroalDataSource dataSource;

    public List<LoanApplication> findLoansAwaitingCancellationAuthorization() throws SQLException {
        List<LoanApplication> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT la.* FROM LoanApplication la " +
                             "INNER JOIN CancelledLoan cl ON la.LoanNumber = cl.LoanNumber " +
                             "WHERE la.Status = 0 AND cl.Status = 1")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LoanApplication loan = new LoanApplication();
                    loan.setLoanId(rs.getInt("LoanID"));
                    loan.setLoanNumber(rs.getString("LoanNumber"));
                    loan.setCustomerNumber(rs.getString("CustomerNumber"));
                    loan.setGrantDate(rs.getDate("GrantDate"));
                    loan.setDocumentRefNo(rs.getString("DocumentRefNo"));
                    loan.setDealAmount(rs.getDouble("DealAmount"));
                    loan.setRemarks(rs.getString("Remarks"));
                    loan.setAccrual(rs.getDouble("Accrual"));
                    loan.setStatus(rs.getInt("Status"));
                    loan.setAuthorizedBy(rs.getString("Authorized_By"));
                    loan.setNoOfDays(rs.getInt("NoOfDays"));
                    loan.setApplicableRate(rs.getDouble("ApplicableRate"));
                    loan.setOdRate(rs.getDouble("ODRate"));
                    list.add(loan);
                }
            }
        }
        return list;
    }

    public List<LoanApplication> listAll() throws SQLException {
        List<LoanApplication> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM LoanApplication")) {
            while (rs.next()) {
                LoanApplication loan = new LoanApplication();
                loan.setLoanId(rs.getInt("LoanID"));
                loan.setLoanNumber(rs.getString("LoanNumber"));
                loan.setCustomerNumber(rs.getString("CustomerNumber"));
                loan.setGrantDate(rs.getDate("GrantDate"));
                loan.setDocumentRefNo(rs.getString("DocumentRefNo"));
                loan.setDealAmount(rs.getDouble("DealAmount"));
                loan.setRemarks(rs.getString("Remarks"));
                loan.setAccrual(rs.getDouble("Accrual"));
                loan.setStatus(rs.getInt("Status"));
                loan.setAuthorizedBy(rs.getString("Authorized_By"));
                loan.setNoOfDays(rs.getInt("NoOfDays"));
                loan.setApplicableRate(rs.getDouble("ApplicableRate"));
                loan.setOdRate(rs.getDouble("ODRate"));
                list.add(loan);
            }
        }
        return list;
    }

    public LoanApplication findById(int id) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM LoanApplication WHERE LoanID = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    LoanApplication loan = new LoanApplication();
                    loan.setLoanId(rs.getInt("LoanID"));
                    loan.setLoanNumber(rs.getString("LoanNumber"));
                    loan.setCustomerNumber(rs.getString("CustomerNumber"));
                    loan.setGrantDate(rs.getDate("GrantDate"));
                    loan.setDocumentRefNo(rs.getString("DocumentRefNo"));
                    loan.setDealAmount(rs.getDouble("DealAmount"));
                    loan.setRemarks(rs.getString("Remarks"));
                    loan.setAccrual(rs.getDouble("Accrual"));
                    loan.setStatus(rs.getInt("Status"));
                    loan.setAuthorizedBy(rs.getString("Authorized_By"));
                    loan.setNoOfDays(rs.getInt("NoOfDays"));
                    loan.setApplicableRate(rs.getDouble("ApplicableRate"));
                    loan.setOdRate(rs.getDouble("ODRate"));
                    return loan;
                }
            }
        }
        return null;
    }

    public void create(LoanApplication loan) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO LoanApplication (LoanNumber, CustomerNumber, GrantDate, DocumentRefNo, DealAmount, Remarks, Accrual, Status, Authorized_By, NoOfDays, ApplicableRate, ODRate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, loan.getLoanNumber());
            stmt.setString(2, loan.getCustomerNumber());
            stmt.setDate(3, loan.getGrantDate());
            stmt.setString(4, loan.getDocumentRefNo());
            stmt.setDouble(5, loan.getDealAmount());
            stmt.setString(6, loan.getRemarks());
            stmt.setDouble(7, loan.getAccrual());
            stmt.setInt(8, loan.getStatus());
            stmt.setString(9, loan.getAuthorizedBy());
            stmt.setInt(10, loan.getNoOfDays());
            stmt.setDouble(11, loan.getApplicableRate());
            stmt.setDouble(12, loan.getOdRate());
            stmt.executeUpdate();
        }
    }

    // New method to delete a loan application
    public void deleteLoanApplication(String loanNumber) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM LoanApplication WHERE LoanNumber = ?")) {
            stmt.setString(1, loanNumber);
            stmt.executeUpdate();
        }
    }

    public boolean updateByLoanNumber(String loanNumber, LoanApplication loan) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE LoanApplication SET CustomerNumber = ?, GrantDate = ?, DocumentRefNo = ?, DealAmount = ?, Remarks = ?, Accrual = ?, Status = ?, Authorized_By = ?, NoOfDays = ?, ApplicableRate = ?, ODRate = ? WHERE LoanNumber = ?")) {

            stmt.setString(1, loan.getCustomerNumber());
            stmt.setDate(2, loan.getGrantDate());
            stmt.setString(3, loan.getDocumentRefNo());
            stmt.setDouble(4, loan.getDealAmount());
            stmt.setString(5, loan.getRemarks());
            stmt.setDouble(6, loan.getAccrual());
            stmt.setInt(7, loan.getStatus());
            stmt.setString(8, loan.getAuthorizedBy());
            if (loan.getNoOfDays() != null) {
                stmt.setInt(9, loan.getNoOfDays());
            } else {
                stmt.setNull(9, java.sql.Types.INTEGER); // Set to NULL in the database
            }
            stmt.setDouble(10, loan.getApplicableRate());
            stmt.setDouble(11, loan.getOdRate());
            stmt.setString(12, loanNumber);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }
    public boolean update(LoanApplication loan) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE LoanApplication SET LoanNumber = ?, CustomerNumber = ?, GrantDate = ?, DocumentRefNo = ?, DealAmount = ?, Remarks = ?, Accrual = ?, Status = ?, Authorized_By = ?, NoOfDays = ?, ApplicableRate = ?, ODRate = ? WHERE LoanID = ?")) {

            stmt.setString(1, loan.getLoanNumber());
            stmt.setString(2, loan.getCustomerNumber());
            stmt.setDate(3, loan.getGrantDate());
            stmt.setString(4, loan.getDocumentRefNo());
            stmt.setDouble(5, loan.getDealAmount());
            stmt.setString(6, loan.getRemarks());
            stmt.setDouble(7, loan.getAccrual());
            stmt.setInt(8, loan.getStatus());
            stmt.setString(9, loan.getAuthorizedBy());
            stmt.setInt(10, loan.getNoOfDays());
            stmt.setDouble(11, loan.getApplicableRate());
            stmt.setDouble(12, loan.getOdRate());
            stmt.setInt(13, loan.getLoanId());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    public LoanApplication findByLoanNumber(String loanNumber) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM LoanApplication WHERE LoanNumber = ?")) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                LoanApplication loan = null;
                if (rs.next()) {
                    loan = new LoanApplication();
                    loan.setLoanId(rs.getInt("LoanID"));
                    loan.setLoanNumber(rs.getString("LoanNumber"));
                    loan.setCustomerNumber(rs.getString("CustomerNumber"));
                    loan.setGrantDate(rs.getDate("GrantDate"));
                    loan.setDocumentRefNo(rs.getString("DocumentRefNo"));
                    loan.setDealAmount(rs.getDouble("DealAmount"));
                    loan.setRemarks(rs.getString("Remarks"));
                    loan.setAccrual(rs.getDouble("Accrual"));
                    loan.setStatus(rs.getInt("Status"));
                    loan.setAuthorizedBy(rs.getString("Authorized_By"));
                    loan.setNoOfDays(rs.getInt("NoOfDays"));
                    loan.setApplicableRate(rs.getDouble("ApplicableRate"));
                    loan.setOdRate(rs.getDouble("ODRate"));
                }
                return loan;
            }
        }
    }

    public boolean cancelLoan(String loanNumber) throws SQLException {
        Connection conn = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement selectStmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            updateStmt = conn.prepareStatement("UPDATE LoanApplication SET Status = 0 WHERE LoanNumber = ? AND Status = 1");
            updateStmt.setString(1, loanNumber);
            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated > 0) {
                selectStmt = conn.prepareStatement("SELECT * FROM LoanApplication WHERE LoanNumber = ?");
                selectStmt.setString(1, loanNumber);
                rs = selectStmt.executeQuery();
                if(rs.next()){
                    insertStmt = conn.prepareStatement("INSERT INTO CancelledLoan (LoanNumber, CustomerNumber, GrantDate, DocumentRefNo, DealAmount, Remarks, Accrual, Status, Authorized_By, OriginalLoanId, NoOfDays, ApplicableRate, ODRate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    insertStmt.setString(1, rs.getString("LoanNumber"));
                    insertStmt.setString(2, rs.getString("CustomerNumber"));
                    insertStmt.setDate(3, rs.getDate("GrantDate"));
                    insertStmt.setString(4, rs.getString("DocumentRefNo"));
                    insertStmt.setDouble(5, rs.getDouble("DealAmount"));
                    insertStmt.setString(6, rs.getString("Remarks"));
                    insertStmt.setDouble(7, rs.getDouble("Accrual"));
                    insertStmt.setInt(8, 1); // Set status to 1 in CancelledLoan
                    insertStmt.setString(9, rs.getString("Authorized_By"));
                    insertStmt.setInt(10, rs.getInt("LoanID"));
                    insertStmt.setInt(11, rs.getInt("NoOfDays")); // Set NoOfDays
                    insertStmt.setDouble(12, rs.getDouble("ApplicableRate")); // Set ApplicableRate
                    insertStmt.setDouble(13, rs.getDouble("ODRate"));
                    insertStmt.executeUpdate();
                }
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (selectStmt != null) try { selectStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (insertStmt != null) try { insertStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (updateStmt != null) try { updateStmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public int getCancelledLoanStatus(String loanNumber) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT Status FROM CancelledLoan WHERE LoanNumber = ?")) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Status");
                }
                return 0;
            }
        }
    }

    public void updateCancelledLoanStatus(String loanNumber, int status) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE CancelledLoan SET Status = ? WHERE LoanNumber = ?")) {
            stmt.setInt(1, status);
            stmt.setString(2, loanNumber);
            stmt.executeUpdate();
        }
    }

    public void updateLoanApplicationStatus(String loanNumber, int status) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE LoanApplication SET Status = ? WHERE LoanNumber = ?")) {
            stmt.setInt(1, status);
            stmt.setString(2, loanNumber);
            stmt.executeUpdate();
        }
    }

    public void updateStatusByLoanNumber(String loanNumber, int status) throws SQLException {
        String sql = "UPDATE LoanApplication SET Status = ? WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, status);
            stmt.setString(2, loanNumber);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("Failed to update status for Loan Number: " + loanNumber);
            }
        }
    }




    public Integer getStatusByLoanNumber(String loanNumber) throws SQLException {
        String sql = "SELECT Status FROM LoanApplication WHERE LoanNumber = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Status");
                }
            }
        }
        return null; // Loan not found
    }

    public boolean checkCancelledLoanExists(String loanNumber) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM CancelledLoan WHERE LoanNumber = ?")) {
            stmt.setString(1, loanNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }




}