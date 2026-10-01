import { formatOneDecimal } from '../lib/format.js';

const STATUS_TONES = {
  COMPLETED: 'success',
  CONFIRMED: 'info',
  IN_PROGRESS: 'accent',
  SEARCHING: 'warn',
  PENDING: 'warn',
  BUSY: 'warn',
  AVAILABLE: 'success',
  OFFLINE: 'muted',
  CANCELLED: 'danger',
  DECLINED: 'danger',
  ACTIVE: 'success',
};

export function StatusPill({ status }) {
  if (!status) return <span className="pill pill-muted">Unknown</span>;
  const tone = STATUS_TONES[String(status).toUpperCase()] ?? 'muted';
  const label = String(status).replace(/_/g, ' ').toLowerCase();
  return <span className={`pill pill-${tone}`}>{label}</span>;
}

export function StatCard({ label, value, hint, icon: Icon, tone = 'default', onClick }) {
  const interactive = Boolean(onClick);
  return (
    <div
      className={`stat-card tone-${tone}${interactive ? ' is-clickable' : ''}`}
      onClick={onClick}
      onKeyDown={interactive ? (event) => event.key === 'Enter' && onClick() : undefined}
      role={interactive ? 'button' : undefined}
      tabIndex={interactive ? 0 : undefined}
    >
      <div className="stat-head">
        <span className="stat-label">{label}</span>
        {Icon ? (
          <span className="stat-icon">
            <Icon size={16} strokeWidth={2.2} aria-hidden="true" />
          </span>
        ) : null}
      </div>
      <div className="stat-value">{value}</div>
      {hint ? <div className="stat-hint">{hint}</div> : null}
    </div>
  );
}

export function Rating({ value }) {
  return (
    <span className="rating">
      <span aria-hidden="true">★</span>
      {formatOneDecimal(value)}
    </span>
  );
}

export function SearchInput({ value, onChange, placeholder = 'Search…' }) {
  return (
    <input
      className="search"
      type="search"
      value={value}
      placeholder={placeholder}
      aria-label={placeholder}
      onChange={(event) => onChange(event.target.value)}
    />
  );
}

export function Select({ value, onChange, options, label }) {
  return (
    <select
      className="select"
      value={value}
      aria-label={label}
      onChange={(event) => onChange(event.target.value)}
    >
      {options.map((option) => (
        <option key={option} value={option}>
          {option === 'ALL' ? 'All' : option.replace(/_/g, ' ').toLowerCase()}
        </option>
      ))}
    </select>
  );
}

export function Button({ children, onClick, variant = 'ghost', disabled, type = 'button' }) {
  return (
    <button
      type={type}
      className={`btn btn-${variant}`}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
}

export function DataTable({ columns, rows, empty = 'Nothing to show yet.', rowKey }) {
  if (rows.length === 0) {
    return <div className="empty">{empty}</div>;
  }
  return (
    <div className="table-wrap">
      <table className="table">
        <thead>
          <tr>
            {columns.map((column) => (
              <th key={column.key} className={column.align === 'right' ? 'right' : undefined}>
                {column.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr key={rowKey ? rowKey(row) : row.id ?? index}>
              {columns.map((column) => (
                <td key={column.key} className={column.align === 'right' ? 'right' : undefined}>
                  {column.render(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function Banner({ tone = 'info', children }) {
  return <div className={`banner banner-${tone}`}>{children}</div>;
}

export function Spinner({ label = 'Loading…' }) {
  return (
    <div className="spinner" role="status">
      <span className="spinner-ring" aria-hidden="true" />
      <span>{label}</span>
    </div>
  );
}

export function PageHeader({ title, subtitle, actions }) {
  return (
    <header className="page-header">
      <div>
        <h1>{title}</h1>
        {subtitle ? <p>{subtitle}</p> : null}
      </div>
      {actions ? <div className="page-actions">{actions}</div> : null}
    </header>
  );
}
