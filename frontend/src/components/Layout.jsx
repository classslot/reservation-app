import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth, homePath } from '../lib/AuthContext.jsx';

/** 共通ヘッダー（ホームへのリンクとログアウト）＋画面本体 */
export default function Layout() {
  const { user, student, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <>
      <header className="header">
        <Link to={user ? homePath(user.role) : '/login'} className="brand">
          予約管理システム
        </Link>
        {user && (
          <div className="header-right">
            <span className="who">{user.role === 'STAFF' ? 'スタッフ' : student?.name ?? '会員'}</span>
            <button className="btn btn-sub" onClick={handleLogout}>
              ログアウト
            </button>
          </div>
        )}
      </header>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
