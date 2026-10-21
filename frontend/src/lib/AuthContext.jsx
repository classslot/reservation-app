import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api } from './api.js';

const AuthContext = createContext(null);

/** ログイン状態（利用者・本人の生徒情報）を画面全体で共有する */
export function AuthProvider({ children }) {
  const [me, setMe] = useState(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    try {
      setMe(await api.get('/api/me'));
    } catch {
      setMe(null); // 未ログイン(401)など
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const login = useCallback(async (loginId, password) => {
    const data = await api.post('/api/login', { login_id: loginId, password });
    setMe(data);
    return data;
  }, []);

  const logout = useCallback(async () => {
    try {
      await api.post('/api/logout');
    } finally {
      setMe(null);
    }
  }, []);

  const value = useMemo(
    () => ({ user: me?.user ?? null, student: me?.student ?? null, loading, login, logout }),
    [me, loading, login, logout],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
export const homePath = (role) => (role === 'STAFF' ? '/staff' : '/member');
