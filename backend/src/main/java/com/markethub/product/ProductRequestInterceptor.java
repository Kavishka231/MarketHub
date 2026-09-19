package com.markethub.product;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ProductRequestInterceptor implements HandlerInterceptor {

    private static final Set<String> SORTS = Set.of("newest", "priceAsc", "priceDesc");
    private static final int MAX_SEARCH_LENGTH = 100;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {
        if (!"GET".equals(request.getMethod())) {
            return true;
        }

        int page = integer(request, "page", 0);
        int size = integer(request, "size", 20);
        if (page < 0) {
            throw new InvalidProductRequestException("Page cannot be negative");
        }
        if (size < 1) {
            throw new InvalidProductRequestException("Size must be at least 1");
        }

        if (request.getRequestURI().equals("/api/products")) {
            validateListing(request);
        }
        return true;
    }

    private void validateListing(HttpServletRequest request) {
        BigDecimal min = decimal(request, "minPrice");
        BigDecimal max = decimal(request, "maxPrice");
        if ((min != null && min.signum() < 0) || (max != null && max.signum() < 0)) {
            throw new InvalidProductRequestException("Price filters cannot be negative");
        }
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new InvalidProductRequestException("Minimum price cannot exceed maximum price");
        }

        String sort = request.getParameter("sort");
        if (sort != null && !SORTS.contains(sort)) {
            throw new InvalidProductRequestException("Invalid product sort");
        }

        String search = request.getParameter("search");
        if (search != null && search.trim().length() > MAX_SEARCH_LENGTH) {
            throw new InvalidProductRequestException("Search must not exceed 100 characters");
        }
    }

    private int integer(HttpServletRequest request, String name, int fallback) {
        String value = request.getParameter(name);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new InvalidProductRequestException("Invalid " + name);
        }
    }

    private BigDecimal decimal(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new InvalidProductRequestException("Invalid " + name);
        }
    }
}
