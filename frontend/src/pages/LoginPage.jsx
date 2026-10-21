import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useAuth, homePath } from '../lib/AuthContext.jsx';
import { ErrorMessage, Field } from '../components/ui.jsx';

/** 01 ログイン */
export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (user) return <Navigate to={homePath(user.role)} replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!loginId.trim() || !password) {
      setError('ログインIDとパスワードを入力してください');
      return;
    }
    setSubmitting(true);
    try {
      const data = await login(loginId, password);
      navigate(homePath(data.user.role));
    } catch (err) {
      setError(err.message); // 認証失敗の理由は一般的な文言のみ表示される
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="card narrow" onSubmit={handleSubmit}>
      <h1>ログイン</h1>
      <ErrorMessage message={error} />
      <Field label="ログインID">
        <input value={loginId} onChange={(e) => setLoginId(e.target.value)} autoComplete="username" />
      </Field>
      <Field label="パスワード">
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
      </Field>
      <button className="btn btn-main block" disabled={submitting}>ログイン</button>
    </form>
  );
}
