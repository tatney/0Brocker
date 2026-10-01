import { VerifyAction } from '../components/VerifyAction.jsx';
import { useMemo, useState } from 'react';
import { Download, Filter } from 'lucide-react';
import { filterRows, useResource } from '../data/useResource.js';
import { downloadCsv, formatInteger, formatUgx, toCsv } from '../lib/format.js';
import {
  Banner,
  Button,
  DataTable,
  PageHeader,
  Rating,
  SearchInput,
  Select,
  Spinner,
  StatusPill,
} from '../components/ui.jsx';

const CATEGORIES = [
  'ALL', 'CLEANING', 'PLUMBING', 'ELECTRICAL', 'PAINTING', 'REPAIRS',
  'MOVING', 'CARPENTRY', 'AC_REPAIR', 'PEST_CONTROL', 'GARDENING',
  'INTERIOR_DESIGN', 'BEAUTY', 'AUTOMOTIVE', 'APPLIANCE',
];

export function Providers() {
  const { rows, setRows, source, error, loading } = useResource('providers');
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('ALL');
  const [status, setStatus] = useState('ALL');

  const available = useMemo(() => new Set(rows.filter((r) => r.status === 'AVAILABLE').map((r) => r.id)), [rows]);

  const visible = useMemo(
    () =>
      filterRows(rows, query, ['name', 'category', 'headline']).filter(
        (row) => (category === 'ALL' || row.category === category) && (status === 'ALL' || row.status === status),
      ),
    [rows, query, category, status],
  );

  const columns = [
    { key: 'moderation', header: 'Verification', align: 'right', render: (row) => <VerifyAction name="providers" row={row} onSaved={saved=>setRows(current=>current.map(r=>r.id===saved.id?saved:r))}/> },
    { key: 'name', header: 'Provider', render: (row) => <span className="cell-strong">{row.emoji} {row.name}</span> },
    { key: 'category', header: 'Category', render: (row) => <span className="muted">{row.category?.replace(/_/g, ' ').toLowerCase()}</span> },
    { key: 'headline', header: 'Headline', render: (row) => <span className="muted truncate">{row.headline}</span> },
    { key: 'experience', header: 'Exp.', align: 'right', render: (row) => `${formatInteger(row.experience_years)}y` },
    { key: 'jobs', header: 'Jobs', align: 'right', render: (row) => formatInteger(row.jobs_completed) },
    { key: 'rating', header: 'Rating', align: 'right', render: (row) => <Rating value={row.rating} /> },
    { key: 'price', header: 'From', align: 'right', render: (row) => formatUgx(row.price_from) },
    {
      key: 'availability',
      header: 'Availability',
      align: 'right',
      render: (row) => (
        <span className="stack-right">
          <StatusPill status={row.status} />
          {available.has(row.id) ? <span className="dot-online" title="Accepting jobs" /> : null}
        </span>
      ),
    },
  ];

  const exportCsv = () =>
    downloadCsv(
      '0brocker-providers.csv',
      toCsv(visible, [
        { label: 'ID', value: (r) => r.id },
        { label: 'Name', value: (r) => r.name },
        { label: 'Category', value: (r) => r.category },
        { label: 'Rating', value: (r) => r.rating },
        { label: 'Reviews', value: (r) => r.reviews_count },
        { label: 'Jobs completed', value: (r) => r.jobs_completed },
        { label: 'Experience years', value: (r) => r.experience_years },
        { label: 'Price from UGX', value: (r) => r.price_from },
        { label: 'Status', value: (r) => r.status },
        { label: 'Verified', value: (r) => r.is_verified },
      ]),
    );

  return (
    <>
      <PageHeader
        title="Providers"
        subtitle={`${formatInteger(visible.length)} of ${formatInteger(rows.length)} providers`}
        actions={
          <Button onClick={exportCsv}>
            <Download size={14} aria-hidden="true" /> Export CSV
          </Button>
        }
      />
      {error ? <Banner tone="danger">{error}</Banner> : null}
      {source === 'demo' && !error ? <Banner tone="warn">Showing bundled demo records.</Banner> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Search provider, category or headline…" />
        <span className="toolbar-filter">
          <Filter size={14} aria-hidden="true" />
          <Select value={category} onChange={setCategory} options={CATEGORIES} label="Category" />
          <Select value={status} onChange={setStatus} options={['ALL', 'AVAILABLE', 'BUSY', 'SCHEDULED', 'OFFLINE']} label="Status" />
        </span>
      </div>

      {loading ? <Spinner /> : <DataTable columns={columns} rows={visible} empty="No providers match these filters." />}
    </>
  );
}
