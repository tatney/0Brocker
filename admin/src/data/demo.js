const now = Date.now();
const hours = (n) => new Date(now - n * 3600_000).toISOString();

export const demoProperties = [
  { id: 1, title: '2 BHK in Ntinda', location: 'Bukoto, Ntinda Road', city: 'Kampala', listing_type: 'RENT', asset_type: 'HOUSE', property_category: 'RESIDENTIAL', price_ugx: 1_200_000, price_label: '/mo', rating: 4.8, is_verified: true, bedrooms: '2', emoji: '🏠' },
  { id: 2, title: '3 BHK with Balcony', location: 'Muyenga, Tank Hill Road', city: 'Kampala', listing_type: 'BUY', asset_type: 'HOUSE', property_category: 'RESIDENTIAL', price_ugx: 385_000_000, price_label: 'onwards', rating: 4.6, is_verified: true, bedrooms: '3', emoji: '🏡' },
  { id: 3, title: 'Portion for Rent', location: 'Kira Road, Kamwokya', city: 'Kampala', listing_type: 'RENT', asset_type: 'HOUSE', property_category: 'RESIDENTIAL', price_ugx: 750_000, price_label: '/mo', rating: 4.2, is_verified: false, bedrooms: '4', emoji: '🏠' },
  { id: 4, title: 'Office Space in Ntinda', location: 'Ntinda Complex, Plot 41', city: 'Kampala', listing_type: 'RENT', asset_type: 'HOUSE', property_category: 'COMMERCIAL', price_ugx: 2_400_000, price_label: '/mo', rating: 4.4, is_verified: true, bedrooms: '—', emoji: '🏢' },
  { id: 5, title: 'Warehouse in Industrial Area', location: '6th Street, Industrial Area', city: 'Kampala', listing_type: 'BUY', asset_type: 'HOUSE', property_category: 'COMMERCIAL', price_ugx: 940_000_000, price_label: 'onwards', rating: 4.1, is_verified: false, bedrooms: '—', emoji: '🏭' },
  { id: 6, title: 'Residential Plot in Muyenga', location: 'Kanisa Road, Muyenga', city: 'Kampala', listing_type: 'BUY', asset_type: 'LAND', property_category: 'RESIDENTIAL', price_ugx: 210_000_000, price_label: 'onwards', rating: 4.7, is_verified: true, bedrooms: '—', emoji: '🌱' },
  { id: 7, title: 'Agricultural Land for Rent', location: 'Kasangati, Wakiso Road', city: 'Kampala', listing_type: 'RENT', asset_type: 'LAND', property_category: 'RESIDENTIAL', price_ugx: 3_500_000, price_label: '/yr', rating: 3.9, is_verified: false, bedrooms: '—', emoji: '🌾' },
  { id: 8, title: 'Commercial Plot in Kololo', location: 'Acacia Avenue, Kololo', city: 'Kampala', listing_type: 'BUY', asset_type: 'LAND', property_category: 'COMMERCIAL', price_ugx: 1_450_000_000, price_label: 'onwards', rating: 4.9, is_verified: true, bedrooms: '—', emoji: '🏗️' },
  { id: 9, title: 'Plot near Entebbe Road', location: 'Nyanama, Entebbe Rd', city: 'Kampala', listing_type: 'RENT', asset_type: 'LAND', property_category: 'COMMERCIAL', price_ugx: 950_000, price_label: '/mo', rating: 4.0, is_verified: false, bedrooms: '—', emoji: '🏗️' },
  { id: 10, title: 'Toyota Urban Cruiser 2022', location: 'Nakasero, Kampala Road', city: 'Kampala', listing_type: 'BUY', asset_type: 'CAR', property_category: 'RESIDENTIAL', price_ugx: 46_000_000, price_label: '', rating: 4.5, is_verified: true, bedrooms: '—', emoji: '🚗' },
  { id: 11, title: 'Self-Drive SUV Weekend', location: 'Bukoto Branch', city: 'Kampala', listing_type: 'RENT', asset_type: 'CAR', property_category: 'RESIDENTIAL', price_ugx: 180_000, price_label: '/day', rating: 4.3, is_verified: true, bedrooms: '—', emoji: '🚙' },
  { id: 12, title: 'Fleet Van for Hire', location: 'Bombo Road Depot', city: 'Kampala', listing_type: 'RENT', asset_type: 'CAR', property_category: 'COMMERCIAL', price_ugx: 650_000, price_label: '/mo', rating: 4.1, is_verified: false, bedrooms: '—', emoji: '🚐' },
  { id: 13, title: 'Lorry for Cargo Transport', location: 'Nasser Road Yard', city: 'Kampala', listing_type: 'BUY', asset_type: 'CAR', property_category: 'COMMERCIAL', price_ugx: 62_000_000, price_label: '', rating: 3.8, is_verified: false, bedrooms: '—', emoji: '🚚' },
  { id: 14, title: 'Studio Apartment Bunamwaya', location: 'Bunamwaya, Inner Ring', city: 'Kampala', listing_type: 'RENT', asset_type: 'HOUSE', property_category: 'RESIDENTIAL', price_ugx: 550_000, price_label: '/mo', rating: 4.4, is_verified: true, bedrooms: '1', emoji: '🏠' },
].map((row) => ({ ...row, created_at: hours(row.id * 30) }));

