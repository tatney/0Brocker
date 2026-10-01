const groupFormatter = new Intl.NumberFormat('en-US');

export function formatInteger(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  return groupFormatter.format(Math.round(value));
}

export function formatUgx(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  return `UGX ${groupFormatter.format(Math.round(value))}`;
}

export function formatMinor(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  return formatUgx(Math.round(value) / 100);
}

export function formatSignedMinor(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  const sign = value < 0 ? '-' : '+';
  return `${sign}${formatUgx(Math.abs(Math.round(value)) / 100)}`;
}

export function formatOneDecimal(value) {
  if (value === null || value === undefined || Number.isNaN(value)) return '—';
  return (Math.round(value * 10) / 10).toFixed(1);
}

export function formatDate(value) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatRelative(value) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '—';
  const diffSeconds = Math.round((Date.now() - date.getTime()) / 1000);
  const units = [
    ['year', 31536000],
    ['month', 2592000],
    ['day', 86400],
    ['hour', 3600],
    ['minute', 60],
  ];
  for (const [unit, seconds] of units) {
    const amount = Math.floor(diffSeconds / seconds);
    if (amount >= 1) return `${amount} ${unit}${amount > 1 ? 's' : ''} ago`;
  }
  return 'just now';
}

export function titleCase(value) {
  if (!value) return '—';
  return String(value)
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (c) => c.toUpperCase());
}

export function toCsv(rows, columns) {
  const escape = (cell) => {
    const text = cell === null || cell === undefined ? '' : String(cell);
    return /[",\n]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
  };
  const header = columns.map((c) => escape(c.label)).join(',');
  const body = rows.map((row) => columns.map((c) => escape(c.value(row))).join(','));
  return [header, ...body].join('\n');
}

export function downloadCsv(filename, csv) {
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}
