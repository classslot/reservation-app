import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../../lib/api.js';
import { ErrorMessage, Field, Loading, PageTitle } from '../../components/ui.jsx';

const EMPTY = {
  name: '', guardian_name: '', contact_email: '', course: '', game: '', level: '',
  login_id: '', password: '', makeup_allowed: false, is_active: true,
};

/** 10 生徒登録（mode="new"） / 11 生徒編集（mode="edit"） */
export default function StudentFormPage({ mode }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = mode === 'new';
  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(!isNew);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isNew) return;
    api.get(`/api/students/${id}`)
      .then((s) => setForm({ ...EMPTY, ...s }))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isNew]);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.type === 'checkbox' ? e.target.checked : e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const common = {
        name: form.name, guardian_name: form.guardian_name, contact_email: form.contact_email,
        course: form.course, game: form.game, level: form.level, makeup_allowed: form.makeup_allowed,
      };
      if (isNew) {
        await api.post('/api/students', { ...common, login_id: form.login_id, password: form.password });
        navigate('/staff/students');
      } else {
        await api.patch(`/api/students/${id}`, { ...common, is_active: form.is_active });
        navigate(`/staff/students/${id}`);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <PageTitle back={{ to: isNew ? '/staff/students' : `/staff/students/${id}` }}>
        {isNew ? '生徒登録' : '生徒編集'}
      </PageTitle>
      <Loading loading={loading} />
      {!loading && (
        <form className="card" onSubmit={handleSubmit}>
          <ErrorMessage message={error} />
          {isNew && (
            <>
              <Field label="ログインID（半角英数字4文字以上）"><input value={form.login_id} onChange={set('login_id')} autoComplete="off" /></Field>
              <Field label="初期パスワード（8文字以上）"><input type="password" value={form.password} onChange={set('password')} autoComplete="new-password" /></Field>
            </>
          )}
          <Field label="生徒氏名"><input value={form.name} onChange={set('name')} /></Field>
          <Field label="保護者氏名"><input value={form.guardian_name} onChange={set('guardian_name')} /></Field>
          <Field label="連絡先メールアドレス"><input type="email" value={form.contact_email} onChange={set('contact_email')} /></Field>
          <Field label="コース"><input value={form.course} onChange={set('course')} /></Field>
          <Field label="ゲーム"><input value={form.game} onChange={set('game')} /></Field>
          <Field label="レベル"><input value={form.level} onChange={set('level')} /></Field>
          <label className="check"><input type="checkbox" checked={form.makeup_allowed} onChange={set('makeup_allowed')} /> 振替予約を許可する</label>
          {!isNew && (
            <label className="check"><input type="checkbox" checked={form.is_active} onChange={set('is_active')} /> 有効（外すと退会）</label>
          )}
          <button className="btn btn-main" disabled={submitting}>{isNew ? '登録' : '保存'}</button>
        </form>
      )}
    </>
  );
}
