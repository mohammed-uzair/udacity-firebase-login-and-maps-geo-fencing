package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val dataSource = FakeDataSource()
    private val reminder1 =
        ReminderDTO("Test1", "Testing1", "Googleplex1", 37.43947694128096, -122.04674122018965, "0")

    @Before
    fun beforeTesting() {
        runBlocking {
            dataSource.deleteAllReminders()
            dataSource.saveReminder(reminder1)
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun test_check_loading() {
        // Given a fresh ViewModel
        val reminderViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        mainCoroutineRule.pauseDispatcher()

        //WHEN fetched all reminders
        reminderViewModel.loadReminders()

        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        // Then the new reminder event is triggered
        assertThat(reminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun test_shouldReturnError() {
        // Given a fresh ViewModel
        val reminderViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        dataSource.shouldReturnError(true)

        //WHEN fetched all reminders
        reminderViewModel.loadReminders()

        // Then the new reminder event is triggered
        assertThat(reminderViewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception"))
    }

    @Test
    fun addNewTask_setsNewTaskEvent() {
        // Given a fresh ViewModel
        val reminderViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)

        //WHEN fetched all reminders
        reminderViewModel.loadReminders()

        // Then the new reminder event is triggered
        val value = reminderViewModel.remindersList.getOrAwaitValue()

        assertThat(value, not(nullValue()))
        assert(value.size == 1)
        assert(value[0].id == reminder1.id)
    }
}