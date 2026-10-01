# 0Brocker admin

React + Vite administration app, deployed as a static web app on Vercel.

## Features

- Overview: booking workload, asset mix, moderation queue and operational totals.
- Bookings: search/filter, app-compatible workflow transitions and filtered CSV exports.
- Providers and listings: search/filter, verification and CSV exports.
- People, wallet transactions and reviews: searchable account and service records, financial summaries and review scorecards.
- Responsive navigation and tables; East Africa Time date display; UGX formatting.
- Supabase admin sign-in and server-enforced RLS; no service-role key in the client.

## Run and verify

```sh
npm ci
npm run dev
npm test
npm run test:e2e
npm run build
```

Without Supabase environment variables the app uses labeled sample records. Verification and booking changes persist only in browser localStorage (`0brocker-demo-patches-v1`). Clear that key to reset demo state. The demo never changes the mobile app's local database.

Copy `.env.example` to `.env.local` and set the public Supabase URL and anon/publishable key for live mode. `VITE_` variables are shipped to the browser; never use a service-role key here. Live reads fail visibly and never fall back to demo data. Each resource loads at most 500 records; a notice identifies truncated datasets and summaries/exports cover only loaded rows.

## Administrator access

Live mode requires a Supabase Auth user with `app_metadata.role = "admin"`. There is no default administrator password. Create or identify the intended account in Supabase Authentication, then set its app metadata through the Supabase administrative API or SQL Editor. For a known user's UUID:

```sql
update auth.users
set raw_app_meta_data = coalesce(raw_app_meta_data, '{}'::jsonb) || '{"role":"admin"}'::jsonb
where id = 'REPLACE_WITH_ADMIN_AUTH_USER_UUID';
```

The account should sign out and sign in again after this change. Never put the role in user metadata: users can edit that themselves.

The schema/RLS contract is `../supabase/migrations/20260101000000_initial_schema.sql`. Apply it through your normal migration process if the database has not been provisioned. The frontend cannot verify that the migration is deployed. RLS, rather than the sign-in UI alone, protects database records. Realtime booking refresh requires enabling the `marketplace_bookings` table in the Supabase realtime publication.

The Kotlin mobile app still uses local SQLDelight repositories. This admin reads Supabase tables; a mobile-to-Supabase repository/sync integration is required before both applications share production data.

Wallet credits represent recorded inflows, not business revenue. Debits use their absolute amount for aggregate calculations; monetary minor units are divided by 100. Users/password hashes are never selected into the admin UI.

## Vercel

Project root: `admin`. Framework: Vite. Build: `npm run build`. Output: `dist`.

Set `VITE_SUPABASE_URL` and `VITE_SUPABASE_ANON_KEY` in the project's Production/Preview environment. The included deployment helper reads only those public variables from local `.env.local`, validates the key, links `0brocker-admin`, sets Vercel Production environment variables, and deploys:

```sh
node scripts/deploy.mjs
```

Once linked and configured, redeploy using `npm run deploy`. The deployed production URL is recorded in `deployment.json`. `.env` files, test artifacts and local previews are excluded from upload. Security headers and CSP are defined in `vercel.json`.

## V1 test workspace

Production includes a separate Test workspace sign-in for review. The server verifies TEST_ADMIN_EMAIL and TEST_ADMIN_PASSWORD_SHA256 and signs an eight-hour, HttpOnly, Secure, SameSite=Strict cookie with TEST_ADMIN_SESSION_SECRET. These are server-only Vercel variables. Test access never grants a Supabase admin role; all test edits use browser-local sample patches. The APK has a separate offline dataset. Test passwords are supplied with the release, not published in this repository.

The verified APK, checksum, benchmark and testing guide are served from /downloads/. APK binaries are ignored by Git and uploaded from the successful CI artifact. See ../docs/V1-RELEASE.json for the exact source commit and acceptance evidence.
