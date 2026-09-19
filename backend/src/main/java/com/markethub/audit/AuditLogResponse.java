package com.markethub.audit;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Instant occurredAt,
        Long actorUserId,
        String actorEmail,
        String actorRole,
        String action,
        String resourceType,
        String resourceId,
        String requestId,
        String result,
        String metadata) {
    static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getOccurredAt(), log.getActorUserId(),
                log.getActorEmail(), log.getActorRole(), log.getAction(), log.getResourceType(),
                log.getResourceId(), log.getRequestId(), log.getResult(), log.getMetadata());
    }
}