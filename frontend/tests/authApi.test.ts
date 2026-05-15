import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from 'axios';
import { loginWithGoogle, setupUserProfile } from '../src/features/auth/api/authApi';
import { CONFIG } from '../src/config/config';

vi.mock('axios');

describe('authApi', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  describe('loginWithGoogle', () => {
    it('should call axios.post and return user data', async () => {
      const mockUser = { id: 1, name: 'Test User', email: 'test@example.com' };
      vi.mocked(axios.post).mockResolvedValueOnce({ data: { user: mockUser } });

      const credential = 'fake-credential';
      const result = await loginWithGoogle(credential);

      expect(axios.post).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/auth/google`, { credential });
      expect(result).toEqual(mockUser);
    });
  });

  describe('setupUserProfile', () => {
    it('should call axios.patch and return updated user data', async () => {
      const mockUser = { id: 1, name: 'Test User', email: 'test@example.com', role: 'REQUESTER', managerId: 2 };
      vi.mocked(axios.patch).mockResolvedValueOnce({ data: { user: mockUser } });

      const params = { userId: 1, role: 'REQUESTER' as const, managerId: 2 };
      const result = await setupUserProfile(params);

      expect(axios.patch).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/users/1/setup`, {
        role: 'REQUESTER',
        managerId: 2,
      });
      expect(result).toEqual(mockUser);
    });

    it('should use null for managerId if not provided', async () => {
      const mockUser = { id: 1, name: 'Test User', email: 'test@example.com', role: 'MANAGER' };
      vi.mocked(axios.patch).mockResolvedValueOnce({ data: { user: mockUser } });

      const params = { userId: 1, role: 'MANAGER' as const };
      const result = await setupUserProfile(params);

      expect(axios.patch).toHaveBeenCalledWith(`${CONFIG.API_BASE}/api/users/1/setup`, {
        role: 'MANAGER',
        managerId: null,
      });
      expect(result).toEqual(mockUser);
    });
  });
});
