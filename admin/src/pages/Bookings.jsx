import { useEffect, useMemo, useState } from 'react';
import { Download, Filter, Radio } from 'lucide-react';
import { filterRows, useResource } from '../data/useResource.js';
import { subscribeToBookings, updateBookingStatus } from '../data/api.js';
import { isSupabaseConfigured } from '../lib/supabase.js';
import { downloadCsv, formatInteger, formatRelative, formatUgx, toCsv } from '../lib/format.js';
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

const STATUSES = ['ALL', 'SEARCHING', 'CONFIRMED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
const NEXT_ACTION = {
  SEARCHING: { label: 'Confirm', value: 'CONFIRMED' },
  CONFIRMED: { label: 'Start', value: 'IN_PROGRESS' },
  IN_PROGRESS: { label: 'Complete', value: 'COMPLETED' },
  COMPLETED: { label: 'Reopen', value: 'IN_PROGRESS' },
};

export function Bookings() {
  const { rows, setRows, source, error, loading, reload } = useResource('bookings');
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('ALL');
  const [pendingId, setPendingId] = useState(null);
  const [notice, setNotice] = useState(null);
  const [live, setLive] = useState(false);

  useEffect(() => {
    if (!isSupabaseConfigured) return undefined;
    const unsubscribe = subscribeToBookings(() => {
      setLive(true);
      reload();
    });
    return unsubscribe;
  }, [reload]);

  const visible = useMemo(
    () => filterRows(rows, query, ['service_type', 'provider_name', 'location_text', 'description']).filter((row) => status === 'ALL' || row.status === status),
    [rows, query, status],
  );

  const revenue = useMemo(
    () => rows.filter((r) => r.is_paid).reduce((sum, r) => sum + (r.total_cost ?? 0), 0),
    [rows],
  );

  const advance = async (row) => {
    const action = NEXT_ACTION[row.status];
    if (!action) return;
    setPendingId(row.id);
    setNotice(null);

    const result = await updateBookingStatus(row.id, action.value);

    if (result.error) {
      setNotice({ tone: 'danger', text: result.error });
    } else {
      setRows((current) => current.map((r) => (r.id === row.id ? { ...r, ...result.data } : r)));
      setNotice({ tone: 'success', text: `Booking #${row.id} marked ${action.value.toLowerCase().replace(/_/g, ' ')}.` });
    }
    setPendingId(null);
  };

  const columns = [
    { key: 'id', header: '#', render: (row) => <span className="muted">#{row.id}</span> },
    {
      key: 'service',
      header: 'Service',
      render: (row) => (
        <div className="cell-stack">
          <span className="cell-strong">{row.provider_emoji} {row.service_type?.replace(/_/g, ' ').toLowerCase()}</span>
          <span className="muted truncate">{row.description}</span>
        </div>
      ),
    },
    { key: 'provider', header: 'Provider', render: (row) => row.provider_name || <span className="muted">Unassigned</span> },
    { key: 'location', header: 'Location', render: (row) => <span className="muted truncate">{row.location_text}</span> },
    { key: 'status', header: 'Status', render: (row) => <StatusPill status={row.status} /> },
    { key: 'paid', header: 'Paid', align: 'right', render: (row) => (row.is_paid ? <span className="tick">{row.payment_method || 'Yes'}</span> : <span className="muted">Unpaid</span>) },
    { key: 'total', header: 'Total', align: 'right', render: (row) => formatUgx(row.total_cost ?? 0) },
    { key: 'age', header: 'Age', align: 'right', render: (row) => formatRelative(row.created_at) },
    {
      key: 'actions',
      header: '',
      align: 'right',
      render: (row) => {
        const action = NEXT_ACTION[row.status];
        if (!action) return <span className="muted">—</span>;
        return (
          <Button
            onClick={() => advance(row)}
            disabled={pendingId === row.id}
          >
            {pendingId === row.id ? 'Saving…' : action.label}
          </Button>
        );
      },
    },
  ];

  const exportCsv = () =>
    downloadCsv(
      '0brocker-bookings.csv',
      toCsv(visible, [
        { label: 'ID', value: (r) => r.id },
        { label: 'Service', value: (r) => r.service_type },
        { label: 'Provider', value: (r) => r.provider_name },
        { label: 'Location', value: (r) => r.location_text },
        { label: 'Status', value: (r) => r.status },
        { label: 'Total UGX', value: (r) => r.total_cost },
        { label: 'Paid', value: (r) => r.is_paid },
        { label: 'Payment method', value: (r) => r.payment_method },
        { label: 'Created', value: (r) => r.created_at },
      ]),
    );

  return (
    <>
      <PageHeader
        title="Bookings"
        subtitle={`${formatInteger(visible.length)} of ${formatInteger(rows.length)} bookings · ${formatUgx(revenue)} collected`}
        actions={
          <>
            {live ? <span className="live-chip"><Radio size={13} aria-hidden="true" /> Live</span> : null}
            <Button onClick={exportCsv}>
              <Download size={14} aria-hidden="true" /> Export CSV
            </Button>
          </>
        }
      />
      {error ? <Banner tone="danger">{error}</Banner> : null}
      {source === 'demo' && !error ? (
        <Banner tone="warn">Showing bundled demo records. Status changes require a configured Supabase project.</Banner>
      ) : null}
      {notice ? <Banner tone={notice.tone}>{notice.text}</Banner> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Search service, provider or area…" />
        <span className="toolbar-filter">
          <Filter size={14} aria-hidden="true" />
          <Select value={status} onChange={setStatus} options={STATUSES} label="Booking status" />
        </span>
      </div>

      {loading ? <Spinner /> : <DataTable columns={columns} rows={visible} empty="No bookings match these filters." />}
    </>
  );
}
