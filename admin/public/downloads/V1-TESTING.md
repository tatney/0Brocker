# Version 1 testing guide

Build: 1.0.0-test.1 (Android version code 4). Android 7.0/API 24 or newer.

The APK is a debug-signed testing build. Allow installation from the browser or file app used to open it. An older installation signed with a different debug key may need uninstalling first; back up anything important because uninstalling removes local test data.

Sign in with the dedicated user test credentials supplied with the release. They work offline on this device. Sign up can also create another local test account. No email or SMS is sent.

1. Search Kampala/Kololo, choose Rent, set a budget and confirm search keeps the selected filters. Clear filters when finished.
2. Save and reload a search. Compare two or three properties; check each price period.
3. Post a listing as the owner. Confirm posting is disabled until the owner declaration is checked. Open the posted listing and check that verification is pending.
4. Open Services, choose a provider, review the displayed starting price and request a service. Test a future Kampala date/time and an invalid/past time.
5. Confirm the request with a payment preference. Open My home / My bookings and confirm the new request is present. Simulate completion and payment, then leave a review.
6. Add maintenance, rent and checklist notes in My home. Mark done, reopen, restart the app and confirm notes persist.
7. Open a listing's owner conversation and add a test message. Messages stay on this device; there is no real recipient in this version.
8. Visit https://0brocker-admin.vercel.app, select Test workspace and use the supplied admin test credentials. Test every navigation item, filtered listings/providers, CSV export, verification, booking transitions and a narrow mobile viewport. Refresh to check persisted browser changes. Sign out and confirm access returns to the login screen.

The APK and sample admin have separate test datasets. The admin account does not grant live Supabase administrator rights. No real money, dispatch, verification or messaging occurs. See V1-BENCHMARK.md for the implementation matrix and remaining launch work.
