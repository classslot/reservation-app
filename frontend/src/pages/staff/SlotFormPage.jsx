import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { api } from '../../lib/api.js';
import { SLOT_TYPE, toInputDateTime } from '../../lib/format.js';
import { ErrorMessage, Field, Loading, PageTitle } from '../../components/ui.jsx';

const EMPTY = {
  type: 'MAKEUP', title: '', teacher_name: '', start_at: '', end_at: '', capacity: 1,
  status: 'OPEN', target_course: '', target_game: '', target_level: '',
};

/** 13 予約枠登録（mode="new"） / 編集・公開停止（mode="edit"） */
export default function SlotFormPage({ mode }) {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = mode === 'new';
  const [form, setForm] = useState(EMPTY);
  const [loading, setLoading] = useState(!isNew);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (isNew) return;
    api.get(`/api/slots/${id}`)
      .then((s) => setForm({
        ...EMPTY, ...s,
        start_at: toInputDateTime(s.start_at), end_at: toInputDateTime(s.end_at),
        target_course: s.target_course ?? '', target_game: s.target_game ?? '', target_level: s.target_level ?? '',
      }))
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [id, isNew]);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const save = async (override = {}) => {
    setError('');
    setSubmitting(true);
    try {
      const body = { ...form, ...override, capacity: Number(form.capacity) };
      delete body.id; delete body.remaining; delete body.reserved;
      if (isNew) await api.post('/api/slots', body);
      else await api.patch(`/api/slots/${id}`, body);
      navigate('/staff/slots');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <PageTitle back={{ to: '/staff/slots', label: '一覧へ戻る' }}>{isNew ? '予約枠登録' : '予約枠編集'}</PageTitle>
      <Loading loading={loading} />
      {!loading && (
        <form className="card" onSubmit={(e) => { e.preventDefault(); save(); }}>
          <ErrorMessage message={error} />
          <Field label="種別">
            <select value={form.type} onChange={set('type')}>
              {Object.entries(SLOT_TYPE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </Field>
          <Field label="タイトル"><input value={form.title} onChange={set('title')} /></Field>
          <Field label="担当者"><input value={form.teacher_name} onChange={set('teacher_name')} /></Field>
          <Field label="開始日時"><input type="datetime-local" value={form.start_at} onChange={set('start_at')} /></Field>
          <Field label="終了日時"><input type="datetime-local" value={form.end_at} onChange={set('end_at')} /></Field>
          <Field label="定員"><input type="number" min="1" value={form.capacity} onChange={set('capacity')} /></Field>
          <p className="muted">対象条件（振替レッスンは必須。イベントは空欄で全員が対象）</p>
          <Field label="対象コース"><input value={form.target_course} onChange={set('target_course')} /></Field>
          <Field label="対象ゲーム"><input value={form.target_game} onChange={set('target_game')} /></Field>
          <Field label="対象レベル"><input value={form.target_level} onChange={set('target_level')} /></Field>
          <div className="actions">
            <button className="btn btn-main" disabled={submitting}>{isNew ? '登録' : '保存'}</button>
            {!isNew && form.status === 'OPEN' && (
              <button type="button" className="btn btn-danger" disabled={submitting}
                onClick={() => window.confirm('この枠の受付を停止しますか？（既存の予約は残ります）') && save({ status: 'CLOSED' })}>
                公開停止
              </button>
            )}
            {!isNew && form.status === 'CLOSED' && (
              <button type="button" className="btn btn-sub" disabled={submitting} onClick={() => save({ status: 'OPEN' })}>
                受付を再開
              </button>
            )}
          </div>
        </form>
      )}
    </>
  );
}
