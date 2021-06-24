package com.udacity.project4.locationreminders.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.KoinComponent
import org.koin.core.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest: KoinComponent {

    //    TODO: Add testing implementation to the RemindersDao.kt
    private val remindersLocalRepository: ReminderDataSource by inject()

    @Before
    fun setUp() {
        //Create the local database
    }

    @After
    fun tearDown() {
        //Close the database
    }

    @Test
    fun test_all_reminders(){

    }
}