package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.BaseRate;
import org.acme.model.Markup;
import org.acme.repository.BaseRateRepository;
import org.acme.repository.MarkupRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class MarkupService {

    @Inject
    MarkupRepository markupRepository;

    @Inject
    BaseRateRepository baseRateRepository;

    public List<Markup> getAllMarkups() throws SQLException {
        return markupRepository.listAll();
    }

    public Optional<Markup> getMarkupByLoanNumber(String loanNumber) throws SQLException {
        return markupRepository.findByLoanNumber(loanNumber);
    }

    public Optional<Markup> getMarkupById(Long id) throws SQLException {
        return markupRepository.findById(id);
    }

    @Transactional
    public void createMarkup(String loanNumber, Long baseRateId, BigDecimal spreadRate, BigDecimal odRate, BigDecimal fixedRate) throws SQLException {
        Optional<BaseRate> baseRateOptional = baseRateRepository.findById(baseRateId);
        if (baseRateOptional.isEmpty()) {
            throw new RuntimeException("Base rate with ID " + baseRateId + " not found.");
        }
        BigDecimal baseRateValue;

        if (baseRateOptional.get().getBaseName().equals("FIXED Rate")) {
            if (fixedRate == null) {
                throw new RuntimeException("Fixed rate value must be provided for 'FIXED Rate'.");
            }
            baseRateValue = fixedRate.setScale(4, BigDecimal.ROUND_HALF_UP);
        } else {
            baseRateValue = baseRateOptional.get().getBaseRate();
        }

        BigDecimal applicableRate = baseRateValue.add(spreadRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (odRate.compareTo(applicableRate) < 0) {
            throw new RuntimeException("OD Rate cannot be less than the Applicable Rate of " + applicableRate);
        }
        markupRepository.create(loanNumber, baseRateId, spreadRate.setScale(2, BigDecimal.ROUND_HALF_UP), applicableRate, odRate.setScale(2, BigDecimal.ROUND_HALF_UP));

        // We no longer reset the FIXED Rate in the BaseRateTable.
    }

    @Transactional
    public void updateMarkup(Long id, String loanNumber, BigDecimal spreadRate, BigDecimal odRate) throws SQLException {
        Optional<Markup> existingMarkupOptional = markupRepository.findById(id);
        if (existingMarkupOptional.isEmpty()) {
            throw new RuntimeException("Markup with ID " + id + " not found.");
        }
        Markup existingMarkup = existingMarkupOptional.get();
        Optional<BaseRate> baseRateOptional = baseRateRepository.findById(existingMarkup.getBaseRateId());
        if (baseRateOptional.isEmpty()) {
            throw new RuntimeException("Base rate with ID " + existingMarkup.getBaseRateId() + " not found.");
        }
        BigDecimal baseRateValue = baseRateOptional.get().getBaseRate();
        BigDecimal applicableRate = baseRateValue.add(spreadRate).setScale(2, BigDecimal.ROUND_HALF_UP);

        if (odRate.compareTo(applicableRate) < 0) {
            throw new RuntimeException("OD Rate cannot be less than the Applicable Rate of " + applicableRate);
        }

        markupRepository.update(id, loanNumber, spreadRate.setScale(2, BigDecimal.ROUND_HALF_UP), applicableRate, odRate.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Transactional
    public void deleteMarkup(Long id) throws SQLException {
        markupRepository.delete(id);
    }
}