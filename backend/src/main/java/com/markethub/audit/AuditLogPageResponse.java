package com.markethub.audit;

import org.springframework.data.domain.Page;
import java.util.List;

public record AuditLogPageResponse(
        List<AuditLogResponse> content, int page, int size, long totalElements, int totalPages) {
    static AuditLogPageResponse from(Page<AuditLog> result) {
        return new AuditLogPageResponse(result.map(AuditLogResponse::from).getContent(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}