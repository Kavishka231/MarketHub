import { Link, useSearchParams } from "react-router-dom";
import AdminPagination from "../components/admin/AdminPagination";
import {
  AdminEmpty,
  AdminError,
  AdminLoading,
} from "../components/admin/AdminState";
import { useAuditLogs } from "../hooks/useAuditLogs";
import type { AuditFilters } from "../types/audit";
import { apiErrorMessage } from "../utils/apiError";

const resourceTypes = ["USER", "VENDOR", "PRODUCT", "ORDER", "ORDER_ITEM", "REVIEW"];

export default function AdminAuditLogsPage() {
  const [params, setParams] = useSearchParams();
  const filters: AuditFilters = {
    page: Math.max(0, Number(params.get("page")) || 0),
    action: params.get("action") || undefined,
    resourceType: params.get("resourceType") || undefined,
    result: (params.get("result") || undefined) as AuditFilters["result"],
  };
  const query = useAuditLogs(filters);

  const change = (key: string, value: string) => {
    const next = new URLSearchParams(params);

    if (value) {
      next.set(key, value);
    } else {
      next.delete(key);
    }

    if (key !== "page") {
      next.delete("page");
    }

    setParams(next);
  };

  return (
    <main className="mx-auto max-w-7xl px-6 py-10">
      <h1 className="text-3xl font-bold">Audit logs</h1>
      <p className="mt-2 text-slate-600">
        Trace sensitive marketplace mutations by actor and request ID.
      </p>

      <div className="my-6 grid gap-4 md:grid-cols-3">
        <label>
          Action
          <input
            aria-label="Audit action"
            value={filters.action ?? ""}
            onChange={(event) => change("action", event.target.value)}
          />
        </label>
        <label>
          Resource
          <select
            aria-label="Audit resource"
            value={filters.resourceType ?? ""}
            onChange={(event) => change("resourceType", event.target.value)}
          >
            <option value="">All</option>
            {resourceTypes.map((resourceType) => (
              <option key={resourceType}>{resourceType}</option>
            ))}
          </select>
        </label>
        <label>
          Result
          <select
            aria-label="Audit result"
            value={filters.result ?? ""}
            onChange={(event) => change("result", event.target.value)}
          >
            <option value="">All</option>
            <option>SUCCESS</option>
            <option>FAILURE</option>
          </select>
        </label>
      </div>

      {query.isLoading ? (
        <AdminLoading label="Loading audit logs" />
      ) : query.isError ? (
        <AdminError
          message={apiErrorMessage(query.error, "Could not load audit logs")}
          retry={() => query.refetch()}
        />
      ) : query.data!.content.length === 0 ? (
        <AdminEmpty message="No audit records match these filters." />
      ) : (
        <>
          <div className="overflow-x-auto rounded-xl border bg-white">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b">
                  <th className="p-3">Time</th>
                  <th>Actor</th>
                  <th>Action</th>
                  <th>Resource</th>
                  <th>Result</th>
                  <th>Request ID</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {query.data!.content.map((log) => (
                  <tr className="border-b" key={log.id}>
                    <td className="p-3">
                      {new Date(log.occurredAt).toLocaleString()}
                    </td>
                    <td>
                      {log.actorEmail ?? "System"}
                      <br />
                      <span className="text-xs text-slate-500">
                        {log.actorRole}
                      </span>
                    </td>
                    <td>{log.action}</td>
                    <td>
                      {log.resourceType} {log.resourceId && `#${log.resourceId}`}
                    </td>
                    <td>{log.result}</td>
                    <td className="font-mono text-xs">
                      {log.requestId ?? "-"}
                    </td>
                    <td>
                      <Link
                        className="underline"
                        to={`/admin/audit-logs/${log.id}`}
                      >
                        Inspect
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <AdminPagination
            page={filters.page}
            totalPages={query.data!.totalPages}
            onPage={(page) => change("page", String(page))}
          />
        </>
      )}
    </main>
  );
}