import axios from "axios";
import { CONFIG } from "../../../config/config";
import type { AuthUser, ManagerOption, UserRole } from "../model/AuthUser";
import { saveToken } from "../../utils/authToken";
import "../../utils/apiClient";


export async function loginWithGoogle(credential: string): Promise<AuthUser> {
  const res = await axios.post(`${CONFIG.API_BASE}/api/auth/google`, {
    credential,
  });
  const token = res.data.token;
  saveToken(token);
  return { ...res.data.user, token };
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

export async function fetchManagerOptions(): Promise<ManagerOption[]> {
  const res = await axios.get(`${CONFIG.API_BASE}/api/users/managers`);

  return res.data;
}
