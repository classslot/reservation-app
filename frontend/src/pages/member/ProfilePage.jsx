import { useFetch } from '../../lib/useFetch.js';
import { api } from '../../lib/api.js';
import { DetailList, ErrorMessage, Loading, PageTitle } from '../../components/ui.jsx';

/** 03 登録情報（閲覧のみ。変更はスタッフに依頼） */
export default function ProfilePage() {
  const { data, error, loading } = useFetch(() => api.get('/api/me'), []);
  const s = data?.student;
  return (
    <>
      <PageTitle back={{ to: '/member', label: 'ホームへ戻る' }}>登録情報</PageTitle>
      <Loading loading={loading} />
      <ErrorMessage message={error} />
      {s && (
        <div className="card">
          <DetailList
            items={[
              ['氏名', s.name],
              ['保護者氏名', s.guardian_name],
              ['連絡先', s.contact_email],
              ['コース', s.course],
              ['ゲーム', s.game],
              ['レベル', s.level],
              ['振替予約', s.makeup_allowed ? '許可' : '不可'],
            ]}
          />
          <p className="muted">登録情報の変更はスタッフにご依頼ください。</p>
        </div>
      )}
    </>
  );
}
