import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useFetch } from '../../lib/useFetch.js';
import { api } from '../../lib/api.js';
import { SLOT_TYPE, formatRange, formatTarget } from '../../lib/format.js';
import { DetailList, ErrorMessage, Loading, PageTitle } from '../../components/ui.jsx';

/** 05 予約枠詳細・予約確認 */
export default function SlotDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: slot, error, loading } = useFetch(() => api.get(`/api/slots/${id}`), [id]);
  const [actionError, setActionError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleReserve = async () => {
    if (!window.confirm(`「${slot.title}」を予約しますか？`)) return;
    setActionError('');
    setSubmitting(true);
    try {
      await api.post('/api/reservations', { slot_id: slot.id });
      navigate('/member/reservations');
    } catch (e) {
      setActionError(e.message); // 満席・重複・受付停止などの理由を表示
    } finally {
      setSubmitting(false);
    }
  };

  const canReserve = slot && slot.status === 'OPEN' && slot.remaining > 0 && !slot.reserved;
  return (
    <>
      <PageTitle back={{ to: '/member/slots', label: '予約枠一覧へ' }}>予約枠の詳細</PageTitle>
      <Loading loading={loading} />
      <ErrorMessage message={error} />
      {slot && (
        <div className="card">
          <DetailList
            items={[
              ['種別', SLOT_TYPE[slot.type]],
              ['タイトル', slot.title],
              ['日時', formatRange(slot.start_at, slot.end_at)],
              ['担当者', slot.teacher_name],
              ['対象', formatTarget(slot)],
              ['残席', `${slot.remaining} / ${slot.capacity}`],
            ]}
          />
          <ErrorMessage message={actionError} />
          {slot.reserved && <p className="alert alert-info">この枠は予約済みです。</p>}
          <button className="btn btn-main" disabled={!canReserve || submitting} onClick={handleReserve}>
            {slot.remaining === 0 ? '満席です' : '予約する'}
          </button>
        </div>
      )}
    </>
  );
}
