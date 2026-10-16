# Native marketplace integration and testing

Status: 15 October 2026. The correct CLI account now accesses the linked 0Brocker project. All five migrations are applied and a linked dry run reports the remote database is up to date. Payments and external identity/trade/title verification remain deferred by request.

## Customer workflow

Sign up, choose Customer during onboarding, share location, select a service and browse eligible providers. Nearest is the default. The server orders eligible catalog records by actual distance and returns approximate map coordinates; financial eligibility, service coverage and current qualification are checked again when requesting/accepting. Arrival estimates are estimates, not GPS tracking. Other sort options remain available.

The request includes a separate work type (inspection, repair, installation, maintenance or general), up to 2,000 characters of issue details, address, immediate/future scheduling and optional photos. Choosing Repair no longer replaces the technician’s qualified trade. Diagnostic price and repair exclusions require acknowledgement before sending. “Send service request” creates an idempotent request; the following page clearly states it has been sent. Payment preference does not move money.

Android accepts up to five JPEG/PNG/WebP photos, each at most 5 MB. Private Supabase Storage paths are reserved against the actual booking/customer, uploads are retryable, and only the booking customer, assigned provider and authorised operations reviewers can read them. Signed viewing links expire after five minutes. Local preview photos remain in app-private storage.

The customer confirms arrival, decides on an itemised quote, confirms the service actually delivered and may review/dispute. A provider cannot supply customer confirmation.

## Provider workflow

Submit credentials through Verification centre. After independent checks and approval, register the approved trade and diagnostic price, share location, set availability and open My workspaces. Incoming requests show descriptions, location, schedules and photos. Providers can accept, issue an itemised quote, request customer completion confirmation or cancel with a recorded reason. Arrival/completion remain customer actions. Registry integration has not been bypassed to populate a live catalog.

## Owner workflow

Choose Owner, submit the exact title/unit and property details, inspect the application status, and publish only after independent approval. The owner dashboard supports listing title/price/address changes, withdrawal, customer conversations, viewing/reminder notes and draft invoices. Management enquiries go to the hosted operations queue; they do not activate a management contract or collect rent. Future phone-sharing consent is stored without publishing contact details.

Customers and owners share server-scoped conversations; sender identity comes from the authenticated session. Business records are account/workspace scoped. In-app notifications cover booking events, messages and verification status changes. Background push, email/WhatsApp delivery, live GPS, rent collection and payment processing are not connected. Reminder records remain notes and invoices remain drafts.

## Offline test accounts

Install `apk/0Brocker-home-preview.apk`. This separate `com.homeapp.preview` package has an OFFLINE TEST WORKSPACE banner and a clearly labelled Kampala test location. Existing app data is retained by `adb install -r`; schema 8 upgrades to 9.

The sign-in screen offers one-tap Customer, Owner and Provider buttons. If already signed in, open menu, Profile, Logout first. Ordinary Create account also works locally and lets testers choose a starting workspace.

| Role | Email | Password |
| --- | --- | --- |
| Customer | customer@test.0brocker.app | HomeTest!2026 |
| Owner | owner@test.0brocker.app | HomeTest!2026 |
| Provider | provider@test.0brocker.app | HomeTest!2026 |

These fixed credentials are offline fixtures, not hosted accounts or live qualifications. Owner fixtures have sample listings. New preview property applications can be explicitly published as samples from the owner workspace. Sign in as the customer to send a request, then as the provider to accept/quote, then as the customer to confirm arrival/quote/completion. Owner enquiries can be exchanged by switching between customer and owner accounts on the same device. Sample records never become hosted verification evidence.

## Live configuration and deployment

Android reads only the public `VITE_SUPABASE_URL` and `VITE_SUPABASE_ANON_KEY` from environment/Gradle properties or the existing ignored `admin/.env.local`. Secret/service-role keys are rejected. Supabase owns real password custody; refresh sessions are encrypted using Android Keystore. The ordinary build uses the cloud repositories; `-PpreviewApp=true` uses only the separate offline fixtures. Other platform entry points have not yet been configured for live credentials/session storage.

Required migration sequence:

1. `20260101000000_initial_schema.sql` (already-existing base schema must be reflected in migration history; do not blindly rerun existing policies).
2. `20261012000000_verification_workflow.sql`.
3. `20261013000000_marketplace_workflows.sql`.
4. `20261014000000_native_marketplace.sql`.
5. `20261014001000_service_photo_storage.sql`.
6. `20261015000000_booking_realtime.sql`.

