export interface AuditLog {
  id: number;
  occurredAt: string;
  actorUserId?: number;
  actorEmail?: string;
  actorRole?: string;
  action: string;
  resourceType: string;
  resourceId?: string;
  requestId?: string;
  result: "SUCCESS" | "FAILURE";
  metadata?: string;
}

export interface AuditLogPage {
  content: AuditLog[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AuditFilters {
  actorId?: number;
  action?: string;
  resourceType?: string;
  result?: "SUCCESS" | "FAILURE";
  from?: string;
  to?: string;
  page: number;
}