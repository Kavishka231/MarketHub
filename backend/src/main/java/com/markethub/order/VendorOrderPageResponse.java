package com.markethub.order;

import java.util.List;

public record VendorOrderPageResponse(List<VendorOrderSummaryResponse> content, int page, int size,
        long totalElements, int totalPages) {}
