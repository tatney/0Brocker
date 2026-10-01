import { isTestWorkspace } from './lib/workspace.js';
import { AdminGate } from './components/AdminGate.jsx';
import { useEffect, useState } from 'react';
import {
  Activity,
  Home,
  LayoutDashboard,
  MessageSquareQuote,
  Star,
  Users,
  Wallet,
  Wrench,
} from 'lucide-react';
import { isSupabaseConfigured, projectRef } from './lib/supabase.js';
import { Overview } from './pages/Overview.jsx';
import { Bookings } from './pages/Bookings.jsx';
import { Providers } from './pages/Providers.jsx';
import { Properties } from './pages/Properties.jsx';
import { People } from './pages/People.jsx';
import { Money } from './pages/Money.jsx';
import { Reviews } from './pages/Reviews.jsx';

const NAV = [
  { id: 'overview', label: 'Overview', icon: LayoutDashboard, component: Overview },
  { id: 'bookings', label: 'Bookings', icon: Wrench, component: Bookings },
  { id: 'providers', label: 'Providers', icon: Star, component: Providers },
  { id: 'properties', label: 'Listings', icon: Home, component: Properties },
  { id: 'people', label: 'People', icon: Users, component: People },
  { id: 'money', label: 'Money', icon: Wallet, component: Money },
  { id: 'reviews', label: 'Reviews', icon: MessageSquareQuote, component: Reviews },
];

function readHash() {
  const raw = window.location.hash.replace(/^#\/?/, '');
  return NAV.some((item) => item.id === raw) ? raw : 'overview';
}

function Workspace() {
  const [active, setActive] = useState(readHash);

  useEffect(() => {
    const onHashChange = () => setActive(readHash());
    window.addEventListener('hashchange', onHashChange);
    return () => window.removeEventListener('hashchange', onHashChange);
  }, []);

  const navigate = (id) => {
    window.location.hash = `/${id}`;
    setActive(id);
  };

  const current = NAV.find((item) => item.id === active) ?? NAV[0];
  const Page = current.component;

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M3 10.5 12 3l9 7.5" />
              <path d="M5 9.5V20h14V9.5" />
            </svg>
          </span>
          <span className="brand-text">
            <strong>0Brocker</strong>
            <span>Admin workspace</span>
          </span>
        </div>

        <nav className="nav">
          {NAV.map(({ id, label, icon: Icon }) => (
            <button
              key={id}
              type="button"
              className={`nav-item${id === active ? ' is-active' : ''}`}
              onClick={() => navigate(id)}
              aria-current={id === active ? 'page' : undefined}
            >
              <Icon size={16} strokeWidth={2.2} aria-hidden="true" />
              <span>{label}</span>
            </button>
          ))}
        </nav>

        <div className={`conn conn-${(isSupabaseConfigured && !isTestWorkspace()) ? 'live' : 'demo'}`}>
          <Activity size={13} aria-hidden="true" />
          <span>
            {(isSupabaseConfigured && !isTestWorkspace()) ? `Live · ${projectRef}` : (isTestWorkspace() ? 'Test workspace - sample data' : 'Demo mode')}
          </span>
        </div>
      </aside>

      <main className="main">
        <Page source={(isSupabaseConfigured && !isTestWorkspace()) ? 'live' : 'demo'} onNavigate={navigate} />
      </main>
    </div>
  );
}

export function App(){return <AdminGate><Workspace/></AdminGate>;}
