import { Link } from 'react-router-dom';

export function ErrorMessage({ message }) {
  return message ? <p className="alert alert-error" role="alert">{message}</p> : null;
}

export function Loading({ loading }) {
  return loading ? <p className="muted">読み込み中...</p> : null;
}

export function PageTitle({ children, back }) {
  return (
    <div className="page-title">
      <h1>{children}</h1>
      {back && <Link to={back.to} className="btn btn-sub">{back.label ?? '戻る'}</Link>}
    </div>
  );
}

export function Badge({ tone = 'gray', children }) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}

export function Pagination({ data, onChange }) {
  if (!data || data.total_pages <= 1) return null;
  return (
    <div className="pagination">
      <button className="btn btn-sub" disabled={data.page === 0} onClick={() => onChange(data.page - 1)}>前へ</button>
      <span>{data.page + 1} / {data.total_pages}</span>
      <button className="btn btn-sub" disabled={data.page + 1 >= data.total_pages} onClick={() => onChange(data.page + 1)}>次へ</button>
    </div>
  );
}

export function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  );
}

export function DetailList({ items }) {
  return (
    <dl className="detail">
      {items.map(([k, v]) => (
        <div key={k}>
          <dt>{k}</dt>
          <dd>{v}</dd>
        </div>
      ))}
    </dl>
  );
}
