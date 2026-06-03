import axios from "axios";
import { CONFIG } from "../../../config/config";
import type { AuthUser, ManagerOption, UserRole } from "../model/AuthUser";
import { sanitizeAuthUser } from "../model/sanitizeAuthUser";
import { saveToken } from "../../utils/authToken";
import "../../utils/apiClient";


export async function loginWithGoogle(credential: string): Promise<AuthUser> {
  const res = await axios.post(`${CONFIG.API_BASE}/api/auth/google`, {
    credential,
  });
  const token = res.data.token;
  saveToken(token);
  const user = sanitizeAuthUser({ ...res.data.user, token });
  if (!user) {
    throw new Error("Invalid login response");
  }
  return user;
}

export async function setupUserProfile(params: {
  role: UserRole;
  managerId?: number | null;
}): Promise<AuthUser> {
  const res = await axios.patch(`${CONFIG.API_BASE}/api/users/me/setup`, {
    role: params.role,
    managerId: params.managerId ?? null,
  });

  const user = sanitizeAuthUser(res.data.user);
  if (!user) {
    throw new Error("Invalid profile setup response");
  }
  return user;
}

export async function fetchManagerOptions(): Promise<ManagerOption[]> {
  const res = await axios.get(`${CONFIG.API_BASE}/api/users/managers`);

  return res.data;
}
