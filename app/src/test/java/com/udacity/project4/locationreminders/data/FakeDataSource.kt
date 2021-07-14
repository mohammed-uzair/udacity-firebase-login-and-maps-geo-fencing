package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private val dataSource = mutableListOf<ReminderDTO>()

    override suspend fun saveReminder(reminder: ReminderDTO) {
        dataSource.add(reminder)
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> = Result.Success(dataSource)

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        Result.Success(dataSource.first { it.id == id })

    override suspend fun deleteAllReminders() {
        dataSource.clear()
    }
}