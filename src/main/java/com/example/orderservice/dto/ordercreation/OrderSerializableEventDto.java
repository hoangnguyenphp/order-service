package com.example.orderservice.dto.ordercreation;

import com.example.orderservice.dto.OrderSerializableDto;

public class OrderSerializableEventDto extends OrderSerializableDto {
    private String sagaId;
    private String productId;
    private int quantity;
    private String creationStatus; // e.g. ORDER_CREATE, ORDER_CREATED, ORDER_CREATION_FAILED, ORDER_CANCEL, ORDER_CANCELLED, ORDER_CANCELLATION_FAILED

    public OrderSerializableEventDto() {
    }

    public OrderSerializableEventDto(String sagaId, String productId, int quantity, String creationStatus) {
        this.sagaId = sagaId;
        this.productId = productId;
        this.quantity = quantity;
        this.creationStatus = creationStatus;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }



    public String getCreationStatus() {
		return creationStatus;
	}

	public void setCreationStatus(String creationStatus) {
		this.creationStatus = creationStatus;
	}

	@Override
    public String toString() {
        return "OrderCreationResponseDto{" +
                "sagaId='" + sagaId + '\'' +
                ", productId='" + productId + '\'' +
                ", quantity=" + quantity +
                ", creationStatus='" + creationStatus + '\'' +
                '}';
    }
}
