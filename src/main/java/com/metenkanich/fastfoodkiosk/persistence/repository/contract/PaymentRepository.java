package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository {
    Payment findById(UUID id) throws EntityNotFoundException;
    List<Payment> findAll();
    Payment save(Payment payment);
    void deleteById(UUID id) throws EntityNotFoundException;
}