package com.metenkanich.fastfoodkiosk.persistence.repository.impl;

import com.metenkanich.fastfoodkiosk.domain.exception.EntityNotFoundException;
import com.metenkanich.fastfoodkiosk.persistence.entity.Payment;
import com.metenkanich.fastfoodkiosk.persistence.entity.enums.PaymentStatus;
import com.metenkanich.fastfoodkiosk.persistence.repository.contract.PaymentRepository;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            preparedStatement.setString(1, id.toString());
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
        String query = "SELECT * FROM Payments";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                payments.add(mapToPayment(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }

    @Override
    public Payment save(Payment payment) {
        String query = payment.id() == null
            ? "INSERT INTO Payments (id, order_id, amount, payment_method, payment_status, created_at) VALUES (?, ?, ?, ?, ?, ?)"
            : "UPDATE Payments SET order_id = ?, amount = ?, payment_method = ?, payment_status = ?, created_at = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            UUID id = payment.id() == null ? UUID.randomUUID() : payment.id();
            int index = 1;
            if (payment.id() == null) {
                preparedStatement.setString(index++, id.toString());
            }
            preparedStatement.setString(index++, payment.orderId().toString());
            preparedStatement.setBigDecimal(index++, payment.amount());
            preparedStatement.setString(index++, payment.paymentMethod());
            preparedStatement.setString(index++, payment.paymentStatus().name());
            preparedStatement.setObject(index++, payment.createdAt());
            if (payment.id() != null) {
                preparedStatement.setString(index, id.toString());
            }
            preparedStatement.executeUpdate();
            return new Payment(id, payment.orderId(), payment.amount(), payment.paymentMethod(), payment.paymentStatus(), payment.createdAt());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(UUID id) throws EntityNotFoundException {
        String query = "DELETE FROM Payments WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, id.toString());
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new EntityNotFoundException("Платіж з ID " + id + " не знайдено");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Payment mapToPayment(ResultSet resultSet) throws SQLException {
        return new Payment(
            UUID.fromString(resultSet.getString("id")),
            UUID.fromString(resultSet.getString("order_id")),
            resultSet.getBigDecimal("amount"),
            resultSet.getString("payment_method"),
            PaymentStatus.valueOf(resultSet.getString("payment_status")),
            resultSet.getObject("created_at", LocalDateTime.class)
        );
    }
}