# Verification implementation - 12 October 2026

## What is implemented

The Compose Verification centre is available from Home and Profile without replacing the colourful storefront layout. Individual technicians can submit a trade, operating jurisdiction, issuer, credential number and expiry. Property applicants provide an official title/property identifier and exact unit/parcel; the form supports individual owners and representatives of owning companies. Applications stay local and pending. No declaration, upload, rating or extracted field is approval evidence.

The local document assistant parses labelled, redacted text and explains missing and conflicting fields. It is explicitly a demo parser; it calls no LLM. The cloud extraction function uses a configured OpenAI model with strict structured output, no action tools, server-only credentials, explicit consent and untrusted-document instructions. It can save an advisory summary only. No model has been trained for this project. Mocked extraction tests do not establish live AI connectivity.

SQLDelight migration 4 creates the pending application store. Migration 5 normalises identifiers and preserves duplicate historical submissions as DUPLICATE_PENDING, then enforces one active local property/unit claim. Existing users, favourites, bookings and application rows are retained. Seeded catalogue flags cannot establish independent qualification: native discovery and new bookings stay blocked until authenticated cloud approval snapshots exist. Historical bookings remain stored.

The React admin replaces catalogue Verify buttons with evidence review. Decisions require a reason and display audit history, credential issuer/expiry/technician matching, exact ownership and company-authority requirements. Live approval eligibility is checked against server rules as well as the advisory UI checks. Demo decisions are browser-local simulations.

## Concrete hosted changes awaiting approval

Nothing in this work deploys migrations or functions. Review these exact source files before a hosted release:

- supabase/migrations/20261012000000_verification_workflow.sql: application/evidence/configuration/audit tables and RLS; reviewer-only deterministic decisions; exact title/unit duplicate protection; ownership-dispute/transfer and credential-revocation invalidation; controlled owner publication and per-technician activation; guarded booking and quote writes; disabled unqualified legacy service booking; explicit diagnostic fee acknowledgement; eligible nearby-provider RPC.
- supabase/functions/verification-extract/index.ts: authenticated, consent-gated advisory extraction; owner/admin application access through RLS; strict structured response validation; no service-role key or approval/publication/evidence-writing capability.

The nearby_qualified_providers RPC filters current independent qualification, trade, availability and operating radius before ranking. A Bayesian rating score uses ten prior reviews at 3.5 stars to avoid prioritising unreliable one-review ratings; estimated arrival breaks ties. Pins are rounded to approximately 1 km. Arrival is a straight-line estimate at 30 km/h with a five-minute floor, not traffic routing. Diagnostic fees must be explicitly configured; repair labour, parts and materials require a separate quote. Native Android offers optional coarse location permission; no permission or location means no invented user dot and no nearby match. A labelled Kampala map preview provides cartography while waiting for device location; it is not used for eligibility, distance or booking. Other native platforms currently have no location adapter.

## Remaining production dependencies

1. Authoritative identity and liveness vendors, trade/issuer registries, land/title and company registries are NOT connected. Implement authenticated server adapters with canonical official identifiers, issuer/technician/trade/jurisdiction/expiry matching, owning-company and representative-authority checks, webhook signature verification, replay prevention and revocation/transfer refresh. Only these trusted adapters may write verification_evidence. A source_reference string alone is not a registry integration.
2. verification_requirements is intentionally empty/disabled. Do not enable a jurisdiction/trade until its authoritative adapters and revocation process have been exercised. Default approvals fail closed. There is no manual browser or LLM override of missing checks.
3. Native accounts are local SQLDelight accounts; numeric local user IDs are not Supabase Auth UUIDs. Implement real native Supabase authentication/session refresh, secure token custody, consented submission synchronisation, idempotent local-to-cloud application mappings, status/audit refresh and short-lived server-authenticated catalogue eligibility. Do not import an admin demo approval or local is_verified flag as evidence. No network synchronisation has been claimed or enabled here.
4. Cloud secrets needed: SUPABASE_URL, SUPABASE_ANON_KEY, OPENAI_API_KEY, OPENAI_MODEL and ALLOWED_ORIGINS for the extraction function; VITE_SUPABASE_URL and VITE_SUPABASE_ANON_KEY for admin. Service-role credentials belong exclusively in trusted registry adapters. Supply no real secrets in Git or an APK. Use legitimate app_metadata admin claims for reviewers; user_metadata cannot grant this role.
5. Live AI, vendor liveness capture, document uploads/OCR, official registry lookups, transfer notifications, route/traffic ETA and end-to-end native/cloud review cannot be validated without those dependencies and approved deployment. The local UI and mocked/server-rule tests cover the available preview boundaries.

## Validation and Android preview

Run native checks with Android Studio's bundled JBR when Java is absent from PATH:

    gradlew.bat :shared:jvmTest :shared:testAndroidHostTest :androidApp:assembleDebug -PpreviewApp=true --console=plain

Run admin checks with npm test, npm run test:e2e and npm run build inside admin. PostgreSQL security tests run the migration against local PGlite with authenticated, anonymous and privileged roles; no hosted database is touched. Extraction tests mock external calls.

Use com.homeapp.preview for the existing preview installation. Install apk/0Brocker-home-preview.apk with adb install -r; do not uninstall or clear data. Preview acceptance includes visible Home/Profile Verification centre entry points, provider registration, pending application/document assistant, owner/company-owner submission and nearby-map empty state. Current validation results and emulator observations are recorded below after the final build.

## Final validation results

- JVM: 77 passed; Android host: 52 passed. Preview debug APK built with -PpreviewApp=true. Existing Compose deprecation warnings remain.
- Admin/security: 22 passed, including local PostgreSQL RLS/RPC boundaries, exact issuer/technician/trade matching, expiry, company authority, duplicate titles, transfers, unavailable/out-of-area dispatch, fee acknowledgement, model prompt isolation and malformed/approval-bearing model output rejection.
- Browser: 11 Playwright tests passed, including evidence review navigation, required reasons, audit persistence and broker rejection. Admin production build passed; Vite reports a non-blocking bundle-size warning.
- Extraction function passed strict TypeScript checking against DOM APIs and a minimal Deno interface. External AI calls were mocked; no live vendor or model credential was used.
- Installed with adb install -r and launched com.homeapp.preview/com.homeapp.MainActivity. The original com.homeapp package was not changed.
- Emulator verified Home and Profile entry points, all provider trade chips and required fields, owner/company representative form, pending DEMO application and advisory document extraction. The extraction listed missing fields and left the application pending. Nearby services displayed no eligible providers.
- Approximate-location permission was granted in the emulator. No device location fix was supplied during the walkthrough; live user-dot/distance/ETA behaviour therefore remains unverified. A labelled Kampala preview viewport supplies map context without a synthetic user dot, eligibility or booking coordinates. Provider pins remain unavailable until authoritative native/cloud synchronisation exists.
- Database upgraded from user_version 4 to 6, retaining 2 users, 14 properties, 20 stored providers and 0 bookings. One explicitly DEMO PLUMBING application was added for the UI smoke test and remains PENDING; no provider record was activated. A safety copy and UI snapshots are in ignored .scratch/verification-preview.
- Existing uncommitted files were preserved; no reset, checkout, commit, hosted migration or function deployment was performed.

The final installed build also showed OpenStreetMap cartography with the Kampala preview label and no eligible matches. Original record counts and the pending DEMO application were checked again after the final install.
