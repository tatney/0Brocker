import { supabase, isSupabaseConfigured } from '../lib/supabase.js';
import { demoData } from './demo.js';

const RESOURCES = {
  properties: { table: 'properties', order: 'rating', ascending: false },
  providers: { table: 'service_providers', order: 'rating', ascending: false },
  bookings: { table: 'marketplace_bookings', order: 'created_at', ascending: false },
  users: { table: 'users', order: 'created_at', ascending: false },
  transactions: { table: 'wallet_transactions', order: 'created_at', ascending: false },
  reviews: { table: 'reviews', order: 'created_at', ascending: false },
};

export const resourceNames = Object.keys(RESOURCES);

export async function fetchResource(name, { limit = 500 } = {}) {
  if (!isSupabaseConfigured) {
    return { data: demoData[name] ?? [], source: 'demo', error: null };
  }

  const config = RESOURCES[name];
  const query = supabase
    .from(config.table)
    .select('*')
    .order(config.order, { ascending: config.ascending })
    .limit(limit);

  const { data, error } = await query;

  if (error) {
    return { data: demoData[name] ?? [], source: 'demo', error: error.message };
  }
  return { data: data ?? [], source: 'live', error: null };
}

export async function updateBookingStatus(id, status) {
  if (!isSupabaseConfigured) {
    return { data: null, error: 'Supabase is not configured, so this is a demo read-only view.' };
  }
  const patch = { status };
  if (status === 'COMPLETED') patch.completed_at = new Date().toISOString();
  if (status === 'IN_PROGRESS') patch.started_at = new Date().toISOString();

  const { data, error } = await supabase
    .from('marketplace_bookings')
    .update(patch)
    .eq('id', id)
    .select()
    .single();

  return { data, error: error?.message ?? null };
}

export function subscribeToBookings(onChange) {
  if (!isSupabaseConfigured) return () => {};
  const channel = supabase
    .channel('admin-bookings')
    .on(
      'postgres_changes',
      { event: '*', schema: 'public', table: 'marketplace_bookings' },
      onChange,
    )
    .subscribe();

  return () => {
    supabase.removeChannel(channel);
  };
}
