package com.markethub.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt = Instant.now();
    @Column(name = "actor_user_id")
    private Long actorUserId;
    @Column(name = "actor_email")
    private String actorEmail;
    @Column(name = "actor_role")
    private String actorRole;
    @Column(nullable = false)
    private String action;
    @Column(name = "resource_type", nullable = false)
    private String resourceType;
    @Column(name = "resource_id")
    private String resourceId;
    @Column(name = "request_id")
    private String requestId;
    @Column(nullable = false)
    private String result;
    private String metadata;

    protected AuditLog() {}

    public AuditLog(Long actorUserId, String actorEmail, String actorRole, String action,
                    String resourceType, String resourceId, String requestId, String result, String metadata) {
        this.actorUserId = actorUserId;
        this.actorEmail = actorEmail;
        this.actorRole = actorRole;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.requestId = requestId;
        this.result = result;
        this.metadata = metadata;
    }

    public Long getId() { return id; }
    public Instant getOccurredAt() { return occurredAt; }
    public Long getActorUserId() { return actorUserId; }
    public String getActorEmail() { return actorEmail; }
    public String getActorRole() { return actorRole; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public String getResourceId() { return resourceId; }
    public String getRequestId() { return requestId; }
    public String getResult() { return result; }
    public String getMetadata() { return metadata; }
}