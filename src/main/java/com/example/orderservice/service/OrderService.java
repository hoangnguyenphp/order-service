package com.example.orderservice.service;

import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Save a new order (Saga will pass sagaId)
    public Order placeOrder(Order order) {
        return orderRepository.save(order);
    }

    // Fetch all orders for a given saga
    public List<Order> getOrdersBySagaId(String sagaId) {
        return orderRepository.findBySagaId(sagaId);
    }

    // Cancel order by sagaId + productId (for compensation)
    public Order cancelOrder(String sagaId, String productId) {
        Optional<Order> optionalOrder = orderRepository.findBySagaIdAndProductId(sagaId, productId);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            if (!"CANCELLED".equals(order.getStatus())) {
                order.setStatus("CANCELLED");
                return orderRepository.save(order);
            } else {
                return order; // already cancelled
            }
        } else {
            throw new IllegalArgumentException("Order not found for sagaId: " + sagaId);
        }
    }
}
