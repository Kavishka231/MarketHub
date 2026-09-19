package com.markethub.audit;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin/audit-logs")
public class AdminAuditController {
    private final AuditLogRepository logs;

    public AdminAuditController(AuditLogRepository logs) {
        this.logs = logs;
    }

    @GetMapping
    public AuditLogPageResponse list(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Specification<AuditLog> specification = Specification.where(null);
        if (actorId != null) specification = specification.and((root, query, cb) -> cb.equal(root.get("actorUserId"), actorId));
        if (action != null && !action.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(root.get("action"), action));
        if (resourceType != null && !resourceType.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(root.get("resourceType"), resourceType));
        if (result != null && !result.isBlank()) specification = specification.and((root, query, cb) -> cb.equal(root.get("result"), result));
        if (from != null) specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
        if (to != null) specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("occurredAt"), to));
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return AuditLogPageResponse.from(logs.findAll(specification,
                PageRequest.of(safePage, safeSize, Sort.by("occurredAt").descending().and(Sort.by("id").descending()))));
    }

    @GetMapping("/{id}")
    public AuditLogResponse get(@PathVariable Long id) {
        return logs.findById(id).map(AuditLogResponse::from)
                .orElseThrow(() -> new com.markethub.admin.AdminNotFoundException("Audit log not found"));
    }
}