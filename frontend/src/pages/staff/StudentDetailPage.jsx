import { Link, useParams } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api } from '../../lib/api.js';
import { DetailList, ErrorMessage, Loading, PageTitle } from '../../components/ui.jsx';

/** 09 生徒詳細 */
export default function StudentDetailPage() {
  const { id } = useParams();
  const { data: s, error, loading } = useFetch(() => api.get(`/api/students/${id}`), [id]);
  return (
    <>
      <PageTitle back={{ to: '/staff/students', label: '一覧へ戻る' }}>生徒詳細</PageTitle>
      <Loading loading={loading} />
      <ErrorMessage message={error} />
      {s && (
        <div className="card">
          <DetailList
            items={[
              ['氏名', s.name],
              ['保護者氏名', s.guardian_name],
              ['連絡先', s.contact_email],
              ['ログインID', s.login_id],
              ['コース', s.course],
              ['ゲーム', s.game],
              ['レベル', s.level],
              ['振替予約', s.makeup_allowed ? '許可' : '不可'],
              ['状態', s.is_active ? '有効' : '退会'],
            ]}
          />
          <Link to={`/staff/students/${s.id}/edit`} className="btn btn-main">編集</Link>
        </div>
      )}
    </>
  );
}
