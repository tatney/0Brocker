import { useMemo, useState } from 'react';
import { Download, Filter, MessageSquareQuote } from 'lucide-react';
import { filterRows, useResource } from '../data/useResource.js';
import { downloadCsv, formatDate, formatInteger, toCsv } from '../lib/format.js';
import {
  Banner,
  Button,
  DataTable,
  PageHeader,
  Rating,
  SearchInput,
  Select,
  Spinner,
} from '../components/ui.jsx';

const DIMENSIONS = [
  { key: 'quality', label: 'Quality' },
  { key: 'professionalism', label: 'Professionalism' },
  { key: 'timeliness', label: 'Timeliness' },
  { key: 'communication', label: 'Communication' },
  { key: 'value_rating', label: 'Value' },
];

const DIMENSION_KEYS = DIMENSIONS.map((d) => d.key);

export function Reviews() {
  const { rows, source, error, loading } = useResource('reviews');
  const [query, setQuery] = useState('');
  const [rating, setRating] = useState('ALL');

  const visible = useMemo(
    () =>
      filterRows(rows, query, ['comment']).filter((row) => {
        if (rating === 'ALL') return true;
        if (rating === 'LOW') return row.overall <= 3;
        if (rating === 'HIGH') return row.overall >= 4;
        return row.overall === Number(rating);
      }),
    [rows, query, rating],
  );

  const averages = useMemo(() => {
    if (rows.length === 0) return { overall: 0, byDimension: {} };
    const overall = rows.reduce((sum, r) => sum + (r.overall ?? 0), 0) / rows.length;
    const byDimension = DIMENSION_KEYS.reduce((acc, key) => {
      acc[key] = rows.reduce((sum, r) => sum + (r[key] ?? 0), 0) / rows.length;
      return acc;
    }, {});
    return { overall, byDimension };
  }, [rows]);

  const columns = [
    {
      key: 'comment',
      header: 'Feedback',
      render: (row) => (
        <div className="cell-stack">
          <span className="cell-strong">{row.comment || <span className="muted">No comment</span>}</span>
          <span className="muted">Booking #{row.booking_id ?? '—'} · Provider #{row.provider_id ?? '—'}</span>
        </div>
      ),
    },
    ...DIMENSIONS.map((dimension) => ({
      key: dimension.key,
      header: dimension.label,
      align: 'right',
      render: (row) => <Rating value={row[dimension.key] ?? 0} />,
    })),
    { key: 'overall', header: 'Overall', align: 'right', render: (row) => <Rating value={row.overall ?? 0} /> },
    { key: 'date', header: 'Date', align: 'right', render: (row) => formatDate(row.created_at) },
  ];

  const exportCsv = () =>
    downloadCsv(
      '0brocker-reviews.csv',
      toCsv(visible, [
        { label: 'ID', value: (r) => r.id },
        { label: 'Provider', value: (r) => r.provider_id },
        { label: 'Booking', value: (r) => r.booking_id },
        ...DIMENSIONS.map((d) => ({ label: d.label, value: (r) => r[d.key] })),
        { label: 'Overall', value: (r) => r.overall },
        { label: 'Comment', value: (r) => r.comment },
        { label: 'Created', value: (r) => r.created_at },
      ]),
    );

  return (
    <>
      <PageHeader
        title="Reviews"
        subtitle={`${formatInteger(visible.length)} of ${formatInteger(rows.length)} reviews · ${averages.overall.toFixed(2)} average`}
        actions={
          <Button onClick={exportCsv}>
            <Download size={14} aria-hidden="true" /> Export CSV
          </Button>
        }
      />
      {error ? <Banner tone="danger">{error}</Banner> : null}
      {source === 'demo' && !error ? <Banner tone="warn">Showing bundled demo records.</Banner> : null}

      <section className="scoreboard">
        {DIMENSIONS.map((dimension) => (
          <div className="score" key={dimension.key}>
            <span className="score-label">{dimension.label}</span>
            <span className="score-bar" aria-hidden="true">
              <span className="score-fill" style={{ width: `${(averages.byDimension[dimension.key] / 5) * 100}%` }} />
            </span>
            <span className="score-value">{(averages.byDimension[dimension.key] ?? 0).toFixed(1)}</span>
          </div>
        ))}
      </section>

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Search feedback text…" />
        <span className="toolbar-filter">
          <Filter size={14} aria-hidden="true" />
          <Select
            value={rating}
            onChange={setRating}
            options={['ALL', 'HIGH', 'LOW', '5', '4', '3', '2', '1']}
            label="Rating filter"
          />
        </span>
      </div>

      {loading ? (
        <Spinner />
      ) : (
        <DataTable
          columns={columns}
          rows={visible}
          empty="No reviews match these filters."
          rowKey={(row) => row.id}
        />
      )}

      {rows.length === 0 ? (
        <div className="empty-inline">
          <MessageSquareQuote size={18} aria-hidden="true" /> Reviews will appear once customers rate a
          completed job.
        </div>
      ) : null}
    </>
  );
}
