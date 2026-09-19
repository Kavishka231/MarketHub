package com.markethub.product;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class PublicProductController {

    private final PublicProductService service;

    public PublicProductController(PublicProductService service) {
        this.service = service;
    }

    @GetMapping
    public ProductPageResponse list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long vendorId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(
                search,
                categoryId,
                vendorId,
                minPrice,
                maxPrice,
                sort,
                page,
                Math.min(size, 100));
    }

    @GetMapping("/{productId}")
    public ProductResponse get(@PathVariable Long productId) {
        return service.get(productId);
    }
}
