package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository {
    Payment findById(UUID id) throws EntityNotFoundException;
    List<Payment> findAll();
    Payment findByCartId(UUID cartId) throws EntityNotFoundException;
    Payment create(Payment payment);
    Payment update(Payment payment) throws EntityNotFoundException;
    void deleteById(UUID id) throws EntityNotFoundException;
}