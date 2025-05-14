package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.client.UserDatesClient; // Import UserDatesClient
import org.acme.model.RepaymentDetails;
import org.acme.repository.RepaymentDetailsRepository;
import org.eclipse.microprofile.rest.client.inject.RestClient;
// import org.acme.repository.UserDatesRepository; // Remove UserDatesRepository import

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@ApplicationScoped
public class RepaymentDetailsService {

    @Inject
    RepaymentDetailsRepository repaymentDetailsRepository;

    @Inject
    @RestClient
    UserDatesClient userDatesClient; // Inject UserDatesClient

    public RepaymentDetails findByLoanNumber(String loanNumber) throws Exception {
        return repaymentDetailsRepository.findByLoanNumber(loanNumber);
    }

    public void create(RepaymentDetails repayment) throws Exception {
        // Fetch the latest currentDate (GRANTDATE) using UserDatesClient
        LocalDateTime grantDateTime = userDatesClient.getLatestCurrentDate();
        LocalDate grantDate;
        if (grantDateTime != null) {
            grantDate = grantDateTime.toLocalDate();
        } else {
            grantDate = LocalDate.now(); // Fallback to server date if not found
        }

        LocalDate maturityLocalDate = grantDate.plusDays(repayment.getNoOfDays());
        Date maturityDate = Date.valueOf(maturityLocalDate);
        repayment.setMaturityDate(maturityDate);
        repaymentDetailsRepository.create(repayment);
    }

    // New method to update only noOfDays and recalculate maturityDate
    public RepaymentDetails updateNoOfDays(String loanNumber, Integer noOfDays) throws Exception {
        RepaymentDetails existingRepayment = repaymentDetailsRepository.findByLoanNumber(loanNumber);
        if (existingRepayment == null) {
            return null;
        }

        // Fetch the latest currentDate (GRANTDATE) using UserDatesClient
        LocalDateTime grantDateTime = userDatesClient.getLatestCurrentDate();
        LocalDate grantDate;
        if (grantDateTime != null) {
            grantDate = grantDateTime.toLocalDate();
        } else {
            grantDate = LocalDate.now(); // Fallback to server date if not found
        }

        LocalDate maturityLocalDate = grantDate.plusDays(noOfDays);
        Date maturityDate = Date.valueOf(maturityLocalDate);

        boolean updated = repaymentDetailsRepository.updateNoOfDays(loanNumber, noOfDays, maturityDate);

        if (updated) {
            return repaymentDetailsRepository.findByLoanNumber(loanNumber);
        }
        return null;
    }

    public void update(String loanNumber, RepaymentDetails repayment) throws Exception {
        // Fetch the latest currentDate (GRANTDATE) using UserDatesClient
        LocalDateTime grantDateTime = userDatesClient.getLatestCurrentDate();
        LocalDate grantDate;
        if (grantDateTime != null) {
            grantDate = grantDateTime.toLocalDate();
        } else {
            grantDate = LocalDate.now(); // Fallback to server date if not found
        }

        LocalDate maturityLocalDate = grantDate.plusDays(repayment.getNoOfDays());
        Date maturityDate = Date.valueOf(maturityLocalDate);
        repayment.setMaturityDate(maturityDate);
        repaymentDetailsRepository.update(loanNumber, repayment);
    }

    public void delete(String loanNumber) throws Exception {
        repaymentDetailsRepository.delete(loanNumber);
    }
}