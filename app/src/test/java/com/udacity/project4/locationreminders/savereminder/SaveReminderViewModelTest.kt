package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //TODO: provide testing to the SaveReminderView and its live data objects

    private val dataSource = FakeDataSource()
    private val reminder =
        ReminderDTO("Test1", "Testing1", "Googleplex1", 37.43947694128096, -122.04674122018965, "0")

    @Before
    fun beforeTesting() {
        runBlocking {
            dataSource.deleteAllReminders()
            dataSource.saveReminder(reminder)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_error_empty_title() = runBlocking {
        // Given a fresh ViewModel
        val saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        val emptyTitleReminder =
            ReminderDataItem(
                "",
                "Testing1",
                "Googleplex1",
                37.43947694128096,
                -122.04674122018965,
                "0"
            )

        //WHEN validate reminder
        saveReminderViewModel.validateAndSaveReminder(emptyTitleReminder)

        // Then the new reminder event is triggered
        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        MatcherAssert.assertThat(value, Matchers.not(Matchers.nullValue()))
        assert(value == R.string.err_enter_title)
    }

    @Test
    fun test_error_empty_location() = runBlocking {
        // Given a fresh ViewModel
        val saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        val emptyLocationReminder =
            ReminderDataItem(
                "Test1",
                "Testing1",
                "",
                37.43947694128096,
                -122.04674122018965,
                "0"
            )

        //WHEN validate reminder
        saveReminderViewModel.validateAndSaveReminder(emptyLocationReminder)

        // Then the new reminder event is triggered
        val value = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()

        MatcherAssert.assertThat(value, Matchers.not(Matchers.nullValue()))
        assert(value == R.string.err_select_location)
    }
}