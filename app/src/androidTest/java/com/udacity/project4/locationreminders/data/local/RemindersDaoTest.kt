package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun setUp() {
        //Create the local database
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun tearDown() {
        //Close the database
        database.close()
    }

    @Test
    fun test_save_and_get_reminders() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder =
            ReminderDTO("Test", "Testing", "Googleplex", 37.43947694128096, -122.04674122018965)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun test_all_reminders() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder =
            ReminderDTO("Test", "Testing", "Googleplex", 37.43947694128096, -122.04674122018965)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val reminders = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assert(reminders.isNotEmpty())
    }

    @Test
    fun test_delete_all_reminders() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder =
            ReminderDTO("Test", "Testing", "Googleplex", 37.43947694128096, -122.04674122018965)
        database.reminderDao().saveReminder(reminder)

        //CHECK GIVEN
        val task1 = database.reminderDao().getReminders()
        assert(task1.isNotEmpty())

        // WHEN - Get the reminder by id from the database.
        database.reminderDao().deleteAllReminders()

        // THEN - The loaded data contains the expected values.
        val task2 = database.reminderDao().getReminders()
        assert(task2.isEmpty())
    }
}