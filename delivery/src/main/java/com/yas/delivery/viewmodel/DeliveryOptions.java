package com.yas.delivery.viewmodel;

public record DeliveryOptions(String deliveryProviderId,
                              String deliveryProviderName,
                              String deliveryServiceTypeId,
                              String deliveryServiceTypeName,
                              Double totalCost,
                              Double totalTax,
                              String expectedDeliveryTime) {
}
