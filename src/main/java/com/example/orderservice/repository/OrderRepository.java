package com.example.orderservice.repository;

import com.example.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find all orders for a specific saga
    List<Order> findBySagaId(String sagaId);

    // Find specific order by sagaId (useful for rollback)
    Optional<Order> findBySagaIdAndProductId(String sagaId, String productId);

    // Find all orders with specific status
    List<Order> findByStatus(String status);
}
