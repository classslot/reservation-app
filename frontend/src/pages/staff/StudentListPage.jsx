import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api, toQuery } from '../../lib/api.js';
import { Badge, ErrorMessage, Loading, PageTitle, Pagination } from '../../components/ui.jsx';

/** 08 生徒一覧（氏名で検索） */
export default function StudentListPage() {
  const [input, setInput] = useState('');
  const [q, setQ] = useState('');
  const [page, setPage] = useState(0);
  const { data, error, loading } = useFetch(() => api.get('/api/students' + toQuery({ q, page })), [q, page]);

  return (
    <>
      <PageTitle back={{ to: '/staff', label: 'ホームへ戻る' }}>生徒一覧</PageTitle>
      <form className="filters" onSubmit={(e) => { e.preventDefault(); setQ(input); setPage(0); }}>
        <input placeholder="氏名で検索" value={input} onChange={(e) => setInput(e.target.value)} />
        <button className="btn btn-sub">検索</button>
        <Link to="/staff/students/new" className="btn btn-main">新規登録</Link>
      </form>
      <Loading loading={loading} />
      <ErrorMessage message={error} />
      {data && data.items.length === 0 && <p className="muted">該当する生徒がいません。</p>}
      {data && data.items.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead><tr><th>氏名</th><th>コース</th><th>状態</th><th></th></tr></thead>
            <tbody>
              {data.items.map((s) => (
                <tr key={s.id}>
                  <td>{s.name}</td>
                  <td>{s.course}</td>
                  <td>{s.is_active ? <Badge tone="green">有効</Badge> : <Badge tone="gray">退会</Badge>}</td>
                  <td><Link to={`/staff/students/${s.id}`} className="btn btn-sub">詳細</Link></td>
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
