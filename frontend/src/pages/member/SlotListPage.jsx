import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api, toQuery } from '../../lib/api.js';
import { SLOT_TYPE, formatRange, formatTarget } from '../../lib/format.js';
import { Badge, ErrorMessage, Loading, PageTitle, Pagination } from '../../components/ui.jsx';

/** 04 予約枠一覧（自分が対象の枠だけがサーバーから返る） */
export default function SlotListPage() {
  const [type, setType] = useState('');
  const [date, setDate] = useState('');
  const [page, setPage] = useState(0);
  const { data, error, loading } = useFetch(() => api.get('/api/slots' + toQuery({ type, date, page })), [type, date, page]);

  return (
    <>
      <PageTitle back={{ to: '/member', label: 'ホームへ戻る' }}>予約枠一覧</PageTitle>
      <div className="filters">
        <select value={type} onChange={(e) => { setType(e.target.value); setPage(0); }}>
          <option value="">すべての種別</option>
          {Object.entries(SLOT_TYPE).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </select>
        <input type="date" value={date} onChange={(e) => { setDate(e.target.value); setPage(0); }} />
      </div>
      <Loading loading={loading} />
      <ErrorMessage message={error} />
      {data && data.items.length === 0 && <p className="muted">予約できる枠がありません。</p>}
      <ul className="list">
        {data?.items.map((s) => (
          <li key={s.id} className="list-item">
            <div>
              <strong>{formatRange(s.start_at, s.end_at)}</strong>
              <div>{s.title} <Badge tone="blue">{SLOT_TYPE[s.type]}</Badge></div>
              <small className="muted">対象: {formatTarget(s)}</small>
            </div>
            <div className="list-side">
              {s.reserved ? <Badge tone="green">予約済み</Badge>
                : s.remaining === 0 ? <Badge tone="red">満席</Badge>
                : <Badge tone="gray">残り {s.remaining}</Badge>}
              <Link to={`/member/slots/${s.id}`} className="btn btn-main">詳細</Link>
            </div>
          </li>
        ))}
      </ul>
      <Pagination data={data} onChange={setPage} />
    </>
  );
}
