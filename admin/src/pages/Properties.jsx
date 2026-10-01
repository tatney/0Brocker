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

const SEARCH_FIELDS = ['title', 'location', 'city'];
const LISTING_TYPES = ['ALL', 'RENT', 'BUY', 'PG', 'PLOT'];
const ASSET_TYPES = ['ALL', 'HOUSE', 'LAND', 'CAR'];

export function Properties() {
  const { rows, source, error, loading } = useResource('properties');
  const [query, setQuery] = useState('');
  const [listingType, setListingType] = useState('ALL');
  const [assetType, setAssetType] = useState('ALL');

  const visible = useMemo(
    () =>
      filterRows(rows, query, SEARCH_FIELDS).filter(
        (row) =>
          (listingType === 'ALL' || row.listing_type === listingType) &&
          (assetType === 'ALL' || row.asset_type === assetType),
      ),
    [rows, query, listingType, assetType],
  );

  const columns = [
    { key: 'title', header: 'Listing', render: (row) => <span className="cell-strong">{row.emoji} {row.title}</span> },
    { key: 'location', header: 'Location', render: (row) => <span className="muted">{row.location}, {row.city}</span> },
    { key: 'listing_type', header: 'Type', render: (row) => <StatusPill status={row.listing_type} /> },
    { key: 'asset', header: 'Asset', render: (row) => <span className="muted">{row.asset_type?.toLowerCase()}</span> },
    { key: 'price', header: 'Price', align: 'right', render: (row) => `${formatUgx(row.price_ugx)} ${row.price_label ?? ''}`.trim() },
    { key: 'rating', header: 'Rating', align: 'right', render: (row) => <Rating value={row.rating} /> },
    { key: 'verified', header: 'Verified', align: 'right', render: (row) => (row.is_verified ? <span className="tick">Yes</span> : <span className="muted">No</span>) },
  ];

  const exportCsv = () =>
    downloadCsv(
      '0brocker-properties.csv',
      toCsv(visible, [
        { label: 'ID', value: (r) => r.id },
        { label: 'Title', value: (r) => r.title },
        { label: 'Location', value: (r) => r.location },
        { label: 'City', value: (r) => r.city },
        { label: 'Listing type', value: (r) => r.listing_type },
        { label: 'Asset type', value: (r) => r.asset_type },
        { label: 'Price UGX', value: (r) => r.price_ugx },
        { label: 'Rating', value: (r) => r.rating },
        { label: 'Verified', value: (r) => r.is_verified },
      ]),
    );

  return (
    <>
      <PageHeader
        title="Listings"
        subtitle={`${formatInteger(visible.length)} of ${formatInteger(rows.length)} listings`}
        actions={
          <Button onClick={exportCsv}>
            <Download size={14} aria-hidden="true" /> Export CSV
          </Button>
        }
      />
      {error ? <Banner tone="danger">{error}</Banner> : null}
      {source === 'demo' && !error ? <Banner tone="warn">Showing bundled demo records.</Banner> : null}

      <div className="toolbar">
        <SearchInput value={query} onChange={setQuery} placeholder="Search title, area or city…" />
        <span className="toolbar-filter">
          <Filter size={14} aria-hidden="true" />
          <Select value={listingType} onChange={setListingType} options={LISTING_TYPES} label="Listing type" />
          <Select value={assetType} onChange={setAssetType} options={ASSET_TYPES} label="Asset type" />
        </span>
      </div>

      {loading ? <Spinner /> : <DataTable columns={columns} rows={visible} empty="No listings match these filters." />}
    </>
  );
}
