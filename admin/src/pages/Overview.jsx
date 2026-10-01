import { useMemo, useState } from 'react';
import {
  Banknote,
  Building2,
  Home,
  Star,
  TrendingUp,
  Users,
  Wrench,
} from 'lucide-react';
import { useResource } from '../data/useResource.js';
import { formatInteger, formatMinor, formatRelative, formatUgx } from '../lib/format.js';
import { Banner, Button, DataTable, PageHeader, Rating, StatCard, StatusPill } from '../components/ui.jsx';

export function Overview({ source, onNavigate }) {
  const { rows: properties } = useResource('properties');
  const { rows: providers } = useResource('providers');
  const { rows: bookings } = useResource('bookings');
  const { rows: users } = useResource('users');
  const { rows: transactions } = useResource('transactions');
  const { rows: reviews } = useResource('reviews');

  const stats = useMemo(() => {
    const open = bookings.filter((b) => ['SEARCHING', 'CONFIRMED', 'IN_PROGRESS'].includes(b.status));
    const revenue = transactions.filter((t) => t.is_credit).reduce((sum, t) => sum + t.amount_minor, 0);
    const spend = transactions.filter((t) => !t.is_credit).reduce((sum, t) => sum + t.amount_minor, 0);
    const avgRating = providers.length
      ? providers.reduce((sum, p) => sum + p.rating, 0) / providers.length
      : 0;
    const verified = properties.filter((p) => p.is_verified).length;
    const professionals = users.filter((u) => u.is_professional).length;
    return { open, revenue, spend, avgRating, verified, professionals };
  }, [bookings, transactions, providers, properties, users]);

  const recent = [...bookings]
    .sort((a, b) => new Date(b.created_at) - new Date(a.created_at))
    .slice(0, 6);

  const topProviders = [...providers].sort((a, b) => b.jobs_completed - a.jobs_completed).slice(0, 5);

  return (
    <>
      <PageHeader
        title="Overview"
        subtitle="Live operational picture across listings, providers and bookings."
        actions={
          <Button variant="primary" onClick={() => onNavigate('bookings')}>
            Review queue
          </Button>
        }
      />

      {source === 'demo' ? (
        <Banner tone="warn">
          <strong>Demo data.</strong> Supabase environment variables are not set, so this workspace is
          showing bundled sample records. Add <code>VITE_SUPABASE_URL</code> and{' '}
          <code>VITE_SUPABASE_ANON_KEY</code> to connect the live database.
        </Banner>
      ) : null}

      <section className="stat-grid">
        <StatCard
          label="Open bookings"
          value={formatInteger(stats.open.length)}
          hint="Awaiting assignment or in progress"
          icon={Wrench}
          tone="accent"
          onClick={() => onNavigate('bookings')}
        />
        <StatCard
          label="Active listings"
          value={formatInteger(properties.length)}
          hint={`${formatInteger(stats.verified)} verified`}
          icon={Home}
        />
        <StatCard
          label="Providers"
          value={formatInteger(providers.length)}
          hint={`Avg rating ${stats.avgRating.toFixed(1)}`}
          icon={Star}
          tone="info"
          onClick={() => onNavigate('providers')}
        />
        <StatCard
          label="Registered users"
          value={formatInteger(users.length)}
          hint={`${formatInteger(stats.professionals)} professionals`}
          icon={Users}
          onClick={() => onNavigate('people')}
        />
        <StatCard
          label="Gross volume"
          value={formatMinor(stats.revenue)}
          hint="Credits recorded"
          icon={Banknote}
          tone="success"
          onClick={() => onNavigate('money')}
        />
        <StatCard
          label="Avg rating"
          value={stats.avgRating ? stats.avgRating.toFixed(2) : '—'}
          hint={`${formatInteger(reviews.length)} reviews collected`}
          icon={TrendingUp}
          tone="warn"
        />
      </section>

      <div className="split">
        <section className="panel">
          <div className="panel-head">
            <h2>Latest bookings</h2>
            <Button onClick={() => onNavigate('bookings')}>View all</Button>
          </div>
          <DataTable
            rows={recent}
            columns={[
              {
                key: 'service',
                header: 'Service',
                render: (row) => (
                  <span className="cell-strong">
                    {row.provider_emoji} {row.service_type?.replace(/_/g, ' ').toLowerCase()}
                  </span>
                ),
              },
              { key: 'provider', header: 'Provider', render: (row) => row.provider_name || <span className="muted">Unassigned</span> },
              { key: 'status', header: 'Status', render: (row) => <StatusPill status={row.status} /> },
              { key: 'total', header: 'Total', align: 'right', render: (row) => formatUgx(row.total_cost ?? 0) },
              { key: 'age', header: 'Age', align: 'right', render: (row) => formatRelative(row.created_at) },
            ]}
            empty="No bookings yet."
          />
        </section>

        <section className="panel">
          <div className="panel-head">
            <h2>Busiest providers</h2>
            <Button onClick={() => onNavigate('providers')}>View all</Button>
          </div>
          <DataTable
            rows={topProviders}
            columns={[
              { key: 'name', header: 'Provider', render: (row) => <span className="cell-strong">{row.emoji} {row.name}</span> },
              { key: 'category', header: 'Category', render: (row) => <span className="muted">{row.category?.replace(/_/g, ' ').toLowerCase()}</span> },
              { key: 'jobs', header: 'Jobs', align: 'right', render: (row) => formatInteger(row.jobs_completed) },
              { key: 'rating', header: 'Rating', align: 'right', render: (row) => <Rating value={row.rating} /> },
            ]}
            empty="No providers yet."
          />
          <div className="panel-foot">
            <Building2 size={14} aria-hidden="true" />
            <span>
              Net flow {formatMinor(stats.revenue - stats.spend)} across {formatInteger(transactions.length)}{' '}
              transactions.
            </span>
          </div>
        </section>
      </div>
    </>
  );
}
