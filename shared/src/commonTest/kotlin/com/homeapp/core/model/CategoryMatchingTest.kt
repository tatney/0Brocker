package com.homeapp.core.model

import com.homeapp.data.model.ServiceCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Guards the category-alias table.
 *
 * These are the exact mismatches that made the map and services filters return
 * nothing: the seed data writes free-text category strings that never equal the
 * enum labels.
 */
class CategoryMatchingTest {

    @Test
    fun `matches the seed data spellings that label equality rejected`() {
        // Enum label is "Interior", database says "Interior Design".
        assertTrue(ServiceCategory.INTERIOR_DESIGN.matches("Interior Design"))
        assertTrue(ServiceCategory.INTERIOR_DESIGN.matches("interior design"))
        // Enum label is "Appliance", database says "Appliance Repair".
        assertTrue(ServiceCategory.APPLIANCE.matches("Appliance Repair"))
        assertTrue(ServiceCategory.APPLIANCE.matches("appliance repair"))
    }

    @Test
    fun `matches the legacy service_professionals vocabulary`() {
        assertTrue(ServiceCategory.PLUMBING.matches("Plumber"))
        assertTrue(ServiceCategory.ELECTRICAL.matches("Electrician"))
        assertTrue(ServiceCategory.BEAUTY.matches("Salon at Home"))
        assertTrue(ServiceCategory.AUTOMOTIVE.matches("Car Wash"))
    }

    @Test
    fun `matches its own label regardless of case and padding`() {
        for (category in ServiceCategory.entries) {
            if (category == ServiceCategory.ALL) continue
            assertTrue(
                category.matches(category.label),
                "${category.name} should match its own label",
            )
            assertTrue(category.matches("  ${category.label.uppercase()}  "))
        }
    }

    @Test
    fun `does not confuse short ambiguous aliases`() {
        // "car" is a substring of "carpentry"; a naive contains-check would file
        // every carpenter under Automotive.
        assertFalse(ServiceCategory.AUTOMOTIVE.matches("Carpentry"))
        assertTrue(ServiceCategory.CARPENTRY.matches("Carpentry"))
        // Likewise "ac" inside other words.
        assertFalse(ServiceCategory.AC_REPAIR.matches("Ridge Accents"))
    }

    @Test
    fun `rejects unrelated categories`() {
        assertFalse(ServiceCategory.PAINTING.matches("Plumbing"))
        assertFalse(ServiceCategory.PLUMBING.matches("Painting"))
        assertFalse(ServiceCategory.GARDENING.matches("Beauty"))
    }

    @Test
    fun `ALL matches everything and nothing matches an empty string`() {
        for (category in ServiceCategory.entries) {
            assertTrue(ServiceCategory.ALL.matches(category.label))
        }
        for (category in ServiceCategory.entries) {
            if (category == ServiceCategory.ALL) continue
            assertFalse(category.matches(""), "${category.name} should not match blank")
            assertFalse(category.matches("   "))
        }
    }

    @Test
    fun `ignores punctuation and case when normalising`() {
        assertTrue(ServiceCategory.MOVING.matches("Packers & Movers"))
        assertTrue(ServiceCategory.MOVING.matches("packers and movers"))
        assertTrue(ServiceCategory.ELECTRICAL.matches("ELECTRICIAN"))
    }

    @Test
    fun `serviceCategoryOf resolves every seed spelling to one category`() {
        assertEquals(ServiceCategory.INTERIOR_DESIGN, serviceCategoryOf("Interior Design"))
        assertEquals(ServiceCategory.APPLIANCE, serviceCategoryOf("Appliance Repair"))
        assertEquals(ServiceCategory.PLUMBING, serviceCategoryOf("Plumber"))
        assertEquals(ServiceCategory.BEAUTY, serviceCategoryOf("Salon at Home"))
        assertEquals(ServiceCategory.AUTOMOTIVE, serviceCategoryOf("Car Wash"))
        assertEquals(ServiceCategory.AC_REPAIR, serviceCategoryOf("AC Repair"))
    }

    @Test
    fun `serviceCategoryOf prefers the most specific alias over general repairs`() {
        // "Appliance Repair" contains REPAIRS' alias "repair". A first-match-wins
        // scan in enum order filed appliances under general repairs, because
        // REPAIRS is declared before APPLIANCE.
        assertEquals(ServiceCategory.APPLIANCE, serviceCategoryOf("Appliance Repair"))
        assertEquals(ServiceCategory.INTERIOR_DESIGN, serviceCategoryOf("Interior Design"))
        // Specificity, not declaration order, decides.
        assertEquals(ServiceCategory.PEST_CONTROL, serviceCategoryOf("Pest Control"))
        assertEquals(ServiceCategory.AC_REPAIR, serviceCategoryOf("AC Repair"))
    }

    @Test
    fun `serviceCategoryOf falls back to general repairs`() {
        // Unknown strings still get a sensible glyph rather than rendering bare.
        assertEquals(ServiceCategory.REPAIRS, serviceCategoryOf("Underwater Basket Weaving"))
        assertEquals(ServiceCategory.REPAIRS, serviceCategoryOf(""))
    }

    @Test
    fun `every category resolves a distinct icon`() {
        val icons = ServiceCategory.entries.map { it.icon }
        // Guards against copy-paste in the `when` mapping a category to another
        // category's glyph.
        assertEquals(ServiceCategory.entries.size, icons.distinct().size)
    }

    @Test
    fun `every category resolves a distinct accent`() {
        val accents = ServiceCategory.entries.map { it.colors.accent }
        assertEquals(ServiceCategory.entries.size, accents.distinct().size)
    }

    @Test
    fun `category colors stay in step with the enum ordinal`() {
        // AppColors indexes accents by ordinal, so the two must not drift.
        for (category in ServiceCategory.entries) {
            val expected = com.homeapp.core.theme.categoryColorsAt(category.ordinal)
            assertEquals(expected.accent, category.colors.accent, category.name)
        }
    }
}
