export const SLOT_TYPE = { MAKEUP: '振替レッスン', EVENT: 'イベント' };
export const SLOT_STATUS = { OPEN: '受付中', CLOSED: '受付停止' };
export const RESERVATION_STATUS = { CONFIRMED: '予約確定', CANCELLED: '取消済み' };

const WEEK = ['日', '月', '火', '水', '木', '金', '土'];

/** "2026-10-25T16:00:00" → "10/25(日) 16:00" */
export function formatDateTime(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, '0');
  return `${d.getMonth() + 1}/${d.getDate()}(${WEEK[d.getDay()]}) ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function formatRange(startIso, endIso) {
  return `${formatDateTime(startIso)}〜${endIso ? endIso.slice(11, 16) : ''}`;
}

/** datetime-local の入力値（YYYY-MM-DDTHH:mm）に揃える */
export const toInputDateTime = (iso) => (iso ? iso.slice(0, 16) : '');

/** 対象条件の表示。未指定なら「全員」 */
export function formatTarget(slot) {
  const parts = [slot.target_course, slot.target_game, slot.target_level].filter(Boolean);
  return parts.length ? parts.join(' / ') : '全員';
}
