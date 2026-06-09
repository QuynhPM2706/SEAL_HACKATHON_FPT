"use client";
/* ----------------------------------------------------------------------------
 * users-management — Admin xem danh sách user và khóa / mở lại tài khoản.
 * Dữ liệu lấy TỪ BACKEND THẬT (GET /api/users), thao tác đổi trạng thái gọi
 * PUT /api/users/{id}/status. Không còn dùng mock (useAllUsers) nữa.
 * -------------------------------------------------------------------------- */
import { PageHeader } from "@/components/app-shell";
import { useRequireRole } from "@/lib/role-guard";
import { listUsers, updateUserStatusApi, type BackendUser } from "@/lib/users-api";
import * as React from "react";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { toast } from "sonner";

export default function UsersManagement() {
  useRequireRole(["Admin"]);
  const [users, setUsers] = React.useState<BackendUser[]>([]);
  const [loading, setLoading] = React.useState(true);
  const [error, setError] = React.useState<string | null>(null);
  const [role, setRole] = React.useState("all");
  const [status, setStatus] = React.useState("all");

  // Tải danh sách user từ backend.
  const load = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setUsers(await listUsers());
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  }, []);
  React.useEffect(() => { void load(); }, [load]);

  // Đổi trạng thái rồi tải lại danh sách.
  const changeStatus = async (id: number, next: "active" | "suspended") => {
    try {
      await updateUserStatusApi(id, next);
      toast.success(next === "active" ? "Reactivated" : "Suspended");
      void load();
    } catch (e) {
      toast.error((e as Error).message);
    }
  };

  const filtered = users.filter(
    (u) => (role === "all" || u.role === role) && (status === "all" || u.status === status),
  );

  return (
    <div>
      <PageHeader title="Users management" subtitle="Admin · suspend or reactivate accounts" />
      <div className="flex gap-2 mb-4">
        <Select value={role} onValueChange={setRole}><SelectTrigger className="w-40"><SelectValue /></SelectTrigger><SelectContent>{["all","Participant","Judge","Mentor","Lecturer","Coordinator","Admin"].map((r) => <SelectItem key={r} value={r}>{r}</SelectItem>)}</SelectContent></Select>
        <Select value={status} onValueChange={setStatus}><SelectTrigger className="w-40"><SelectValue /></SelectTrigger><SelectContent>{["all","active","pending","suspended"].map((s) => <SelectItem key={s} value={s}>{s}</SelectItem>)}</SelectContent></Select>
      </div>

      {loading && <div className="rounded-xl border bg-card p-10 text-center text-sm text-muted-foreground">Loading from backend…</div>}
      {error && <div className="rounded-xl border bg-card p-6 text-sm text-destructive">Couldn't reach backend: {error}<div className="text-xs text-muted-foreground mt-1">Make sure the backend is running at http://localhost:8080.</div></div>}

      {!loading && !error && (
        <div className="rounded-xl border bg-card overflow-hidden">
          {/* Hàng tiêu đề cột */}
          <div className="grid grid-cols-[2fr_2fr_1fr_1fr_auto] items-center gap-3 bg-muted/40 px-4 py-2.5 text-[11px] font-medium uppercase tracking-wider text-muted-foreground">
            <span>User</span>
            <span>Email</span>
            <span>Role</span>
            <span>Status</span>
            <span className="text-right">Action</span>
          </div>

          <div className="divide-y">
            {filtered.length === 0 && <div className="p-10 text-center text-sm text-muted-foreground">No users.</div>}
            {filtered.map((u) => (
              <div key={u.id} className="grid grid-cols-[2fr_2fr_1fr_1fr_auto] items-center gap-3 px-4 py-3">
                <div className="flex items-center gap-3 min-w-0">
                  <div className="h-8 w-8 shrink-0 rounded-full btn-gradient grid place-items-center text-xs text-primary-foreground">{u.name.slice(0,1)}</div>
                  <div className="font-medium text-sm truncate">{u.name}</div>
                </div>
                <div className="text-xs text-muted-foreground truncate">{u.email}</div>
                <div><Badge variant="outline">{u.role}</Badge></div>
                <div><Badge variant={u.status === "active" ? "default" : u.status === "pending" ? "secondary" : "destructive"}>{u.status}</Badge></div>
                <div className="text-right">
                  {u.status === "active" ? (
                    <button onClick={() => void changeStatus(u.id, "suspended")} className="text-xs text-destructive">Suspend</button>
                  ) : (
                    <button onClick={() => void changeStatus(u.id, "active")} className="text-xs text-success">Reactivate</button>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
