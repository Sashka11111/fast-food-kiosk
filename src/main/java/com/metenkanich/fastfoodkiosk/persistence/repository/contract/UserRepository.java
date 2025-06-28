package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserRepository {
  User findById(UUID id) throws EntityNotFoundException;
  User findByUsername(String username) throws EntityNotFoundException;
  List<User> findAll();
  User save(User user);
  void deleteById(UUID id) throws EntityNotFoundException;
}