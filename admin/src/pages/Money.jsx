import { summarizeMoney } from '../lib/metrics.js';
import { useMemo, useState } from 'react';
import { ArrowDownLeft, ArrowUpRight, Download, Wallet } from 'lucide-react';
import { useResource } from '../data/useResource.js';
import { downloadCsv, formatDate, formatInteger, formatSignedMinor, toCsv } from '../lib/format.js';
import { Banner, Button, DataTable, PageHeader, Spinner } from '../components/ui.jsx';

export function Money() {
  const { rows, source, error, loading } = useResource('transactions');

  const totals = useMemo(() => {
    const credits = rows.filter((r) => r.is_credit);
    const debits = rows.filter((r) => !r.is_credit);
    const sum = (list) => list.reduce((acc, r) => acc + Math.abs(r.amount_minor), 0);
    return {
      credits: sum(credits),
      debits: sum(debits),
      count: rows.length,
      net: sum(credits) - sum(debits),
    };
  }, [rows]);

  const columns = [
    {
      key: 'title',
      header: 'Description',
      render: (row) => (
        <div className="cell-stack">
          <span className="cell-strong">{row.title}</span>
          {row.meta ? <span className="muted truncate">{row.meta}</span> : null}
        </div>
      ),
    },
    {
      key: 'direction',
      header: 'Type',
      render: (row) =>
        row.is_credit ? (
          <span className="pill pill-success"><ArrowDownLeft size={12} aria-hidden="true" /> Credit</span>
        ) : (
          <span className="pill pill-danger"><ArrowUpRight size={12} aria-hidden="true" /> Debit</span>
        ),
    },
    {
      key: 'amount',
      header: 'Amount',
      align: 'right',
      render: (row) => (
        <span className={row.is_credit ? 'amount-credit' : 'amount-debit'}>
          {formatSignedMinor(row.is_credit ? Math.abs(row.amount_minor) : -Math.abs(row.amount_minor))}
        </span>
      ),
    },
    { key: 'date', header: 'Date', align: 'right', render: (row) => formatDate(row.created_at) },
  ];

  const exportCsv = () =>
    downloadCsv(
      '0brocker-transactions.csv',
      toCsv(rows, [
        { label: 'ID', value: (r) => r.id },
        { label: 'Title', value: (r) => r.title },
        { label: 'Meta', value: (r) => r.meta },
        { label: 'Amount minor', value: (r) => r.amount_minor },
        { label: 'Credit', value: (r) => r.is_credit },
        { label: 'Created', value: (r) => r.created_at },
      ]),
    );

  return (
    <>
      <PageHeader
        title="Money"
        subtitle={`${formatInteger(totals.count)} wallet transactions`}
        actions={
          <Button onClick={exportCsv}>
            <Download size={14} aria-hidden="true" /> Export CSV
          </Button>
        }
      />
      {error ? <Banner tone="danger">{error}</Banner> : null}
      {source === 'demo' && !error ? <Banner tone="warn">Showing bundled demo records.</Banner> : null}

      <section className="stat-grid stat-grid-3">
        <div className="stat-card tone-success">
          <div className="stat-head"><span className="stat-label">Credits in</span><span className="stat-icon"><ArrowDownLeft size={16} aria-hidden="true" /></span></div>
          <div className="stat-value amount-credit">{formatSignedMinor(totals.credits)}</div>
        </div>
        <div className="stat-card tone-danger">
          <div className="stat-head"><span className="stat-label">Debits out</span><span className="stat-icon"><ArrowUpRight size={16} aria-hidden="true" /></span></div>
          <div className="stat-value amount-debit">{formatSignedMinor(-totals.debits)}</div>
        </div>
        <div className="stat-card tone-accent">
          <div className="stat-head"><span className="stat-label">Net flow</span><span className="stat-icon"><Wallet size={16} aria-hidden="true" /></span></div>
          <div className="stat-value">{formatSignedMinor(totals.net)}</div>
        </div>
      </section>

      {loading ? <Spinner /> : <DataTable columns={columns} rows={rows} empty="No wallet activity yet." />}
    </>
  );
}
