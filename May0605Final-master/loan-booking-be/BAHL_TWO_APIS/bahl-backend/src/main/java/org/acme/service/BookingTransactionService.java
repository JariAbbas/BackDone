package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.BookingTransaction;
import org.acme.repository.BookingTransactionRecordRepository;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class BookingTransactionService {

    @Inject
    BookingTransactionRecordRepository bookingTransactionRecordRepository;

    // Remove getTransactionsByLoanNumberRange and getTransactionsByLoanNumber methods

    public List<BookingTransaction> getTransactionsByVoucherNumber(String voucherNumber) throws SQLException {
        return bookingTransactionRecordRepository.findByVoucherNumber(voucherNumber);
    }
}