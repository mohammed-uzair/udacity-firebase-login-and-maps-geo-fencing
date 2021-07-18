package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun test_IsActivityInView() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    //    TODO: test the navigation of the fragments.
    @Test
    fun test_navigation_to_save_new_reminder() {
        // GIVEN - On the reminder list screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(null, R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //WHEN
        onView(withId(R.id.addReminderFAB)).perform(click())

        //THEN Assert navigation
        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    @KoinApiExtension
    @Test
    fun test_navigation_to_reminder_maps() {
        // GIVEN - On the reminder list screen
        val scenario = launchFragmentInContainer<SaveReminderFragment>(null, R.style.AppTheme)

        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //WHEN
        onView(withId(R.id.selectLocation)).perform(click())

        //THEN Assert navigation
        verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
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