package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Cart;
import java.util.List;
import java.util.UUID;
public interface CartRepository {
    List<Cart> findAll();
    Cart findById(UUID orderItemId) throws EntityNotFoundException;
    List<Cart> findByUserId(UUID userId);
    List<Cart> findByCategoryId(UUID categoryId);
    Cart create(Cart cartItem);
    void deleteById(UUID orderItemId) throws EntityNotFoundException;
}
