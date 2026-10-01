-- 0Brocker / HomeApp — Supabase schema
--
-- Mirrors the local-first SQLDelight schema in
-- shared/src/commonMain/sqldelight/com/homeapp/db/HomeAppDatabase.sq
--
-- Deliberate differences from the local SQLite schema:
--   * ids are server-generated (bigint identity) instead of client-assigned
--   * timestamps are timestamptz instead of integer epoch seconds
--   * boolean flags are real booleans instead of INTEGER 0/1
--   * users.password_hash is GONE — password custody belongs to Supabase Auth
--     (see shared/.../data/security/PasswordHasher.kt, a demo-grade hash that
--     must never hold a real credential)

-- ============================================================================
-- Extensions
-- ============================================================================
create extension if not exists "pgcrypto";

-- ============================================================================
-- Shared helpers
-- ============================================================================

-- True when the calling JWT belongs to an administrator. Admins are flagged via
-- app_metadata (not user_metadata) because user_metadata is user-writable.
create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
  select coalesce(
    (auth.jwt() -> 'app_metadata' ->> 'role') = 'admin',
    false
  );
$$;

-- ============================================================================
-- Users
-- ============================================================================
create table if not exists public.users (
  id              bigint generated always as identity primary key,
  auth_user_id    uuid unique references auth.users (id) on delete set null,
  full_name       text        not null,
  phone           text        not null,
  email           text        unique,
  avatar_emoji    text        not null default '🙂',
  is_professional boolean     not null default false,
  created_at      timestamptz not null default now()
);

create index if not exists users_phone_idx  on public.users (phone);
create index if not exists users_email_idx  on public.users (email);

-- ============================================================================
-- Properties (listings)
-- ============================================================================
create table if not exists public.properties (
  id                bigint generated always as identity primary key,
  title             text        not null,
  location          text        not null,
  city              text        not null,
  listing_type      text        not null check (listing_type in ('RENT', 'BUY', 'PG', 'PLOT')),
  asset_type        text        not null default 'HOUSE' check (asset_type in ('HOUSE', 'LAND', 'CAR')),
  property_category text        not null default 'RESIDENTIAL' check (property_category in ('RESIDENTIAL', 'COMMERCIAL')),
  price_ugx         bigint      not null check (price_ugx >= 0),
  price_label       text        not null default '',
  rating            real        not null default 0 check (rating between 0 and 5),
  is_verified       boolean     not null default true,
  bedrooms          text        not null default '—',
  emoji             text        not null default '🏠',
  created_at        timestamptz not null default now()
);

create index if not exists properties_rating_idx on public.properties (rating desc);
create index if not exists properties_type_idx   on public.properties (listing_type);

-- ============================================================================
-- Favorites
-- ============================================================================
create table if not exists public.favorites (
  user_id       uuid        not null references auth.users (id) on delete cascade,
  property_id   bigint      not null references public.properties (id) on delete cascade,
  created_at    timestamptz not null default now(),
  primary key (user_id, property_id)
);

-- ============================================================================
-- Service providers (marketplace)
-- ============================================================================
create table if not exists public.service_providers (
  id                 bigint generated always as identity primary key,
  name               text        not null,
  emoji              text        not null default '🔧',
  category           text        not null,
  rating             real        not null default 4.5 check (rating between 0 and 5),
  reviews_count      integer     not null default 0 check (reviews_count >= 0),
  jobs_completed     integer     not null default 0 check (jobs_completed >= 0),
  experience_years   integer     not null default 1 check (experience_years >= 0),
  headline           text        not null default '',
  lat                real        not null default 0,
  lng                real        not null default 0,
  status             text        not null default 'AVAILABLE',
  price_from         bigint      not null default 0 check (price_from >= 0),
  is_verified        boolean     not null default false,
  coverage_radius_km real        not null default 10,
  created_at         timestamptz not null default now()
);

create index if not exists service_providers_category_idx on public.service_providers (category);
create index if not exists service_providers_rating_idx   on public.service_providers (rating desc);

-- ============================================================================
-- Legacy service professionals (pre-marketplace catalogue)
-- ============================================================================
create table if not exists public.service_professionals (
  id             bigint generated always as identity primary key,
  name           text    not null,
  emoji          text    not null default '👩',
  category       text    not null,
  rating         real    not null default 0 check (rating between 0 and 5),
  bookings_count integer not null default 0,
  headline       text    not null default '',
  created_at     timestamptz not null default now()
);

-- ============================================================================
-- Service bookings
-- ============================================================================
create table if not exists public.service_bookings (
  id                bigint generated always as identity primary key,
  user_id           bigint      not null references public.users (id) on delete cascade,
  professional_id   bigint      not null references public.service_professionals (id) on delete cascade,
  service_name      text        not null,
  status            text        not null default 'PENDING',
  amount_minor      bigint      not null default 0,
  booked_at         timestamptz not null default now()
);

