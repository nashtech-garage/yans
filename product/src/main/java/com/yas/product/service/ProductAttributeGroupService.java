package com.yas.product.service;

import com.yas.product.exception.DuplicatedNameException;
import com.yas.product.utils.Constants;
import com.yas.product.viewmodel.productattribute.ProductAttributeGroupVm;
import com.yas.product.model.attribute.ProductAttributeGroup;
import com.yas.product.viewmodel.productattribute.ProductAttributeGroupListGetVm;
import com.yas.product.repository.ProductAttributeGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductAttributeGroupService {
    private final ProductAttributeGroupRepository productAttributeGroupRepository;

    public ProductAttributeGroupService(ProductAttributeGroupRepository productAttributeGroupRepository) {
        this.productAttributeGroupRepository = productAttributeGroupRepository;
    }

    public ProductAttributeGroupListGetVm getPageableProductAttributeGroups(int pageNo, int pageSize) {
        List<ProductAttributeGroupVm> productAttributeGroupVms = new ArrayList<>();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<ProductAttributeGroup> productAttributeGroupPage = productAttributeGroupRepository.findAll(pageable);
        List<ProductAttributeGroup> productAttributeGroups = productAttributeGroupPage.getContent();
        for (ProductAttributeGroup productAttributeGroup : productAttributeGroups) {
            productAttributeGroupVms.add(ProductAttributeGroupVm.fromModel(productAttributeGroup));
        }

        return new ProductAttributeGroupListGetVm(
            productAttributeGroupVms,
            productAttributeGroupPage.getNumber(),
            productAttributeGroupPage.getSize(),
            (int) productAttributeGroupPage.getTotalElements(),
            productAttributeGroupPage.getTotalPages(),
            productAttributeGroupPage.isLast()
        );
    }

    public void save(ProductAttributeGroup productAttributeGroup) {
        if (checkExistedName(productAttributeGroup.getName(), productAttributeGroup.getId())) {
            throw new DuplicatedNameException(Constants.ERROR_CODE.NAME_ALREADY_EXITED, productAttributeGroup.getName());
        }
        productAttributeGroupRepository.save(productAttributeGroup);
    }

    private boolean checkExistedName(String name, Long id) {
        return productAttributeGroupRepository.findByNameAndId(name, id) != null;
    }
}
