import { useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api, toQuery } from '../../lib/api.js';
import { RESERVATION_STATUS, formatDateTime } from '../../lib/format.js';
import { Badge, ErrorMessage, Loading, PageTitle, Pagination } from '../../components/ui.jsx';

/** 14 予約管理（枠・状態で検索。参加者の確認と予約取消） */
export default function ReservationManagePage() {
  const [params, setParams] = useSearchParams();
  const slotId = params.get('slot_id') ?? '';
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [actionError, setActionError] = useState('');
  const { data, error, loading, reload } = useFetch(
    () => api.get('/api/reservations' + toQuery({ slot_id: slotId, status, page })), [slotId, status, page]);

  const handleCancel = async (r) => {
    if (!window.confirm(`${r.student.name} さんの予約を取り消しますか？`)) return;
    setActionError('');
    try {
      await api.post(`/api/reservations/${r.id}/cancel`);
      reload();
    } catch (e) {
      setActionError(e.message);
    }
  };

  const isUpcoming = (r) => new Date(r.slot.start_at) > new Date();
  return (
    <>
      <PageTitle back={{ to: '/staff', label: 'ホームへ戻る' }}>予約管理</PageTitle>
      <div className="filters">
        <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="">すべての状態</option>
          {Object.entries(RESERVATION_STATUS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        {slotId && (
          <button className="btn btn-sub" onClick={() => { setParams({}); setPage(0); }}>枠の絞り込みを解除 (枠ID: {slotId})</button>
        )}
      </div>
      <Loading loading={loading} />
      <ErrorMessage message={error || actionError} />
      {data && data.items.length === 0 && <p className="muted">予約がありません。</p>}
      {data && data.items.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead><tr><th>生徒</th><th>日時</th><th>タイトル</th><th>状態</th><th></th></tr></thead>
            <tbody>
              {data.items.map((r) => (
                <tr key={r.id}>
                  <td>{r.student.name}<br /><small className="muted">{r.student.course}</small></td>
                  <td>{formatDateTime(r.slot.start_at)}</td>
                  <td>{r.slot.title}</td>
                  <td><Badge tone={r.status === 'CONFIRMED' ? 'green' : 'gray'}>{RESERVATION_STATUS[r.status]}</Badge></td>
                  <td>
                    {r.status === 'CONFIRMED' && isUpcoming(r) && (
                      <button className="btn btn-danger" onClick={() => handleCancel(r)}>取消</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <Pagination data={data} onChange={setPage} />
    </>
  );
}
