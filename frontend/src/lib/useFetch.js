import { useCallback, useEffect, useState } from 'react';

/** 取得処理の状態(data / error / loading)をまとめる。reload で再取得できる */
export function useFetch(fetcher, deps) {
  const [state, setState] = useState({ data: null, error: '', loading: true });

  const load = useCallback(async () => {
    setState((s) => ({ ...s, loading: true, error: '' }));
    try {
      setState({ data: await fetcher(), error: '', loading: false });
    } catch (e) {
      setState({ data: null, error: e.message, loading: false });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    load();
  }, [load]);

  return { ...state, reload: load };
}