create index if not exists service_bookings_user_idx on public.service_bookings (user_id);

-- ============================================================================
-- Marketplace bookings
-- ============================================================================
create table if not exists public.marketplace_bookings (
  id              bigint generated always as identity primary key,
  customer_id     uuid        references auth.users (id) on delete set null,
  provider_id     bigint      references public.service_providers (id) on delete set null,
  provider_name   text        not null default '',
  provider_emoji  text        not null default '🔧',
  service_type    text        not null,
  description     text        not null default '',
  status          text        not null default 'SEARCHING',
  location_text   text        not null default '',
  lat             real        not null default 0,
  lng             real        not null default 0,
  scheduled_at    timestamptz,
  started_at      timestamptz,
  completed_at    timestamptz,
  base_cost       bigint      not null default 0 check (base_cost >= 0),
  additional_cost bigint      not null default 0 check (additional_cost >= 0),
  platform_fee    bigint      not null default 0 check (platform_fee >= 0),
  total_cost      bigint      not null default 0 check (total_cost >= 0),
  payment_method  text        not null default '',
  is_paid         boolean     not null default false,
  created_at      timestamptz not null default now()
);

create index if not exists marketplace_bookings_status_idx on public.marketplace_bookings (status);
create index if not exists marketplace_bookings_created_idx on public.marketplace_bookings (created_at desc);

-- ============================================================================
-- Quotes
-- ============================================================================
create table if not exists public.quotes (
  id                 bigint generated always as identity primary key,
  booking_id         bigint      not null references public.marketplace_bookings (id) on delete cascade,
  provider_id        bigint      references public.service_providers (id) on delete set null,
  provider_name      text        not null default '',
  provider_emoji     text        not null default '🔧',
  labour_cost        bigint      not null default 0 check (labour_cost >= 0),
  materials_cost     bigint      not null default 0 check (materials_cost >= 0),
  service_fee        bigint      not null default 0 check (service_fee >= 0),
  total              bigint      not null default 0 check (total >= 0),
  estimated_duration text        not null default '',
  arrival_time       text        not null default '',
  status             text        not null default 'PENDING',
  created_at         timestamptz not null default now()
);

create index if not exists quotes_booking_idx on public.quotes (booking_id);

-- ============================================================================
-- Reviews
-- ============================================================================
create table if not exists public.reviews (
  id               bigint generated always as identity primary key,
  booking_id       bigint      references public.marketplace_bookings (id) on delete set null,
  provider_id      bigint      not null references public.service_providers (id) on delete cascade,
  customer_id      uuid        references auth.users (id) on delete set null,
  quality          integer     not null default 5 check (quality between 1 and 5),
  professionalism  integer     not null default 5 check (professionalism between 1 and 5),
  timeliness       integer     not null default 5 check (timeliness between 1 and 5),
  communication    integer     not null default 5 check (communication between 1 and 5),
  value_rating     integer     not null default 5 check (value_rating between 1 and 5),
  overall          integer     not null default 5 check (overall between 1 and 5),
  comment          text        not null default '',
  created_at       timestamptz not null default now()
);

create index if not exists reviews_provider_idx on public.reviews (provider_id);

-- ============================================================================
-- Wallet
-- ============================================================================
create table if not exists public.wallet_accounts (
  id           bigint generated always as identity primary key,
  user_id      uuid        not null unique references auth.users (id) on delete cascade,
  balance_minor bigint     not null default 0,
  currency     text        not null default 'UGX',
  card_last4   text        not null default '',
  card_type    text        not null default '',
  card_name    text        not null default '',
  created_at   timestamptz not null default now()
);

create table if not exists public.wallet_transactions (
  id             bigint generated always as identity primary key,
  account_id     bigint      not null references public.wallet_accounts (id) on delete cascade,
  title          text        not null,
  meta           text        not null default '',
  amount_minor   bigint      not null,
  is_credit      boolean     not null,
  created_at     timestamptz not null default now()
);

create index if not exists wallet_transactions_account_idx on public.wallet_transactions (account_id, created_at desc);

-- ============================================================================
-- Chat
-- ============================================================================
create table if not exists public.conversations (
  id             bigint generated always as identity primary key,
  customer_id    uuid        references auth.users (id) on delete cascade,
  name           text        not null,
  avatar_emoji   text        not null default '💬',
  last_message   text        not null default '',
  last_message_at timestamptz not null default now(),
  unread_count   integer     not null default 0
);

create table if not exists public.messages (
  id              bigint generated always as identity primary key,
  conversation_id bigint      not null references public.conversations (id) on delete cascade,
  sender          text        not null,
  text            text        not null,
  created_at      timestamptz not null default now()
);

create index if not exists messages_conversation_idx on public.messages (conversation_id, created_at);

