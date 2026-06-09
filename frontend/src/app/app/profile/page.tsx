"use client";
/* ----------------------------------------------------------------------------
 * profile — xem & sửa hồ sơ cá nhân của user đang đăng nhập (mọi vai trò).
 * Dữ liệu THẬT: GET/PUT /api/users/me, đổi mật khẩu PUT /api/users/me/password.
 * MSSV chỉ hiển thị cho Participant.
 * -------------------------------------------------------------------------- */
import * as React from "react";
import { PageHeader } from "@/components/app-shell";
import { useAuth } from "@/lib/auth";
import {
  getMyProfileApi,
  updateMyProfileApi,
  changeMyPasswordApi,
  type MyProfile,
} from "@/lib/profile-api";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Select, SelectTrigger, SelectValue, SelectContent, SelectItem } from "@/components/ui/select";
import { toast } from "sonner";
import { User as UserIcon, KeyRound } from "lucide-react";

export default function ProfilePage() {
  const { user, refreshSession } = useAuth();

  const [profile, setProfile] = React.useState<MyProfile | null>(null);
  const [loading, setLoading] = React.useState(true);

  // form hồ sơ
  const [name, setName] = React.useState("");
  const [phone, setPhone] = React.useState("");
  const [dob, setDob] = React.useState("");
  const [gender, setGender] = React.useState("");
  const [studentId, setStudentId] = React.useState("");
  const [saving, setSaving] = React.useState(false);

  // form đổi mật khẩu
  const [oldPw, setOldPw] = React.useState("");
  const [newPw, setNewPw] = React.useState("");
  const [confirmPw, setConfirmPw] = React.useState("");
  const [changingPw, setChangingPw] = React.useState(false);

  React.useEffect(() => {
    getMyProfileApi()
        .then((p) => {
          setProfile(p);
          setName(p.name ?? "");
          setPhone(p.phone ?? "");
          setDob(p.dateOfBirth ?? "");
          setGender(p.gender ?? "");
          setStudentId(p.studentId ?? "");
        })
        .catch((e) => toast.error(e instanceof Error ? e.message : "Failed to load profile"))
        .finally(() => setLoading(false));
  }, []);

  const isParticipant = (profile?.role ?? user?.role) === "Participant";

  const saveProfile = async () => {
    if (!name.trim()) { toast.error("Full name is required."); return; }
    try {
      setSaving(true);
      const updated = await updateMyProfileApi({
        name: name.trim(),
        phone: phone.trim() || null,
        dateOfBirth: dob || null,
        gender: gender || null,
        studentId: isParticipant ? (studentId.trim() || null) : undefined,
      });
      setProfile(updated);
      toast.success("Profile updated");
      await refreshSession(); // cập nhật tên hiển thị ở header
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to update profile");
    } finally {
      setSaving(false);
    }
  };

  const changePassword = async () => {
    if (newPw.length < 6) { toast.error("New password must be ≥ 6 characters."); return; }
    if (newPw !== confirmPw) { toast.error("New password and confirmation do not match."); return; }
    try {
      setChangingPw(true);
      await changeMyPasswordApi(oldPw, newPw);
      toast.success("Password changed successfully.");
      setOldPw(""); setNewPw(""); setConfirmPw("");
    } catch (e) {
      toast.error(e instanceof Error ? e.message : "Failed to change password");
    } finally {
      setChangingPw(false);
    }
  };

  return (
      <div className="max-w-2xl">
        <PageHeader title="Profile" subtitle="View and edit your personal information" />

        {loading ? (
            <div className="rounded-xl border bg-card p-6 text-sm text-muted-foreground">Loading…</div>
        ) : (
            <div className="space-y-6">
              {/* Thông tin cơ bản */}
              <div className="rounded-xl border bg-card p-5">
                <div className="flex items-center gap-2 mb-4">
                  <UserIcon className="h-4 w-4 text-primary" />
                  <h3 className="font-medium">Personal information</h3>
                </div>

                <div className="grid sm:grid-cols-2 gap-4">
                  <div className="sm:col-span-2">
                    <Label>Full name</Label>
                    <Input value={name} onChange={(e) => setName(e.target.value)} className="mt-1.5" />
                  </div>
                  <div className="sm:col-span-2">
                    <Label>Email</Label>
                    <Input value={profile?.email ?? ""} disabled className="mt-1.5" />
                  </div>
                  <div>
                    <Label>Phone number</Label>
                    <Input value={phone} onChange={(e) => setPhone(e.target.value)} className="mt-1.5" placeholder="09xxxxxxxx" />
                  </div>
                  <div>
                    <Label>Date of birth</Label>
                    <Input type="date" value={dob} onChange={(e) => setDob(e.target.value)} className="mt-1.5" />
                  </div>
                  <div>
                    <Label>Gender</Label>
                    <Select value={gender || undefined} onValueChange={setGender}>
                      <SelectTrigger className="mt-1.5"><SelectValue placeholder="Select…" /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="Male">Male</SelectItem>
                        <SelectItem value="Female">Female</SelectItem>
                        <SelectItem value="Other">Other</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  {isParticipant && (
                      <div>
                        <Label>Student ID (MSSV)</Label>
                        <Input value={studentId} onChange={(e) => setStudentId(e.target.value)} className="mt-1.5" placeholder="SE193799" />
                      </div>
                  )}
                </div>

                <div className="mt-5 flex justify-end">
                  <Button onClick={() => void saveProfile()} disabled={saving} className="btn-gradient text-primary-foreground">
                    {saving ? "Saving…" : "Save changes"}
                  </Button>
                </div>
              </div>

              {/* Đổi mật khẩu */}
              <div className="rounded-xl border bg-card p-5">
                <div className="flex items-center gap-2 mb-4">
                  <KeyRound className="h-4 w-4 text-primary" />
                  <h3 className="font-medium">Change password</h3>
                </div>
                <div className="grid sm:grid-cols-2 gap-4">
                  <div className="sm:col-span-2">
                    <Label>Current password</Label>
                    <Input type="password" value={oldPw} onChange={(e) => setOldPw(e.target.value)} className="mt-1.5" autoComplete="current-password" />
                  </div>
                  <div>
                    <Label>New password</Label>
                    <Input type="password" value={newPw} onChange={(e) => setNewPw(e.target.value)} className="mt-1.5" autoComplete="new-password" placeholder="At least 6 characters" />
                  </div>
                  <div>
                    <Label>Confirm new password</Label>
                    <Input type="password" value={confirmPw} onChange={(e) => setConfirmPw(e.target.value)} className="mt-1.5" autoComplete="new-password" />
                  </div>
                </div>
                <div className="mt-5 flex justify-end">
                  <Button onClick={() => void changePassword()} disabled={changingPw} variant="outline">
                    {changingPw ? "Updating…" : "Update password"}
                  </Button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
}
