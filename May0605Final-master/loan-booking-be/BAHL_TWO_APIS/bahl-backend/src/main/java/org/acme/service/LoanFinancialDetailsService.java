package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.LoanApplication;
import org.acme.model.LoanFinancialDetails;
import org.acme.model.Markup;
import org.acme.repository.LoanApplicationRepository;
import org.acme.repository.LoanFinancialDetailsRepository;
import org.acme.repository.MarkupRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@ApplicationScoped
public class LoanFinancialDetailsService {

    @Inject
    LoanFinancialDetailsRepository loanFinancialDetailsRepository;

    @Inject
    LoanApplicationRepository loanApplicationRepository;

    @Inject
    MarkupRepository markupRepository;

    @Transactional
    public Map<String, Object> create(String loanNumber, int noOfDays) throws SQLException {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanNumber(loanNumber);
        Optional<Markup> markupOptional = markupRepository.findByLoanNumber(loanNumber);

        if (loanApplication == null) {
            throw new SQLException("Loan application not found for LoanNumber: " + loanNumber);
        }

        if (markupOptional.isEmpty()) {
            throw new SQLException("Markup details not found for LoanNumber: " + loanNumber);
        }

        Markup markup = markupOptional.get();
        double loanAmount = loanApplication.getDealAmount();
        double applicableRate = markup.getApplicableRate().doubleValue();

        // Calculation for Profit Amount
        double profitAmount = (loanAmount * applicableRate / 36500) * noOfDays;

        // Calculation for Receivable Amount at Maturity
        double receivableAmountAtMaturity = loanAmount + profitAmount;

        LoanFinancialDetails financialDetails = new LoanFinancialDetails(loanNumber);
        financialDetails.setProfitAmount(profitAmount);
        financialDetails.setReceivableAmountAtMaturity(receivableAmountAtMaturity);

        loanFinancialDetailsRepository.create(financialDetails);

        // Return the calculated values
        return Map.of(
                "message", "Loan financial details stored successfully.",
                "profitAmount", String.format("%.2f", profitAmount),
                "receivableAmountAtMaturity", String.format("%.2f", receivableAmountAtMaturity)
        );
    }

    @Transactional
    public Map<String, Object> update(String loanNumber, int noOfDays) throws SQLException {
        LoanApplication loanApplication = loanApplicationRepository.findByLoanNumber(loanNumber);
        Optional<Markup> markupOptional = markupRepository.findByLoanNumber(loanNumber);
        LoanFinancialDetails existingFinancialDetails = loanFinancialDetailsRepository.findByLoanNumber(loanNumber);

        if (loanApplication == null || markupOptional.isEmpty() || existingFinancialDetails == null) {
            return null; // Or throw an exception
        }

        Markup markup = markupOptional.get();
        double loanAmount = loanApplication.getDealAmount();
        double applicableRate = markup.getApplicableRate().doubleValue();

        // Recalculate Profit Amount based on the new noOfDays
        double profitAmount = (loanAmount * applicableRate / 36500) * noOfDays;

        // Recalculate Receivable Amount at Maturity
        double receivableAmountAtMaturity = loanAmount + profitAmount;

        existingFinancialDetails.setProfitAmount(profitAmount);
        existingFinancialDetails.setReceivableAmountAtMaturity(receivableAmountAtMaturity);

        loanFinancialDetailsRepository.update(existingFinancialDetails);

        return Map.of(
                "message", "Loan financial details updated successfully.",
                "profitAmount", String.format("%.2f", profitAmount),
                "receivableAmountAtMaturity", String.format("%.2f", receivableAmountAtMaturity)
        );
    }

    public Map<String, Object> findByLoanNumber(String loanNumber) throws SQLException {
        LoanFinancialDetails financialDetails = loanFinancialDetailsRepository.findByLoanNumber(loanNumber);
        if (financialDetails != null) {
            return Map.of(
                    "loanNumber", financialDetails.getLoanNumber(),
                    "profitAmount", String.format("%.2f", financialDetails.getProfitAmount()),
                    "receivableAmountAtMaturity", String.format("%.2f", financialDetails.getReceivableAmountAtMaturity())
            );
        }
        return null;
    }
}