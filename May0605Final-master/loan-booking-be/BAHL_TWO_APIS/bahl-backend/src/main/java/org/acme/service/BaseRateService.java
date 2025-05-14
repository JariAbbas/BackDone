package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.BaseRate;
import org.acme.repository.BaseRateRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BaseRateService {

    @Inject
    BaseRateRepository baseRateRepository;

    public List<BaseRate> getAllBaseRates() throws SQLException {
        return baseRateRepository.listAll();
    }

    public Optional<BaseRate> getBaseRateByName(String baseName) throws SQLException {
        return baseRateRepository.findByBaseName(baseName);
    }

    @Transactional
    public void createBaseRate(String baseName, BigDecimal baseRate) throws SQLException {
        if (baseRateRepository.findByBaseName(baseName).isPresent()) {
            throw new RuntimeException("Base rate with name " + baseName + " already exists.");
        }
        baseRateRepository.create(baseName, baseRate);
    }

    @Transactional
    public void updateBaseRate(String baseName, BigDecimal newBaseRate) throws SQLException {
        if (baseRateRepository.findByBaseName(baseName).isEmpty()) {
            throw new RuntimeException("Base rate with name " + baseName + " not found.");
        }
        baseRateRepository.update(baseName, newBaseRate);
    }

    @Transactional
    public void deleteBaseRate(Long id) throws SQLException {
        baseRateRepository.delete(id);
    }
}