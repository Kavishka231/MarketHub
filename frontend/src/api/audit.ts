import client from "./client";
import type { AuditFilters, AuditLog, AuditLogPage } from "../types/audit";

export async function fetchAuditLogs(filters: AuditFilters): Promise<AuditLogPage> {
  const response = await client.get<AuditLogPage>("/api/admin/audit-logs", {
    params: { ...filters, size: 20 },
  });
  return response.data;
}

export async function fetchAuditLog(id: number): Promise<AuditLog> {
  return (await client.get<AuditLog>(`/api/admin/audit-logs/${id}`)).data;
}