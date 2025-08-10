package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentMethod;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.PaymentRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

public class PaymentRepositoryImpl implements PaymentRepository {
    private final DataSource dataSource;

    public PaymentRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Payment findById(UUID id) throws EntityNotFoundException {
        String query = "SELECT * FROM Payments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToPayment(resultSet);
                } else {
                    throw new EntityNotFoundException("Платіж з ID " + id + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку платежу з ID " + id, e);
        }
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM Payments ORDER BY created_at DESC";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                payments.add(mapToPayment(resultSet));
            }
        } catch (SQLException e) {
            System.err.println("Помилка при отриманні всіх платежів: " + e.getMessage());
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public Payment findByCartId(UUID cartId) throws EntityNotFoundException {
        String query = "SELECT * FROM Payments WHERE cart_id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, cartId, Types.OTHER);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapToPayment(resultSet);
                } else {
                    throw new EntityNotFoundException("Платіж для кошика з ID " + cartId + " не знайдено");
                }
            }
        } catch (SQLException e) {
            throw new EntityNotFoundException("Помилка під час пошуку платежу для кошика з ID " + cartId, e);
        }
    }

    @Override
    public Payment create(Payment payment) {
        String query = "INSERT INTO Payments (id, cart_id, payment_method, payment_status, created_at) VALUES (?, ?, ?, ?, ?)";
        System.out.println("DEBUG PaymentRepository: Створюємо платіж з query: " + query);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = UUID.randomUUID();
            System.out.println("DEBUG PaymentRepository: payment ID = " + id + ", cart_id = " + payment.cartId());
            preparedStatement.setObject(1, id, Types.OTHER);
            preparedStatement.setObject(2, payment.cartId(), Types.OTHER);
            preparedStatement.setString(3, payment.paymentMethod().name());
            preparedStatement.setString(4, payment.paymentStatus().name());
            preparedStatement.setObject(5, payment.createdAt());
            System.out.println("DEBUG PaymentRepository: Виконуємо executeUpdate()");
            preparedStatement.executeUpdate();
            System.out.println("DEBUG PaymentRepository: executeUpdate() виконано успішно");
            return new Payment(id, payment.cartId(), payment.paymentMethod(), payment.paymentStatus(), payment.createdAt());
        } catch (SQLException e) {
            System.err.println("DEBUG PaymentRepository: SQL Exception при створенні: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Payment update(Payment payment) throws EntityNotFoundException {
        if (payment.id() == null) {
            throw new EntityNotFoundException("ID платежу не може бути null для оновлення");
        }
        String query = "UPDATE Payments SET cart_id = ?, payment_method = ?, payment_status = ?, created_at = ? WHERE id = ?";
        System.out.println("DEBUG PaymentRepository: Оновлюємо платіж з query: " + query);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            System.out.println("DEBUG PaymentRepository: payment ID = " + payment.id() + ", cart_id = " + payment.cartId());
            preparedStatement.setObject(1, payment.cartId(), Types.OTHER);
            preparedStatement.setString(2, payment.paymentMethod().name());
            preparedStatement.setString(3, payment.paymentStatus().name());
            preparedStatement.setObject(4, payment.createdAt());
            preparedStatement.setObject(5, payment.id(), Types.OTHER);
            System.out.println("DEBUG PaymentRepository: Виконуємо executeUpdate()");
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Платіж з ID " + payment.id() + " не знайдено для оновлення");
            }
            System.out.println("DEBUG PaymentRepository: executeUpdate() виконано успішно");
            return payment;
        } catch (SQLException e) {
            System.err.println("DEBUG PaymentRepository: SQL Exception при оновленні: " + e.getMessage());
            e.printStackTrace();
            throw new EntityNotFoundException("Помилка під час оновлення платежу з ID " + payment.id(), e);
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM Payments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setObject(1, id, Types.OTHER);
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Платіж з ID " + id + " не знайдено");
            }
        } catch (SQLException e) {
            System.err.println("Помилка при видаленні платежу з ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            throw new EntityNotFoundException("Помилка під час видалення платежу з ID " + id, e);
        }
    }

    private Payment mapToPayment(ResultSet resultSet) throws SQLException {
        String createdAtString = resultSet.getString("created_at");
        LocalDateTime createdAt;

        try {
            if (createdAtString.contains("+")) {
                createdAtString = createdAtString.substring(0, createdAtString.indexOf("+"));
            }
            createdAt = LocalDateTime.parse(createdAtString.replace(" ", "T"));
        } catch (Exception e) {
            System.err.println("Помилка парсингу дати: " + createdAtString + ", використовуємо поточний час");
            createdAt = LocalDateTime.now();
        }

        return new Payment(
            UUID.fromString(resultSet.getString("id")),
            UUID.fromString(resultSet.getString("cart_id")),
            PaymentMethod.valueOf(resultSet.getString("payment_method")),
            PaymentStatus.valueOf(resultSet.getString("payment_status")),
            createdAt
        );
    }
}