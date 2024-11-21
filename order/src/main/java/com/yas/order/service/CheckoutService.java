package com.yas.order.service;

import com.yas.commonlibrary.constants.ApiConstant;
import com.yas.commonlibrary.constants.MessageCode;
import com.yas.commonlibrary.exception.ForbiddenException;
import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.commonlibrary.utils.AuthenticationUtils;
import com.yas.order.mapper.CheckoutMapper;
import com.yas.order.model.Checkout;
import com.yas.order.model.CheckoutItem;
import com.yas.order.model.Order;
import com.yas.order.model.enumeration.CheckoutState;
import com.yas.order.repository.CheckoutRepository;
import com.yas.order.utils.Constants;
import static com.yas.order.utils.Constants.ErrorCode.CHECKOUT_NOT_FOUND;
import com.yas.order.viewmodel.checkout.CheckoutItemVm;
import com.yas.order.viewmodel.checkout.CheckoutPaymentMethodPutVm;
import com.yas.order.viewmodel.checkout.CheckoutPostVm;
import com.yas.order.viewmodel.checkout.CheckoutStatusPutVm;
import com.yas.order.viewmodel.checkout.CheckoutVm;
import com.yas.order.viewmodel.product.ProductCheckoutListVm;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CheckoutService {

    private final CheckoutRepository checkoutRepository;
    private final OrderService orderService;
    private final ProductService productService;
    private final CheckoutMapper checkoutMapper;

    public CheckoutVm createCheckout(CheckoutPostVm checkoutPostVm) {

        Checkout checkout = checkoutMapper.toModel(checkoutPostVm);
        checkout.setCheckoutState(CheckoutState.PENDING);
        checkout.setCustomerId(AuthenticationUtils.extractUserId());

        checkout = setCheckoutItems(checkout, checkoutPostVm);

        checkout = checkoutRepository.save(checkout);

        CheckoutVm checkoutVm = checkoutMapper.toVm(checkout);
        Set<CheckoutItemVm> checkoutItemVms = checkout.getCheckoutItems()
                .stream()
                .map(checkoutMapper::toVm)
                .collect(Collectors.toSet());
        log.info(Constants.MessageCode.CREATE_CHECKOUT, checkout.getId(), checkout.getCustomerId());
        return checkoutVm.toBuilder().checkoutItemVms(checkoutItemVms).build();
    }

    private Checkout setCheckoutItems(Checkout checkout, CheckoutPostVm checkoutPostVm) {
        Set<Long> productIds = new HashSet<>();
        List<CheckoutItem> checkoutItems = checkoutPostVm.checkoutItemPostVms()
                .stream()
                .map(checkoutItemPostVm -> {
                    CheckoutItem item = checkoutMapper.toModel(checkoutItemPostVm);
                    item.setCheckout(checkout);
                    productIds.add(item.getProductId());
                    return item;
                })
                .toList();

        Map<Long, ProductCheckoutListVm> products
                = productService.getProductInfomation(productIds, 0, productIds.size());
        associateProductWithCheckoutItem(checkout, products, checkoutItems);

        checkout.setCheckoutItems(checkoutItems);
        return checkout;
    }

    private void associateProductWithCheckoutItem(
            Checkout checkout,
            Map<Long, ProductCheckoutListVm> products,
            List<CheckoutItem> checkoutItems) {
        checkoutItems.forEach(t -> {
            ProductCheckoutListVm product = products.get(t.getProductId());
            if (product == null) {
                throw new NotFoundException(MessageCode.PRODUCT_NOT_FOUND, t.getProductId());
            } else {
                t.setProductName(product.getName());
                t.setProductPrice(BigDecimal.valueOf(product.getPrice()));
                checkout.addAmount(
                        BigDecimal.valueOf(product.getPrice())
                                .multiply(BigDecimal.valueOf(t.getQuantity()))
                );
            }
        });
    }

    public CheckoutVm getCheckoutPendingStateWithItemsById(String id) {

        Checkout checkout = checkoutRepository.findByIdAndCheckoutState(id, CheckoutState.PENDING).orElseThrow(()
                -> new NotFoundException(CHECKOUT_NOT_FOUND, id));

        if (!checkout.getCreatedBy().equals(AuthenticationUtils.extractUserId())) {
            throw new ForbiddenException(ApiConstant.FORBIDDEN, "You can not view this checkout");
        }

        CheckoutVm checkoutVm = checkoutMapper.toVm(checkout);

        List<CheckoutItem> checkoutItems = checkout.getCheckoutItems();
        if (CollectionUtils.isEmpty(checkoutItems)) {
            return checkoutVm;
        }

        Set<CheckoutItemVm> checkoutItemVms = checkoutItems
                .stream()
                .map(checkoutMapper::toVm)
                .collect(Collectors.toSet());

        return checkoutVm.toBuilder().checkoutItemVms(checkoutItemVms).build();
    }

    public Long updateCheckoutStatus(CheckoutStatusPutVm checkoutStatusPutVm) {
        Checkout checkout = checkoutRepository.findById(checkoutStatusPutVm.checkoutId())
                .orElseThrow(() -> new NotFoundException(CHECKOUT_NOT_FOUND, checkoutStatusPutVm.checkoutId()));
        checkout.setCheckoutState(CheckoutState.valueOf(checkoutStatusPutVm.checkoutStatus()));
        checkoutRepository.save(checkout);
        log.info(Constants.MessageCode.UPDATE_CHECKOUT_STATUS,
                checkout.getId(),
                checkoutStatusPutVm.checkoutStatus(),
                checkout.getCheckoutState()
        );
        Order order = orderService.findOrderByCheckoutId(checkoutStatusPutVm.checkoutId());
        return order.getId();
    }

    public void updateCheckoutPaymentMethod(String id, CheckoutPaymentMethodPutVm checkoutPaymentMethodPutVm) {
        Checkout checkout = checkoutRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CHECKOUT_NOT_FOUND, id));
        checkout.setPaymentMethodId(checkoutPaymentMethodPutVm.paymentMethodId());
        log.info(Constants.MessageCode.UPDATE_CHECKOUT_PAYMENT,
                checkout.getId(),
                checkoutPaymentMethodPutVm.paymentMethodId(),
                checkout.getPaymentMethodId()
        );
        checkoutRepository.save(checkout);
    }
}