Use a CLI account with sufficient access to the linked project, inspect `npx supabase migration list --linked` and `npx supabase db push --linked --dry-run`, then apply outstanding reviewed migrations. Existing booking/quote/review direct writers are intentionally revoked: the native app uses identity-bound RPCs. Do not apply only the storage migration or skip verification/workflow dependencies. No funding adapter, subscriptions or registry adapter is activated by deployment.

The previous project-access block is resolved. The correct account lists 0Brocker (snvplaeaoxmuimtpxxpn) as ACTIVE_HEALTHY. Migration history includes all six local migrations, including the booking realtime publication. Hosted authentication, marketplace endpoints and the private request-photo bucket are available.

Three confirmed, ordinary hosted test accounts (Customer, Owner, Provider) have been provisioned. Their generated credentials are stored only in the ignored local file [.scratch/live-test-accounts.json](../.scratch/live-test-accounts.json), separate from the offline fixed credentials above. Secret/service-role API keys were used only in the local provisioning process and were not saved, printed or embedded in the app. Normal public sign-up still requires email confirmation; the test accounts were individually confirmed through the authorised administrative API.

All 23 live API acceptance checks passed: password sign-in, profile bootstrap, workspace persistence for each role, cross-account read/write isolation, owner/provider pending application delivery, blocked publication/activation without independent verification, discovery RPCs, notifications, photo metadata scoping, private storage limits and session refresh. Temporary business-record test rows were removed. Clearly labelled TEST_ONLY verification applications remain pending for admin review; no fixture was qualified, published as a live listing or funded.

A separate live Android QA build is available using `./gradlew :androidApp:assembleDebug -PliveQaApp=true`, exporting `apk/0Brocker-live-qa.apk` with package `com.homeapp.liveqa`. This avoids replacing an existing differently signed `com.homeapp` installation and preserves both its data and the offline preview. The QA APK uses real Supabase authentication; it contains no test passwords. Native customer sign-in, encrypted session restoration after force-stop/relaunch, and the hosted customer workspace have been verified on the emulator. It is currently signed in as the hosted test customer. Sign in with other roles using the local credentials file.

## Validation

- 101 regular JVM tests and 61 Android host tests passed; the opt-in hosted realtime test passed separately (skipped in the normal regression run).
- 24 admin/backend security tests passed, including the new PostgreSQL role/photo/idempotency/inbox integration test.
- 12 admin browser tests passed; admin production build passed.
- Preview, ordinary live-configured and isolated Live QA Android debug builds passed.
- All 23 hosted API acceptance checks passed. Native live password sign-in, secure session restoration and hosted workspace loading passed.
- Emulator install retained existing data and confirmed database version 9 and the three role fixtures.
- Emulator checks confirmed one-tap customer sign-in, nearby results with Nearest selected, separate work-type controls, description/address/scheduling and the Android photo picker. A selected sample image was successfully attached and persisted with a booking whose trade remained Repairs and work type remained Repair.

Live API acceptance has passed. A complete real customer-provider service and actual request-photo upload require a genuinely qualified provider and remain deferred with the external verification integration. No live evidence or booking was fabricated to test that path. Background push and live GPS remain unconnected. Existing Kotlin deprecation and Vite bundle-size warnings remain.

## Live administrator and booking realtime - 15 October 2026

The confirmed hosted Auth account `rutatinafrancis@gmail.com` has `app_metadata.role = "admin"`. Password sign-in and the server `is_admin` check passed. Its generated initial password is available only in ignored local `.scratch/live-admin-account.json`; no invitation email was sent. Customer, owner and provider hosted test accounts remain ordinary accounts.

Realtime is enabled on marketplace_bookings, quotes, booking_workflow, booking_events, booking_photos and reviews. Android booking observers share an authenticated WebSocket, refresh authoritative records under RLS when changes arrive, reconnect with backoff and retain an eight-second recovery polling fallback. The UI reports connection status. The admin booking screen reports subscription state as well.

Hosted delivery checks passed for customer and admin, with a measured 525 ms update in this test. Another owner received no booking event and could not read the record. The native live test independently received an actual booking change and refreshed a snapshot within five seconds. Admin browser login and connected status passed against hosted Supabase using the local current frontend; this does not claim a new production frontend deployment. The temporary unassigned QA booking was removed. Payment processing and external verification remain deferred.
