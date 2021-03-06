package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var repository: RemindersLocalRepository

    private val reminder1 =
        ReminderDTO("Test1", "Testing1", "Googleplex1", 37.43947694128096, -122.04674122018965, "0")
    private val reminder2 =
        ReminderDTO("Test2", "Testing2", "Googleplex2", 37.43947694128096, -122.04674122018965, "1")

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun setUp() {
        //Create the local database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        //Close the database
        database.close()
    }

    @Test
    fun test_get_reminder_by_id() = runBlocking {
        repository.saveReminder(reminder1)

        val task1 = repository.getReminder(reminder1.id)
        assert((task1 as Result.Success).data.id == "0")
    }

    @Test
    fun test_get_all_reminders() = runBlocking {
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val task1 = repository.getReminders()
        assert((task1 as Result.Success).data.size == 2)
    }

    @Test
    fun test_reminder_not_found() = runBlocking {
        val task1 = repository.getReminder("0")
        assert((task1 as Result.Error).message!!.contains("Reminder not found!"))
    }

    @Test
    fun test_delete_all_reminders() = runBlocking {
        repository.saveReminder(reminder1)
        repository.saveReminder(reminder2)

        val task1 = repository.getReminders()
        assert((task1 as Result.Success).data.size == 2)

        repository.deleteAllReminders()

        val task2 = repository.getReminders()
        assert((task2 as Result.Success).data.isEmpty())
    }
}