package com.metenkanich.fastfoodkiosk.persistence.repository.contract;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.User;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.Role;
import java.util.List;
import java.util.UUID;

public interface UserRepository {
  User findById(UUID id) throws EntityNotFoundException;
  User findByUsername(String username) throws EntityNotFoundException;
  List<User> findAll();
  void addUser(User user);
  void updateUser(User user) throws EntityNotFoundException;
  void updateUserRole(String username, Role newRole) throws EntityNotFoundException;
  void deleteUser(String username) throws EntityNotFoundException;
}
