package com.homeapp.data.database

import com.homeapp.data.security.PasswordHasher
import com.homeapp.db.HomeAppDatabase

internal object SeedData {

    private const val DEMO_EMAIL = "arjun@homeapp.in"
    private const val DEMO_PASSWORD = "password123"

    fun seed(db: HomeAppDatabase) {
        val q = db.homeAppDatabaseQueries

        q.insertUser(
            id = 1,
            full_name = "Arjun Mehta",
            phone = "9876543210",
            email = DEMO_EMAIL,
            avatar_emoji = "🧑",
            is_professional = 0,
            password_hash = PasswordHasher.hash(DEMO_PASSWORD, DEMO_EMAIL),
        )

        q.insertProperty(
            id = 1, title = "2 BHK in Ntinda", location = "Bukoto, Ntinda Road",
            city = "Kampala", listing_type = "RENT", asset_type = "HOUSE", property_category = "RESIDENTIAL",
            price_ugx = 3_500_000, price_label = "/mo", rating = 4.8, is_verified = 1, bedrooms = "2 BHK", emoji = "🏠",
        )
        q.insertProperty(
            id = 2, title = "3 BHK with Balcony", location = "Muyenga, Tank Hill Road",
            city = "Kampala", listing_type = "BUY", asset_type = "HOUSE", property_category = "RESIDENTIAL",
            price_ugx = 950_000_000, price_label = "", rating = 4.6, is_verified = 1, bedrooms = "3 BHK", emoji = "🏡",
        )
        q.insertProperty(
            id = 3, title = "Portion for Rent", location = "Kira Road, Kamwokya",
            city = "Kampala", listing_type = "RENT", asset_type = "HOUSE", property_category = "RESIDENTIAL",
            price_ugx = 1_800_000, price_label = "/mo", rating = 4.7, is_verified = 1, bedrooms = "1 BHK", emoji = "🏢",
        )
        q.insertProperty(
            id = 4, title = "Office Space in Ntinda", location = "Ntinda Complex, Plot 41",
            city = "Kampala", listing_type = "RENT", asset_type = "HOUSE", property_category = "COMMERCIAL",
            price_ugx = 8_500_000, price_label = "/mo", rating = 4.5, is_verified = 1, bedrooms = "3 BHK", emoji = "🏢",
        )
        q.insertProperty(
            id = 5, title = "Warehouse in Industrial Area", location = "6th Street, Industrial Area",
            city = "Kampala", listing_type = "BUY", asset_type = "HOUSE", property_category = "COMMERCIAL",
            price_ugx = 2_400_000_000, price_label = "", rating = 4.4, is_verified = 1, bedrooms = "—", emoji = "🏭",
        )
        q.insertProperty(
            id = 6, title = "Residential Plot in Muyenga", location = "Kanisa Road, Muyenga",
            city = "Kampala", listing_type = "BUY", asset_type = "LAND", property_category = "RESIDENTIAL",
            price_ugx = 180_000_000, price_label = "", rating = 4.7, is_verified = 1, bedrooms = "—", emoji = "🌳",
        )
        q.insertProperty(
            id = 7, title = "Agricultural Land for Rent", location = "Kasangati, Wakiso Road",
            city = "Kampala", listing_type = "RENT", asset_type = "LAND", property_category = "RESIDENTIAL",
            price_ugx = 3_000_000, price_label = "/acre", rating = 4.3, is_verified = 1, bedrooms = "—", emoji = "🌾",
        )
        q.insertProperty(
            id = 8, title = "Commercial Plot in Kololo", location = "Acacia Avenue, Kololo",
            city = "Kampala", listing_type = "BUY", asset_type = "LAND", property_category = "COMMERCIAL",
            price_ugx = 750_000_000, price_label = "", rating = 4.8, is_verified = 1, bedrooms = "—", emoji = "🏗️",
        )
        q.insertProperty(
            id = 9, title = "Plot near Entebbe Road", location = "Nyanama, Entebbe Rd",
            city = "Kampala", listing_type = "RENT", asset_type = "LAND", property_category = "COMMERCIAL",
            price_ugx = 5_500_000, price_label = "/mo", rating = 4.2, is_verified = 1, bedrooms = "—", emoji = "🚜",
        )
        q.insertProperty(
            id = 10, title = "Toyota Urban Cruiser 2022", location = "Nakasero, Kampala Road",
            city = "Kampala", listing_type = "BUY", asset_type = "CAR", property_category = "RESIDENTIAL",
            price_ugx = 72_000_000, price_label = "", rating = 4.6, is_verified = 1, bedrooms = "—", emoji = "🚗",
        )
        q.insertProperty(
            id = 11, title = "Self-Drive SUV Weekend", location = "Bukoto Branch",
            city = "Kampala", listing_type = "RENT", asset_type = "CAR", property_category = "RESIDENTIAL",
            price_ugx = 250_000, price_label = "/day", rating = 4.4, is_verified = 1, bedrooms = "—", emoji = "🚙",
        )
        q.insertProperty(
            id = 12, title = "Fleet Van for Hire", location = "Bombo Road Depot",
            city = "Kampala", listing_type = "RENT", asset_type = "CAR", property_category = "COMMERCIAL",
            price_ugx = 350_000, price_label = "/day", rating = 4.3, is_verified = 1, bedrooms = "—", emoji = "🚐",
        )
        q.insertProperty(
            id = 13, title = "Lorry for Cargo Transport", location = "Nasser Road Yard",
            city = "Kampala", listing_type = "BUY", asset_type = "CAR", property_category = "COMMERCIAL",
            price_ugx = 150_000_000, price_label = "", rating = 4.1, is_verified = 1, bedrooms = "—", emoji = "🚛",
        )
        q.insertProperty(
            id = 14, title = "Studio Apartment Bunamwaya", location = "Bunamwaya, Inner Ring",
            city = "Kampala", listing_type = "RENT", asset_type = "HOUSE", property_category = "RESIDENTIAL",
            price_ugx = 1_200_000, price_label = "/mo", rating = 4.5, is_verified = 1, bedrooms = "1 RK", emoji = "🏠",
        )

        // Legacy service professionals (kept for backward compat)
        val professionals = listOf(
            Triple(1L, "Priya S.", "👩") to ("Salon at Home" to "Salon expert"),
            Triple(2L, "Rahul K.", "🧑‍🔧") to ("Plumber" to "Pipe & leak specialist"),
            Triple(3L, "Vikram D.", "⚡") to ("Electrician" to "Wiring & fixture expert"),
            Triple(4L, "Anita M.", "🧹") to ("Cleaning" to "Deep cleaning pro"),
            Triple(5L, "Suresh P.", "❄️") to ("AC Repair" to "All AC brands serviced"),
            Triple(6L, "Deepa N.", "💇") to ("Salon at Home" to "Hair & beauty specialist"),
            Triple(7L, "Kumar R.", "🚗") to ("Car Wash" to "Doorstep car wash"),
            Triple(8L, "Meena L.", "🐜") to ("Pest Control" to "Termite & roach treatment"),
        )
        professionals.forEach { (idNameEmoji, catHeadline) ->
            val (id, name, emoji) = idNameEmoji
            val (category, headline) = catHeadline
            q.insertProfessional(
                id = id, name = name, emoji = emoji, category = category,
                rating = 4.5 + (id % 5) * 0.1, bookings_count = 20 + id * 15, headline = headline,
            )
        }

        // ─── Marketplace service providers ───
        // Kampala, Uganda area coordinates: ~0.3476° N, 32.5825° E
        val baseLat = 0.3476
        val baseLng = 32.5825
        data class ProviderSeed(
            val id: Long, val name: String, val emoji: String, val category: String,
            val rating: Double, val reviews: Long, val jobs: Long, val exp: Int,
            val headline: String, val latOff: Double, val lngOff: Double,
            val status: String, val priceFrom: Long, val verified: Boolean, val radius: Double,
        )
        val providers = listOf(
            ProviderSeed(1, "Joseph Plumbing", "🔧", "Plumbing", 4.9, 187, 327, 8, "Leak & pipe specialist", 0.003, 0.002, "AVAILABLE", 35000, true, 12.0),
            ProviderSeed(2, "Grace Electric Co", "⚡", "Electrical", 4.8, 142, 289, 6, "Wiring & fixtures expert", -0.005, 0.004, "AVAILABLE", 40000, true, 10.0),
            ProviderSeed(3, "CleanPro Uganda", "🧹", "Cleaning", 4.7, 231, 456, 5, "Deep cleaning & fumigation", 0.002, -0.003, "BUSY", 50000, true, 15.0),
            ProviderSeed(4, "paintWorks Studio", "🎨", "Painting", 4.6, 98, 178, 10, "Interior & exterior painting", -0.004, -0.002, "AVAILABLE", 80000, true, 20.0),
            ProviderSeed(5, "MoveRight Movers", "🚚", "Moving", 4.5, 156, 312, 7, "Furniture & office relocation", 0.006, 0.001, "AVAILABLE", 100000, true, 25.0),
            ProviderSeed(6, "CoolBreeze AC", "❄️", "AC Repair", 4.8, 112, 234, 4, "All AC brands serviced", -0.001, 0.005, "AVAILABLE", 60000, true, 10.0),
            ProviderSeed(7, "PestGuard Uganda", "🐜", "Pest Control", 4.7, 89, 198, 6, "Termite, cockroach & rodent", 0.004, -0.004, "SCHEDULED", 70000, true, 18.0),
            ProviderSeed(8, "WoodCraft Carpentry", "🪚", "Carpentry", 4.6, 67, 145, 12, "Custom furniture & repairs", -0.003, 0.003, "AVAILABLE", 55000, false, 15.0),
            ProviderSeed(9, "GreenThumb Garden", "🌿", "Gardening", 4.5, 54, 112, 3, "Lawn care & landscaping", 0.001, -0.006, "AVAILABLE", 30000, false, 8.0),
            ProviderSeed(10, "BeautyBox Home", "💇", "Beauty", 4.9, 278, 534, 9, "Hair, nails & spa at home", -0.006, 0.002, "AVAILABLE", 45000, true, 10.0),
            ProviderSeed(11, "AutoFix Garage", "🚗", "Automotive", 4.4, 43, 89, 5, "Mobile mechanic & detailing", 0.005, 0.006, "OFFLINE", 65000, false, 20.0),
            ProviderSeed(12, "FixIt All Repairs", "🛠", "Repairs", 4.7, 167, 345, 8, "General home repairs & maintenance", -0.002, -0.001, "AVAILABLE", 25000, true, 12.0),
            ProviderSeed(13, "HomeStyle Interior", "🛋", "Interior Design", 4.8, 78, 134, 11, "Modern interior transformations", 0.003, 0.005, "BUSY", 200000, true, 30.0),
            ProviderSeed(14, "ApplianceMedic", "🔌", "Appliance Repair", 4.6, 134, 267, 6, "Washing machine, fridge, cooker", -0.004, 0.001, "AVAILABLE", 45000, true, 10.0),
            ProviderSeed(15, "SparkPlumb Express", "🔧", "Plumbing", 4.3, 56, 123, 3, "Emergency plumbing services", 0.007, -0.003, "AVAILABLE", 30000, false, 8.0),
            ProviderSeed(16, "BrightWire Electric", "⚡", "Electrical", 4.5, 91, 178, 4, "Solar & generator installation", -0.001, -0.005, "AVAILABLE", 50000, true, 15.0),
            ProviderSeed(17, "SparkleClean Daily", "🧹", "Cleaning", 4.4, 198, 412, 2, "Daily & weekly cleaning plans", 0.002, 0.003, "AVAILABLE", 25000, true, 8.0),
            ProviderSeed(18, "WallMaster Paint", "🎨", "Painting", 4.5, 76, 156, 7, "Waterproofing & textured paint", -0.005, -0.004, "AVAILABLE", 60000, false, 18.0),
            ProviderSeed(19, "QuickMove Logistics", "🚚", "Moving", 4.3, 112, 234, 4, "Same-day moving within Kampala", 0.004, 0.002, "AVAILABLE", 80000, true, 25.0),
            ProviderSeed(20, "FixIt All Repairs 2", "🛠", "Repairs", 4.2, 34, 67, 2, "Appliance & furniture repair", 0.006, -0.005, "AVAILABLE", 20000, false, 10.0),
        )
        providers.forEach { p ->
            q.insertProvider(
                id = p.id, name = p.name, emoji = p.emoji, category = p.category,
                rating = p.rating, reviews_count = p.reviews, jobs_completed = p.jobs,
                experience_years = p.exp.toLong(), headline = p.headline,
                lat = baseLat + p.latOff, lng = baseLng + p.lngOff,
                status = p.status, price_from = p.priceFrom,
                is_verified = if (p.verified) 1 else 0, coverage_radius_km = p.radius,
            )
        }

        // ─── Wallet ───
        q.insertAccount(
            id = 1, user_id = 1, balance_paise = 12_47_50_50, currency = "UGX",
            card_last4 = "4521", card_type = "VISA", card_name = "0Brocker Platinum Card",
        )

        val nowEpoch = 1_752_678_000L
        val day = 86_400L

        q.insertTransaction(id = 1, account_id = 1, title = "Salary", meta = "Bank Transfer • Today",
            amount_paise = 42_00_00, is_credit = 1, created_at_epoch = nowEpoch)
        q.insertTransaction(id = 2, account_id = 1, title = "Rent to Landlord", meta = "Mobile Money • Yesterday",
            amount_paise = 18_50_00, is_credit = 0, created_at_epoch = nowEpoch - day)
        q.insertTransaction(id = 3, account_id = 1, title = "AC Repair", meta = "Service Booking",
            amount_paise = 1_25_00, is_credit = 0, created_at_epoch = nowEpoch - 2 * day)
        q.insertTransaction(id = 4, account_id = 1, title = "Cashback", meta = "0Brocker Rewards",
            amount_paise = 2_50_00, is_credit = 1, created_at_epoch = nowEpoch - 3 * day)

        // ─── Chat ───
        seedChat(q, nowEpoch)
    }

