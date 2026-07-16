package fr.quinquenaire.p15_eventorias_jr


import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.google.firebase.Timestamp
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import fr.quinquenaire.p15_eventorias_jr.domain.model.Event
import fr.quinquenaire.p15_eventorias_jr.fake.FakeEventRepository
import fr.quinquenaire.p15_eventorias_jr.presentation.eventlist.EventListScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class EventListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Inject
    lateinit var fakeRepository: FakeEventRepository

    private val fakeEvents = listOf(
        Event(
            id = "1",
            name = "Concert de Rock",
            date = Timestamp(1792056000L, 0),
            locationName = "Stade de France",
            category = "MUSIQUE",
            imageUrl = ""
        ),
        Event(
            id = "2",
            name = "Match de Tennis",
            date = Timestamp(1795179600L, 0),
            locationName = "Roland Garros",
            category = "SPORT",
            imageUrl = ""
        )
    )

    @Before
    fun setup() {
        hiltRule.inject()
        fakeRepository.setEvents(fakeEvents)
        composeTestRule.setContent {
            EventListScreen(
                onNavigateToDetail = { _ -> },
                onNavigateToCreate = { }
            )
        }
    }

    @Test
    fun test_display_event() {
        composeTestRule.onNodeWithText("Concert de Rock").assertIsDisplayed()
        composeTestRule.onNodeWithText("Match de Tennis").assertIsDisplayed()
        //    .assertCountEquals(2)
    }

   @Test
    fun test_search_event() {
       val searchDesc = composeTestRule.activity.getString(R.string.search_placeholder)
       composeTestRule
           .onNodeWithContentDescription(searchDesc)
           .performClick()

       composeTestRule
           .onNodeWithText(searchDesc)
           .performTextInput("Concert")

       composeTestRule
           .onNodeWithText("Concert de Rock")
           .assertIsDisplayed()

       composeTestRule.onNodeWithText("Match de Tennis")
           .assertDoesNotExist()
    }

    @Test
    fun test_filter_by_category() {
        composeTestRule
            .onNodeWithTag("Chip_SPORT")
            .performClick()

        composeTestRule
            .onNodeWithText("Match de Tennis")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Concert de Rock")
            .assertDoesNotExist()
    }

    @Test
    fun test_sort_events() {
        val sortDesc = composeTestRule.activity.getString(R.string.sort_by_date)

        // 1. Clic sur le bouton de tri
        composeTestRule
            .onNodeWithContentDescription(sortDesc)
            .performClick()

        composeTestRule.waitForIdle()
        // récupération des positions
        val sportY = composeTestRule.onNodeWithText("Match de Tennis")
            .fetchSemanticsNode().positionInWindow.y

        val musicY = composeTestRule.onNodeWithText("Concert de Rock")
            .fetchSemanticsNode().positionInWindow.y

        assert( sportY<musicY) { "Le sport devrait être affiché sous la musique après le tri" }

        // 2. On retourne au tri par date
        composeTestRule
            .onNodeWithContentDescription(sortDesc)
            .performClick()

        composeTestRule.waitForIdle()

        // récupération des positions
        val sportY2 = composeTestRule.onNodeWithText("Match de Tennis")
            .fetchSemanticsNode().positionInWindow.y

        val musicY2 = composeTestRule.onNodeWithText("Concert de Rock")
            .fetchSemanticsNode().positionInWindow.y

        assert( sportY2>musicY2) { "Le sport devrait être affiché au dessus de la musique après le tri" }
    }

}