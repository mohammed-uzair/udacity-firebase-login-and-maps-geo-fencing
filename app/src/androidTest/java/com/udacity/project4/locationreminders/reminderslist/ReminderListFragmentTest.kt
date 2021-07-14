package com.udacity.project4.locationreminders.reminderslist

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    @Test
    fun test_IsActivityInView() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun test_navigation_to_reminder() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Assert navigation
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun test_navigation_to_reminder_maps() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Assert navigation
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.selectLocation)).perform(click())

        //Assert navigation
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun test_displaying_data() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Assert navigation
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.selectLocation)).perform(click())

        //Assert navigation
        onView(withId(R.id.map)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    //    TODO: add testing for the error messages.
    @Test
    fun test_title_error_message() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Assert navigation
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        onView(withId(R.id.saveReminder)).perform(click())

        //Assert error
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))

        activityScenario.close()
    }

    @Test
    fun test_location_error_message() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Assert navigation
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        //Add a title
        onView(withId(R.id.reminderTitle)).perform(click()).perform(typeText("Test Title"))
            .perform(closeSoftKeyboard())

        onView(withId(R.id.saveReminder)).perform(click())

        //Assert error
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))

        activityScenario.close()
    }
}