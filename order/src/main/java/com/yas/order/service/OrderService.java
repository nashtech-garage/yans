package com.yas.order.service;

import com.yas.order.exception.NotFoundException;
import com.yas.order.model.Order;
import com.yas.order.model.OrderAddress;
import com.yas.order.model.OrderItem;
import com.yas.order.model.enumeration.EOrderStatus;
import com.yas.order.repository.OrderAddressRepository;
import com.yas.order.repository.OrderItemRepository;
import com.yas.order.repository.OrderRepository;
import com.yas.order.utils.Constants;
import com.yas.order.viewmodel.OrderAddressPostVm;
import com.yas.order.viewmodel.OrderAddressVm;
import com.yas.order.viewmodel.OrderPostVm;
import com.yas.order.viewmodel.OrderVm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderAddressRepository orderAddressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderAddressRepository = orderAddressRepository;
    }


    public OrderVm createOrder(OrderPostVm orderPostVm) {
//        TO-DO: handle check inventory when inventory is complete
//        ************

//        TO-DO: handle payment
//        ************

        OrderAddressPostVm billingAddressPostVm = orderPostVm.billingAddressPostVm();
        OrderAddress billOrderAddress = OrderAddress.builder()
                .phone(billingAddressPostVm.phone())
                .contactName(billingAddressPostVm.contactName())
                .addressLine1(billingAddressPostVm.addressLine1())
                .addressLine2(billingAddressPostVm.addressLine2())
                .city(billingAddressPostVm.city())
                .zipCode(billingAddressPostVm.zipCode())
                .district(billingAddressPostVm.district())
                .stateOrProvince(billingAddressPostVm.stateOrProvince())
                .country(billingAddressPostVm.country())
                .build();

        OrderAddressPostVm shipOrderAddressPostVm = orderPostVm.shippingAddressPostVm();
        OrderAddress shippOrderAddress = OrderAddress.builder()
                .phone(shipOrderAddressPostVm.phone())
                .contactName(shipOrderAddressPostVm.contactName())
                .addressLine1(shipOrderAddressPostVm.addressLine1())
                .addressLine2(shipOrderAddressPostVm.addressLine2())
                .city(shipOrderAddressPostVm.city())
                .zipCode(shipOrderAddressPostVm.zipCode())
                .district(shipOrderAddressPostVm.district())
                .stateOrProvince(shipOrderAddressPostVm.stateOrProvince())
                .country(shipOrderAddressPostVm.country())
                .build();

        Order order = Order.builder()
                .email(orderPostVm.email())
                .note(orderPostVm.note())
                .tax(orderPostVm.tax())
                .discount(orderPostVm.discount())
                .numberItem(orderPostVm.numberItem())
                .totalPrice(orderPostVm.totalPrice())
                .couponCode(orderPostVm.couponCode())
                .orderStatus(EOrderStatus.PENDING)
                .deliveryFee(orderPostVm.deliveryFee())
                .deliveryMethod(orderPostVm.deliveryMethod())
                .deliveryStatus(orderPostVm.deliveryStatus())
                .paymentMethod(orderPostVm.paymentMethod())
                .paymentStatus(orderPostVm.paymentStatus())
                .shippingAddressId(shippOrderAddress)
                .billingAddressId(billOrderAddress)
                .build();
        orderRepository.save(order);



        Set<OrderItem> orderItems = orderPostVm.orderItemPostVms().stream()
                .map(item -> OrderItem.builder()
                        .productId(item.productId())
                        .quantity(item.quantity())
                        .productPrice(item.productPrice())
                        .note(item.note())
                        .orderId(order)
                        .build())
                .collect(Collectors.toSet());
        orderItemRepository.saveAll(orderItems);

        //setOrderItems so that we able to return order with orderItems
        order.setOrderItems(orderItems);

 //        TO-DO: delete Item in Cart
//        ************

//        TO-DO: decrement inventory when inventory is complete
//        ************

        return OrderVm.fromModel(order);
    }

    public OrderVm getOrderWithItemsById(long id) {
        Order order = orderRepository.findById(id).orElseThrow(()
                -> new NotFoundException(Constants.ERROR_CODE.ORDER_NOT_FOUND, id));

        return OrderVm.fromModel(order);
    }
}
