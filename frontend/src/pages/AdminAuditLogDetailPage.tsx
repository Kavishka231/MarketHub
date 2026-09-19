import { Link, useParams } from "react-router-dom";
import { AdminError, AdminLoading } from "../components/admin/AdminState";
import { useAuditLog } from "../hooks/useAuditLogs";
import { apiErrorMessage } from "../utils/apiError";

export default function AdminAuditLogDetailPage() {
  const { auditId = "" } = useParams();
  const query = useAuditLog(Number(auditId));

  if (query.isLoading) {
    return <AdminLoading label="Loading audit record" />;
  }

  if (query.isError || !query.data) {
    return (
      <AdminError
        message={apiErrorMessage(query.error, "Audit record not found")}
        retry={() => query.refetch()}
      />
    );
  }

  const log = query.data;

  return (
    <main className="mx-auto max-w-3xl px-6 py-10">
      <Link className="text-sm underline" to="/admin/audit-logs">
        Back to audit logs
      </Link>
      <h1 className="mt-4 text-3xl font-bold">{log.action}</h1>
      <dl className="mt-6 grid gap-4 rounded-xl border bg-white p-6 sm:grid-cols-2">
        <div>
          <dt className="text-sm text-slate-500">Timestamp</dt>
          <dd>{new Date(log.occurredAt).toLocaleString()}</dd>
        </div>
        <div>
          <dt className="text-sm text-slate-500">Result</dt>
          <dd>{log.result}</dd>
        </div>
        <div>
          <dt className="text-sm text-slate-500">Actor</dt>
          <dd>{log.actorEmail ?? "System"}</dd>
        </div>
        <div>
          <dt className="text-sm text-slate-500">Role</dt>
          <dd>{log.actorRole}</dd>
        </div>
        <div>
          <dt className="text-sm text-slate-500">Resource</dt>
          <dd>
            {log.resourceType} {log.resourceId && `#${log.resourceId}`}
          </dd>
        </div>
        <div>
          <dt className="text-sm text-slate-500">Request ID</dt>
          <dd className="font-mono text-sm">{log.requestId ?? "-"}</dd>
        </div>
        <div className="sm:col-span-2">
          <dt className="text-sm text-slate-500">Safe metadata</dt>
          <dd className="mt-1 rounded bg-slate-50 p-3 font-mono text-sm">
            {log.metadata ?? "None"}
          </dd>
        </div>
      </dl>
    </main>
  );
}