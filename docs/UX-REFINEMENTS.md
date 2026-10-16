# UI and wallet refinements - 13 October 2026

The services map now uses layered full-colour SVG path illustrations rendered as native ImageVectors. Editable SVG originals are in assets/service-icons. The icons include a blue plumber helmet, electrician pliers, chef hat, lawn mower, barber clippers, welding mask, paint roller, moving box, carpentry saw, washing machine, air conditioner, pest spray, sofa and car. Chef and Welding are selectable categories; barber and lawn-mower aliases match Beauty and Gardening. Qualification remains mandatory. No unqualified or invented providers are placed on the map to demonstrate the icons.

Map service filtering and provider registration use a compact illustrated dropdown. The map offers Suggest a missing service, opening the customer workspace. Suggestions validate name and description, save per account with LOCAL_PENDING status and never register a provider. They are not sent to the company until native/cloud synchronisation is connected.

0Bcash now shares the Payments storefront, colours, header, wallet card, transaction history and bottom navigation. Both expose add/send/history actions against the same signed-in user's test wallet. Wallet balance and ledger changes occur in one database transaction. Amount limits, account ownership, insufficient balance and duplicate-submit guards protect the flow. New users start with a zero test balance; the original sample user's balance is retained. Real card, bank, mobile-money, utility and school-payment providers remain unconnected. The app labels the wallet and transfers as simulated; it does not claim to move real money.

Workspaces show role-relevant plan descriptions, records and verification forms. Owner viewing records are excluded from provider tools, and provider client records are excluded from owner tools. Customer screens show free chat, search and service suggestions. Plans has a dedicated screen, reachable from the drawer and View plans within My workspaces. It shows the free launch and the relevant future optional plan, with local no-charge interest registration. Prices and activation dates remain undecided.

My Activity / My Bookings now has working bottom navigation to Home, Property, Services and Payments. Map and activity headers respect the status-bar inset.

SQLDelight migration 7.sqm upgrades schema 7 to 8 with the service-suggestion table. Native test coverage includes wallet ownership, atomic ledger/balance updates, invalid amounts, overdrafts, suggestion validation/scoping and migration preservation. Final native results: 92 JVM tests and 61 Android host tests passed; preview APK built with -PpreviewApp=true. Installed using adb install -r to retain data, then checked the revised screens on com.homeapp.preview. No hosted deployment was performed. Existing marketplace implementation and uncommitted changes were retained.

## Visual polish - 15 October 2026

Preserved existing screen layouts, dimensions, spacing, typography sizes, corner shapes, artwork, navigation and workflow behavior. Material surface container tones now use the established warm-neutral palette; outlined controls have clearer contrast in light and dark themes. Live and preview workspaces use the shared background, and their existing cards gain a subtle border and one-dp elevation without changing content or arrangement.