-- ============================================================================
-- Row Level Security
-- ============================================================================
alter table public.users                 enable row level security;
alter table public.properties            enable row level security;
alter table public.favorites             enable row level security;
alter table public.service_providers     enable row level security;
alter table public.service_professionals enable row level security;
alter table public.service_bookings      enable row level security;
alter table public.marketplace_bookings  enable row level security;
alter table public.quotes                enable row level security;
alter table public.reviews               enable row level security;
alter table public.wallet_accounts       enable row level security;
alter table public.wallet_transactions   enable row level security;
alter table public.conversations         enable row level security;
alter table public.messages              enable row level security;

-- Properties: world-readable, admin-writable.
create policy "properties are public readable"
  on public.properties for select using (true);
create policy "admins manage properties"
  on public.properties for all
  using (public.is_admin()) with check (public.is_admin());

-- Providers: world-readable, admin-writable.
create policy "providers are public readable"
  on public.service_providers for select using (true);
create policy "admins manage providers"
  on public.service_providers for all
  using (public.is_admin()) with check (public.is_admin());

create policy "professionals are public readable"
  on public.service_professionals for select using (true);
create policy "admins manage professionals"
  on public.service_professionals for all
  using (public.is_admin()) with check (public.is_admin());

-- Users: self-readable, admin full access.
create policy "users read own or admin"
  on public.users for select
  using (auth.uid() = auth_user_id or public.is_admin());
create policy "users update own or admin"
  on public.users for update
  using (auth.uid() = auth_user_id or public.is_admin())
  with check (auth.uid() = auth_user_id or public.is_admin());
create policy "admins insert users"
  on public.users for insert with check (public.is_admin());
create policy "admins delete users"
  on public.users for delete using (public.is_admin());

-- Favorites: strictly owner-scoped.
create policy "favorites are owner scoped"
  on public.favorites for all
  using (auth.uid() = user_id)
  with check (auth.uid() = user_id);

-- Bookings: participants and admins.
create policy "bookings visible to participants or admin"
  on public.marketplace_bookings for select
  using (auth.uid() = customer_id or public.is_admin());
create policy "customers create own bookings"
  on public.marketplace_bookings for insert
  with check (auth.uid() = customer_id);
create policy "participants or admin update bookings"
  on public.marketplace_bookings for update
  using (auth.uid() = customer_id or public.is_admin())
  with check (auth.uid() = customer_id or public.is_admin());

create policy "service bookings visible to owner or admin"
  on public.service_bookings for select
  using (public.is_admin() or exists (
    select 1 from public.users u where u.id = service_bookings.user_id and u.auth_user_id = auth.uid()
  ));

-- Quotes: readable by booking participants and admins.
create policy "quotes visible to participants or admin"
  on public.quotes for select
  using (public.is_admin() or exists (
    select 1 from public.marketplace_bookings b
    where b.id = quotes.booking_id and b.customer_id = auth.uid()
  ));
create policy "providers or admin manage quotes"
  on public.quotes for all
  using (public.is_admin() or public.is_admin())
  with check (public.is_admin() or public.is_admin());

-- Reviews: public readable, customers write their own, admins moderate.
create policy "reviews are public readable"
  on public.reviews for select using (true);
create policy "customers create own reviews"
  on public.reviews for insert with check (auth.uid() = customer_id);
create policy "admins manage reviews"
  on public.reviews for all
  using (public.is_admin()) with check (public.is_admin());

-- Wallet: strictly owner-scoped, admins read-only.
create policy "wallet accounts are owner scoped"
  on public.wallet_accounts for select
  using (auth.uid() = user_id or public.is_admin());
create policy "admins manage wallet accounts"
  on public.wallet_accounts for all
  using (public.is_admin()) with check (public.is_admin());

create policy "transactions follow account ownership"
  on public.wallet_transactions for select
  using (public.is_admin() or exists (
    select 1 from public.wallet_accounts a
    where a.id = wallet_transactions.account_id and a.user_id = auth.uid()
  ));
create policy "admins manage transactions"
  on public.wallet_transactions for all
  using (public.is_admin()) with check (public.is_admin());

-- Chat: owner-scoped.
create policy "conversations are owner scoped"
  on public.conversations for select
  using (auth.uid() = customer_id or public.is_admin());
create policy "users create own conversations"
  on public.conversations for insert with check (auth.uid() = customer_id);
create policy "participants or admin update conversations"
  on public.conversations for update
  using (auth.uid() = customer_id or public.is_admin())
  with check (auth.uid() = customer_id or public.is_admin());

create policy "messages follow conversation ownership"
  on public.messages for select
  using (public.is_admin() or exists (
    select 1 from public.conversations c
    where c.id = messages.conversation_id and c.customer_id = auth.uid()
  ));
create policy "participants create messages"
  on public.messages for insert with check (public.is_admin() or exists (
    select 1 from public.conversations c
    where c.id = messages.conversation_id and c.customer_id = auth.uid()
  ));
