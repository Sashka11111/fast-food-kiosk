package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Order;
import java.util.List;
import java.util.UUID;

public interface OrderRepository {
  Order findById(UUID id) throws EntityNotFoundException;
  List<Order> findAll();
  Order save(Order order);
  void deleteById(UUID id) throws EntityNotFoundException;
}