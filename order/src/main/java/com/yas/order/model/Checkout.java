package com.yas.order.model;

import com.yas.order.model.enumeration.CheckoutState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "checkout")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Checkout extends AbstractAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String email;

    private String note;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private CheckoutState checkoutState;

    @SuppressWarnings("unused")
    private String progress;

    @SuppressWarnings("unused")
    private String customerId;

    @SuppressWarnings("unused")
    private String shipmentMethodId;

    @SuppressWarnings("unused")
    private String paymentMethodId;

    @SuppressWarnings("unused")
    private Long shippingAddressId;

    @SuppressWarnings("unused")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "last_error", columnDefinition = "jsonb")
    private String lastError;

    @SuppressWarnings("unused")
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes;

    @SuppressWarnings("unused")
    @Builder.Default
    private long totalAmount = 0;

    @SuppressWarnings("unused")
    private BigDecimal totalShipmentFee;

    @SuppressWarnings("unused")
    private BigDecimal totalShipmentTax;

    @SuppressWarnings("unused")
    private BigDecimal totalTax;

    @SuppressWarnings("unused")
    @Builder.Default
    private BigDecimal totalDiscountAmount = BigDecimal.ZERO;

    public void addAmount(long a) {
        this.totalAmount += a;
    }

    public void subtractAmount(long a) {
        this.totalAmount -= a;
        if (this.totalAmount < 0) {
            this.totalAmount = 0;
        }
    }
}
