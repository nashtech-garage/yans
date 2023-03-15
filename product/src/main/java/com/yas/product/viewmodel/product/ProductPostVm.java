package com.yas.product.viewmodel.product;

import com.yas.product.validation.ValidateProductPrice;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProductPostVm(
        @NotEmpty String name,
        @NotEmpty String slug,
        Long brandId,
        List<Long> categoryIds,
        String shortDescription,
        String description,
        String specification,
        String sku,
        String gtin,
        @ValidateProductPrice Double price,
        Boolean isAllowedToOrder,
        Boolean isPublished,
        Boolean isFeatured,
        Boolean isVisibleIndividually,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        Long thumbnailMediaId,
        List<Long> productImageIds,
        List<ProductVariationPostVm> variations) {
}
