package com.example.orderservice.service;

import java.util.List;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.OrderSerializableDto;
import com.example.orderservice.dto.ordercreation.OrderSerializableEventDto;
import com.example.orderservice.entity.Order;
import com.example.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrderService {

	private KafkaTemplate<String, String> kafkaTemplate;
	private final OrderRepository orderRepository;
	private ObjectMapper objectMapper;

	public OrderService(KafkaTemplate<String, String> kafkaTemplate, OrderRepository orderRepository, ObjectMapper objectMapper) {
		this.orderRepository = orderRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "order-events", groupId = "order-service")
	public void handleOrderCreationEvent(String orderCreationMessage) {
		try {
			OrderSerializableEventDto orderCreationRequestDto = objectMapper.readValue(orderCreationMessage, OrderSerializableEventDto.class);
			if ("ORDER_CREATE".equals(orderCreationRequestDto.getCreationStatus())) {
				Order order = new Order();
				order.setSagaId(orderCreationRequestDto.getSagaId());
				order.setProductId(orderCreationRequestDto.getProductId());
				order.setQuantity(orderCreationRequestDto.getQuantity());
				Order newOrder = placeOrder(order); // use sagaId inside the event
				
				// Send order successful order creation message to Kafka
				OrderSerializableEventDto orderCreationResponseDto = new OrderSerializableEventDto(newOrder.getSagaId(),
						newOrder.getProductId(), newOrder.getQuantity(), "ORDER_CREATED");
				kafkaTemplate.send("order-events", newOrder.getSagaId(), objectMapper.writeValueAsString(orderCreationResponseDto));
			} else if ("ORDER_CANCEL".equals(orderCreationRequestDto.getCreationStatus())) {
				//Cancel order
				Order cancelOrder = cancelOrder(orderCreationRequestDto.getSagaId(), orderCreationRequestDto.getProductId());
				
				// Send order successful cancellation order message to Kafka
				OrderSerializableEventDto orderCancellationResponseDto = new OrderSerializableEventDto(cancelOrder.getSagaId(),
						cancelOrder.getProductId(), cancelOrder.getQuantity(), "ORDER_CREATED");
				kafkaTemplate.send("order-events", cancelOrder.getSagaId(), objectMapper.writeValueAsString(orderCancellationResponseDto));
			}
		} catch (Exception exception) {
			// Send order fail order creation message to Kafka
			OrderSerializableEventDto orderCreationRequestDto = objectMapper.convertValue(orderCreationMessage, OrderSerializableEventDto.class);
			OrderSerializableEventDto orderCreationResponseDto = new OrderSerializableEventDto(
					orderCreationRequestDto.getSagaId(), orderCreationRequestDto.getProductId(),
					orderCreationRequestDto.getQuantity(), "ORDER_CREATION_FAILED");
			try {
				kafkaTemplate.send("order-events", orderCreationRequestDto.getSagaId(), objectMapper.writeValueAsString(orderCreationResponseDto));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
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
