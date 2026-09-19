CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actor_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    actor_email VARCHAR(255),
    actor_role VARCHAR(20),
    action VARCHAR(80) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(100),
    request_id VARCHAR(64),
    result VARCHAR(20) NOT NULL,
    metadata VARCHAR(1000),
    CONSTRAINT chk_audit_result CHECK (result IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_audit_occurred_at ON audit_logs(occurred_at DESC, id DESC);
CREATE INDEX idx_audit_actor ON audit_logs(actor_user_id, occurred_at DESC);
CREATE INDEX idx_audit_action ON audit_logs(action, occurred_at DESC);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id, occurred_at DESC);