package com.yas.product.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.yas.product.service.ProductService;
import com.yas.product.viewmodel.ErrorVm;
import com.yas.product.viewmodel.ProductGetDetailVm;
import com.yas.product.viewmodel.ProductListVm;
import com.yas.product.viewmodel.ProductPostVm;
import com.yas.product.viewmodel.ProductThumbnailVm;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/backoffice/products")
    public ResponseEntity<List<ProductListVm>> listProduct() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @PostMapping(path = "/backoffice/products", consumes ={ MediaType.APPLICATION_JSON_VALUE ,MediaType.MULTIPART_FORM_DATA_VALUE})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = ProductGetDetailVm.class))),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ErrorVm.class)))})
    public ResponseEntity<ProductGetDetailVm> createProduct(@RequestPart("productDetails") ProductPostVm productPostVm, @RequestPart("files") List<MultipartFile> files ,UriComponentsBuilder uriComponentsBuilder) {
        ProductGetDetailVm productGetDetailVm = productService.createProduct(productPostVm, files);
        return ResponseEntity.created(uriComponentsBuilder.replacePath("/products/{id}").buildAndExpand(productGetDetailVm.id()).toUri())
                .body(productGetDetailVm);
    }

    @GetMapping("/storefront/products/featured")
    public ResponseEntity<List<ProductThumbnailVm>> getFeaturedProducts() {
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/storefront/brand/{brandSlug}/products")
    public ResponseEntity<List<ProductThumbnailVm>> getProductsByBrand(@PathVariable String brandSlug) {
        return ResponseEntity.ok(productService.getProductsByBrand(brandSlug));
    }

    @GetMapping("/storefront/category/{categorySlug}/products")
    public ResponseEntity<List<ProductThumbnailVm>> getProductsByCategory(@PathVariable String categorySlug) {
        return ResponseEntity.ok(productService.getProductsByCategory(categorySlug));
    }
}