export const demoProviders = [
  { id: 1, name: 'Amina Nakato', emoji: '🧹', category: 'CLEANING', rating: 4.9, reviews_count: 214, jobs_completed: 380, experience_years: 6, headline: 'Deep cleaning specialists for homes and offices.', status: 'AVAILABLE', price_from: 45_000, is_verified: true, coverage_radius_km: 18 },
  { id: 2, name: 'Joseph Kiwanuka', emoji: '🔧', category: 'PLUMBING', rating: 4.8, reviews_count: 187, jobs_completed: 295, experience_years: 9, headline: 'Leaks, pipes and drainage — 24h emergency callout.', status: 'BUSY', price_from: 60_000, is_verified: true, coverage_radius_km: 25 },
  { id: 3, name: 'Grace Wanjiku', emoji: '⚡', category: 'ELECTRICAL', rating: 4.7, reviews_count: 132, jobs_completed: 210, experience_years: 7, headline: 'Certified electrician. Wiring, DBs and lighting.', status: 'AVAILABLE', price_from: 70_000, is_verified: true, coverage_radius_km: 20 },
  { id: 4, name: 'Daniel Ssekandi', emoji: '🎨', category: 'PAINTING', rating: 4.6, reviews_count: 96, jobs_completed: 141, experience_years: 5, headline: 'Interior and exterior painting, free quotes.', status: 'AVAILABLE', price_from: 120_000, is_verified: false, coverage_radius_km: 15 },
  { id: 5, name: 'Sarah Namutebi', emoji: '💇', category: 'BEAUTY', rating: 4.9, reviews_count: 268, jobs_completed: 502, experience_years: 8, headline: 'Braids, locs and home beauty services.', status: 'AVAILABLE', price_from: 35_000, is_verified: true, coverage_radius_km: 12 },
  { id: 6, name: 'Peter Ouma', emoji: '🪚', category: 'CARPENTRY', rating: 4.5, reviews_count: 78, jobs_completed: 118, experience_years: 11, headline: 'Custom furniture and fitted wardrobes.', status: 'OFFLINE', price_from: 90_000, is_verified: true, coverage_radius_km: 30 },
  { id: 7, name: 'Mercy Atuhaire', emoji: '❄️', category: 'AC_REPAIR', rating: 4.4, reviews_count: 64, jobs_completed: 97, experience_years: 4, headline: 'AC servicing, gas refill and installation.', status: 'AVAILABLE', price_from: 110_000, is_verified: false, coverage_radius_km: 22 },
  { id: 8, name: 'Patrick Mugisha', emoji: '🐛', category: 'PEST_CONTROL', rating: 4.3, reviews_count: 51, jobs_completed: 84, experience_years: 6, headline: 'Safe, discreet fumigation for homes and offices.', status: 'AVAILABLE', price_from: 150_000, is_verified: true, coverage_radius_km: 28 },
  { id: 9, name: 'Lucy Nansubuga', emoji: '🌿', category: 'GARDENING', rating: 4.6, reviews_count: 43, jobs_completed: 66, experience_years: 3, headline: 'Lawn care, hedge trimming and landscaping.', status: 'AVAILABLE', price_from: 40_000, is_verified: false, coverage_radius_km: 16 },
  { id: 10, name: 'Samuel Okello', emoji: '🚚', category: 'MOVING', rating: 4.2, reviews_count: 88, jobs_completed: 132, experience_years: 5, headline: 'House and office relocation with packing.', status: 'BUSY', price_from: 180_000, is_verified: true, coverage_radius_km: 40 },
].map((row) => ({ ...row, lat: 0.3476 + (row.id % 5) * 0.004, lng: 32.5825 + (row.id % 7) * 0.006, created_at: hours(row.id * 45) }));

