import { useMemo, useState } from 'react';
import { Download, Filter } from 'lucide-react';
import { filterRows, useResource } from '../data/useResource.js';
import { downloadCsv, formatDate, formatInteger, toCsv } from '../lib/format.js';
import {
  Banner,
  Button,
  DataTable,
  PageHeader,
  SearchInput,
  Select,
  Spinner,
  StatusPill,
} from '../components/ui.jsx';

export function People() {
  const { rows, source, error, loading } = useResource('users');
  const [query, setQuery] = useState('');
  const [kind, setKind] = useState('ALL');

  const visible = useMemo(
    () =>
      filterRows(rows, query, ['full_name', 'email', 'phone']).filter((row) => {
        if (kind === 'ALL') return true;
        if (kind === 'PROFESSIONAL') return Boolean(row.is_professional);
        return !row.is_professional;
      }),
    [rows, query, kind],
  );

  const columns = [
    {
      key: 'name',
      header: 'User',
      render: (row) => (
        <span className="cell-strong">{row.avatar_emoji} {row.full_name}</span>
      ),
    },
    { key: 'email', header: 'Email', render: (row) => row.email || <span className="muted">—</span> },
    { key: 'phone', header: 'Phone', render: (row) => <span className="muted">{row.phone}</span> },
    { key: 'kind', header: 'Account', render: (row) => <StatusPill status={row.is_professional ? 'PRO' : 'CUSTOMER'} /> },
    { key: 'joined', header: 'Joined', align: 'right', render: (row) => formatDate(row.created_at) },
  ];

  const exportCsv = () =>
    downloadCsv(
      '0brocker-users.csv',
      toCsv(visible, [
        { label: 'ID', value: (r) => r.id },
        { label: 'Full name', value: (r) => r.full_name },
        { label: 'Email', value: (r) => r.email },
        { label: 'Phone', value: (r) => r.phone },
        { label: 'Professional', value: (r) => r.is_professional },
        { label: 'Joined', value: (r) => r.created_at },
      ]),
    );

  return (
    <>
      <PageHeader
        title="People"
        subtitle={`${formatInteger(visible.length)} of ${formatInteger(rows.length)} accounts`}
        actions={
          <Button onClick={exportCsv}>
            <Download size={14} aria-hidden="true" /> Export CSV
          </Button>
        }
      />
      {error ? <Banner tone="danger">{error}</Banner> : null}
      {source === 'demo' && !error ? <Banner tone="warn">Showing bundled demo records.</Banner> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Search name, email or phone…" />
        <span className="toolbar-filter">
          <Filter size={14} aria-hidden="true" />
          <Select value={kind} onChange={setKind} options={['ALL', 'PROFESSIONAL', 'CUSTOMER']} label="Account type" />
        </span>
      </div>

      {loading ? <Spinner /> : <DataTable columns={columns} rows={visible} empty="No accounts match these filters." />}
    </>
  );
}
