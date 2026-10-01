package com.homeapp

import com.homeapp.data.model.Property
import com.homeapp.features.property.PropertyFilter
import com.homeapp.features.property.PostPropertyUiState
import com.homeapp.features.property.filterProperties
import com.homeapp.features.services.booking2.bookingSchedule
import kotlin.test.*

class V1DiscoveryTest {
    private fun property(id: Long, type: String, price: Long, verified: Boolean) =
        Property(id, "Kololo home", "Main Road", "Kampala", type, "HOUSE", "RESIDENTIAL", price, if(type == "RENT") "/mo" else "", 0.0, verified, "2 bedrooms", "HOUSE")
    private val rows = listOf(property(1,"RENT",500,true),property(2,"BUY",300,true),property(3,"RENT",100,false))
    @Test fun searchIntersectsEveryConstraintAndSorts() {
        val result=filterProperties(rows," Kampala   Kololo ",PropertyFilter(listingType="RENT",maxPrice=400,sort="PRICE_ASC"))
        assertEquals(listOf(3L),result.map { it.id })
        assertTrue(filterProperties(rows,"Kololo",PropertyFilter(listingType="RENT",maxPrice=400,verifiedOnly=true)).isEmpty())
        assertEquals(listOf(3L,2L,1L),filterProperties(rows,"",PropertyFilter(sort="PRICE_ASC")).map { it.id })
        assertTrue(filterProperties(rows,"missing",PropertyFilter()).isEmpty())
    }
    @Test fun ownerDeclarationIsRequired() {
        val state=PostPropertyUiState(title="Home",location="Kampala",priceText="500")
        assertFalse(state.canSubmit);assertTrue(state.copy(ownerDeclared=true).canSubmit)
    }
    @Test fun schedulesUseKampalaTimeAndRejectInvalidOrPastInput() {
        val epoch=bookingSchedule("Schedule","2027-01-01","12:00",0)
        assertEquals(1798794000L,epoch)
        assertEquals(123L,bookingSchedule("NOW","","",123))
        assertFails { bookingSchedule("Schedule","2027-02-30","12:00",0) }
        assertFails { bookingSchedule("Schedule","2027-01-01","12:00",epoch+1) }
    }
    @Test fun unknownAmenitiesNeverInventAnAreaOrTitle() {
        val amenities=rows.first().amenities().toMap()
        assertEquals("Not supplied",amenities["Area"])
        assertEquals("Confirm with owner",amenities["Deposit / title"])
    }
}
