import axios from "axios";
import { CONFIG } from "../../../config/config";
import type { AuthUser, UserRole } from "../model/AuthUser";


export async function loginWithGoogle(credential: string): Promise<AuthUser> {
  const res = await axios.post(`${CONFIG.API_BASE}/api/auth/google`, {
    credential,
  });
  console.log("Login response:", res.data);
  return res.data.user;
}

export async function setupUserProfile(params: {
  userId: number;
  role: UserRole;
  managerId?: number | null;
}): Promise<AuthUser> {
  const res = await axios.patch(`${CONFIG.API_BASE}/api/users/${params.userId}/setup`, {
    role: params.role,
    managerId: params.managerId ?? null,
  });

  return res.data.user;
}