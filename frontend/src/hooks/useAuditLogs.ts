import { useQuery } from "@tanstack/react-query";
import { fetchAuditLog, fetchAuditLogs } from "../api/audit";
import type { AuditFilters } from "../types/audit";

export const auditKeys = {
  all: ["admin", "audit-logs"] as const,
  list: (filters: AuditFilters) => ["admin", "audit-logs", "list", filters] as const,
  detail: (id: number) => ["admin", "audit-logs", id] as const,
};

export function useAuditLogs(filters: AuditFilters) {
  return useQuery({
    queryKey: auditKeys.list(filters),
    queryFn: () => fetchAuditLogs(filters),
    placeholderData: (previous) => previous,
  });
}

export function useAuditLog(id: number) {
  return useQuery({
    queryKey: auditKeys.detail(id),
    queryFn: () => fetchAuditLog(id),
    enabled: Number.isInteger(id) && id > 0,
    retry: false,
  });
}