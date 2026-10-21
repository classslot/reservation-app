// バックエンドAPIを呼び出す共通処理。Cookie(セッション)を送るため credentials: 'include' を指定する。
const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '';

export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

async function request(method, path, body) {
  let res;
  try {
    res = await fetch(BASE_URL + path, {
      method,
      credentials: 'include',
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'サーバーに接続できません。しばらくしてからもう一度お試しください');
  }
  if (res.status === 204) return null;
  let data = null;
  try {
    data = await res.json();
  } catch {
    /* 本文なし */
  }
  if (!res.ok) throw new ApiError(res.status, data?.message ?? 'エラーが発生しました');
  return data;
}

export const api = {
  get: (path) => request('GET', path),
  post: (path, body) => request('POST', path, body ?? {}),
  patch: (path, body) => request('PATCH', path, body),
};

/** 空の値を除いてクエリ文字列を作る（先頭の ? 付き） */
export function toQuery(params) {
  const q = new URLSearchParams(
    Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined),
  ).toString();
  return q ? `?${q}` : '';
}
