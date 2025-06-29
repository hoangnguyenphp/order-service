package com.example.orderservice.controller;

import com.example.orderservice.entity.Order;
import com.example.orderservice.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 🎯 Create a new order
    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody Order order) {
        Order created = orderService.placeOrder(order);
        return ResponseEntity.ok(created);
    }

    // 🔄 Cancel an order (for rollback)
    @PutMapping("/cancel")
    public ResponseEntity<?> cancelOrder(
            @RequestParam String sagaId,
            @RequestParam String productId
    ) {
        try {
            Order cancelled = orderService.cancelOrder(sagaId, productId);
            return ResponseEntity.ok(Map.of(
                "message", "Order cancelled successfully",
                "orderId", cancelled.getId()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    // 🧪 Optional: Get all orders for a sagaId (debug/logging)
    @GetMapping("/saga/{sagaId}")
    public ResponseEntity<List<Order>> getOrdersBySagaId(@PathVariable String sagaId) {
        return ResponseEntity.ok(orderService.getOrdersBySagaId(sagaId));
    }
}
