import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api } from '../../lib/api.js';
import { RESERVATION_STATUS, formatRange } from '../../lib/format.js';
import { Badge, ErrorMessage, Loading, PageTitle, Pagination } from '../../components/ui.jsx';

/** 06 自分の予約（取消は一覧内で確認ダイアログを出して実行） */
export default function MyReservationsPage() {
  const [page, setPage] = useState(0);
  const { data, error, loading, reload } = useFetch(() => api.get(`/api/me/reservations?page=${page}`), [page]);
  const [actionError, setActionError] = useState('');

  const handleCancel = async (r) => {
    if (!window.confirm(`「${r.slot.title}」の予約を取り消しますか？`)) return;
    setActionError('');
    try {
      await api.post(`/api/me/reservations/${r.id}/cancel`);
      reload();
    } catch (e) {
      setActionError(e.message);
    }
  };

  const isUpcoming = (r) => new Date(r.slot.start_at) > new Date();
  return (
    <>
      <PageTitle back={{ to: '/member', label: 'ホームへ戻る' }}>自分の予約</PageTitle>
      <Loading loading={loading} />
      <ErrorMessage message={error || actionError} />
      {data && data.items.length === 0 && <p className="muted">予約はまだありません。</p>}
      <ul className="list">
        {data?.items.map((r) => (
          <li key={r.id} className="list-item">
            <div>
              <strong>{formatRange(r.slot.start_at, r.slot.end_at)}</strong>
              <div>{r.slot.title}</div>
            </div>
            <div className="list-side">
              <Badge tone={r.status === 'CONFIRMED' ? 'green' : 'gray'}>{RESERVATION_STATUS[r.status]}</Badge>
              {r.status === 'CONFIRMED' && isUpcoming(r) && (
                <button className="btn btn-danger" onClick={() => handleCancel(r)}>取消</button>
              )}
            </div>
          </li>
        ))}
      </ul>
      <Pagination data={data} onChange={setPage} />
      <p><Link to="/member/slots" className="btn btn-main">予約枠一覧へ</Link></p>
    </>
  );
}
