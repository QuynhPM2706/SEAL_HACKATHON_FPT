"use client";
/* ============================================================================
 * profile-api.ts — hồ sơ cá nhân của user đang đăng nhập.
 *   GET  /api/users/me            → xem hồ sơ
 *   PUT  /api/users/me            → cập nhật hồ sơ
 *   PUT  /api/users/me/password   → đổi mật khẩu
 * ========================================================================== */
import { apiGet, apiPut } from "@/lib/api";

export interface MyProfile {
  id: number;
  name: string;
  email: string;
  role: string;
  status: string;
  studentId?: string | null;
  school?: string | null;
  phone?: string | null;
  dateOfBirth?: string | null; // "YYYY-MM-DD"
  gender?: string | null;
}

export interface UpdateProfileInput {
  name?: string;
  phone?: string | null;
  dateOfBirth?: string | null;
  gender?: string | null;
  studentId?: string | null;
}

export const getMyProfileApi = () => apiGet<MyProfile>("/api/users/me");

export const updateMyProfileApi = (input: UpdateProfileInput) =>
  apiPut<MyProfile>("/api/users/me", input);

export const changeMyPasswordApi = (oldPassword: string, newPassword: string) =>
  apiPut<string>("/api/users/me/password", { oldPassword, newPassword });