export const demoBookings = [
  { id: 1001, customer_id: 'demo-a', provider_id: 2, provider_name: 'Joseph Kiwanuka', provider_emoji: '🔧', service_type: 'PLUMBING', description: 'Kitchen sink is leaking under the cabinet.', status: 'IN_PROGRESS', location_text: 'Bukoto, Ntinda Road', total_cost: 180_000, is_paid: true, payment_method: 'WALLET', created_at: hours(2), scheduled_at: hours(3) },
  { id: 1002, customer_id: 'demo-b', provider_id: 1, provider_name: 'Amina Nakato', provider_emoji: '🧹', service_type: 'CLEANING', description: 'Monthly deep clean, 3 bedroom house.', status: 'COMPLETED', location_text: 'Muyenga, Tank Hill Road', total_cost: 135_000, is_paid: true, payment_method: 'VISA •4521', created_at: hours(9), completed_at: hours(7) },
  { id: 1003, customer_id: 'demo-c', provider_id: null, provider_name: '', provider_emoji: '🔧', service_type: 'ELECTRICAL', description: 'Living room sockets sparking.', status: 'SEARCHING', location_text: 'Kololo, Acacia Avenue', total_cost: 0, is_paid: false, payment_method: '', created_at: hours(1) },
  { id: 1004, customer_id: 'demo-a', provider_id: 5, provider_name: 'Sarah Namutebi', provider_emoji: '💇', service_type: 'BEAUTY', description: 'Braids appointment for two.', status: 'CONFIRMED', location_text: 'Nakasero, Kampala Road', total_cost: 90_000, is_paid: false, payment_method: 'WALLET', created_at: hours(20), scheduled_at: hours(-26) },
  { id: 1005, customer_id: 'demo-d', provider_id: 4, provider_name: 'Daniel Ssekandi', provider_emoji: '🎨', service_type: 'PAINTING', description: 'Interior repaint, 2 bedroom.', status: 'CANCELLED', location_text: 'Kamwokya, Kira Road', total_cost: 480_000, is_paid: false, payment_method: '', created_at: hours(50) },
].map((row) => ({ ...row, base_cost: row.total_cost, additional_cost: 0, platform_fee: 0, lat: 0.3476, lng: 32.5825, started_at: 0 }));

export const demoUsers = [
  { id: 1, full_name: 'Arjun Mehta', email: 'arjun@homeapp.in', phone: '+256772000001', avatar_emoji: '🙂', is_professional: false, created_at: hours(720) },
  { id: 2, full_name: 'Sarah Namutebi', email: 'sarah@homeapp.in', phone: '+256772000002', avatar_emoji: '💇', is_professional: true, created_at: hours(600) },
  { id: 3, full_name: 'Joseph Kiwanuka', email: 'joseph@homeapp.in', phone: '+256772000003', avatar_emoji: '🔧', is_professional: true, created_at: hours(540) },
  { id: 4, full_name: 'Amina Nakato', email: 'amina@homeapp.in', phone: '+256772000004', avatar_emoji: '🧹', is_professional: true, created_at: hours(480) },
  { id: 5, full_name: 'Grace Wanjiku', email: 'grace@homeapp.in', phone: '+256772000005', avatar_emoji: '⚡', is_professional: true, created_at: hours(300) },
  { id: 6, full_name: 'Daniel Ssekandi', email: 'daniel@homeapp.in', phone: '+256772000006', avatar_emoji: '🎨', is_professional: true, created_at: hours(160) },
];

export const demoTransactions = [
  { id: 1, title: 'Salary', meta: 'Bank Transfer', amount_minor: 12_475_050, is_credit: true, created_at: hours(26) },
  { id: 2, title: 'Deep clean — Amina N.', meta: 'Service booking #1002', amount_minor: -13_500, is_credit: false, created_at: hours(9) },
  { id: 3, title: 'Refund — Booking #998', meta: 'Cancelled electrician visit', amount_minor: 47_000, is_credit: true, created_at: hours(64) },
  { id: 4, title: 'Rent payment — Ntinda', meta: 'Property #1', amount_minor: -120_000, is_credit: false, created_at: hours(140) },
  { id: 5, title: 'Referral bonus', meta: 'Invited 2 neighbours', amount_minor: 20_000, is_credit: true, created_at: hours(210) },
  { id: 6, title: 'AC servicing', meta: 'Service booking #977', amount_minor: -110_000, is_credit: false, created_at: hours(300) },
];

export const demoReviews = [
  { id: 1, booking_id: 1002, provider_id: 1, overall: 5, quality: 5, professionalism: 5, timeliness: 5, communication: 5, value_rating: 4, comment: 'Absolutely spotless house and right on time.', created_at: hours(7) },
  { id: 2, booking_id: 991, provider_id: 2, overall: 5, quality: 5, professionalism: 4, timeliness: 5, communication: 5, value_rating: 4, comment: 'Fixed a burst pipe at 11pm. Saved the ceiling.', created_at: hours(120) },
  { id: 3, booking_id: 988, provider_id: 5, overall: 4, quality: 5, professionalism: 4, timeliness: 3, communication: 4, value_rating: 4, comment: 'Great braids, ran about 30 minutes late.', created_at: hours(260) },
  { id: 4, booking_id: 977, provider_id: 7, overall: 3, quality: 3, professionalism: 4, timeliness: 3, communication: 3, value_rating: 2, comment: 'AC cooled but the quote went up at the end.', created_at: hours(400) },
];

export const demoData = {
  properties: demoProperties,
  providers: demoProviders,
  bookings: demoBookings,
  users: demoUsers,
  transactions: demoTransactions,
  reviews: demoReviews,
};
