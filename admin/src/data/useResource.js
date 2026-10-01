import { useCallback, useEffect, useState } from 'react';
import { fetchResource } from './api.js';

export function useResource(name, deps = []) {
  const [rows, setRows] = useState([]);
  const [source, setSource] = useState('demo');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    const result = await fetchResource(name);
    setRows(result.data);
    setSource(result.source);
    setError(result.error);
    setLoading(false);
  }, [name]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    fetchResource(name).then((result) => {
      if (cancelled) return;
      setRows(result.data);
      setSource(result.source);
      setError(result.error);
      setLoading(false);
    });
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [name, ...deps]);

  return { rows, setRows, source, error, loading, reload };
}

export function filterRows(rows, query, fields) {
  if (!query.trim()) return rows;
  const needle = query.trim().toLowerCase();
  return rows.filter((row) =>
    fields.some((field) => String(row[field] ?? '').toLowerCase().includes(needle)),
  );
}
