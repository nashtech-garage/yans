package com.yas.order.model;


import com.yas.order.model.enumeration.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends AbstractAuditEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shipping_address_id", referencedColumnName = "id")
    private OrderAddress shippingAddressId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_address_id", referencedColumnName = "id")
    private OrderAddress billingAddressId;
    private String note;
    private float tax;
    private float discount;
    private int numberItem;
    private String couponCode;
    private BigDecimal totalPrice;
    private BigDecimal deliveryFee;
    @Enumerated(EnumType.STRING)
    private EOrderStatus orderStatus;
    @Enumerated(EnumType.STRING)
    private EDeliveryMethod deliveryMethod;
    @Enumerated(EnumType.STRING)
    private EDeliveryStatus deliveryStatus;
    private Long paymentId;

    @OneToMany(mappedBy = "orderId", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<OrderItem> orderItems;

}