    private fun seedChat(q: com.homeapp.db.HomeAppDatabaseQueries, nowEpoch: Long) {
        val day = 86_400L
        q.insertConversation(
            id = 1, name = "Joseph M.", avatar_emoji = "🔧", last_message = "Yes, I can come by Saturday.",
            last_message_at_epoch = nowEpoch - 30 * 60, unread_count = 2,
        )
        q.insertConversation(
            id = 2, name = "Grace K.", avatar_emoji = "⚡", last_message = "Your AC slot is confirmed for 4 PM.",
            last_message_at_epoch = nowEpoch - 2 * 3600, unread_count = 0,
        )
        q.insertConversation(
            id = 3, name = "Landlord – James", avatar_emoji = "🏠", last_message = "Please share the rental agreement.",
            last_message_at_epoch = nowEpoch - 26 * 3600, unread_count = 1,
        )
        q.insertConversation(
            id = 4, name = "BeautyBox", avatar_emoji = "💇", last_message = "Blow dry and styling will be UGX 180,000.",
            last_message_at_epoch = nowEpoch - 3 * day, unread_count = 0,
        )

        var mid = 0L
        fun msg(cid: Long, sender: String, text: String, hoursAgo: Int) {
            mid += 1
            q.insertMessage(
                id = mid, conversation_id = cid, sender = sender, text = text,
                created_at_epoch = nowEpoch - (hoursAgo * 3600L),
            )
        }

        msg(1, "me", "Hi Joseph, can you fix a leaking kitchen tap?", 30)
        msg(1, "them", "Sure, I handle that regularly.", 29)
        msg(1, "me", "How much would it cost?", 2)
        msg(1, "them", "Yes, I can come by Saturday.", 1)

        msg(2, "me", "My AC is not cooling properly.", 8)
        msg(2, "them", "I can do a service tomorrow afternoon.", 7)
        msg(2, "them", "Your AC slot is confirmed for 4 PM.", 4)

        msg(3, "them", "Reminder for this month's rent.", 28)
        msg(3, "me", "I will transfer it tonight.", 27)
        msg(3, "them", "Please share the rental agreement.", 26)

        msg(4, "me", "Do you do bridal makeup?", 75)
        msg(4, "them", "Yes, I have a salon at home package.", 74)
        msg(4, "them", "Blow dry and styling will be UGX 180,000.", 72)
    }
}
