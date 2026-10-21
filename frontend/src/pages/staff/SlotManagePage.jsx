import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api, toQuery } from '../../lib/api.js';
import { SLOT_STATUS, SLOT_TYPE, formatRange } from '../../lib/format.js';
import { Badge, ErrorMessage, Loading, PageTitle, Pagination } from '../../components/ui.jsx';

/** 12 予約枠管理（種別・日付・状態で検索） */
export default function SlotManagePage() {
  const [filters, setFilters] = useState({ type: '', date: '', status: '' });
  const [page, setPage] = useState(0);
  const { data, error, loading } = useFetch(() => api.get('/api/slots' + toQuery({ ...filters, page })), [filters, page]);
  const change = (key) => (e) => { setFilters({ ...filters, [key]: e.target.value }); setPage(0); };

  return (
    <>
      <PageTitle back={{ to: '/staff', label: 'ホームへ戻る' }}>予約枠管理</PageTitle>
      <div className="filters">
        <select value={filters.type} onChange={change('type')}>
          <option value="">すべての種別</option>
          {Object.entries(SLOT_TYPE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        <input type="date" value={filters.date} onChange={change('date')} />
        <select value={filters.status} onChange={change('status')}>
          <option value="">すべての状態</option>
          {Object.entries(SLOT_STATUS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        <Link to="/staff/slots/new" className="btn btn-main">新規登録</Link>
      </div>
      <Loading loading={loading} />
      <ErrorMessage message={error} />
      {data && data.items.length === 0 && <p className="muted">予約枠がありません。</p>}
      {data && data.items.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead><tr><th>日時</th><th>タイトル</th><th>定員(残)</th><th>状態</th><th></th></tr></thead>
            <tbody>
              {data.items.map((s) => (
                <tr key={s.id}>
                  <td>{formatRange(s.start_at, s.end_at)}</td>
                  <td>{s.title}<br /><Badge tone="blue">{SLOT_TYPE[s.type]}</Badge></td>
                  <td>{s.capacity}（{s.remaining}）</td>
                  <td>{s.status === 'OPEN' ? <Badge tone="green">受付中</Badge> : <Badge tone="gray">受付停止</Badge>}</td>
                  <td className="actions">
                    <Link to={`/staff/slots/${s.id}/edit`} className="btn btn-sub">編集</Link>
                    <Link to={`/staff/reservations?slot_id=${s.id}`} className="btn btn-sub">予約確認</Link>
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
