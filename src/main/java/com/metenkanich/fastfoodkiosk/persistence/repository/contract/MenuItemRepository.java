package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.MenuItem;
import java.util.List;
import java.util.UUID;

public interface MenuItemRepository {
    MenuItem findById(UUID id) throws EntityNotFoundException;
    MenuItem findByName(String name) throws EntityNotFoundException;
    List<MenuItem> findAll();
    List<MenuItem> findByCategory(UUID categoryId);
    MenuItem save(MenuItem menuItem);
    void deleteById(UUID id) throws EntityNotFoundException;
}
