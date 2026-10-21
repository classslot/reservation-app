import { Navigate } from 'react-router-dom';
import { useAuth, homePath } from '../lib/AuthContext.jsx';

/** 未ログインはログイン画面へ、権限が違う場合は自分のホームへ戻す（サーバー側でも権限は確認している） */
export default function RequireRole({ role, children }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="muted">読み込み中...</p>;
  if (!user) return <Navigate to="/login" replace />;
  if (user.role !== role) return <Navigate to={homePath(user.role)} replace />;
  return children;
}
