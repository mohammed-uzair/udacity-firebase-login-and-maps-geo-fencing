package com.udacity.project4

import android.app.Application
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.Matcher
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
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


    //    TODO: add End to End testing to the app
    @Test
    fun test_black_box() {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        onView(withId(R.id.reminderssRecyclerView))
            .check(matches(isDisplayed()))

        //Navigate
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Assert navigation
        onView(withId(R.id.reminderTitle))
            .check(matches(isDisplayed()))

        //Add a title
        onView(withId(R.id.reminderTitle))
            .perform(click()).perform(typeText("Test Title"))
            .perform(closeSoftKeyboard())

        //Add a title
        onView(withId(R.id.reminderDescription))
            .perform(click()).perform(typeText("Test Description"))
            .perform(closeSoftKeyboard())

        //Add the location
        onView(withId(R.id.selectLocation))
            .perform(click())
        onView(withId(R.id.map))
            .perform(click())

        CoroutineScope(Dispatchers.Main).launch {
            delay(700)

            onView(withId(android.R.id.button1)).perform(click())

            //Assert if the reminder is added
            onView(withId(R.id.reminderssRecyclerView))
                .check(matches(isDisplayed()))

            //Assert the recycler list count is 1
            onView(withId(R.id.reminderssRecyclerView)).check(RecyclerViewItemCountAssertion(1))

            activityScenario.close()
        }
    }
}

class RecyclerViewItemCountAssertion : ViewAssertion {
    private val matcher: Matcher<Int>

    constructor(expectedCount: Int) {
        matcher = `is`(expectedCount)
    }

    constructor(matcher: Matcher<Int>) {
        this.matcher = matcher
    }

    override fun check(view: View, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        val recyclerView = view as RecyclerView
        val adapter = recyclerView.adapter
        assertThat(adapter!!.itemCount, matcher)
    }
}
